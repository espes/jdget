package org.jdownloader.extensions.extraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jdownloader.controlling.FileCreationManager;

public class ExtractLogFileWriter {

    private File           file;
    private BufferedWriter output;
    private String         id;

    public ExtractLogFileWriter(String name, String filePath, String id) {
        this.id = id;
        File f = Archive.getArchiveLogFileById(id);
        file = new File(new File(f.getParentFile(), "open"), f.getName());
        FileCreationManager.getInstance().mkdir(file.getParentFile());
        try {
            file.createNewFile();

            if (!file.canWrite()) { throw new IllegalArgumentException("Cannot write to file: " + file); }

            output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        write("Archive Name: " + name);
        write("Archive Path: " + filePath);
    }

    public void delete() {
        close();
        FileCreationManager.getInstance().delete(file, null);
    }

    public void write(String string) {
        if (output != null) {
            try {
                output.write(System.currentTimeMillis() + " - " + string);

                output.write("\r\n");
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void close() {
        try {
            output.close();
            File newFile;
            newFile = Archive.getArchiveLogFileById(id);
            FileCreationManager.getInstance().delete(newFile, null);
            file.renameTo(newFile);
            file = newFile;
        } catch (Exception e) {
        }
    }

}
