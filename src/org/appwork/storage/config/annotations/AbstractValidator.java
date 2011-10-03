/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config.annotations
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.annotations;

import org.appwork.storage.config.ValidationException;

/**
 * @author Thomas
 * 
 */
public abstract class AbstractValidator<T> {
    public abstract void validate(T object) throws ValidationException;
}
