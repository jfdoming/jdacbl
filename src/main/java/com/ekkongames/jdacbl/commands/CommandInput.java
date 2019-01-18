package com.ekkongames.jdacbl.commands;

import net.dv8tion.jda.core.entities.User;

import java.util.List;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class CommandInput {

    private final List<String> commandParts;
    private final List<User> mentionedUsers;
    private User sender;

    public CommandInput(List<String> commandParts, List<User> mentionedUsers, User sender) {
        this.commandParts = commandParts;
        this.mentionedUsers = mentionedUsers;
        this.sender = sender;
    }

    public int getTokenCount() {
        return commandParts.size();
    }

    public String getToken(int index) {
        return commandParts.get(index);
    }

    public List<User> getMentionedUsers() {
        return mentionedUsers;
    }

    public void discardToken(int index) {
        commandParts.remove(index);
    }

    public User getSender() {
        return sender;
    }
}
