package org.appwork.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

public class IO {
public static void moveTo(File source,File dest,FileFilter filter) throws IOException{
    ArrayList<File> files = Files.getFiles(filter,source);
//TODO Proper delete
    for(File src:files){
        String rel = Files.getRelativePath(source, src);
        File file = new File(dest,rel);
        if(src.isDirectory()){
            file.mkdirs();
        }else{
            file.getParentFile().mkdirs();
            System.out.println(src+ " -> "+file);
            if(!src.renameTo(file)){
                throw new IOException("Could not move file "+src+" to "+file);
            }
        }
    }
    
    
    
}


    public static void copyFile(final File in, final File out) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        if(Log.L.isLoggable(Level.FINEST))Log.L.finest("Copy " + in+" to "+out);
        try {
            inChannel = (fis = new FileInputStream(in)).getChannel();
            outChannel = (fos = new FileOutputStream(out)).getChannel();
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

    public static String readInputStreamToString(final InputStream fis) throws UnsupportedEncodingException, IOException {
        BufferedReader f = null;
           try {
            f = new BufferedReader(new InputStreamReader(fis, "UTF8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            final String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                if (ret.length() > 0) {
                    ret.append(sep);
                }
                ret.append(line);
            }
            return ret.toString();
        } finally {
            try {
                f.close();
            } catch (final Throwable e) {
            }
          
        }
    }

    /*
     * this function reads a line from a bufferedinputstream up to a maxLength.
     * in case the line is longer than maxLength the rest of the line is read
     * but not returned
     * 
     * this function skips emtpy lines
     */
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
            if(Log.L.isLoggable(Level.FINEST))Log.L.finest("Read " + url+" max size: "+maxSize);
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
            if(Log.L.isLoggable(Level.FINEST))Log.L.finest("Read " + ressourceURL);
            fis = ressourceURL.openStream();
            return IO.readInputStreamToString(fis);
        } finally {
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
        if(Log.L.isLoggable(Level.FINEST))Log.L.finest("Write " + file);
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
        if(Log.L.isLoggable(Level.FINEST))Log.L.finest("Write " + file);
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
