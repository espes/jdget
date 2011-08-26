/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.locale
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.locale;

import java.io.IOException;
import java.net.URISyntaxException;

import org.appwork.txtresource.TranslationFactory;
import org.appwork.txtresource.TranslationUtils;

/**
 * @author thomas
 * 
 */
public class _AWU {
    public static final AWUTranslation T = TranslationFactory.create(AWUTranslation.class);

    public static void main(final String[] args) throws URISyntaxException, IOException {
        TranslationUtils.createFiles(false, AWUTranslation.class);
    }

}