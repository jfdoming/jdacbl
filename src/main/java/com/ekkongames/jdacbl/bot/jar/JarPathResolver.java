package com.ekkongames.jdacbl.bot.jar;

import com.ekkongames.jdacbl.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class JarPathResolver {

    private static final String TAG = "JarPathResolver";

    private ArrayList<Supplier<File>> suppliers;

    public JarPathResolver() {
        suppliers = new ArrayList<>();
    }

    private boolean isUnusableFile(File file) {
        return !(file.exists() && file.isFile());
    }

    public JarPathResolver checkArgs(String[] args) {
        suppliers.add(() -> {
            if (args.length == 0) {
                return null;
            }

            File result = new File(args[0]);
            if (isUnusableFile(result)) {
                return null;
            }
            return result;
        });
        return this;
    }

    public JarPathResolver checkConfig(File configFile) {
        suppliers.add(() -> {
            if (isUnusableFile(configFile)) {
                return null;
            }

            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(configFile));
            } catch (IOException e) {
                // should never occur, unless the File implementation is bugged
                Log.wtf(TAG, "Your JDK is probably corrupt!!!");
                return null;
            }

            File result = new File(properties.getProperty("jar"));
            if (isUnusableFile(result)) {
                return null;
            }
            return result;
        });
        return this;
    }

    public File resolve() {
        for (Supplier<File> supplier : suppliers) {
            File result = supplier.get();
            if (result != null) {
                return result;
            }
        }
        Log.w(TAG, "Failed to locate bot command archive. Your bot may not work correctly.");
        return null;
    }
}
