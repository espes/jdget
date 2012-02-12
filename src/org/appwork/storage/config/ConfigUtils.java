package org.appwork.storage.config;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class ConfigUtils {

    /**
     * @param class1
     */
    public static void printStaticMappings(final Class<? extends ConfigInterface> configInterface) {
        // TODO Auto-generated method stub
        StringBuilder strBuild = new StringBuilder();
        System.err.println(configInterface);
        System.err.flush();
        strBuild.append("\r\n");
        strBuild.append("//Static Mappings for " + configInterface);
        strBuild.append("\r\n");
        strBuild.append("public static final " + configInterface.getSimpleName() + "                 CFG                               = JsonConfig.create(" + configInterface.getSimpleName() + ".class);");
        strBuild.append("\r\n");
        strBuild.append("public static final StorageHandler<" + configInterface.getSimpleName() + ">                 SH                               = (StorageHandler<" + configInterface.getSimpleName() + ">) CFG.getStorageHandler();");
        strBuild.append("\r\n");
        strBuild.append("//let's do this mapping here. If we map all methods to static handlers, access is faster, and we get an error on init if mappings are wrong.");

        // public static final BooleanKeyHandler LINK_FILTER_ENABLED =
        // SH.getKeyHandler("LinkFilterEnabled", BooleanKeyHandler.class);
        HashSet<KeyHandler<?>> unique = new HashSet<KeyHandler<?>>();
        HashMap<Method, KeyHandler<?>> map = JsonConfig.create(configInterface).getStorageHandler().getMap();
        for (KeyHandler<?> kh : map.values()) {
            if (!unique.add(kh)) continue;
            strBuild.append("\r\n");
            strBuild.append("// " + kh);
            // String key = kh.getKey();
            String methodname = kh.getGetter().getMethod().getName().startsWith("is")?kh.getGetter().getMethod().getName().substring(2):kh.getGetter().getMethod().getName().substring(3);
            StringBuilder sb = new StringBuilder();
            char c, lastc;
            lastc = ' ';
            for (int i = 0; i < methodname.length(); i++) {
                c = methodname.charAt(i);
                if (sb.length() > 0) {

                    if (Character.isUpperCase(c) && Character.isLowerCase(lastc)) {
                        sb.append('_');

                    }
                }

                sb.append(Character.toUpperCase(c));
                lastc = c;
            }
            /**
             * 
             */

            if (kh.getAnnotation(Description.class) != null) {
                strBuild.append("\r\n");
                strBuild.append("/**");
                strBuild.append("\r\n");
                strBuild.append(" * " + kh.getAnnotation(Description.class).value());
                strBuild.append("\r\n");
                strBuild.append("**/");
            }
            strBuild.append("\r\n");
            strBuild.append("public static final " + kh.getClass().getSimpleName() + " " + sb + " = SH.getKeyHandler(\"" + methodname + "\", " + kh.getClass().getSimpleName() + ".class);");
        }

        System.err.println("=======================");
        System.err.flush();
        try {
            Dialog.getInstance().showInputDialog(Dialog.STYLE_LARGE, configInterface.toString(), strBuild.toString());
        } catch (DialogClosedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DialogCanceledException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
