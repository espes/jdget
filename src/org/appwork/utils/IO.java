package org.appwork.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
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
        return IO.importFileToString(file, -1);
    }

    public static String importFileToString(final File file, final int maxSize) throws IOException {
        final byte[] bytes = IO.readFile(file, maxSize);
        if (bytes == null) { return null; }
        return new String(bytes, "UTF-8");
    }

    public static byte[] readFile(final File ressource) throws IOException {
        return IO.readFile(ressource, -1);
    }

    public static byte[] readFile(final File ressource, final int maxSize) throws IOException {
        return IO.readURL(ressource.toURI().toURL(), maxSize);
    }

    public static String readFileToString(final File file) throws IOException {
        return IO.readURLToString(file.toURI().toURL());
    }

    /**
     * @param f
     * @return
     * @throws IOException
     */
    public static byte[] readURL(final URL f) throws IOException {
        // TODO Auto-generated method stub
        return IO.readURL(f, -1);
    }

    /**
     * @param url
     * @param maxSize
     * @return
     * @throws IOException
     */
    public static byte[] readURL(final URL url, final int maxSize) throws IOException {

        InputStream input = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;
        try {
            input = url.openStream();
            bis = new BufferedInputStream(input);
            baos = new ByteArrayOutputStream();

            final byte[] b = new byte[32767];
            int len;
            while ((len = bis.read(b)) != -1) {
                if (len > 0) {
                    baos.write(b, 0, len);
                    if (maxSize > 0 && baos.size() > maxSize) { throw new IOException("Max size exeeded!"); }
                }

            }
        } finally {
            try {
                input.close();
            } catch (final Exception e) {
            }
            try {
                bis.close();
            } catch (final Exception e) {
            }
            try {
                baos.close();
            } catch (final Throwable e) {
            }
        }
        return baos.toByteArray();
    }

    /**
     * @param ressourceURL
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String readURLToString(final URL ressourceURL) throws IOException {
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
                output.flush();
            } catch (final Throwable e) {
            }
            try {
                fw.flush();
            } catch (final Throwable e) {
            }
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
