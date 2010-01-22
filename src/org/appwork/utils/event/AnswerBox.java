/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.event
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.event;

/**
 * The answerbox can be passed as parameter in events. Registered listenerts can
 * change the value in the answerbox
 * 
 * @author coalado
 * 
 */
public class AnswerBox {
    /**
     * @return the i
     */
    public int getInteger() {
        return integer;
    }

    /**
     * @param i
     *            the i to set
     */
    public void setInteger(int i) {
        this.integer = i;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @param string
     *            the string to set
     */
    public void setString(String string) {
        this.string = string;
    }

    /**
     * @return the obj
     */
    public Object getObj() {
        return obj;
    }

    /**
     * @param obj
     *            the obj to set
     */
    public void setObj(Object obj) {
        this.obj = obj;
    }

    private int integer;
    private String string;
    private Object obj;
    private boolean bool;

    /**
     * @return the bool
     */
    public boolean isBool() {
        return bool;
    }

    /**
     * @param bool
     *            the bool to set
     */
    public void setBool(boolean bool) {
        this.bool = bool;
    }

}
