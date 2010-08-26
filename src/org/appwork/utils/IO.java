package org.appwork.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.channels.FileChannel;

import org.appwork.utils.os.CrossSystem;

public class IO {
    public static String readFileToString(File file) throws IOException {
        BufferedReader f = null;
        InputStreamReader isr = null;
        FileInputStream fis = null;
        try {
            f = new BufferedReader(isr = new InputStreamReader(fis = new FileInputStream(file), "UTF8"));
            String line;
            final StringBuilder ret = new StringBuilder();
            String sep = System.getProperty("line.separator");
            while ((line = f.readLine()) != null) {
                ret.append(line + sep);
            }
            return ret.toString();
        } finally {
            try {
                f.close();
            } catch (Throwable e) {
            }
            try {
                isr.close();
            } catch (Throwable e) {
            }
            try {
                fis.close();
            } catch (Throwable e) {
            }
        }
    }

    /**
     * @param file
     * @param string
     */
    public static void writeStringToFile(File file, String string) throws IOException {
        if (file == null) { throw new IllegalArgumentException("File is null."); }
        if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
        file.createNewFile();
        if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
        if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }
        FileWriter fw = null;
        Writer output = new BufferedWriter(fw = new FileWriter(file));

        try {
            output.write(string);
        } finally {
            try {
                output.close();
            } catch (Throwable e) {
            }
            try {
                fw.close();
            } catch (Throwable e) {
            }
        }

    }

    /**
     * @param tmp
     * @param encrypt
     * @throws IOException
     */
    public static void writeToFile(File file, byte[] data) throws IOException {
        if (file == null) { throw new IllegalArgumentException("File is null."); }
        if (file.exists()) { throw new IllegalArgumentException("File already exists: " + file); }
        file.createNewFile();
        if (!file.isFile()) { throw new IllegalArgumentException("Is not a file: " + file); }
        if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }

        FileOutputStream out = new FileOutputStream(file);
        try {
            out.write(data);
        } finally {
            try {
                out.close();
            } catch (Throwable e) {
            }
        }
    }

    /**
     * @param ressource
     * @return
     * @throws IOException
     */
    public static byte[] readFile(File ressource) throws IOException {
        FileInputStream in = new FileInputStream(ressource);
        byte[] bytes = null;
        try {
            bytes = new byte[(int) ressource.length()];
            in.read(bytes);
        } finally {
            try {
                in.close();
            } catch (Throwable e) {
            }
        }
        return bytes;
    }

    /**
     * @param file
     * @param file2
     */
    public static void copyFile(File in, File out) throws IOException {
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel inChannel = (fis = new FileInputStream(in)).getChannel();
        FileChannel outChannel = (fos = new FileOutputStream(out)).getChannel();
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
                int maxCount = (64 * 1024 * 1024) - (32 * 1024);
                long size = inChannel.size();
                long position = 0;
                while (position < size) {
                    position += inChannel.transferTo(position, maxCount, outChannel);
                }
            } else {
                inChannel.transferTo(0, inChannel.size(), outChannel);

            }
        } catch (IOException e) {
            throw e;
        } finally {
            try{
                outChannel.close();
            }catch(Throwable e){                
            }
            try{
                fos.close();
            }catch(Throwable e){                
            }
            try{
                inChannel.close();
            }catch(Throwable e){                
            }
            try{
                fis.close();
            }catch(Throwable e){                
            }
        }
    }
}
