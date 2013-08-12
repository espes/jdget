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

package org.jdownloader.extensions.extraction;

import java.awt.Color;
import java.io.File;

import jd.controlling.TaskQueue;
import jd.gui.UserIO;

import org.appwork.utils.event.queue.QueueAction;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.controlling.FileCreationEvent;
import org.jdownloader.controlling.FileCreationManager;
import org.jdownloader.extensions.extraction.translate.T;

/**
 * Updates the Extractionprogess for archives in the downloadlist
 * 
 * @author botzi
 * 
 */
public class ExtractionListenerList implements ExtractionListener {
    private ExtractionExtension ex;

    ExtractionListenerList() {
        this.ex = ExtractionExtension.getIntance();
    }

    public void onExtractionEvent(ExtractionEvent event) {
        final ExtractionController controller = event.getCaller();
        final LogSource logger = controller.getLogger();
        switch (event.getType()) {
        case QUEUED:

            controller.getArchiv().setStatus(ExtractionStatus.IDLE);
            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_queued());
            break;
        case EXTRACTION_FAILED:
            try {
                logger.warning("Extraction failed");
                ArchiveFile af = null;
                if (controller.getException() != null) {
                    if (controller.getException() instanceof ExtractionException) {
                        af = ((ExtractionException) controller.getException()).getLatestAccessedArchiveFile();
                    }
                }
                for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                    if (link == null) continue;
                    if (af == link) {
                        link.setStatus(ExtractionStatus.ERROR_CRC);
                        link.setMessage(T._.failed(controller.getException().getMessage()));
                    } else if (controller.getException() != null) {
                        link.setStatus(ExtractionStatus.ERROR);
                        link.setMessage(T._.failed(controller.getException().getMessage()));
                    } else {
                        link.setStatus(ExtractionStatus.ERROR);
                        link.setMessage(T._.failed_no_details());
                    }
                }
                for (File f : controller.getArchiv().getExtractedFiles()) {
                    if (f.exists()) {
                        if (!FileCreationManager.getInstance().delete(f)) {
                            logger.warning("Could not delete file " + f.getAbsolutePath());
                        } else {
                            logger.warning("Deleted file " + f.getAbsolutePath());
                        }
                    }
                }
            } finally {
                controller.getArchiv().setStatus(ExtractionStatus.ERROR);
                controller.getArchiv().setActive(false);
                ex.onFinished(controller);
            }
            break;
        case PASSWORD_NEEDED_TO_CONTINUE:
            // ??
            // //
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();

            if (ex.getSettings().isAskForUnknownPasswordsEnabled() || controller.isAskForUnknownPassword()) {
                String pass = UserIO.getInstance().requestInputDialog(0, T._.plugins_optional_extraction_askForPassword(controller.getArchiv().getFirstArchiveFile().getName()), "");
                if (pass == null || pass.length() == 0) {

                    controller.getArchiv().getFirstArchiveFile().setStatus(ExtractionStatus.ERROR);
                    controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_extractfailedpass());
                    ex.onFinished(controller);
                    break;
                }
                controller.getArchiv().setFinalPassword(pass);
            }
            break;
        case START_CRACK_PASSWORD:
            try {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(((10000 * controller.getCrackProgress()) / controller.getPasswordListSize()) / 100.00));
            } catch (Throwable e) {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(0.00d));

            } // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case START:
            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_openingarchive());
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            controller.getArchiv().setStatus(ExtractionStatus.RUNNING);
            break;
        case OPEN_ARCHIVE_SUCCESS:

            break;
        case PASSWORD_FOUND:
            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_passfound());
            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            controller.getArchiv().getFirstArchiveFile().setProgress(0, 0, null);
            break;
        case PASSWORT_CRACKING:
            try {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(((10000 * controller.getCrackProgress()) / controller.getPasswordListSize()) / 100.00));
            } catch (Throwable e) {
                controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_crackingpass_progress(0.00d));

            }
            controller.getArchiv().getFirstArchiveFile().setProgress(controller.getCrackProgress(), controller.getPasswordListSize(), Color.GREEN.darker());

            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case EXTRACTING:

            controller.getArchiv().getFirstArchiveFile().setMessage(T._.plugins_optional_extraction_status_extracting2());

            controller.getArchiv().getFirstArchiveFile().setProgress((long) (controller.getProgress() * 100), 10000, Color.YELLOW.darker());

            // controller.getArchiv().getFirstArchiveFile().requestGuiUpdate();
            break;
        case EXTRACTION_FAILED_CRC:
            try {
                logger.warning("Extraction failed(CRC)");
                if (controller.getArchiv().getCrcError().size() != 0) {
                    for (ArchiveFile link : controller.getArchiv().getCrcError()) {
                        if (link == null) continue;
                        link.setStatus(ExtractionStatus.ERROR_CRC);
                    }
                } else {
                    for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                        if (link == null) continue;
                        link.setMessage(T._.plugins_optional_extraction_error_extrfailedcrc());
                    }
                }
                for (File f : controller.getArchiv().getExtractedFiles()) {
                    if (f.exists()) {
                        if (!FileCreationManager.getInstance().delete(f)) {
                            logger.warning("Could not delete file " + f.getAbsolutePath());
                        } else {
                            logger.warning("Deleted file " + f.getAbsolutePath());
                        }
                    }
                }
            } finally {
                controller.getArchiv().setStatus(ExtractionStatus.ERROR_CRC);
                controller.getArchiv().setActive(false);
                ex.onFinished(controller);
            }
            break;
        case FINISHED:
            try {
                TaskQueue.getQueue().add(new QueueAction<Void, RuntimeException>() {

                    @Override
                    protected Void run() throws RuntimeException {
                        FileCreationManager.getInstance().getEventSender().fireEvent(new FileCreationEvent(controller, FileCreationEvent.Type.NEW_FILES, controller.getArchiv().getExtractedFiles().toArray(new File[controller.getArchiv().getExtractedFiles().size()])));
                        if (ex.getSettings().isDeleteInfoFilesAfterExtraction()) {
                            File fileOutput = new File(controller.getArchiv().getFirstArchiveFile().getFilePath());
                            File infoFiles = new File(fileOutput.getParentFile(), fileOutput.getName().replaceFirst("(?i)(\\.pa?r?t?\\.?[0-9]+\\.rar|\\.rar)$", "") + ".info");
                            if (infoFiles.exists() && infoFiles.delete()) {
                                logger.info(infoFiles.getName() + " removed");
                            }
                        }
                        for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                            if (link == null) continue;
                            link.setStatus(ExtractionStatus.SUCCESSFUL);
                        }
                        return null;
                    }
                });
                controller.getArchiv().setStatus(ExtractionStatus.SUCCESSFUL);
            } finally {
                controller.getArchiv().setActive(false);
                ex.onFinished(controller);
            }
            break;
        case NOT_ENOUGH_SPACE:
            controller.getArchiv().setStatus(ExtractionStatus.ERROR_NOT_ENOUGH_SPACE);
            ex.onFinished(controller);
            break;
        case CLEANUP:
            try {
                logger.warning("Cleanup");
                ArchiveFile af = null;
                if (controller.getException() != null) {
                    if (controller.getException() instanceof ExtractionException) {
                        af = ((ExtractionException) controller.getException()).getLatestAccessedArchiveFile();
                        af.deleteFile();
                    }
                }
                if (controller.gotKilled()) {
                    controller.getArchiv().getFirstArchiveFile().setMessage(null);
                    for (File f : controller.getArchiv().getExtractedFiles()) {
                        if (f.exists()) {
                            if (!FileCreationManager.getInstance().delete(f)) {
                                logger.warning("Could not delete file " + f.getAbsolutePath());
                            } else {
                                logger.warning("Deleted file " + f.getAbsolutePath());
                            }
                        }
                    }
                }
            } finally {
                controller.getArchiv().setActive(false);
                controller.getArchiv().getFirstArchiveFile().setProgress(0, 0, null);
                ex.removeArchive(controller.getArchiv());
                if (controller.isSuccessful() && !controller.getArchiv().getGotInterrupted()) {

                    for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                        if (link == null) continue;
                        link.onCleanedUp(controller);
                    }

                    controller.removeArchiveFiles();

                }
            }
            break;
        case FILE_NOT_FOUND:
            try {
                logger.warning("FileNotFound");
                if (controller.getArchiv().getCrcError().size() != 0) {
                    controller.getArchiv().setStatus(ExtractionStatus.ERRROR_FILE_NOT_FOUND);
                } else {
                    for (ArchiveFile link : controller.getArchiv().getArchiveFiles()) {
                        if (link == null) continue;
                        link.setMessage(T._.plugins_optional_extraction_filenotfound());
                    }

                    controller.getArchiv().setStatus(ExtractionStatus.ERROR_CRC);
                }
                for (File f : controller.getArchiv().getExtractedFiles()) {
                    if (f.exists()) {
                        if (!FileCreationManager.getInstance().delete(f)) {
                            logger.warning("Could not delete file " + f.getAbsolutePath());
                        } else {
                            logger.warning("Deleted file " + f.getAbsolutePath());
                        }
                    }
                }
            } finally {
                controller.getArchiv().setActive(false);
                ex.onFinished(controller);
            }
            break;
        }
    }
}