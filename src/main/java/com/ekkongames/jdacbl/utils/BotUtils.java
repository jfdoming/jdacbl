package com.ekkongames.jdacbl.utils;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.commands.CommandInput;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.RestAction;

import java.awt.Color;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public final class BotUtils {

    private static MessageReceivedEvent event;
    private static boolean usingStaticGuild;
    private static Guild guild;
    private static Member self;
    private static User author;
    private static String authorMention;
    private static MessageChannel channel;

    public static void begin(MessageReceivedEvent currentEvent) {
        event = currentEvent;

        if (!usingStaticGuild) {
            guild = event.getGuild();
        }
        self = guild.getSelfMember();
        channel = event.getChannel();
        author = event.getAuthor();
        authorMention = getMemberName(author);
    }

    public static void begin(Guild guild, User author, MessageChannel channel) {
        event = null;
        if (!usingStaticGuild) {
            BotUtils.guild = guild;
        }
        self = guild.getSelfMember();
        BotUtils.channel = channel;
        BotUtils.author = author;
        authorMention = null;
    }

    public static void end() {
        authorMention = null;
        author = null;
        channel = null;
        self = null;
        if (!usingStaticGuild) {
            guild = null;
        }
        event = null;
    }

    /**
     * @return the event that is currently bound
     */
    public static MessageReceivedEvent getEvent() {
        return event;
    }

    /**
     * Toggles whether to update the guild upon receiving an event.
     */
    public static void toggleStaticGuild() {
        usingStaticGuild = !isUsingStaticGuild()
                || (event != null && !guild.getId().equals(event.getGuild().getId()));
    }

    /**
     * @return whether to update the guild upon receiving an event
     */
    public static boolean isUsingStaticGuild() {
        return usingStaticGuild;
    }

    /**
     * @return the currently bound guild
     */
    public static Guild getGuild() {
        return guild;
    }

    /**
     * @return the member representing the bot user on the current guild
     */
    public static Member getSelf() {
        return self;
    }

    /**
     * @return the user who sent the current message
     */
    public static User getAuthor() {
        return author;
    }

    /**
     * @return a String representing a mention of the user who sent the current message
     */
    public static String getAuthorMention() {
        return authorMention;
    }

    /**
     * @return a MessageChannel representing the channel the current message was sent in
     */
    public static MessageChannel getMessageChannel() {
        return channel;
    }

    /**
     * @param target     the user who sent the command
     * @param roleString a String representing the role to check for
     * @return whether the user has permissions higher or equal to the provided role
     */
    public static boolean checkPermission(User target, String roleString) {
        Role role = PrimitiveUtils.get(guild.getRolesByName(roleString, false));
        if (role == null) {
            return false;
        }

        boolean higherInRoleOrder = guild.getMember(target).canInteract(role);
        boolean hasTheRole = guild.getMember(target).getRoles().contains(role);

        return higherInRoleOrder || hasTheRole;
    }

    private static String getMemberName(User target) {
        return Objects.requireNonNull(guild.retrieveMember(target).complete()).getAsMention();
    }

    private static boolean canInteractWith(Member target) {
        if (!self.canInteract(target)) {
            sendMessage("I don't have permission to modify the user " + target.getAsMention());
            return false;
        }
        return true;
    }

    private static boolean modifyTargetRoles(User targetUser, String roleString, BiFunction<Member, Role, RestAction<Void>> consumer) {
        Member target = guild.getMember(targetUser);

        if (!canInteractWith(target)) {
            return false;
        }

        Role role = PrimitiveUtils.get(guild.getRolesByName(roleString, false));
        if (role == null) {
            sendMessage("The role \"" + roleString + "\" doesn't exist");
            return false;
        }

        consumer.apply(target, role)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to modify roles of the user " + target.getAsMention()));
        return true;
    }

    public static User getTargetUser(CommandInput input, int index) {
        // make sure the user specified a valid target
        User target = PrimitiveUtils.get(input.getMentionedUsers());
        if (target == null) {
            try {
                long targetID = Long.parseLong(input.getToken(index));
                Member member = guild.getMemberById(targetID);

                if (member == null) {
                    return null;
                }
                return member.getUser();
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return target;
    }

    /**
     * Adds the specified role to the target user.
     *
     * @param targetUser the user to add the role to
     * @param roleString the role to add
     * @return whether the role was successfully added to the user
     */
    public static boolean addRoleToUser(User targetUser, String roleString) {
        return modifyTargetRoles(
                targetUser,
                roleString,
                (target, role) -> guild.addRoleToMember(target, role)
        );
    }

    /**
     * Removes the specified role from the target user.
     *
     * @param targetUser the user to remove the role from
     * @param roleString the role to remove
     * @return whether the role was successfully removed from the user
     */
    public static boolean removeRoleFromUser(User targetUser, String roleString) {
        return modifyTargetRoles(
                targetUser,
                roleString,
                (target, role) -> guild.removeRoleFromMember(target, role)
        );
    }

    /**
     * Modify the target role to change its @mentionable status.
     *
     * @param roleString  the role to modify
     * @param mentionable whether the specified role should be @mentionable
     * @return whether the role was successfully modified
     */
    public static boolean setRoleMentionable(String roleString, boolean mentionable) {
        Role role = PrimitiveUtils.get(guild.getRolesByName(roleString, false));
        if (role == null) {
            sendMessage("The role \"" + roleString + "\" doesn't exist");
            return false;
        }

        role.getManager().setMentionable(mentionable)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to modify the role " + roleString)
                );
        return true;
    }

    /**
     * Modify the target role to change its colour.
     *
     * @param roleString the role to modify
     * @param colour     the new colour of the role
     * @return whether the role was successfully modified
     */
    public static boolean setRoleColour(String roleString, Color colour) {
        Role role = PrimitiveUtils.get(guild.getRolesByName(roleString, false));
        if (role == null) {
            sendMessage("The role \"" + roleString + "\" doesn't exist");
            return false;
        }

        role.getManager().setColor(colour)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to modify the role " + roleString)
                );

        return true;
    }

    /**
     * Modify the target role to change its name.
     *
     * @param roleString    the role to modify
     * @param newRoleString the new name of the role
     * @return whether the role was successfully modified
     */
    public static boolean setRoleName(String roleString, String newRoleString) {
        Role role = PrimitiveUtils.get(guild.getRolesByName(roleString, false));
        if (role == null) {
            sendMessage("The role \"" + roleString + "\" doesn't exist");
            return false;
        }

        role.getManager().setName(newRoleString)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to modify the role " + roleString)
                );

        return true;
    }

    /**
     * @param roleString the name of the role to create
     * @return whether the role was successfully created
     */
    public static boolean makeRole(String roleString) {
        guild.createRole().setName(roleString)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to create the role " + roleString)
                );
        return true;
    }

    /**
     * Moves the target user to the specified voice channel.
     *
     * @param targetUser  the user to move
     * @param channelName the voice channel to move the user to
     * @return whether the user was sucessfully moved
     */
    public static boolean moveUserToVoiceChannel(User targetUser, String channelName) {
        // determine the channel to move the target to
        VoiceChannel vChannel = PrimitiveUtils.get(guild.getVoiceChannelsByName(channelName, false));
        if (vChannel == null) {
            sendMessage("The voice channel \"" + channelName + "\" doesn't exist");
            return false;
        }

        // move the target to the new voice channel
        try {
            guild.moveVoiceMember(guild.getMember(targetUser), vChannel).queue();
        } catch (IllegalStateException e) {
            sendMessage("You cannot move a user who isn't in a voice channel");
            return false;
        }
        return true;
    }

    /**
     * Sets the nickname for the target user in the current server.
     *
     * @param targetUser the user to change the nickname of
     * @param nickname   the nickname to use
     * @return whether the user's nickname was successfully changed
     */
    public static boolean setUserNickname(User targetUser, String nickname) {
        if (targetUser == null) {
            Log.d("BotUtils:setUserNickname", "NPE");
            return false;
        }

        Member targetMember = guild.getMember(targetUser);
        if (!self.canInteract(targetMember)) {
            sendMessage("You do not have permission to change the nickname of someone with higher privileges than yourself");
            return false;
        }

        targetMember.modifyNickname(nickname)
                .queue(
                        null,
                        (t) -> sendMessage("Failed to modify the nickname of the user " + targetUser.getAsMention()));
        return true;
    }

    /**
     * Posts a message in the channel of the current event. The message will contain an @mention
     * for the author of the trigger message.
     *
     * @param message the message to send
     */
    public static void sendMessage(String message) {
        if (event == null) {
            Log.i("Bot", message + "!");
        } else if (authorMention == null) {
            sendPlainMessage(message + "!");
        } else {
            sendPlainMessage(message + ", " + authorMention + "!");
        }
    }

    /**
     * Posts a message in the channel of the current event.
     *
     * @param message the message to send
     */
    public static void sendPlainMessage(String message) {
        if (channel != null) {
            channel.sendMessage(message).queue(result -> {});
        } else {
            Log.i("Bot", message);
        }
    }

    /**
     * Sends a direct message to the target user.
     *
     * @param target  the user to DM
     * @param message the message to send
     */
    public static void sendPrivateMessage(User target, String message) {
        target.openPrivateChannel()
                .queue(
                        privateChannel -> privateChannel.sendMessage(message).queue(),
                        t -> System.err.println("Failed to send PM!")
                );
    }

    private BotUtils() {
    }

}
