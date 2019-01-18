package com.ekkongames.jdacbl.utils;

import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Log {
    
    public enum Level {
        NONE(true), ERROR(true), WARN(true), INFO(false), DEBUG(false), VERBOSE(false), ALL(false);

        private final boolean error;

        Level(boolean error) {
            this.error = error;
        }

        public boolean isFinerThan(Level other) {
            return other.ordinal() < this.ordinal();
        }

        PrintStream getOut() {
            return error ? System.err : System.out;
        }
    }

    private static final String LOG_FORMAT = "[%s] [%s] [%s]: ";

    private static Level logLevel = Level.DEBUG;

    public static void setLevel(Level logLevel) {
        Log.logLevel = logLevel;
    }

    public static void v(String tag, String message) {
        write(Level.VERBOSE, tag, message);
    }

    public static void d(String tag, String message) {
        write(Level.DEBUG, tag, message);
    }

    public static void i(String tag, String message) {
        write(Level.INFO, tag, message);
    }

    public static void w(String tag, String message) {
        write(Level.WARN, tag, message);
    }

    public static void w(String tag, String message, Throwable t) {
        write(Level.WARN, tag, message, t);
    }

    public static void w(String tag, Throwable t) {
        write(Level.WARN, tag, t);
    }

    public static void e(String tag, String message) {
        write(Level.ERROR, tag, message);
    }

    public static void e(String tag, Throwable t) {
        write(Level.ERROR, tag, t);
    }

    public static void e(String tag, String message, Throwable t) {
        write(Level.ERROR, tag, message, t);
    }

    public static void wtf(String tag, String message) {
        write(Level.ERROR, tag, message);
    }

    public static void wtf(String tag, Throwable t) {
        write(Level.ERROR, tag, t);
    }

    public static void wtf(String tag, String message, Throwable t) {
        write(Level.ERROR, tag, message, t);
    }

    //utility methods

    private static void write(Level level, String tag, String message) {
        write(level, tag, message, null);
    }

    private static void write(Level level, String tag, Throwable t) {
        write(level, tag, null, t);
    }

    private static void write(Level level, String tag, String message, Throwable t) {
        if (level.isFinerThan(logLevel)) {
            return;
        }

        PrintStream out = level.getOut();
        String logPrefix = getLogPrefix(level, tag);

        if (message != null) {
            out.println(logPrefix.concat(message));
        }

        if (t != null) {
            out.println(logPrefix.concat(t.toString()));

            StackTraceElement[] stackTrace = t.getStackTrace();
            String logTracePrefix = logPrefix.concat("at ");
            for (StackTraceElement element : stackTrace) {
                out.println(logTracePrefix.concat(element.toString()));
            }
        }
    }

    private static String getLogPrefix(Level level, String tag) {
        return String.format(LOG_FORMAT, getTimestamp(), level, tag);
    }

    private static String getTimestamp() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.MEDIUM);
        ZonedDateTime currentDate = ZonedDateTime.now();
        return dateTimeFormatter.format(currentDate);

    }

}
