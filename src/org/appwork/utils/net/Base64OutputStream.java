/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author daniel
 * 
 */
public class Base64OutputStream extends FilterOutputStream {
    private static final char[] BASE64         = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private final int           base64buffer[] = new int[3];
    private final byte          writebuffer[]  = new byte[4];
    private int                 index          = 0;
    private static final byte   PADDING        = (byte) '=';
    private boolean             endFlush       = false;

    /**
     * @param out
     */
    public Base64OutputStream(final OutputStream out) {
        super(out);
    }

    @Override
    public void close() throws IOException {
        this.flush(true);
        super.close();
    }

    @Override
    public void flush() throws IOException {
        if (this.index == 0 || !this.endFlush) {
            this.out.flush();
            return;
        }
        if (this.endFlush == true) {
            /* a Base64 Stream can only be padded once at the end! */
            this.writebuffer[2] = Base64OutputStream.PADDING;
            this.writebuffer[3] = Base64OutputStream.PADDING;
            switch (this.index) {
            case 1:
                this.writebuffer[0] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0xFC) >> 2];
                this.writebuffer[1] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0x03) << 4];
                this.out.write(this.writebuffer);
                break;
            case 2:
                this.writebuffer[0] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0xFC) >> 2];
                this.writebuffer[1] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0x03) << 4 | (this.base64buffer[1] & 0xF0) >> 4];
                this.writebuffer[2] = (byte) Base64OutputStream.BASE64[(this.base64buffer[1] & 0x0F) << 2];
                this.out.write(this.writebuffer);
                break;
            }
        }
        this.index = 0;
        this.out.flush();
    }

    public void flush(final boolean padding) throws IOException {
        if (padding) {
            this.endFlush = true;
        }
        this.flush();
    }

    @Override
    public void write(final int b) throws IOException {
        /* put byte into base64Buffer */
        this.base64buffer[this.index++] = b;
        if (this.index == 3) {
            /* first 6 bits, &0xFC returns bit 7-2 and >>2 shifts down */
            this.writebuffer[0] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0xFC) >> 2];
            /*
             * second 6 bits, &0x03 returns bit 1-0 and <<4 shifts up, &0xF0
             * returns bit 7-4 and >>4 shifts down
             */
            this.writebuffer[1] = (byte) Base64OutputStream.BASE64[(this.base64buffer[0] & 0x03) << 4 | (this.base64buffer[1] & 0xF0) >> 4];
            /*
             * third 6 bits, &0x0F returns bit 3-0 and <<2 shifts up, &0xC0
             * returns bit 7-6 and >>6 shifts down
             */
            this.writebuffer[2] = (byte) Base64OutputStream.BASE64[(this.base64buffer[1] & 0x0F) << 2 | (this.base64buffer[2] & 0xC0) >> 6];
            /* last 6 bits, 0x3F returns bit 5-0 */
            this.writebuffer[3] = (byte) Base64OutputStream.BASE64[this.base64buffer[2] & 0x3F];
            this.out.write(this.writebuffer);
            this.index = 0;
        }
    }
}
