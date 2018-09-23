package org.mlayer.batterydrain.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * <p>Simple log writer</p>
 *
 * <p>Writes logs until {@code sizeLimit} reached. Then moves file to file with suffix
 * "_log" and continues.
 * </p>
 *ad
 * <p>It has buffer of entries. When there are more than {@code bufferSize} it flushes to the
 * {@code logFilePath} in a background thread</p>
 */
public class LogWriter {

    // Priority constants
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    private final File file;
    private final File fileOld;
    private final int sizeLimit;
    private final int bufferSize;

    private final ArrayList<Entry> entries = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    private LogWriter(File file, File fileOld, int sizeLimit, int bufferSize) throws IOException {
        this.file = file;
        this.fileOld = fileOld;
        this.sizeLimit = sizeLimit;
        this.bufferSize = bufferSize;
    }

    /**
     * @param logFilePath path to log file
     * @param sizeLimit size in kb
     * @param bufferSize max buffer size before flush
     * @return instance of LogWriter
     * @throws IOException in case of {@code logFilePath} not found or not writable
     */
    public static LogWriter init(String logFilePath, String logFilePathSecondary, int sizeLimit, int bufferSize) throws IOException {
        File primaryFile = new File(logFilePath);
        checkFile(primaryFile);
        File secondaryFile = new File(logFilePathSecondary);
        return new LogWriter(primaryFile, secondaryFile, sizeLimit, bufferSize);
    }

    private static void checkFile(File file) throws IOException {
        if (file == null) {
            throw new FileNotFoundException("Log file is null");
        }
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException(String.format("Can't create log file parent dirs %s", file.getAbsolutePath()));
        }
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException(String.format("Can't create log file %s", file.getAbsolutePath()));
        }
        if (!file.canWrite()) {
            throw new IOException(String.format("Can't write to file %s", file.getAbsolutePath()));
        }
    }

    public void log(int level, String tag, String message) {
        Entry entry = new Entry(level, tag, message, prettify(Thread.currentThread()));
        int count = addEntry(entry);
        if (count > bufferSize) {
            flushAsync();
        }
    }

    /**
     * Adds log entry to the buffer
     * @return size of entries list
     */
    private synchronized int addEntry(Entry entry) {
        entries.add(entry);
        return entries.size();
    }

    /**
     * <p>Flushes the buffer to file synchronously from calling thread.</p>
     * <p>Use carefully, it locks other log entries from being added. The best use case is to call
     * it from uncaught exception handler to be sure your logs are complete.</p>
     */
    public synchronized void flush() {
        if (!entries.isEmpty()) {
            final List<Entry> entriesToWrite = new ArrayList<>(entries);
            entries.clear();
            appendToFile(entriesToWrite);
        }
    }

    public synchronized void flushAsync() {
        if (!entries.isEmpty()) {
            final List<Entry> entriesToWrite = new ArrayList<>(entries);
            entries.clear();
            executor.execute(() -> appendToFile(entriesToWrite));
        }
    }

    private void appendToFile(List<Entry> entriesToWrite) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        PrintWriter out = null;
        try {
            if (!file.exists()) {
                createFile(file);
            }
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            out = new PrintWriter(bw);
            for (Entry entry : entriesToWrite) {
                out.println(entry.prettyPrint());
            }
            out.close();
            if (isBufferOverLimit()) {
                rollFiles();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // todo wtf?
        } finally {
            FileIOUtils.closeQuietly(fw);
            FileIOUtils.closeQuietly(bw);
            FileIOUtils.closeQuietly(out);
        }
    }

    private boolean isBufferOverLimit() {
        return (file.length() / 1024) > sizeLimit;
    }

    private void rollFiles() {
        try {
            if (file.renameTo(fileOld)) {
                createFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
            // todo wtf?
        }
    }

    private boolean createFile(File file) throws IOException {
        if (!file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs()) {
                return file.createNewFile();
            }
        }
        return false;
    }

    public synchronized void clear() {
        entries.clear();
        executor.execute(() -> {
            file.delete();
            fileOld.delete();
        });
    }

    public void setPeriodicFlush() {
        // todo
        throw new UnsupportedOperationException();
    }

    private static String prettify(Thread thread) {
        String name;
        if (thread.getName() != null && thread.getName().length() > 0) {
            name = thread.getName();
        } else {
            name = String.valueOf(thread.getId());
        }
        return String.format("[%s]", name);
    }
}
