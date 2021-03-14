package com.ekkongames.jdacbl.bot;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class WindowCloseListener extends WindowAdapter {

    private final Bot bot;

    public WindowCloseListener(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        bot.disposeGUI();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        bot.shutdown();
    }
}
