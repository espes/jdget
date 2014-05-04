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

/**
 * @author thomas
 * 
 */
@Defaults(lngs = { "en", "de" })
public interface Translate extends TranslateInterface {

    /**
     * 
     * @param i
     * @param j
     * @param k
     * @param l
     * @return
     */
    @DescriptionForTranslationEntry("Shows how to order\r\nvariables")
    @Default(lngs = { "en", "de" }, values = { "Ordered: %s3 %s2 %s4 %s1", "Geordnet: %s3 %s2 %s4 %s1" })
    String getOrderedText(int i, int j, int k, int l);

    @Default(lngs = { "en" }, values = { "This is a test" })
    String getTestText();

}
