package org.appwork.utils.zip;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;

import org.appwork.utils.IO;

public class ZipUtils {

    /**
     * Unzips a data package which has been zipped previously by
     * {@link #zipString(String)}
     * 
     * @param data
     * @return
     * @throws ZipIOException
     * @throws IOException
     * @throws UnsupportedEncodingException
     */
    public static String unzipString(final byte[] data) throws ZipIOException, UnsupportedEncodingException, IOException {
        final ZipIOReader zip = new ZipIOReader(data);
        final ZipEntry entry = zip.getZipFile("dat.dat");
        final String json = IO.readInputStreamToString(zip.getInputStream(entry));
        zip.close();
        return json;
    }

    /**
     * Zips a String to a byte array
     * 
     * @param fileList
     * @return
     * @throws ZipIOException
     * @throws IOException
     */
    public static byte[] zipString(final String fileList) throws ZipIOException, IOException {
        final ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        final ZipIOWriter zip = new ZipIOWriter(oStream);
        zip.addByteArry(fileList.getBytes("UTF-8"), true, null, "dat.dat");
        zip.close();
        return oStream.toByteArray();

    }

}
