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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

/**
 * Used to join the separated HJSplit and 7z files.
 * 
 * @author botzi
 * 
 */
class MultiOpener implements IArchiveOpenVolumeCallback, ICryptoGetTextPassword {
    private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<String, RandomAccessFile>();
    private String                        password;

    MultiOpener() {
        this.password = "";
    }

    MultiOpener(String password) {
        this.password = password;
    }

    public Object getProperty(PropID propID) throws SevenZipException {
        return null;
    }

    public boolean isStreamOpen(String filename) {
        return openedRandomAccessFileList.containsKey(filename);
    }

    public IInStream getStream(String filename) throws SevenZipException {
        try {
            RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
            if (randomAccessFile != null) {
                randomAccessFile.seek(0);
                return new RandomAccessFileInStream(randomAccessFile);
            }

            randomAccessFile = new RandomAccessFile(filename, "r");
            openedRandomAccessFileList.put(filename, randomAccessFile);

            return new RandomAccessFileInStream(randomAccessFile);
        } catch (FileNotFoundException fileNotFoundException) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes all open files.
     * 
     * @throws IOException
     */
    void close() throws IOException {
        Iterator<Entry<String, RandomAccessFile>> it = openedRandomAccessFileList.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, RandomAccessFile> next = it.next();
            try {
                next.getValue().close();
            } catch (final Throwable e) {
            }
            it.remove();
        }
    }

    public String cryptoGetTextPassword() throws SevenZipException {
        return password;
    }
}