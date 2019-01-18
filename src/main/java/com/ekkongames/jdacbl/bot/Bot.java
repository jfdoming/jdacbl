package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.bot.jar.EntryPoint;
import com.ekkongames.jdacbl.bot.jar.JarPathResolver;
import com.ekkongames.jdacbl.client.HostWindow;
import com.ekkongames.jdacbl.utils.Log;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class Bot {

    private static final String TAG = "Bot";

    private BotInfo info;
    private final Object infoLock = new Object();

    private HostWindow window;

    private JDA jda;
    private File jarFile;

    private AudioPlayerManager audioManager;

    private Map<String, GuildState> guildStates;

    public enum State {
        IDLE("Idle."),
        CONNECTING("Connecting..."),
        CONNECTED("Connected."),
        CONNECT_FAILED("Failed to connect to Discord!"),
        LOAD_FAILED("Bot loading failed!");

        private final String statusText;

        State(String statusText) {
            this.statusText = statusText;
        }

        public String getStatusText() {
            return statusText;
        }
    }

    private State state;
    private final Object stateLock = new Object();

    private final Object initializationLock = new Object();
    private volatile boolean done = false;

    private ExecutorService asyncExecutor;

    public void doInBackground(Runnable task) {
        asyncExecutor.execute(task);
    }

    public GuildState getGuildState(String guildID) {
        return guildStates.get(guildID);
    }

    void start(String[] args) {
        EventQueue.invokeLater(() -> {
            openHostWindow();

            done = true;

            synchronized (initializationLock) {
                initializationLock.notify();
            }
        });

        // wait until the window opens
        synchronized (initializationLock) {
            while (!done) {
                try {
                    initializationLock.wait();
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        Log.i(TAG, "Starting up...");

        resolvePath(args);
        prepareAsync();
        setState(State.IDLE);
        loadInfo();
        initAudio();
    }

    private void openHostWindow() {
        window = new HostWindow();
        window.addWindowListener(new WindowCloseListener(this));
        window.addEventListener(HostWindow.EventId.LOGIN, this::doBotLogin);
        window.addEventListener(HostWindow.EventId.SERVER_ADD, this::openAddToServerURL);
        window.addEventListener(HostWindow.EventId.RELOAD, this::loadInfo);
        window.addEventListener(HostWindow.EventId.LOGOUT, this::doBotLogout);
        window.addEventListener(HostWindow.EventId.QUIT, this::disposeGUI);
        window.open();
    }

    private void resolvePath(String[] args) {
        jarFile = new JarPathResolver()
                .checkArgs(args)
                .checkConfig(new File("config.properties"))
                .resolve();
    }

    private void prepareAsync() {
        asyncExecutor = Executors.newCachedThreadPool();
    }

    private void loadInfo() {
        asyncExecutor.execute(() -> {
            setInfo(null);

            if (jarFile == null) {
                EventQueue.invokeLater(() -> setState(State.LOAD_FAILED));
                return;
            }

            String errMsg = null;
            try {
                JarFile jar = new JarFile(jarFile);
                Manifest jarManifest = jar.getManifest();
                try (URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()})) {
                    Class<?> clazz = loader.loadClass(jarManifest.getMainAttributes().getValue("Entry-Point"));
                    EntryPoint entryPoint = (EntryPoint) clazz.newInstance();
                    setInfo(entryPoint.run());
                    loader.close();

                    if (getInfo() == null) {
                        errMsg = "BotInfo was null";
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    errMsg = "Failed to locate entry point!";
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    errMsg = "Failed to access entry point!";
                } catch (InstantiationException e) {
                    errMsg = "Failed to instantiate entry point!";
                }
                jar.close();
            } catch (IOException e) {
                errMsg = "Failed to open bot!";
                System.out.println(jarFile.getAbsolutePath());
            }

            if (errMsg != null) {
                EventQueue.invokeLater(() -> setState(State.LOAD_FAILED));

                Log.e(TAG, State.LOAD_FAILED.getStatusText().concat(" Reason: ").concat(errMsg));
            } else {
                Log.i(TAG, "Bot JAR successfully loaded.");
            }
        });
    }

    private void initAudio() {
        audioManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioManager);
    }

    void onReady(ReadyEvent event) {
        SwingUtilities.invokeLater(() -> {
            setState(State.CONNECTED);

            // TODO move this so it is called within setState (probably inside configureUI())
            window.setTitle(event.getJDA().getSelfUser().getName());
        });
    }

    private void doBotLogin() {
        setState(State.CONNECTING);

        asyncExecutor.execute(() -> {
            try {
                Log.i(TAG, "Logging in...");

                String gameString = info.getGame();
                Game game = gameString.isEmpty() ? null : Game.playing(gameString);

                jda = new JDABuilder(AccountType.BOT)
                        .setToken(info.getAuthToken())
                        .setGame(game)
                        .addEventListener(new BotListener(this))
                        .setBulkDeleteSplittingEnabled(false)
                        .buildBlocking();

                guildStates = jda.getGuilds().stream().map(guild -> new GuildState(audioManager, guild))
                        .collect(Collectors.toMap(
                                (GuildState guildState) -> guildState.getGuild().getId(),
                                (guildState -> guildState)
                        ));

                synchronized (infoLock) {
                    info.getParentCommandGroup().onLogin();
                }

                Log.i(TAG, "Successfully logged in!");
                return;
            } catch (LoginException ex) {
                System.err.println("Failed to log the bot in! (Reason: " + ex.getMessage() + ")");
            } catch (IllegalArgumentException ex) {
                System.err.println("Missing bot token! (Source: " + ex.getMessage() + ")");
            } /*catch (RateLimitedException ex) {
                System.err.println("Bot login rate limited! (Reason: " + ex.getMessage() + ")");
            } */catch (InterruptedException ex) {
                System.err.println("Bot login interrupted! (Reason: " + ex.getMessage() + ")");
            }

            // if we reach here, an exception occurred
            EventQueue.invokeLater(() -> setState(State.CONNECT_FAILED));
        });
    }

    private void doBotLogout() {
        doBotLogout(null);
    }

    private void doBotLogout(Runnable after) {
        if (isTerminalState()) {
            if (!isIdleState()) {
                Log.i(TAG, "Logging out...");
                synchronized (infoLock) {
                    info.getParentCommandGroup().onLogout();
                }
            }

            asyncExecutor.execute(() -> {
                if (jda != null) {
                    jda.shutdown();
                    jda = null;
                    Log.i(TAG, "Successfully logged out!");
                }

                EventQueue.invokeLater(() -> {
                    setState(State.IDLE);

                    if (after != null) {
                        after.run();
                    }
                });
            });
        }
    }

    private void openAddToServerURL() {
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        asyncExecutor.execute(() -> {
            try {
                Desktop.getDesktop().browse(URI.create(jda.asBot().getInviteUrl()));
            } catch (IOException ex) {
                System.err.println("Invalid URI!");
            }
        });
    }

    void disposeGUI() {
        if (isTerminalState()) {
            window.close();
        }
    }

    void shutdown() {
        doBotLogout(() -> {
            try {
                asyncExecutor.shutdown();
                if (!asyncExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    asyncExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                asyncExecutor.shutdownNow();
            }
        });
    }

    private void setState(State newState) {
        synchronized (stateLock) {
            state = newState;
        }
        window.onState(state);
    }

    private boolean isTerminalState() {
        synchronized (stateLock) {
            return (state != State.CONNECTING);
        }
    }

    private boolean isIdleState() {
        synchronized (stateLock) {
            return (state == State.IDLE);
        }
    }

    private State getState() {
        synchronized (stateLock) {
            return state;
        }
    }

    private void setInfo(BotInfo info) {
        synchronized (infoLock) {
            BotInfo oldInfo = this.info;

            if (oldInfo != null) {
                oldInfo.getParentCommandGroup().free();

                if (info == null || !oldInfo.getAuthToken().equals(info.getAuthToken())) {
                    doBotLogout();
                }
            }

            this.info = info;
            if (info != null) {
                info.load(this);
            }
        }
    }

    public BotInfo getInfo() {
        synchronized (infoLock) {
            return info;
        }
    }
}
