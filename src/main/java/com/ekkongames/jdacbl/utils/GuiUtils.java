package com.ekkongames.jdacbl.utils;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.function.Supplier;

/**
 * Created by Dolphish on 2016-10-29.
 */
public class GuiUtils {

    private static final String TAG = "GuiUtils";

    public static void useSystemLookAndFeel() {
        useLookAndFeel(UIManager::getSystemLookAndFeelClassName);
    }

    public static void useCrossPlatformLookAndFeel() {
        useLookAndFeel(UIManager::getCrossPlatformLookAndFeelClassName);
    }

    private static void useLookAndFeel(Supplier<String> nameSupplier) {
        try {
            UIManager.setLookAndFeel(nameSupplier.get());
        } catch (ClassNotFoundException e) {
            Log.w(TAG, "Requested look-and-feel is not available!");
        } catch (InstantiationException e) {
            Log.w(TAG, "Failed to set look-and-feel!");
        } catch (IllegalAccessException e) {
            Log.w(TAG, "Invalid look-and-feel format!");
        } catch (UnsupportedLookAndFeelException e) {
            Log.w(TAG, "Unsupported look-and-feel!");
        }
    }

}
