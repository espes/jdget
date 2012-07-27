package org.appwork.storage.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
/**
 * can be used to assign a description to a config entry. This makes sense of there is no settings panel, but only advanced config (about:config) or config-json-files
 */
public @interface DescriptionForConfigEntry {

    String value();

}
