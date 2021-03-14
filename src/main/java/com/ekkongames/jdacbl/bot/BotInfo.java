package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.commands.CommandGroup;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BotInfo {

    private final String authToken;

    private String game;
    private CommandGroup[] commandGroups;

    private volatile boolean loaded;

    private BotInfo(String authToken) {
        this.authToken = authToken;
        this.loaded = false;
    }

    void load(Bot bot) {
        for (CommandGroup commandGroup : commandGroups) {
            commandGroup.addTo(bot);
        }
        loaded = true;
    }

    void onLogin() {
        for (CommandGroup commandGroup : commandGroups) {
            commandGroup.onLogin();
        }
    }

    void onLogout() {
        for (CommandGroup commandGroup : commandGroups) {
            commandGroup.onLogout();
        }
    }

    void free() {
        for (CommandGroup commandGroup : commandGroups) {
            commandGroup.free();
        }
    }

    private void assertLoaded() {
        if (!loaded) {
            throw new IllegalStateException("Bot not loaded!");
        }
    }

    public CommandGroup[] getCommandGroups() {
        return commandGroups;
    }

    String getAuthToken() {
        return authToken;
    }

    public String getGame() {
        return game;
    }

    public static final class Builder {

        private String game;
        private final ArrayList<CommandGroup> commandGroups;

        public Builder() {
            game = "";
            commandGroups = new ArrayList<>();
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
        public BotInfo.Builder addCommandGroup(CommandGroup commandGroup) {
            this.commandGroups.add(commandGroup);
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
            botInfo.game = game;

            // ensure a command group exists
            if (commandGroups.size() == 0) {
                commandGroups.add(new CommandGroup.Builder().build());
            }

            botInfo.commandGroups = commandGroups.toArray(new CommandGroup[0]);
            return botInfo;
        }

    }
}
