//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.jdownloader.extensions.extraction.multi;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.jdownloader.extensions.extraction.CPUPriority;
import org.jdownloader.extensions.extraction.ExtractionConfig;
import org.jdownloader.extensions.extraction.ExtractionController;
import org.jdownloader.extensions.extraction.ExtractionControllerConstants;

/**
 * Gets the decrypted bytes and writes it into the file.
 * 
 * @author botzi
 * 
 */
class MultiCallback implements ISequentialOutStream {
    private FileOutputStream              fos    = null;
    private ExtractionController          con;
    private CPUPriority                   priority;
    private ReusableByteArrayOutputStream buffer = null;
    private BufferedOutputStream          bos    = null;

    MultiCallback(File file, ExtractionController con, ExtractionConfig config, boolean shouldCrc) throws FileNotFoundException {
        this.con = con;
        priority = config.getCPUPriority();
        if (priority == null || CPUPriority.HIGH.equals(priority)) {
            this.priority = null;
        }
        int maxbuffersize = config.getBufferSize() * 1024;
        buffer = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream(Math.max(maxbuffersize, 10240), false);
        fos = new FileOutputStream(file, true);
        bos = new BufferedOutputStream(fos, 1) {
            {
                this.buf = buffer.getInternalBuffer();
            }
        };
    }

    public int write(byte[] data) throws SevenZipException {
        try {
            bos.write(data);
            if (priority != null && !CPUPriority.HIGH.equals(priority)) {
                try {
                    Thread.sleep(priority.getTime());
                } catch (InterruptedException e) {
                    throw new MultiSevenZipException(e, ExtractionControllerConstants.EXIT_CODE_FATAL_ERROR);
                }
            }
        } catch (IOException e) {
            throw new MultiSevenZipException(e, ExtractionControllerConstants.EXIT_CODE_WRITE_ERROR);
        }
        return data.length;
    }

    /**
     * Closes the unpacking.
     * 
     * @throws IOException
     */
    void close() throws IOException {
        try {
            bos.flush();
        } catch (Throwable e) {
        }
        try {
            bos.close();
        } catch (Throwable e) {
        }
        try {
            ReusableByteArrayOutputStreamPool.reuseReusableByteArrayOutputStream(buffer);
        } catch (Throwable e) {
        } finally {
            buffer = null;
        }
        try {
            fos.flush();
        } catch (Throwable e) {
        }
        try {
            fos.close();
        } catch (Throwable e) {
        }

    }
}