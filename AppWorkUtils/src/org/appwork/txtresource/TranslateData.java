/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.txtresource
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.txtresource;

import java.util.HashMap;

import org.appwork.storage.Storable;

/**
 * @author thomas
 * 
 */
public class TranslateData extends HashMap<String, String> implements Storable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TranslateData() {
        // require for STorable interface
    }

}
