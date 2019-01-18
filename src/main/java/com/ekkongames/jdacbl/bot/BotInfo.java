package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.commands.CommandGroup;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BotInfo {

    // persistent preferences
    private static final Preferences preferences = Preferences.userNodeForPackage(Bot.class);

    private final String authToken;

    private String commandPrefix;
    private String game;
    private CommandGroup commandGroup;

    private Lottery[] lotteries;
    private volatile boolean loaded;

    private BotInfo(String authToken) {
        this.authToken = authToken;
        this.loaded = false;
    }

    void load(Bot bot) {
        commandGroup.addTo(bot);
        loaded = true;
    }

    /**
     * @return whether swear words will be allowed or blocked
     */
    public boolean isAllowSwears() {
        assertLoaded();

        synchronized (preferences) {
            return preferences.getBoolean("allowSwears", true);
        }
    }

    private void assertLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Bot not loaded!");
        }
    }

    public void setAllowSwears(boolean allowSwears) {
        assertLoaded();

        synchronized (preferences) {
            preferences.putBoolean("allowSwears", allowSwears);
        }
    }

    /**
     * @return whether swear words will be filtered using regexes
     */
    public boolean isSmartFiltering() {
        assertLoaded();

        synchronized (preferences) {
            return preferences.getBoolean("allowSwears", true);
        }
    }

    public void setSmartFiltering(boolean smartFiltering) {
        assertLoaded();

        synchronized (preferences) {
            preferences.putBoolean("allowSwears", smartFiltering);
        }
    }

    public String getCommandPrefix() {
        return commandPrefix;
    }

    public CommandGroup getParentCommandGroup() {
        return commandGroup;
    }

    public Lottery[] getLotteries() {
        return lotteries;
    }

    String getAuthToken() {
        return authToken;
    }

    public String getGame() {
        return game;
    }

    public static final class Builder {

        private String commandPrefix;
        private String game;
        private CommandGroup commandGroup;
        private ArrayList<Lottery> lotteries;

        public Builder() {
            commandPrefix = "b!";
            game = "";
            lotteries = new ArrayList<>();
        }

        /**
         * Sets a string that will prefix commands.
         *
         * @param commandPrefix the string to use, not empty
         * @return the builder for method call chaining
         */
        public BotInfo.Builder setCommandPrefix(String commandPrefix) {
            if (commandPrefix.isEmpty()) {
                return this;
            }

            this.commandPrefix = commandPrefix;
            return this;
        }

        /**
         * Sets the game the bot is playing.
         *
         * @param game the game the bot is playing
         * @return the builder for method call chaining
         */
        public BotInfo.Builder setGame(String game) {
            this.game = game;
            return this;
        }

        /**
         * Sets the commands that are available with this bot.
         *
         * @param commandGroup the commands
         * @return the builder for method call chaining
         */
        public BotInfo.Builder setCommandGroup(CommandGroup commandGroup) {
            this.commandGroup = commandGroup;
            return this;
        }

        /**
         * Adds a lottery to the bot, that will be drawn every time a message is sent.
         *
         * @param lottery the lottery to add
         * @return the builder for method call chaining
         */
        public BotInfo.Builder addLottery(Lottery lottery) {
            lotteries.add(lottery);
            return this;
        }

        /**
         * Builds a BotInfo object that can be used to initialize a bot.
         *
         * @param authToken the token used by the bot to log in
         * @return a BotInfo object that can be used to initialize a bot
         */
        public BotInfo build(String authToken) {
            BotInfo botInfo = new BotInfo(authToken);
            botInfo.commandPrefix = commandPrefix;
            botInfo.game = game;
            botInfo.lotteries = lotteries.toArray(new Lottery[0]);

            // ensure a command group exists
            if (commandGroup == null) {
                commandGroup = new CommandGroup.Builder()
                        .build();
            }

            botInfo.commandGroup = commandGroup;
            return botInfo;
        }

    }
}
