package com.ekkongames.jdacbl.commands;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.utils.BotUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dolphish on 2016-10-28.
 */
public class CommandGroup {

    private Command[] commands;

    // data cached for use in the help command
    private List<Command> visibleCommands;
    private boolean containsAuthCommands;
    private int longestAuthRole;

    private Bot bot;

    private CommandGroup(Builder builder) {
        // store a sorted list of all commands
        this.commands = builder.commands.toArray(new Command[builder.commands.size()]);

        // store a sorted list of visible commands
        visibleCommands = Collections.unmodifiableList(
                builder.commands.parallelStream()
                        .filter((Command s) -> s.getCommandInfo().isVisible())
                        .sorted(
                                Comparator.comparing((Command c) -> c.getCommandInfo().getAuthenticationRole())
                                        .thenComparing(Command::toString)
                        )
                        .collect(Collectors.toList())
        );

        // determine if there are any commands that require authentication
        longestAuthRole = visibleCommands.stream()
                .mapToInt(command -> command.getCommandInfo().getAuthenticationRole().length())
                .max()
                .orElse(0);
        containsAuthCommands = (longestAuthRole > 0);
    }

    public boolean exec(CommandInput input) {
        if (commands == null) {
            return false;
        }

        if (input.getTokenCount() == 0) {
            BotUtils.sendMessage("Empty command");
            return false;
        }

        // check whether a user sent a valid command
        for (Command command : commands) {
            CommandInfo commandInfo = command.getCommandInfo();
            String[] commandNames = commandInfo.getNames();
            for (String name : commandNames) {
                if (name.equals(input.getToken(0))) {
                    if (commandInfo.requiresAuthentication()) {
                        if (!BotUtils.checkPermission(input.getSender(), commandInfo.getAuthenticationRole())) {
                            BotUtils.sendMessage("You do not have permission to use this command");
                            return false;
                        }
                    }
                    command.exec(input);
                    return true;
                }
            }
        }

        // the user sent an unknown command
        BotUtils.sendMessage("Couldn't understand that");
        return false;
    }

    List<Command> getVisibleCommands() {
        return visibleCommands;
    }

    boolean containsAuthCommands() {
        return containsAuthCommands;
    }

    int getLongestAuthRole() {
        return longestAuthRole;
    }

    public void free() {
        visibleCommands = null;
        commands = null;
    }

    public void onLogin() {
        if (commands != null) {
            for (Command c : commands) {
                c.onLogin();
            }
        }
    }

    public void onLogout() {
        if (commands != null) {
            for (Command c : commands) {
                c.onLogout();
            }
        }
    }

    public void addTo(Bot bot) {
        this.bot = bot;

        for (Command command : commands) {
            command.addTo(this);
        }
    }

    public Bot getBot() {
        return bot;
    }

    public static class Builder {
        private ArrayList<Command> commands;

        public Builder() {
            this.commands = new ArrayList<>();
            this.add(new Help());
        }

        public Builder add(Command command) {
            commands.add(command);
            return this;
        }

        public CommandGroup build() {
            return new CommandGroup(this);
        }
    }
}
