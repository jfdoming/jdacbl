package com.ekkongames.jdacbl.commands;

import com.ekkongames.jdacbl.utils.BotUtils;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class Help extends Command {

    private String generalHelpText;

    public Help() {
        super(new CommandInfo.Builder()
                .names("help", "?")
                .summary("provide information about commands you can use with this bot")
                .usage("[command]")
                .build());
    }

    @Override
    public void addTo(CommandGroup commandGroup) {
        super.addTo(commandGroup);

        // build the general output
        StringBuilder output = new StringBuilder();
        output.append("```");
        for (Command command : commandGroup.getVisibleCommands()) {
            CommandInfo commandInfo = command.getCommandInfo();

            if (commandGroup.containsAuthCommands()) {
                output.append(
                        String.format(
                                "[ %-" + commandGroup.getLongestAuthRole() + "s ] ",
                                (commandInfo.requiresAuthentication() ? commandInfo.getAuthenticationRole() : "")
                        )
                );
            }
            output.append(commandGroup.getPrefix());
            output.append(commandInfo.getNames()[0]);
            output.append(": ");
            output.append(commandInfo.getHelpText());
            output.append("\n");
        }
        output.deleteCharAt(output.length() - 1);
        output.append("```");

        generalHelpText = output.toString();
    }

    @Override
    public void exec(CommandInput input) {
        // check if the user is asking for a specific command or for general information
        if (input.getTokenCount() > 1) {
            StringBuilder output = new StringBuilder();

            for (Command command : commandGroup.getVisibleCommands()) {
                CommandInfo commandInfo = command.getCommandInfo();
                String[] commandNames = commandInfo.getNames();
                for (String name : commandNames) {
                    if (name.equals(input.getToken(1))) {
                        if (commandInfo.requiresAuthentication()) {
                            output.append(String.format("[ %s ] ", commandInfo.getAuthenticationRole()));
                        }

                        output.append(commandInfo.getNames()[0]).append(" ").append(commandInfo.getUsage());
                        output.append("\n\n");
                        String helpText = commandInfo.getHelpText();
                        output.append(Character.toUpperCase(helpText.charAt(0)));
                        output.append(helpText.substring(1)).append(". ");
                        output.append(commandInfo.getMoreHelpText());
                        break;
                    }
                }
            }
            if (output.length() == 0) {
                output = new StringBuilder("Didn't find a command with the name \"" + input.getToken(1) + "\"");
            }
            BotUtils.sendPlainMessage("```" + output.toString().trim() + "```");
        } else if (generalHelpText == null || generalHelpText.isEmpty()) {
            throw new IllegalStateException("Help command not initialized!");
        } else {
            BotUtils.sendPlainMessage(generalHelpText);
        }
    }

}
