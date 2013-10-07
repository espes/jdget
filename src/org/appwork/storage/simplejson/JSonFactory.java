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
 */
public class JSonFactory {

    public static boolean       DEBUG  = false;
    private int                 global = 0;
    private char                c;
    private final String        str;
    final StringBuilder         sb;
    private final StringBuilder sb2;
    private int                 counter;
    private String              debug;

    public JSonFactory(final String json) {
        str = json;
        sb = new StringBuilder();
        sb2 = new StringBuilder();
        counter = 0;
    }

    private ParserException bam(final String expected) {
        String pre = str.substring(Math.max(global - 20, 0), global);
        pre = pre.replace("\r", "\\r").replace("\n", "\\n");
        final StringBuilder sb = new StringBuilder();
        sb.append(expected);
        sb.append("\r\n\t");
        sb.append(pre);
        sb.append(str.substring(global, Math.min(str.length(), global + 20)));
        sb.append("\r\n\t");
        for (int i = 1; i < pre.length(); i++) {
            sb.append("-");
        }
        sb.append('|');
        return new ParserException(sb.toString());
    }

    private String findString() throws ParserException {
        // string
        try {
            sb.delete(0, sb.length());
            c = str.charAt(global++);
            if (c != '\"') { throw bam("'\"' expected"); }
            boolean escaped = false;
            while (true) {
                c = str.charAt(global++);
                switch (c) {
                case '\"':
                    return sb.toString();
                case '\\':
                    escaped = true;
                    while ((c = str.charAt(global++)) == '\\') {
                        escaped = !escaped;
                        if (!escaped) {
                            sb.append("\\");
                        }
                    }
                    if (escaped) {
                        switch (c) {
                        case '"':
                            sb.append('"');
                            continue;
                        case 'r':
                            sb.append('\r');
                            continue;
                        case 'n':
                            sb.append('\n');
                            continue;
                        case 't':
                            sb.append('\t');
                            continue;
                        case 'f':
                            sb.append('\f');
                            continue;
                        case 'b':
                            sb.append('\b');
                            continue;
                        case 'u':
                            sb2.delete(0, sb2.length());

                            // this.global++;
                            counter = global + 4;
                            for (; global < counter; global++) {
                                c = getChar();
                                if (sb2.length() > 0 || c != '0') {
                                    sb2.append(c);
                                }
                            }
                            // this.global--;

                            if (sb2.length() == 0) {
                                sb.append((char) 0);
                            } else {
                                sb.append((char) Short.parseShort(sb2.toString(), 16));
                            }
                            continue;
                        default:
                            throw bam("illegal escape char");
                        }
                    } else {
                        global--;
                    }
                    break;
                default:
                    sb.append(c);
                }
            }
        } catch (final StringIndexOutOfBoundsException e) {
            global--;
            throw bam("Unexpected End of String \"" + sb.toString());
        }
    }

    private char getChar() throws ParserException {
        if (JSonFactory.DEBUG) {
            final String pos = str.substring(0, global);
            debug = pos + str.substring(global) + "\r\n";
            for (int i = 0; i < pos.length(); i++) {
                debug += "-";
            }
            debug += '\u2934';
            System.err.println(debug);
        }
        if (global >= str.length()) { throw bam("Ended unexpected"); }
        return str.charAt(global);
    }

    public JSonNode parse() throws ParserException {
        final JSonNode ret = parseValue();
        skipWhiteSpace();
        if (global != str.length()) {
            global++;
            throw bam("Unexpected End of JSonString");
        }
        return ret;
    }

    private JSonArray parseArray() throws ParserException {
        global++;

        final JSonArray ret = new JSonArray();
        while (true) {
            // skip whitespace
            skipWhiteSpace();
            c = getChar();
            switch (c) {
            case ']':
                global++;
                return ret;
            case ',':
                throw bam("Value missing");
            default:
                ret.add(parseValue());
                skipWhiteSpace();
                c = getChar();
                switch (c) {
                case ',':
                    // ok another round:
                    global++;
                    continue;
                case ']':
                    // end
                    global++;
                    return ret;
                default:
                    throw bam("']' or ',' expected");
                }
            }
        }
    }

    private JSonValue parseNumber() throws ParserException, NoNumberException {
        sb.delete(0, sb.length());
        boolean pointFound = false;
        boolean potFound = false;
        c = getChar();
        if (c == '+' || c == '-' || Character.isDigit(c)) {
            sb.append(c);
            while (global + 1 < str.length()) {
                global++;
                c = getChar();
                if (Character.isDigit(c) || !pointFound && c == '.' || pointFound && c == 'e' || pointFound && c == 'E' || potFound && c == '+' || potFound && c == '-') {
                    if (c == '.') {
                        pointFound = true;
                    } else if (pointFound && (c == 'e' || c == 'E')) {
                        potFound = true;
                    }
                    sb.append(c);
                } else {
                    global--;
                    break;
                }
            }
            global++;
            if (pointFound) {
                return new JSonValue(Double.parseDouble(sb.toString()));
            } else {
                return new JSonValue(Long.parseLong(sb.toString()));
            }
        } else {
            throw new NoNumberException();
        }
    }

    private JSonObject parseObject() throws ParserException {
        String key;
        global++;
        final JSonObject ret = new JSonObject();

        skipWhiteSpace();
        c = getChar();
        if (c == '}') {
            global++;
            return ret;
        }
        while (true) {
            // check for object end markers

            bs: switch (c) {

            case '"':
                key = findString();
                skipWhiteSpace();
                c = getChar();

                if (c != ':') { throw bam("':' expected"); }
                global++;
                skipWhiteSpace();
                ret.put(key, parseValue());
                skipWhiteSpace();
                if (global >= str.length()) { throw bam("} or , expected"); }
                c = getChar();
                switch (c) {
                case ',':
                    // ok another value...probably
                    global++;
                    break bs;
                case '}':
                    // end of object:
                    global++;
                    return ret;
                default:
                    throw bam(", or }' expected");
                }
            default:
                throw bam("\" expected");
            }

            skipWhiteSpace();
            c = getChar();
        }
    }

    private JSonValue parseString() throws ParserException {
        return new JSonValue(findString());
    }

    private JSonNode parseValue() throws ParserException {
        global = skipWhiteSpace();

        switch (getChar()) {
        case '{':
            return parseObject();
        case '[':
            return parseArray();
        case 'n':
            // null
            global += 4;
            return new JSonValue(null);
        case 't':
            // true;
            global += 4;
            return new JSonValue(true);
        case 'f':
            // false;
            global += 5;
            return new JSonValue(false);
        case '"':
            return parseString();
        }
        try {
            return parseNumber();
        } catch (final NoNumberException e) {
            global++;
            throw bam("Illegal Char");
        }

    }

    private int skipWhiteSpace() {
        while (global < str.length()) {
            if (!Character.isWhitespace(str.charAt(global++))) {
                global--;
                break;
            }
        }
        return global;
    }

}
