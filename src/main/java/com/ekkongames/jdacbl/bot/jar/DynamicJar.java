package com.ekkongames.jdacbl.bot.jar;

import com.ekkongames.jdacbl.utils.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author Julian Dominguez-Schatz <jfdoming at ekkon.dx.am>
 */
public class DynamicJar<R, T extends Supplier<R>> {

    private final File sourceJar;
    private URLClassLoader loader;
    private final Class<? extends T> clazz;
    private final T entryPoint;

    private DynamicJar(File sourceJar, URLClassLoader loader, Class<? extends T> clazz, T entryPoint) {
        this.sourceJar = sourceJar;
        this.loader = loader;
        this.clazz = clazz;
        this.entryPoint = entryPoint;
    }

    public String getAbsolutePath() {
        return sourceJar.getAbsolutePath();
    }

    public String getEntryPointName() {
        return clazz.getName();
    }

    public R run() {
        if (loader == null) {
            throw new IllegalStateException("Cannot run an unloaded JAR!");
        }
        return entryPoint.get();
    }

    public void unload() throws IOException {
        loader.close();
        loader = null;
    }

    public static class Loader<R, T extends Supplier<R>> {
        private final Class<T> clazz;
        private final File sourceJar;

        private Loader(Class<T> clazz, File sourceJar) {
            this.clazz = clazz;
            this.sourceJar = sourceJar;
        }

        public String getAbsolutePath() {
            return sourceJar.getAbsolutePath();
        }

        public DynamicJar<R, T> load()
                throws IOException,
                InstantiationException,
                IllegalAccessException,
                ClassNotFoundException,
                NoSuchMethodException,
                InvocationTargetException {
            JarFile jar = new JarFile(sourceJar);

            // Locate the class to load on startup.
            Manifest jarManifest = jar.getManifest();
            String className = jarManifest.getMainAttributes().getValue("Entry-Point");
            if (className == null) {
                throw new IllegalArgumentException("Missing Entry-Point in JAR manifest!");
            }

            try {
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{sourceJar.toURI().toURL()},
                        this.getClass().getClassLoader()
                );
                Class<? extends T> subclass = Class.forName(
                        className,
                        true,
                        loader
                ).asSubclass(clazz);
                return new DynamicJar<R, T>(
                        sourceJar,
                        loader,
                        subclass,
                        subclass.getConstructor().newInstance()
                );
            } finally {
                jar.close();
            }
        }
    }

    public static class Resolver<R, T extends Supplier<R>> {

        private static final String TAG = "JarPathResolver";

        private final ArrayList<Supplier<File>> suppliers;
        private final Class<T> clazz;

        public Resolver(Class<T> clazz) {
            this.clazz = clazz;
            suppliers = new ArrayList<>();
        }

        private boolean isUnusableFile(File file) {
            return !(file.exists() && file.isFile());
        }

        public Resolver<R, T> checkArgs(String[] args) {
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

        public Resolver<R, T> checkConfig(File configFile) {
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

        public Loader<R, T> resolve() {
            for (Supplier<File> supplier : suppliers) {
                File result = supplier.get();
                if (result != null) {
                    return new Loader<R, T>(clazz, result);
                }
            }
            Log.w(TAG, "Failed to locate bot command archive. Your bot may not work correctly.");
            return null;
        }
    }
}
