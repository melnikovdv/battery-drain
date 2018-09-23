package org.mlayer.batterydrain.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Entry {

    private static SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

    public final int level;
    public final String thread;
    public final long time;
    public final String tag;
    public final String message;

    public Entry(int level, String tag, String message, String thread) {
        this.level = level;
        this.tag = tag;
        this.message = message;
        this.thread = thread;
        time = System.currentTimeMillis();
    }

    public String prettyPrint() {
        return String.format("%s %s %s %s", TIME_FORMAT.format(new Date(time)), displayLevel(), thread,
                (tag == null ? "" : tag + " ") + (message != null ? message.replaceAll("\\n", "\n                      ") : ""));
    }

    public String displayLevel() {
        switch (level) {
            case LogWriter.VERBOSE:
                return "V";
            case LogWriter.DEBUG:
                return "D";
            case LogWriter.INFO:
                return "I";
            case LogWriter.WARN:
                return "W";
            case LogWriter.ERROR:
                return "E";
            case LogWriter.ASSERT:
                return "A";
            default:
                return "?";
        }
    }
}
