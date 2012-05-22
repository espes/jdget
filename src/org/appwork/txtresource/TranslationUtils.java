package org.appwork.txtresource;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import org.appwork.storage.JSonStorage;
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

    /**
     * @param txt
     * @param class1
     * @return
     */
    public static TranslateData restoreFromString(String txt, Class<TranslateData> class1) {
        if (txt.startsWith("{")) {
            return JSonStorage.restoreFromString(txt, class1);
        } else {
            // key==value
            TranslateData ret = new TranslateData();
            int index = 0;
            int found = 0;
            int found2 = 0;
            int found3 = 0;
            String key;
            String value;
            while (true) {
                found = txt.indexOf("=", index);
                if (found < 0) break;
                key = txt.substring(index, found);
                found2 = txt.indexOf("\r", found + 1);
                found3 = txt.indexOf("\n", found + 1);
                if (found2 < 0 && found3 < 0) break;
                if ((found2 < found3 && found2 >= 0) || found3 < 0) {
                    value = txt.substring(found + 1, found2);
                } else {
                    value = txt.substring(found + 1, found3);
                }
                ret.put(key, value);
                index = Math.max(found2, found3);

            }
            return ret;
        }
    }

    /**
     * @param map
     * @return
     */
    public static String serialize(TranslateData map) {
        // TODO Auto-generated method stub
        // return JSonStorage.serializeToJson(map);

        StringBuilder ret = new StringBuilder();

        for (Entry<String, String> entry : map.entrySet()) {
            ret.append(entry.getKey());
            ret.append("=");
            ret.append(entry.getValue().replace("\r", "\\r").replace("\n", "\\n"));
            ret.append("\r\n");
        }

        return ret.toString();
    }
}
