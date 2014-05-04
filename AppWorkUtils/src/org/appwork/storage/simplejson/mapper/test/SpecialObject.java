/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper.test;

import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.Date;

/**
 * @author thomas
 * 
 */
public class SpecialObject {
    private SpecialObject() {

    }

    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    private URL      url;
    private Class<?> clazz;
    private Date     date;
    private Color color;
    private Color color1;
    public Color getColor1() {
        return color1;
    }

    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    public Color getRed() {
        return red;
    }

    public void setRed(Color red) {
        this.red = red;
    }

    public Color getGreen() {
        return green;
    }

    public void setGreen(Color green) {
        this.green = green;
    }

    private Color red;
    private Color green;

    /**
     * @param resource
     * @param resource2
     * @param class1
     */
    public SpecialObject(File resource, URL resource2, Class<?> class1) {
        this.file = resource;
        this.url = resource2;
        this.clazz = class1;
        color = new Color(32,64,23,76);
        color1=null;
        red=Color.RED;
        green=Color.GREEN;
        this.date = new Date();
      
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
