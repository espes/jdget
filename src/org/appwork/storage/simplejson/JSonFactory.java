/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.simplejson
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.simplejson;

/**
 * @author thomas
 * 
 */
public class JSonFactory {

    /**
     * @param jsonString
     */
    public static JSonNode parse(final String str) {
        if (str.trim().startsWith("[")) {
            final JSonArray ret = new JSonArray();
            JSonFactory.parseArray(0, ret, str);
            return ret;
        } else {
            final JSonObject ret = new JSonObject();
            JSonFactory.parseObject(0, ret, str);
            return ret;
        }

    }

    /**
     * @param indexKeyStart
     * @param obj
     * @param str
     * @return
     */
    private static int parseArray(int global, final JSonArray _this, final String str) {
        char c;
        int var;
        global++;
        int counter;

        boolean found;
        int indexKeyStart, indexKeyEnd;

        while (true) {
            // skip whitespace
            var = 0;
            c = str.charAt(global + var++);
            while (Character.isWhitespace(c) || c == ',') {
                c = str.charAt(global + var++);
            }

            indexKeyStart = global + var - 1;
            if (str.charAt(indexKeyStart) == ']') {
                global = indexKeyStart + 1;
                return global;
            }
            if (str.charAt(indexKeyStart) == '"') {
                // string
                indexKeyEnd = str.indexOf('"', indexKeyStart + 1);
                while (true) {

                    // check if the match is escaped
                    var = 1;
                    counter = 0;
                    while (indexKeyEnd - var >= global && str.charAt(indexKeyEnd - var++) == '\\') {
                        counter++;
                    }
                    if (counter % 2 == 0) {
                        // not escaped match is ok
                        break;
                    } else {
                        // escaped match is bad
                        indexKeyEnd = str.indexOf('"', indexKeyEnd + 1);
                    }
                }
                _this.add(new JSonObject(str.substring(indexKeyStart + 1, indexKeyEnd)));
                global = indexKeyEnd + 1;
            } else if (str.charAt(indexKeyStart) == '[') {
                final JSonNode obj = new JSonArray();
                global = JSonFactory.parseArray(indexKeyStart, (JSonArray) obj, str);
                _this.add(obj);
            } else if (str.charAt(indexKeyStart) == '{') {
                // object
                final JSonObject obj = new JSonObject();
                global = JSonFactory.parseObject(indexKeyStart + 1, obj, str);
                _this.add(obj);
            } else if (str.charAt(indexKeyStart) == 'n') {
                _this.add(new JSonObject(null));
                global = indexKeyStart + 4;
                // null
            } else if (str.charAt(indexKeyStart) == 't') {
                _this.add(new JSonObject(true));
                global = indexKeyStart + 4;
            } else if (str.charAt(indexKeyStart) == 'f') {
                _this.add(new JSonObject(false));
                global = indexKeyStart + 5;
            } else {
                var = 1;
                c = str.charAt(indexKeyStart + var);
                found = false;

                while (Character.isDigit(c) || !found && c == '.') {
                    if (c == '.') {
                        found = true;
                    }

                    var++;
                    c = str.charAt(indexKeyStart + var);
                }
                if (found) {
                    _this.add(new JSonObject(Double.parseDouble(str.substring(indexKeyStart, indexKeyStart + var))));
                } else {
                    _this.add(new JSonObject(Long.parseLong(str.substring(indexKeyStart, indexKeyStart + var))));

                }

                global = indexKeyStart + var;
                // number
            }

        }

    }

    /**
     * @param _this
     * @param str
     */
    private static int parseObject(int global, final JSonObject _this, final String str) {

        int var;
        int counter;
        char c;
        boolean found;
        int indexKeyStart, indexKeyEnd;
        String key;

        while (true) {
            // check for object end markers
            for (var = global; var < str.length(); var++) {
                c = str.charAt(var);
                if (Character.isWhitespace(c)) {
                    continue;
                } else if (c == ',') {

                    continue;
                } else if (c == '{') {
                    continue;
                } else if (c == '}') {
                    // object end;

                    return var + 1;
                } else {
                    // no object end found
                    break;
                }
            }
            indexKeyStart = str.indexOf('"', global);
            if (indexKeyStart == -1) {
                break;
            }
            indexKeyEnd = str.indexOf('"', indexKeyStart + 1);
            key = str.substring(indexKeyStart + 1, indexKeyEnd);
            // set to true if : has been found
            found = false;
            for (var = indexKeyEnd + 1; var < str.length(); var++) {
                c = str.charAt(var);
                if (Character.isWhitespace(c)) {
                    continue;
                } else if (found) {
                    break;
                }
                if (c == ':') {
                    found = true;
                }
            }
            // now, next char may be:<br>
            // number,object,null, or string,boolean,array
            indexKeyStart = var;
            if (str.charAt(indexKeyStart) == '"') {
                // string
                indexKeyEnd = str.indexOf('"', indexKeyStart + 1);
                while (true) {

                    // check if the match is escaped
                    var = 1;
                    counter = 0;
                    while (indexKeyEnd - var >= global && str.charAt(indexKeyEnd - var++) == '\\') {
                        counter++;
                    }
                    if (counter % 2 == 0) {
                        // not escaped match is ok
                        break;
                    } else {
                        // escaped match is bad
                        indexKeyEnd = str.indexOf('"', indexKeyEnd + 1);
                    }
                }
                _this.put(key, new JSonObject(str.substring(indexKeyStart + 1, indexKeyEnd)));
                global = indexKeyEnd + 1;
            } else if (str.charAt(indexKeyStart) == '[') {
                final JSonNode obj = new JSonArray();
                global = JSonFactory.parseArray(indexKeyStart, (JSonArray) obj, str);
                _this.put(key, obj);
            } else if (str.charAt(indexKeyStart) == '{') {
                // object
                // final String dfsad = str.substring(indexKeyStart);
                final JSonObject obj = new JSonObject();
                global = JSonFactory.parseObject(indexKeyStart, obj, str);
                _this.put(key, obj);
            } else if (str.charAt(indexKeyStart) == 'n') {
                _this.put(key, new JSonObject(null));
                global = indexKeyStart + 4;
                // null
            } else if (str.charAt(indexKeyStart) == 't') {
                _this.put(key, new JSonObject(true));
                global = indexKeyStart + 4;
            } else if (str.charAt(indexKeyStart) == 'f') {
                _this.put(key, new JSonObject(false));
                global = indexKeyStart + 5;
            } else {
                var = 1;
                c = str.charAt(indexKeyStart + var);
                found = false;

                while (Character.isDigit(c) || !found && c == '.') {
                    if (c == '.') {
                        found = true;
                    }

                    var++;
                    c = str.charAt(indexKeyStart + var);
                }
                if (found) {
                    _this.put(key, new JSonObject(Double.parseDouble(str.substring(indexKeyStart, indexKeyStart + var))));
                } else {
                    _this.put(key, new JSonObject(Long.parseLong(str.substring(indexKeyStart, indexKeyStart + var))));

                }

                global = indexKeyStart + var;
                // number
            }

        }
        return global;

    }

}
