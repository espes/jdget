package org.appwork.storage.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
/**
 * Objects are cached in a mintime weakreference. If you do not want this, for example because you want to get a fresh new instance on each call, use this annotation
 * @author thomas
 *
 */
public @interface DisableObjectCache {

}
