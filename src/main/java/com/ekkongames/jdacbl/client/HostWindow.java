package com.ekkongames.jdacbl.client;

import com.ekkongames.jdacbl.bot.Bot;
import com.ekkongames.jdacbl.utils.GuiUtils;
import com.ekkongames.jdacbl.utils.MultiOutputStream;
import com.ekkongames.jdacbl.utils.TextAreaOutputStream;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class HostWindow implements ActionListener {

    public enum EventId {
        LOGIN, SERVER_ADD, RELOAD, LOGOUT, QUIT
    }

    private static final String TAG = "Host Window";

    private JFrame window;
    private JLabel statusLabel;
    private JMenuBar menuBar;
    private BotMenu botMenu;
    private OutputConsole outputConsole;
    JTextField commandArea;

    private List<Consumer<String>> inputListeners;

    public HostWindow() {
        configureEnvironment();
        initializeUIComponents();
    }

    private void configureEnvironment() {
        GuiUtils.useSystemLookAndFeel();
    }

    private void initializeUIComponents() {
        window = new JFrame("Discord Bot - ");

        menuBar = new JMenuBar();

        botMenu = new BotMenu();
        menuBar.add(botMenu.getComponent());

        window.setJMenuBar(menuBar);

        // prepare the console
        outputConsole = new OutputConsole();
        window.add(outputConsole.getComponent(), BorderLayout.CENTER);

        // redirect System.out.and System.err to the console
        TextAreaOutputStream textAreaOutputStream = new TextAreaOutputStream((JTextArea) ((JScrollPane) outputConsole.getComponent()).getViewport().getView());
        PrintStream outStream = new PrintStream(new MultiOutputStream(textAreaOutputStream, System.out));
        System.setOut(outStream);

//        PrintStream errStream = new PrintStream(new MultiOutputStream(textAreaOutputStream, System.err));
//        System.setErr(errStream);

        // prepare the bottom panel
        // I initialize it with a whitespace String since this allows it to be sized properly for the pack() call.
        JPanel bottomPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ");
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        commandArea = new JTextField();
        commandArea.setEnabled(false);
        inputListeners = new ArrayList<>();
        commandArea.addActionListener(this);
        bottomPanel.add(commandArea, BorderLayout.CENTER);

        window.add(bottomPanel, BorderLayout.SOUTH);
    }

    public void addWindowListener(WindowListener listener) {
        window.addWindowListener(listener);
    }

    public void addInputListener(Consumer<String> listener) {
        inputListeners.add(listener);
    }

    public void removeInputListener(Consumer<String> listener) {
        inputListeners.remove(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = commandArea.getText();
        commandArea.setText("");
        for (Consumer<String> listener : inputListeners) {
            listener.accept(text);
        }
    }

    public BotMenu getBotMenu() {
        return botMenu;
    }

    public void setTitle(String title) {
        window.setTitle("Discord Bot - " + title);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void open() {
        //window.pack();
        window.setSize(600, 400);
        window.setLocationRelativeTo(null);
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setVisible(true);
    }

    public void close() {
        window.dispose();
    }

    public void onState(Bot.State state) {
        setStatus(state.getStatusText());

        switch (state) {
            case IDLE:
                botMenu.onLogout();
                commandArea.setEnabled(false);
                break;
            case CONNECTING:
                botMenu.onIntermediateState();
                commandArea.setEnabled(false);
                break;
            case CONNECTED:
                botMenu.onLogin();
                commandArea.setEnabled(true);
                break;
            case LOAD_FAILED:
                botMenu.onError();
                commandArea.setEnabled(false);
                break;
            default:
                // no change in UI
                break;
        }
    }

    public void addEventListener(HostWindow.EventId eventId, Runnable listener) {
        if (botMenu.isValidEventIdForMenu(eventId)) {
            botMenu.addEventListener(eventId, listener);
        }
    }

    public void clearOutput() {
        outputConsole.clear();
    }
}
