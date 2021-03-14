package com.ekkongames.jdacbl.bot.admin;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.bot.BotListener;
import com.ekkongames.jdacbl.commands.Command;
import com.ekkongames.jdacbl.commands.CommandInfo;
import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class PinSenderCommand extends Command {
    private final BotListener botListener;
    private final Bot bot;

    public PinSenderCommand(BotListener botListener, Bot bot) {
        super(new CommandInfo.Builder()
                .names("u", "pinUser")
                .summary("select the sender to use from the UI")
                .build());
        this.botListener = botListener;
        this.bot = bot;
    }

    @Override
    public void exec(CommandInput input) {
        if (input.getTokenCount() < 2) {
            Log.w("PinSenderCommand", "Please specify a user ID to use.");
            return;
        }
        Guild pinnedGuild = botListener.getPinnedGuild();
        if (pinnedGuild == null) {
            Log.w("PinSenderCommand", "Please pin a guild first.");
            return;
        }
        User senderToPin;
        try {
            senderToPin = bot.getJDA().getUserById(input.getToken(1));
        } catch (NumberFormatException e) {
            Log.w("PinSenderCommand", "Bad number format. Please use a user ID.");
            return;
        }
        if (senderToPin == null) {
            Log.w("PinSenderCommand", "User not found.");
            return;
        }

        // Load the corresponding member into the cache.
        if (pinnedGuild.retrieveMember(senderToPin).complete() == null) {
            Log.w("PinSenderCommand", "Failed to retrieve the member for that user. Maybe they're in a different guild?");
            return;
        }

        botListener.setPinnedSender(senderToPin);
    }
}
