package com.ekkongames.jdacbl.commands;

/**
 *
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public abstract class Command {

    private final CommandInfo commandInfo;
    protected CommandGroup commandGroup;
    protected CommandGroup children;

    protected Command(CommandInfo commandInfo) {
        this(commandInfo, null);
    }

    protected Command(CommandInfo commandInfo, CommandGroup children) {
        this.commandInfo = commandInfo;
        this.children = children;
    }

    public void addTo(CommandGroup commandGroup) {
        this.commandGroup = commandGroup;

        if (children != null) {
            children.addTo(commandGroup.getBot());
        }
    }

    public CommandInfo getCommandInfo() {
        return commandInfo;
    }

    public abstract void exec(CommandInput input);

    /**
     * Called when the bot is logged in, to initialize any resources used while logged in.
     */
    public void onLogin() {}

    /**
     * Called when the bot is logged out, to dispose of any resources used while logged in.
     */
    public void onLogout() {}

    @Override
    public String toString() {
        return commandInfo.getNames()[0];
    }
}
