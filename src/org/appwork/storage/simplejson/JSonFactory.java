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
        this.str = json;
        this.sb = new StringBuilder();
        this.sb2 = new StringBuilder();
        this.counter = 0;
    }

    private ParserException bam(final String expected) {
        final String pre = this.str.substring(Math.max(this.global - 20, 0), this.global);
        final StringBuilder sb = new StringBuilder();
        sb.append(expected);
        sb.append("\r\n\t");
        sb.append(pre);
        sb.append(this.str.substring(this.global, Math.min(this.str.length(), this.global + 20)));
        sb.append("\r\n\t");
        for (int i = 1; i < pre.length(); i++) {
            sb.append("-");
        }
        sb.append('\u2934');
        return new ParserException(sb.toString());
    }

    private String findString() throws ParserException {
        // string
        try {
            this.sb.delete(0, this.sb.length());
            this.c = this.str.charAt(this.global++);
            if (this.c != '\"') { throw this.bam("'\"' expected"); }
            boolean escaped = false;
            while (true) {
                this.c = this.str.charAt(this.global++);
                switch (this.c) {
                case '\"':
                    return this.sb.toString();
                case '\\':
                    escaped = true;
                    while ((this.c = this.str.charAt(this.global++)) == '\\') {
                        escaped = !escaped;
                        if (!escaped) {
                            this.sb.append("\\");
                        }
                    }
                    if (escaped) {
                        switch (this.c) {
                        case '"':
                            this.sb.append('"');
                            continue;
                        case 'r':
                            this.sb.append('\r');
                            continue;
                        case 'n':
                            this.sb.append('\n');
                            continue;
                        case 't':
                            this.sb.append('\t');
                            continue;
                        case 'f':
                            this.sb.append('\f');
                            continue;
                        case 'b':
                            this.sb.append('\b');
                            continue;
                        case 'u':
                            this.sb2.delete(0, this.sb2.length());

                            this.global++;
                            this.counter = this.global + 4;
                            for (; this.global < this.counter; this.global++) {
                                this.c = this.getChar();
                                if (this.sb2.length() > 0 || this.c != '0') {
                                    this.sb2.append(this.c);
                                }
                            }
                            this.global--;
                            this.sb.append((char) Short.parseShort(this.sb2.toString(), 16));
                            continue;
                        default:
                            throw this.bam("illegal escape char");
                        }
                    } else {
                        this.global--;
                    }
                    break;
                default:
                    this.sb.append(this.c);
                }
            }
        } catch (final StringIndexOutOfBoundsException e) {
            this.global--;
            throw this.bam("Unexpected End of String \"" + this.sb.toString());
        }
    }

    private char getChar() {
        if (JSonFactory.DEBUG) {
            final String pos = this.str.substring(0, this.global);
            this.debug = pos + this.str.substring(this.global) + "\r\n";
            for (int i = 0; i < pos.length(); i++) {
                this.debug += "-";
            }
            this.debug += '\u2934';
            System.err.println(this.debug);
        }
        return this.str.charAt(this.global);
    }

    public JSonNode parse() throws ParserException {
        final JSonNode ret = this.parseValue();
        this.skipWhiteSpace();
        if (this.global != this.str.length()) {
            this.global++;
            throw this.bam("Unexpected End of JSonString");
        }
        return ret;
    }

    private JSonArray parseArray() throws ParserException {
        this.global++;

        final JSonArray ret = new JSonArray();
        while (true) {
            // skip whitespace
            this.skipWhiteSpace();
            this.c = this.getChar();
            switch (this.c) {
            case ']':
                this.global++;
                return ret;
            case ',':
                throw this.bam("Value missing");
            default:
                ret.add(this.parseValue());
                this.skipWhiteSpace();
                this.c = this.getChar();
                switch (this.c) {
                case ',':
                    // ok another round:
                    this.global++;
                    continue;
                case ']':
                    // end
                    this.global++;
                    return ret;
                default:
                    throw this.bam("']' or ',' expected");
                }
            }
        }
    }

    private JSonValue parseNumber() throws ParserException, NoNumberException {
        this.sb.delete(0, this.sb.length());
        boolean pointFound = false;
        boolean potFound = false;
        this.c = this.getChar();
        if (this.c == '+' || this.c == '-' || Character.isDigit(this.c)) {
            this.sb.append(this.c);
            while (this.global + 1 < this.str.length()) {
                this.global++;
                this.c = this.getChar();
                if (Character.isDigit(this.c) || !pointFound && this.c == '.' || pointFound && this.c == 'e' || pointFound && this.c == 'E' || potFound && this.c == '+' || potFound && this.c == '-') {
                    if (this.c == '.') {
                        pointFound = true;
                    } else if (pointFound && (this.c == 'e' || this.c == 'E')) {
                        potFound = true;
                    }
                    this.sb.append(this.c);
                } else {
                    this.global--;
                    break;
                }
            }
            this.global++;
            if (pointFound) {
                return new JSonValue(Double.parseDouble(this.sb.toString()));
            } else {
                return new JSonValue(Long.parseLong(this.sb.toString()));
            }
        } else {
            throw new NoNumberException();
        }
    }

    private JSonObject parseObject() throws ParserException {
        String key;
        this.global++;
        final JSonObject ret = new JSonObject();
        while (true) {
            // check for object end markers
            this.skipWhiteSpace();
            this.c = this.getChar();
            switch (this.c) {
            case '}':
                this.global++;
                return ret;
            case '"':
                key = this.findString();
                this.skipWhiteSpace();
                this.c = this.getChar();

                if (this.c != ':') { throw this.bam("':' expected"); }
                this.global++;
                this.skipWhiteSpace();
                ret.put(key, this.parseValue());
                this.skipWhiteSpace();
                this.c = this.getChar();
                switch (this.c) {
                case ',':
                    // ok another value...probably
                    this.global++;
                    continue;
                case '}':
                    // end of object:
                    this.global++;
                    return ret;
                default:
                    throw this.bam("', or }' expected");
                }
            default:
                throw this.bam("\", or }' expected");
            }
        }
    }

    private JSonValue parseString() throws ParserException {
        return new JSonValue(this.findString());
    }

    private JSonNode parseValue() throws ParserException {
        this.global = this.skipWhiteSpace();

        switch (this.getChar()) {
        case '{':
            return this.parseObject();
        case '[':
            return this.parseArray();
        case 'n':
            // null
            this.global += 4;
            return new JSonValue(null);
        case 't':
            // true;
            this.global += 4;
            return new JSonValue(true);
        case 'f':
            // false;
            this.global += 5;
            return new JSonValue(false);
        case '"':
            return this.parseString();
        }
        try {
            return this.parseNumber();
        } catch (final NoNumberException e) {
            this.global++;
            throw this.bam("Illegal Char");
        }

    }

    private int skipWhiteSpace() {
        while (this.global < this.str.length()) {
            if (!Character.isWhitespace(this.str.charAt(this.global++))) {
                this.global--;
                break;
            }
        }
        return this.global;
    }

}
