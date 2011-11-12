package org.appwork.storage.config.test;

import java.util.HashSet;

import org.appwork.storage.config.ConfigInterface;
import org.appwork.storage.config.JsonConfig;
import org.appwork.storage.config.annotations.Description;
import org.appwork.storage.config.handler.KeyHandler;

public class ConfigUtils {

    /**
     * @param class1
     */
    public static void printStaticMappings(final Class<? extends ConfigInterface> configInterface) {
        // TODO Auto-generated method stub
        System.err.println(configInterface);
        System.err.flush();
        System.out.println("//Static Mappings for " + configInterface);
        System.out.println("public static final " + configInterface.getSimpleName() + "                 CFG                               = JsonConfig.create(" + configInterface.getSimpleName() + ".class);");
        System.out.println("public static final StorageHandler<" + configInterface.getSimpleName() + ">                 SH                               = (StorageHandler<" + configInterface.getSimpleName() + ">) CFG.getStorageHandler();");
        System.out.println("//let's do this mapping here. If we map all methods to static handlers, access is faster, and we get an error on init if mappings are wrong.");

        // public static final BooleanKeyHandler LINK_FILTER_ENABLED =
        // SH.getKeyHandler("LinkFilterEnabled", BooleanKeyHandler.class);
        HashSet<KeyHandler<?>> unique = new HashSet<KeyHandler<?>>();
        for (KeyHandler<?> kh : JsonConfig.create(configInterface).getStorageHandler().getMap().values()) {
            if (!unique.add(kh)) continue;
//            String key = kh.getKey();
            String methodname = kh.getSetter().getMethod().getName().substring(3);
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
                System.out.println("/**");
                System.out.println(" * " + kh.getAnnotation(Description.class).value());
                System.out.println("**/");
            }
            System.out.println("public static final " + kh.getClass().getSimpleName() + " " + sb + " = SH.getKeyHandler(\"" + methodname + "\", " + kh.getClass().getSimpleName() + ".class);");
        }
        
        System.out.flush();
        System.err.println("=======================");
        System.err.flush();

    }
}
