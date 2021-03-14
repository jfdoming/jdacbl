package com.ekkongames.jdacbl.bot.jar;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.bot.BotInfo;

import java.util.function.Supplier;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public interface EntryPoint extends Supplier<BotInfo> {

    /**
     * Called automatically (assuming the proper manifest attributes) to generate a BotInfo object
     * that will be used to initialize the Bot.
     *
     * @return a BotInfo object that will be used to initialize a Bot
     */
    BotInfo getInfo();

    default BotInfo get() {
        return getInfo();
    }
}
