package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.bot.admin.*;
import com.ekkongames.jdacbl.commands.CommandGroup;
import com.ekkongames.jdacbl.utils.BotUtils;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.DisconnectEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BotListener extends ListenerAdapter implements Consumer<String> {

    private final CommandGroup adminGroup;
    private final Bot bot;
    private Guild pinnedGuild;
    private User pinnedSender;
    private MessageChannel pinnedChannel;

    public BotListener(Bot bot) {
        this.bot = bot;
        this.adminGroup = new CommandGroup.Builder()
                .setCommandPrefix("\\")
                .add(new PinGuildCommand(this, bot))
                .add(new PinSenderCommand(this, bot))
                .add(new PinChannelCommand(this))
                .add(new PinStatusCommand(this))
                .add(new ClearConsoleCommand(bot))
                .build();
        pinnedGuild = null;
        pinnedSender = null;
        pinnedChannel = null;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        bot.onReady(event);

        Log.d("BotListener", "Bot is ready!");
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Log.d("BotListener", "User joined: " + event.getMember().getEffectiveName());
    }

    @Override
    public void onDisconnect(@NotNull DisconnectEvent event) {
        super.onDisconnect(event);
    }

    public void setPinnedGuild(Guild pinnedGuild) {
        this.pinnedGuild = pinnedGuild;
    }

    public Guild getPinnedGuild() {
        return pinnedGuild;
    }

    public void setPinnedSender(User pinnedSender) {
        this.pinnedSender = pinnedSender;
    }

    public User getPinnedSender() {
        return pinnedSender;
    }

    public void setPinnedChannel(MessageChannel pinnedChannel) {
        this.pinnedChannel = pinnedChannel;
    }

    public MessageChannel getPinnedChannel() {
        return pinnedChannel;
    }

    @Override
    public void accept(String command) {
        BotInfo botInfo = bot.getInfo();

        if (command.startsWith("\\")) {
            BotUtils.end(); // Make sure we don't have any leftover state.
            adminGroup.exec(command, new ArrayList<>(), null, false);
            return;
        }

        if (pinnedGuild == null) {
            Log.w("BotListener", "You must select a guild to run commands in first!");
            return;
        }
        if (pinnedSender == null) {
            Log.w("BotListener", "You must select a sender to run commands first!");
            return;
        }
        if (pinnedChannel == null) {
            Log.w("BotListener", "You must select a channel to send messages to first!");
            return;
        }

        // Load the corresponding member into the cache.
        if (pinnedGuild.retrieveMember(pinnedSender).complete() == null) {
            Log.w("BotListener", "Failed to retrieve the member for the pinned sender.");
            return;
        }

        if (pinnedGuild.getMember(pinnedSender) == null) {
            Log.w("BotListener", "Inconsistent sender member state.");
            return;
        }

        BotUtils.begin(pinnedGuild, pinnedSender, pinnedChannel);
        CommandGroup[] groups = botInfo.getCommandGroups();
        for (CommandGroup group : groups) {
            if (group.exec(
                    command,
                    new ArrayList<>(),
                    pinnedSender,
                    false
            )) {
                break;
            }
        }
        BotUtils.end();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (this.pinnedGuild == null) {
            this.pinnedGuild = event.getGuild();
        }

        // private messages shouldn't trigger this bot
        if (event.isFromType(ChannelType.PRIVATE)) {
            return;
        }

        User sender = event.getAuthor();

        // don't receive messages from this or other bots
        if (sender.isBot()) {
            return;
        }

        BotUtils.begin(event);
        String messageText = event.getMessage().getContentRaw();

        BotInfo botInfo = bot.getInfo();

        CommandGroup[] groups = botInfo.getCommandGroups();
        for (CommandGroup group : groups) {
            if (group.exec(
                    messageText,
                    event.getMessage().getMentionedUsers(),
                    sender,
                    false
            )) {
                break;
            }
        }
        BotUtils.end();
    }
}
