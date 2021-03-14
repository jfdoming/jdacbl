package com.ekkongames.jdacbl.commands;

import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class CommandInput {

    private final List<String> commandParts;
    private final List<User> mentionedUsers;
    private final User sender;

    public CommandInput(CommandInput toCopy) {
        this.commandParts = new ArrayList<>(toCopy.commandParts);
        this.mentionedUsers = toCopy.mentionedUsers;
        this.sender = toCopy.sender;
    }

    public CommandInput(List<String> commandParts) {
        this.commandParts = commandParts;
        this.mentionedUsers = new ArrayList<>();
        this.sender = null;
    }

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
