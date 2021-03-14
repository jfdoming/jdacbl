package com.ekkongames.jdacbl.bot.admin;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.bot.BotListener;
import com.ekkongames.jdacbl.commands.Command;
import com.ekkongames.jdacbl.commands.CommandInfo;
import com.ekkongames.jdacbl.commands.CommandInput;
import com.ekkongames.jdacbl.utils.BotUtils;
import net.dv8tion.jda.api.entities.Member;

public class ClearConsoleCommand extends Command {
    private final Bot bot;

    public ClearConsoleCommand(Bot bot) {
        super(new CommandInfo.Builder()
                .names("clear")
                .summary("clear the console")
                .build());
        this.bot = bot;
    }

    @Override
    public void exec(CommandInput input) {
        bot.clearOutput();
    }
}
