package org.appwork.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;

import org.appwork.utils.os.CrossSystem;

public class IO {

    public static void copyFile(final File in, final File out) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        final FileChannel inChannel = (fis = new FileInputStream(in)).getChannel();
        final FileChannel outChannel = (fos = new FileOutputStream(out)).getChannel();
        try {
            if (CrossSystem.isWindows()) {
                // magic number for Windows, 64Mb - 32Kb)
                // On the Windows plateform, you can't copy a file bigger than
                // 64Mb,
                // an Exception in thread "main" java.io.IOException:
                // Insufficient
                // system resources exist to complete the requested service is
                // thrown.
                //
                // For a discussion about this see :
                // http://forum.java.sun.com/thread.jspa?threadID=439695&messageID=2917510
                final int maxCount = 64 * 1024 * 1024 - 32 * 1024;
                final long size = inChannel.size();
                long position = 0;
                while (position < size) {
                    position += inChannel.transferTo(position, maxCount, outChannel);
                }
            } else {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
        } catch (final IOException e) {
            throw e;
        } finally {
            try {
                outChannel.close();
            } catch (final Throwable e) {
            }
            try {
                fos.close();
            } catch (final Throwable e) {
            }
            try {
                inChannel.close();
            } catch (final Throwable e) {
            }
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
    }

    public static String importFileToString(final File file) throws IOException {
        return importFileToString(file, -1);
    }

    public static String importFileToString(final File file, final int maxSize) throws IOException {
        final byte[] bytes = IO.readFile(file, maxSize);
        if (bytes == null) { return null; }
        return new String(bytes, "UTF-8");
    }

    public static byte[] readFile(final File ressource) throws IOException {
        return readFile(ressource, -1);
    }

    public static byte[] readFile(final File ressource, final int maxSize) throws IOException {
        final FileInputStream in = new FileInputStream(ressource);
        byte[] bytes = null;
        try {
            int length = (int) ressource.length();
            /* avoid reading too big files if not wanted */
            if (maxSize > 0 && length > maxSize) throw new IOException("file " + ressource.getName() + " exceeds maximum length");
            bytes = new byte[length];
            int offset = 0;
            int read = 0;
            while ((offset < length) && ((read = in.read(bytes, offset, length - offset)) >= 0)) {
                offset += read;
            }
            if (offset < length) { throw new IOException("file " + ressource.getName() + " not read completely"); }
        } finally {
            try {
                in.close();
            } catch (final Throwable e) {
            }
        }
        return bytes;
    }

    public static String readFileToString(final File file) throws IOException {
        return IO.readFileToString(file.toURI().toURL());
    }

    /**
     * @param ressourceURL
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String readFileToString(final URL ressourceURL) throws IOException {
        BufferedReader f = null;
        InputStreamReader isr = null;

        InputStream fis = null;
        try {

            f = new BufferedReader(isr = new InputStreamReader(fis = ressourceURL.openStream(), "UTF8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            final String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                ret.append(line).append(sep);
            }
            return ret.toString();
        } finally {
            try {
                f.close();
            } catch (final Throwable e) {
            }
            try {
                isr.close();
            } catch (final Throwable e) {
            }
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
    }

    public static void writeStringToFile(final File file, final String string) throws IOException {
        if (file == null) { throw new IllegalArgumentException("File is null."); }
        if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
        file.createNewFile();
        if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
        if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }
        FileWriter fw = null;
        final Writer output = new BufferedWriter(fw = new FileWriter(file));

        try {
            output.write(string);
        } finally {
            try {
                output.close();
            } catch (final Throwable e) {
            }
            try {
                fw.close();
            } catch (final Throwable e) {
            }
        }

    }

    public static void writeToFile(final File file, final byte[] data) throws IOException {
        if (file == null) { throw new IllegalArgumentException("File is null."); }
        if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
        file.createNewFile();
        if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
        if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }

        final FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(data);
        } finally {
            try {
                out.close();
            } catch (final Throwable e) {
            }
        }
    }

}
