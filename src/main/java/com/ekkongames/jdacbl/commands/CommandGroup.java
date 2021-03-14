package com.ekkongames.jdacbl.commands;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.utils.BotUtils;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A logical group of commands in your Discord bot. Groups have a common prefix (optional)
 * and a common help command.
 */
public class CommandGroup {

    private Command[] commands;

    // data cached for use in the help command
    private List<Command> visibleCommands;
    private final boolean containsAuthCommands;
    private final boolean requestSilent;
    private final int longestAuthRole;
    private final String commandPrefix;

    private Bot bot;

    private CommandGroup(Builder builder) {
        // store a sorted list of all commands
        this.commands = builder.commands.toArray(new Command[0]);

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
        commandPrefix = builder.commandPrefix;
        requestSilent = builder.requestSilent;
    }

    private static final Pattern PARTS_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");
    private List<String> breakIntoParts(String commandString) {
        List<String> list = new ArrayList<>();
        Matcher m = PARTS_PATTERN.matcher(commandString);
        while (m.find())
            list.add(m.group(1).replace("\"", ""));
        return list;
    }

    public boolean exec(String message, List<User> mentionedUsers, User sender, boolean silent) {
        // only look at messages using a command
        if (!message.startsWith(commandPrefix)) {
            return false;
        }

        // strip the command string out
        message = message.substring(commandPrefix.length());

        // break the command down into its parameters
        List<String> commandParts = breakIntoParts(message);
        return exec(new CommandInput(commandParts, new ArrayList<>(), null), silent);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean exec(CommandInput input) {
        return exec(input, false);
    }

    public boolean exec(CommandInput input, boolean silent) {
        if (commands == null) {
            return false;
        }

        if (input.getTokenCount() == 0) {
            if (!silent && !requestSilent) {
                BotUtils.sendMessage("Empty command");
            }
            return false;
        }

        // Check whether a user sent a valid command.
        boolean found = false;
        for (Command command : commands) {
            CommandInfo commandInfo = command.getCommandInfo();
            String[] commandNames = commandInfo.getNames();
            for (String name : commandNames) {
                if (name.isEmpty() || name.equals(input.getToken(0))) {
                    if (commandInfo.requiresAuthentication()) {
                        if (!BotUtils.checkPermission(input.getSender(), commandInfo.getAuthenticationRole())) {
                            if (!silent && !requestSilent) {
                                BotUtils.sendMessage("You do not have permission to use this command");
                            }
                            continue;
                        }
                    }
                    command.exec(input);
                    found = true;
                }
            }
        }

        if (found) {
            return true;
        }

        // The user sent an unknown command.
        if (!silent && !requestSilent) {
            BotUtils.sendMessage("Couldn't understand that");
        }
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

    String getPrefix() {
        return commandPrefix;
    }

    public static class Builder {
        private final ArrayList<Command> commands;
        private String commandPrefix;
        private boolean helpEnabled;
        private boolean requestSilent;

        public Builder() {
            this.commands = new ArrayList<>();
            this.commandPrefix = "";
            this.helpEnabled = true;
        }

        public Builder add(Command command) {
            commands.add(command);
            return this;
        }

        public Builder setCommandPrefix(String prefix) {
            this.commandPrefix = prefix;
            return this;
        }

        public Builder disableHelp() {
            this.helpEnabled = false;
            return this;
        }

        public Builder setSilent(boolean silent) {
            this.requestSilent = silent;
            return this;
        }

        public CommandGroup build() {
            if (this.helpEnabled) {
                this.add(new Help());
            }
            return new CommandGroup(this);
        }
    }
}
