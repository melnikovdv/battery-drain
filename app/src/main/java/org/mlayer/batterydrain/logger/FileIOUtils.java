package org.mlayer.batterydrain.logger;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Copied from apache commons
 * http://www.docjar.com/html/api/org/apache/commons/io/IOUtils.java.html
 * https://commons.apache.org/proper/commons-io/apidocs/src-html/org/apache/commons/io/FileUtils.html
 */
public class FileIOUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 20;

    public static String readFileToString(final File file) throws IOException {
        return readFileToString(file, Charset.forName("UTF-8"));
    }

    public static String readFileToString(final File file, final Charset encoding) throws IOException {
        InputStream in = null;
        try {
            in = openInputStream(file);
            return toString(in, encoding.name());
        } finally {
            closeQuietly(in);
        }
    }

    private static FileInputStream openInputStream(final File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    public static void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static void closeQuietly(Writer writer) {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    public static void closeQuietly(OutputStream stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ignored) {
            // ignore
        }
    }

    private static String toString(InputStream input) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw);
        return sw.toString();
    }

    private static String toString(InputStream input, String encoding)
            throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    private static void copy(InputStream input, Writer output)
            throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    private static void copy(InputStream input, Writer output, String encoding)
            throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    private static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        return copyLarge(input, output, false);
    }

    private static long copyLarge(InputStream input, OutputStream output, boolean interruptable)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            if (interruptable && Thread.currentThread().isInterrupted()) {
                throw new InterruptedIOException();
            }
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void inputStreamToFile(InputStream input, File file) throws IOException {
        inputStreamToFile(input, file, false);
    }

    public static void inputStreamToFile(InputStream input, File file, boolean interruptable) throws IOException {
        OutputStream output = new FileOutputStream(file);
        try {
            copyLarge(input, output, interruptable);
            output.flush();
        } finally {
            closeQuietly(input);
            closeQuietly(output);
        }
    }

    public static String inputStreamToString(InputStream input) throws IOException {
        StringWriter stringWriter = new StringWriter();
        copy(input, stringWriter);
        return stringWriter.toString();
    }

    public static void stringToFile(String string, File file) throws IOException {
        stringToFile(string, file, true);
    }

    public static void stringToFile(String string, File file, boolean appendToEnd) throws IOException {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file, appendToEnd));
            writer.write(string);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {}
            }
        }
    }

    public static void byteArrayToFile(byte[] bytes, File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(bytes);
        closeQuietly(stream);
    }

    public static byte[] byteArrayFromFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        copyLarge(inputStream, outputStream);
        byte[] bytes = outputStream.toByteArray();
        closeQuietly(inputStream);
        closeQuietly(outputStream);
        return bytes;
    }

    public static void copy(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
