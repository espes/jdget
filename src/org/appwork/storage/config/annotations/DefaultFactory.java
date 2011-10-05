package org.appwork.storage.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.appwork.storage.config.defaults.AbstractDefaultFactory;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface DefaultFactory {
    Class<? extends AbstractDefaultFactory<?>> value();
}
