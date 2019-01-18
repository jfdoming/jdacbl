package com.ekkongames.jdacbl.client;

import javax.swing.JMenu;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.util.EnumMap;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class BotMenu extends ComponentProvider {

    public static final int LOGIN_ITEM_INDEX = 0;
    public static final int ADD_TO_SERVER_ITEM_INDEX = 1;
    public static final int RELOAD_ITEM_INDEX = 2;
    public static final int LOGOUT_ITEM_INDEX = 3;
    public static final int QUIT_ITEM_INDEX = 4;

    public static final int ITEM_COUNT = 5;

    private static final HostWindow.EventId[] ITEM_EVENT_IDS = {
            HostWindow.EventId.LOGIN,
            HostWindow.EventId.SERVER_ADD,
            HostWindow.EventId.RELOAD,
            HostWindow.EventId.LOGOUT,
            HostWindow.EventId.QUIT
    };

    private static final String[] ITEM_LABELS = {
            "Log in",
            "Add to server",
            "Reload from disk",
            "Log out",
            "Quit"
    };

    private static final int[] ITEM_SHORTCUTS = {
            KeyEvent.VK_I,
            KeyEvent.VK_A,
            KeyEvent.VK_R,
            KeyEvent.VK_O,
            KeyEvent.VK_Q,
    };


    private JMenu menu;

    private EnumMap<HostWindow.EventId, BasicMenuItem> menuItems;

    public BotMenu() {
        menu = new JMenu("Bot");
        menu.setMnemonic(KeyEvent.VK_B);

        menuItems = new EnumMap<>(HostWindow.EventId.class);
        for (int i = 0; i < ITEM_COUNT; i++) {
            BasicMenuItem item = new BasicMenuItem(ITEM_LABELS[i], ITEM_SHORTCUTS[i]);
            menuItems.put(ITEM_EVENT_IDS[i], item);
            menu.add(item.getComponent());
        }
    }

    boolean isValidEventIdForMenu(HostWindow.EventId eventId) {
        return menuItems.containsKey(eventId);
    }

    public void addEventListener(HostWindow.EventId eventId, Runnable listener) {
        menuItems.get(eventId).addActionListener((ActionEvent) -> listener.run());
    }

    public void onLogin() {
        menuItems.get(HostWindow.EventId.LOGIN).setEnabled(false);
        menuItems.get(HostWindow.EventId.SERVER_ADD).setEnabled(true);
        menuItems.get(HostWindow.EventId.RELOAD).setEnabled(true);
        menuItems.get(HostWindow.EventId.LOGOUT).setEnabled(true);
        // quit item is always enabled
    }

    public void onLogout() {
        menuItems.get(HostWindow.EventId.LOGIN).setEnabled(true);
        menuItems.get(HostWindow.EventId.SERVER_ADD).setEnabled(false);
        menuItems.get(HostWindow.EventId.RELOAD).setEnabled(true);
        menuItems.get(HostWindow.EventId.LOGOUT).setEnabled(false);
        // quit item is always enabled
    }

    public void onError() {
        menuItems.get(HostWindow.EventId.LOGIN).setEnabled(false);
        menuItems.get(HostWindow.EventId.SERVER_ADD).setEnabled(false);
        menuItems.get(HostWindow.EventId.RELOAD).setEnabled(true);
        menuItems.get(HostWindow.EventId.LOGOUT).setEnabled(false);
        // quit item is always enabled
    }

    @Override
    Component getComponent() {
        return menu;
    }

}
