package org.appwork.txtresource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.appwork.storage.config.defaults.AbstractDefaultFactory;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface DynamicResourcePath {
    Class<? extends AbstractResourcePath> value();
}
