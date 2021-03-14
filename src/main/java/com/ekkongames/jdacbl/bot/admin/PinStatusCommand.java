package com.ekkongames.jdacbl.bot.admin;

import com.ekkongames.jdacbl.bot.BotListener;
import com.ekkongames.jdacbl.commands.Command;
import com.ekkongames.jdacbl.commands.CommandInfo;
import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.BotUtils;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;

public class PinStatusCommand extends Command {
    private final BotListener botListener;

    public PinStatusCommand(BotListener botListener) {
        super(new CommandInfo.Builder()
                .names("s", "pinStatus")
                .summary("print status information about the pinned entities")
                .build());
        this.botListener = botListener;
    }

    @Override
    public void exec(CommandInput input) {
        StringBuilder messageBuilder = new StringBuilder();

        if (BotUtils.getEvent() != null) {
            messageBuilder.append("```\n");
        }

        messageBuilder.append("Guild: ");
        if (botListener.getPinnedGuild() == null) {
            messageBuilder.append("null\n");
        } else {
            messageBuilder.append(botListener.getPinnedGuild().getId());
            messageBuilder.append("\n");
        }

        messageBuilder.append("Sender: ");
        if (botListener.getPinnedSender() == null) {
            messageBuilder.append("null\n");
        } else {
            messageBuilder.append(botListener.getPinnedSender().getId());
            messageBuilder.append("\n");

            messageBuilder.append("Sender member: ");
            if (botListener.getPinnedGuild() == null) {
                messageBuilder.append("null\n");
            } else {
                Member pinnedMember = botListener
                        .getPinnedGuild()
                        .retrieveMember(botListener.getPinnedSender())
                        .complete();
                if (pinnedMember == null) {
                    messageBuilder.append("null\n");
                } else {
                    messageBuilder.append(pinnedMember.getId());
                    messageBuilder.append("\n");
                }
            }
        }

        messageBuilder.append("Channel: ");
        if (botListener.getPinnedChannel() == null) {
            messageBuilder.append("null\n");
        } else {
            messageBuilder.append(botListener.getPinnedChannel().getId());
            messageBuilder.append("\n");
        }

        if (BotUtils.getEvent() != null) {
            messageBuilder.append("```");
        }

        BotUtils.sendPlainMessage(messageBuilder.toString());
    }
}
