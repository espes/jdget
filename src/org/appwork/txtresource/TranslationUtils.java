package org.appwork.txtresource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.appwork.utils.Application;
import org.appwork.utils.IO;

public class TranslationUtils {
    /**
     * This function reads all given TranslateInterfaces and writes lng files
     * for all their Defaults Languages.
     * 
     * @param addComments
     * @param classes
     * @throws URISyntaxException
     * @throws IOException
     */
    public static void createFiles(final boolean addComments, final Class<? extends TranslateInterface>... classes) throws URISyntaxException, IOException {

        for (final Class<? extends TranslateInterface> class1 : classes) {
            final String rel = class1.getName().replace(".", "/") + ".class";
            final String file = new File(Application.getRessourceURL(rel).toURI()).getParentFile().getAbsolutePath().replace("\\bin\\", "\\src\\");

            for (final String lng : class1.getAnnotation(Defaults.class).lngs()) {
                final File f = new File(file + "/" + class1.getSimpleName() + "." + lng + ".lng");
                final String txt = TranslationFactory.create(class1)._getHandler().createFile(lng, addComments);
                f.delete();
                IO.writeStringToFile(f, txt);
                System.out.println("Wrote " + f);

            }
        }
    }
}
