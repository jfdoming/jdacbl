package com.ekkongames.jdacbl.bot.admin;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.bot.BotListener;
import com.ekkongames.jdacbl.commands.Command;
import com.ekkongames.jdacbl.commands.CommandInfo;
import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class PinChannelCommand extends Command {
    private final BotListener botListener;

    public PinChannelCommand(BotListener botListener) {
        super(new CommandInfo.Builder()
                .names("c", "pinChannel")
                .summary("select the channel to use from the UI")
                .build());
        this.botListener = botListener;
    }

    @Override
    public void exec(CommandInput input) {
        if (input.getTokenCount() < 2) {
            Log.w("PinChannelCommand", "Please specify a channel ID to use.");
            return;
        }
        Guild pinnedGuild = botListener.getPinnedGuild();
        if (pinnedGuild == null) {
            Log.w("PinChannelCommand", "Please pin a guild first.");
            return;
        }
        MessageChannel channelToPin;
        try {
            channelToPin = pinnedGuild.getTextChannelById(input.getToken(1));
        } catch (NumberFormatException e) {
            Log.w("PinChannelCommand", "Bad number format. Please use a channel ID.");
            return;
        }
        if (channelToPin == null) {
            Log.w("PinChannelCommand", "Channel not found.");
            return;
        }
        botListener.setPinnedChannel(channelToPin);
    }
}
