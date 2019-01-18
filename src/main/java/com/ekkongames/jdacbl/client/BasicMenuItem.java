package com.ekkongames.jdacbl.client;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BasicMenuItem extends ComponentProvider {

    private final JMenuItem item;

    public BasicMenuItem(String text, int shortcut) {
        item = new JMenuItem(text, shortcut);
        item.setAccelerator(KeyStroke.getKeyStroke(shortcut, InputEvent.ALT_MASK));
    }

    public void addActionListener(ActionListener listener) {
        item.addActionListener(listener);
    }

    public void setEnabled(boolean enabled) {
        item.setEnabled(enabled);
    }

    @Override
    Component getComponent() {
        return item;
    }

}
