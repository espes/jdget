package org.appwork.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.appwork.utils.Files.Handler;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.appwork.utils.os.CrossSystem;

public class IO {
    private static IOErrorHandler ERROR_HANDLER = null;

    public static void copyFile(final File in, final File out) throws IOException {
        try {
            FileInputStream fis = null;
            FileOutputStream fos = null;
            FileChannel inChannel = null;
            FileChannel outChannel = null;
            try {
                if (out.exists()) { throw new IOException("Cannot overwrite " + out); }
                if (!in.exists()) { throw new FileNotFoundException(in.getAbsolutePath()); }
                inChannel = (fis = new FileInputStream(in)).getChannel();
                outChannel = (fos = new FileOutputStream(out)).getChannel();
                if (CrossSystem.isWindows()) {
                    // magic number for Windows, 64Mb - 32Kb)
                    // On the Windows plateform, you can't copy a file bigger
                    // than
                    // 64Mb,
                    // an Exception in thread "main" java.io.IOException:
                    // Insufficient
                    // system resources exist to complete the requested service
                    // is
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
        } catch (final IOException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onCopyException(e, in, out);
            }

            throw e;
        } catch (final RuntimeException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onCopyException(e, in, out);
            }
            throw e;
        } catch (final Error e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onCopyException(e, in, out);
            }
            throw e;
        }
    }

    /**
     * @param dist
     * @param dist2
     * @throws IOException
     */
    public static void copyFolderRecursive(final File src, final File dest) throws IOException {
        Files.walkThroughStructure(new Handler<IOException>() {

            @Override
            public void onFile(final File f) throws IOException {
                final String path = Files.getRelativePath(src, f);
                if (path == null) { throw new IOException("No rel Path " + src + "-" + f); }
                if (f.isDirectory()) {
                    new File(dest, path).mkdirs();
                } else {
                    IO.copyFile(f, new File(dest, path));
                }

            }
        }, src);

    }

    public static IOErrorHandler getErrorHandler() {
        return IO.ERROR_HANDLER;
    }

    public static String importFileToString(final File file) throws IOException {
        return IO.importFileToString(file, -1);
    }

    public static String importFileToString(final File file, final int maxSize) throws IOException {
        final byte[] bytes = IO.readFile(file, maxSize);
        if (bytes == null) { return null; }
        return new String(bytes, "UTF-8");
    }

    public static void moveTo(final File source, final File dest, final FileFilter filter) throws IOException {
        final java.util.List<File> files = Files.getFiles(filter, source);
        // TODO Proper delete
        for (final File src : files) {
            final String rel = Files.getRelativePath(source, src);
            final File file = new File(dest, rel);
            if (src.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                System.out.println(src + " -> " + file);
                if (!src.renameTo(file)) { throw new IOException("Could not move file " + src + " to " + file); }
            }
        }

    }

    public static byte[] readFile(final File ressource) throws IOException {
        return IO.readFile(ressource, -1);
    }

    /*
     * this function reads a line from a bufferedinputstream up to a maxLength.
     * in case the line is longer than maxLength the rest of the line is read
     * but not returned
     * 
     * this function skips emtpy lines
     */

    public static byte[] readFile(final File ressource, final int maxSize) throws IOException {
        return IO.readURL(ressource.toURI().toURL(), maxSize);
    }

    public static String readFileToString(final File file) throws IOException {
        return IO.readURLToString(file.toURI().toURL());
    }

    public static String readInputStreamToString(final InputStream fis) throws UnsupportedEncodingException, IOException {
        BufferedReader f = null;
        try {
            f = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            final String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                if (ret.length() > 0) {
                    ret.append(sep);
                } else if (line.startsWith("\uFEFF")) {
                    /*
                     * Workaround for this bug:
                     * http://bugs.sun.com/view_bug.do?bug_id=4508058
                     * http://bugs.sun.com/view_bug.do?bug_id=6378911
                     */

                    line = line.substring(1);
                }
                ret.append(line);
            }

            return ret.toString();
        } catch (final IOException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, fis);
            }

            throw e;
        } catch (final RuntimeException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, fis);
            }
            throw e;
        } catch (final Error e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, fis);
            }
            throw e;
        } finally {
            try {
                f.close();
            } catch (final Throwable e) {
            }
        }
    }

    public static String readLine(final BufferedInputStream is, final byte[] array) throws IOException {
        Arrays.fill(array, 0, array.length, (byte) 0);
        int read = 0;
        int total = 0;
        int totalString = 0;
        boolean nextLineReached = false;
        while (true) {
            read = is.read();
            if (read == -1 && total == 0) {
                /* EOS */
                return null;
            }
            if (read == 13 || read == 10) {
                /* line break found, mark in inputstream */
                nextLineReached = true;
                is.mark(1024);
            } else if (nextLineReached) {
                /* new text found */
                is.reset();
                total--;
                break;
            } else if (total < array.length) {
                /* only write to outputstream if maxlength not reached yet */
                array[totalString++] = (byte) read;
            }
            total++;
        }
        return new String(array, 0, totalString, "UTF-8");
    }

    public static byte[] readStream(final int maxSize, final InputStream input) throws IOException {
        return IO.readStream(maxSize, input, new ByteArrayOutputStream());
    }

    public static byte[] readStream(final int maxSize, final InputStream input, final ByteArrayOutputStream baos) throws IOException {
        ReusableByteArrayOutputStream os = null;
        try {
            os = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(32767);
            int len;
            while ((len = input.read(os.getInternalBuffer())) != -1) {
                if (len > 0) {
                    baos.write(os.getInternalBuffer(), 0, len);
                    if (maxSize > 0 && baos.size() > maxSize) { throw new IOException("Max size exeeded!"); }
                }
            }

        } catch (final IOException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, input);
            }

            throw e;
        } catch (final RuntimeException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, input);
            }
            throw e;
        } catch (final Error e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onReadStreamException(e, input);
            }
            throw e;
        } finally {
            try {
                ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(os);
            } catch (final Throwable e) {
            }
            try {
                input.close();
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
        try {
            input = url.openStream();
            return IO.readStream(maxSize, input);
        } finally {
            try {
                input.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param ressourceURL
     * @return
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String readURLToString(final URL ressourceURL) throws IOException {

        InputStream fis = null;
        try {
            fis = ressourceURL.openStream();
            return IO.readInputStreamToString(fis);
        } finally {
            try {
                fis.close();
            } catch (final Throwable e) {
            }
        }
    }

    /**
     * @param file
     * @param bytes
     * @throws IOException
     */
    public static void secureWrite(final File file, final byte[] bytes) throws IOException {
        final File bac = new File(file.getAbsolutePath() + ".bac");
        bac.delete();
        file.getParentFile().mkdirs();
        try {
            IO.writeToFile(bac, bytes);
            file.delete();
            if (!bac.renameTo(file)) { throw new IOException("COuld not rename " + bac + " to " + file); }
        } finally {
            bac.delete();
        }

    }

    /**
     * Want to get informed in case of any io problems, set this handler
     * 
     * @param handler
     */
    public static void setErrorHandler(final IOErrorHandler handler) {

        IO.ERROR_HANDLER = handler;
    }

    public static void writeStringToFile(final File file, final String string) throws IOException {
        try {
            if (file == null) { throw new IllegalArgumentException("File is null."); }
            if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
            file.createNewFile();
            if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
            if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }
            final FileWriter fw = null;

            final Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

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
        } catch (final IOException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, string.getBytes());
            }

            throw e;
        } catch (final RuntimeException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, string.getBytes());
            }
            throw e;
        } catch (final Error e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, string.getBytes());
            }
            throw e;
        }

    }

    public static void writeToFile(final File file, final byte[] data) throws IOException {
        try {
            if (file == null) { throw new IllegalArgumentException("File is null."); }
            if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
            file.createNewFile();
            if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
            if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                out.write(data);
            } finally {
                try {
                    out.close();
                } catch (final Throwable e) {
                }
            }
        } catch (final IOException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, data);
            }

            throw e;
        } catch (final RuntimeException e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, data);
            }
            throw e;
        } catch (final Error e) {
            if (IO.ERROR_HANDLER != null) {
                IO.ERROR_HANDLER.onWriteException(e, file, data);
            }
            throw e;
        }
    }
}
