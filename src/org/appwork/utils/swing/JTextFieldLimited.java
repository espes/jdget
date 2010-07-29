/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.swing;

import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @author daniel
 * 
 */
public class JTextFieldLimited extends JTextField {
    /**
     * 
     */
    private static final long serialVersionUID = 4659158584673623059L;
    private int limit = 0;
    private Pattern validCharsRegex = null;

    public JTextFieldLimited(int limit) {
        super();
        this.limit = limit;
        this.setDocument(new JTextFieldLimiter());
    }

    public JTextFieldLimited(int limit, Pattern validCharsRegex) {
        super();
        this.limit = limit;
        this.validCharsRegex = validCharsRegex;
        this.setDocument(new JTextFieldLimiter());
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Pattern getValidCharsRegex() {
        return this.getValidCharsRegex();
    }

    public void setValidCharsRegex(Pattern valid) {
        this.validCharsRegex = valid;
    }

    class JTextFieldLimiter extends PlainDocument {
        /**
         * 
         */
        private static final long serialVersionUID = 2849987429671585606L;

        public JTextFieldLimiter() {
            super();
        }

        public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
            if (str == null) return;
            if (limit <= 0 || (getLength() + str.length()) <= limit) {
                if (validCharsRegex == null || validCharsRegex.matcher(str).matches()) {
                    super.insertString(offset, str, attr);
                }
            }
        }
    }

}
