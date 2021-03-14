package com.ekkongames.jdacbl.client;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class OutputConsole extends ComponentProvider {
    private final JScrollPane scrollPane;
    private final JTextArea console;

    public OutputConsole() {
        console = new JTextArea();

        console.setEditable(false);
        console.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        console.setForeground(Color.GREEN);
        console.setBackground(Color.BLACK);
        console.setSelectedTextColor(Color.BLACK);
        console.setSelectionColor(Color.GREEN);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(console);
    }

    @Override
    Component getComponent() {
        return scrollPane;
    }

    void clear() {
        console.setText("");
    }
}
