package com.ekkongames.jdacbl.bot;

import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.BotUtils;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BotListener extends ListenerAdapter {

    private final Bot bot;

    public BotListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void onReady(ReadyEvent event) {
        super.onReady(event);
        bot.onReady(event);

        Log.d("BotListener", "Bot is ready!");
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        super.onGuildMemberJoin(event);
        Log.d("BotListener", "User joined: " + event.getMember().getEffectiveName());
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        super.onDisconnect(event);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
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

        // only look at messages using a command
        if (!messageText.startsWith(botInfo.getCommandPrefix())) {
            parsePlaintext(botInfo, messageText.toLowerCase(), sender);
            BotUtils.end();
            return;
        }

        // strip the command string out
        messageText = messageText.substring(botInfo.getCommandPrefix().length());

        // break the command down into its parameters
        List<String> commandParts = breakIntoParts(messageText);

        botInfo.getParentCommandGroup().exec(new CommandInput(
                commandParts,
                event.getMessage().getMentionedUsers(), sender)
        );
        BotUtils.end();
    }

    private List<String> breakIntoParts(String commandString) {
        List<String> list = new ArrayList<>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(commandString);
        while (m.find())
            list.add(m.group(1).replace("\"", ""));
        return list;
    }

    private void parsePlaintext(BotInfo botInfo, String messageText, User user) {
        for (Lottery lottery : botInfo.getLotteries()) {
            // check if this user won the lottery
            if (Math.random() < lottery.getWinChance()) {
                BotUtils.sendPrivateMessage(user, lottery.getWinMessage());
            }
        }

        // check if the bot should correct poor language
        if (!botInfo.isAllowSwears()) {
            // this functionality has been disabled
            /*for (String swear : BotConstants.SWEARS) {
                // if smart filtering is on, use a regex to match swears more effectively
                boolean condition;
                if (botInfo.isSmartFiltering()) {
                    condition = messageText.matches("(.*\\s)?" + swear + "(\\s.*)?");
                } else {
                    condition = messageText.contains(swear);
                }

                if (condition) {
                    BotUtils.sendMessage("Watch your language");
                    return;
                }
            }*/
        }
    }

}
