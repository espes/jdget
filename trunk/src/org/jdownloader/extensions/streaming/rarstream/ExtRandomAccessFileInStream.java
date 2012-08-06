package org.jdownloader.extensions.streaming.rarstream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import org.jdownloader.extensions.extraction.ArchiveFile;

public class ExtRandomAccessFileInStream extends RandomAccessFileInStream {

    private ArchiveFile archiveFile;
    private String      filename;

    public ArchiveFile getArchiveFile() {
        return archiveFile;
    }

    public void setArchiveFile(ArchiveFile archiveFile) {
        this.archiveFile = archiveFile;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private RarStreamProvider owner;

    public ExtRandomAccessFileInStream(ArchiveFile archiveFile, String filename, RarStreamProvider rarOpener) throws FileNotFoundException {
        super(new RandomAccessFile(archiveFile == null ? filename : archiveFile.getFilePath(), "r"));
        this.archiveFile = archiveFile;
        this.filename = filename;
        owner = rarOpener;
    }

    @Override
    public long seek(long l, int i) throws SevenZipException {
        return super.seek(l, i);
    }

    @Override
    public int read(byte[] abyte0) throws SevenZipException { //

        return super.read(abyte0);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

}
