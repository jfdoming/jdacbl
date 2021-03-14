package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.bot.jar.EntryPoint;
import com.ekkongames.jdacbl.bot.jar.DynamicJar;
import com.ekkongames.jdacbl.client.HostWindow;
import com.ekkongames.jdacbl.utils.Log;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import javax.security.auth.login.LoginException;
import javax.swing.SwingUtilities;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private DynamicJar.Loader<BotInfo, EntryPoint> jarFile;
    private DynamicJar<BotInfo, EntryPoint> jar = null;

    private AudioPlayerManager audioManager;

    private Map<String, GuildState> guildStates;
    private BotListener botListener;

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
        window.addInputListener(this::receiveUICommand);
        window.open();
    }

    private void receiveUICommand(String message) {
        if (botListener != null) {
            botListener.accept(message);
        }
    }

    private void resolvePath(String[] args) {
        jarFile = new DynamicJar.Resolver<>(EntryPoint.class)
                .checkArgs(args)
                .checkConfig(new File("config.properties"))
                .resolve();
    }

    private void prepareAsync() {
        asyncExecutor = Executors.newCachedThreadPool();
    }

    private void loadInfo() {
        if (jar != null) {
            try {
                jar.unload();
            } catch (IOException e) {
                Log.e(TAG, e);
            }
            jar = null;
        }

        asyncExecutor.execute(() -> {
            State originalState;
            synchronized (stateLock) {
                originalState = state;
            }

            setInfo(null);

            if (jarFile == null) {
                EventQueue.invokeLater(() -> setState(State.LOAD_FAILED));
                return;
            }

            String errMsg = null;
            Throwable errThrowable = null;
            try {
                jar = jarFile.load();

                setInfo(jar.run());
                if (getInfo() == null) {
                    errMsg = "BotInfo was null";
                }
            } catch (IOException e) {
                errMsg = "Failed to open bot!";
                errThrowable = e;
                Log.d(TAG, "Jar load failure path: " + jarFile.getAbsolutePath());
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                errMsg = "Failed to locate entry point!";
                errThrowable = e;
            } catch (IllegalAccessException e) {
                errMsg = "Failed to access entry point!";
                errThrowable = e;
            } catch (InstantiationException e) {
                errMsg = "Failed to instantiate entry point!";
                errThrowable = e;
            } catch (NoSuchMethodException | InvocationTargetException e) {
                errMsg = "Failed to invoke entry point!";
                errThrowable = e;
            }

            if (errMsg != null) {
                EventQueue.invokeLater(() -> setState(State.LOAD_FAILED));

                Log.e(TAG, State.LOAD_FAILED.getStatusText().concat(" Reason: ").concat(errMsg), errThrowable);
            } else {
                Log.i(TAG, "Bot JAR successfully loaded.");

                if (originalState == State.CONNECTED) {
                    // Need to log back in.
                    doBotLogin();
                } else {
                    synchronized (stateLock) {
                        // Refresh the UI for the current state.
                        window.onState(state);
                    }
                }
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
                Activity game = gameString.isEmpty() ? null : Activity.playing(gameString);

                botListener = new BotListener(this);
                jda = JDABuilder.createDefault(info.getAuthToken())
                        .enableIntents(GatewayIntent.GUILD_MEMBERS)
                        .enableIntents(GatewayIntent.GUILD_PRESENCES)
                        .enableIntents(GatewayIntent.GUILD_VOICE_STATES)
                        .enableCache(CacheFlag.ACTIVITY)
                        .setMemberCachePolicy(MemberCachePolicy.ALL)
                        .setActivity(game)
                        .addEventListeners(botListener)
                        .setBulkDeleteSplittingEnabled(false)
                        .build();
                jda.awaitReady();

                guildStates = jda.getGuilds().stream().map(guild -> new GuildState(audioManager, guild))
                        .collect(Collectors.toMap(
                                (GuildState guildState) -> guildState.getGuild().getId(),
                                (guildState -> guildState)
                        ));

                synchronized (infoLock) {
                    info.onLogin();
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
                    info.onLogout();
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
                Desktop.getDesktop().browse(URI.create(jda.getInviteUrl()));
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

    public void clearOutput() {
        window.clearOutput();
    }

    private void setState(State newState) {
        synchronized (stateLock) {
            state = newState;
        }
        window.onState(newState);
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

    private void setInfo(BotInfo info) {
        synchronized (infoLock) {
            BotInfo oldInfo = this.info;

            if (oldInfo != null) {
                oldInfo.free();

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

    public JDA getJDA() {
        return jda;
    }
}
