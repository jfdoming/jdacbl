package com.ekkongames.jdacbl.bot.admin;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.bot.BotListener;
import com.ekkongames.jdacbl.commands.Command;
import com.ekkongames.jdacbl.commands.CommandInfo;
import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.Log;
import net.dv8tion.jda.api.entities.Guild;

public class PinGuildCommand extends Command {
    private final BotListener botListener;
    private final Bot bot;

    public PinGuildCommand(BotListener botListener, Bot bot) {
        super(new CommandInfo.Builder()
                .names("g", "pinGuild")
                .summary("select the guild to use from the UI")
                .build());
        this.botListener = botListener;
        this.bot = bot;
    }

    @Override
    public void exec(CommandInput input) {
        if (input.getTokenCount() < 2) {
            Log.w("PinGuildCommand", "Please specify a guild ID to use.");
            return;
        }
        Guild guildToPin;
        try {
            guildToPin = bot.getJDA().getGuildById(input.getToken(1));
        } catch (NumberFormatException e) {
            Log.w("PinGuildCommand", "Bad number format. Please use a guild ID.");
            return;
        }
        if (guildToPin == null) {
            Log.w("PinGuildCommand", "Guild not found.");
            return;
        }
        botListener.setPinnedGuild(guildToPin);
    }
}
