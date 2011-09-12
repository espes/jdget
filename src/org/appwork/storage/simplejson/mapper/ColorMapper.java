/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson.mapper
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson.mapper;

import java.awt.Color;

import org.appwork.storage.simplejson.JSonNode;
import org.appwork.storage.simplejson.JSonValue;

/**
 * @author thomas
 * 
 */
public class ColorMapper extends TypeMapper<Color> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.simplejson.mapper.TypeMapper#obj2Json(java.lang.Object
     * )
     */
    @Override
    public JSonNode obj2Json(Color c) {
      
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        String hex;
        int length;
        if (c.getAlpha() == 255) {
            hex = Integer.toHexString(c.getRGB() & 0x00ffffff);
            length = 6;
        } else {
            hex = Integer.toHexString(c.getRGB() & 0xffffffff);
            length = 8;
        }
        for (int i = 0; i < length - hex.length(); i++) {
            sb.append("0");
        }
        sb.append(hex);
        return new JSonValue(sb.toString());

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.appwork.storage.simplejson.mapper.TypeMapper#json2Obj(org.appwork
     * .storage.simplejson.JSonNode)
     */
    @Override
    public Color json2Obj(JSonNode json) {
        // TODO Auto-generated method stub
        String str = getString(json);
        if(str.length()>7){
            return new Color(Integer.parseInt(str.substring(1), 16),true);
        }else{
            return new Color(Integer.parseInt(str.substring(1), 16),false);
        }
    
        
   
    }

}
