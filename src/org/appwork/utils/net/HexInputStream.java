/**
 * Copyright (c) 2009 - 2013 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author daniel
 * 
 */
public class HexInputStream extends FilterInputStream {

    private static final int[] HEXMAP = new int[256];
    static {
        for (int index = 0; index < HexInputStream.HEXMAP.length; index++) {
            HexInputStream.HEXMAP[index] = -1;
        }
        for (int index = 48; index <= 57; index++) {
            HexInputStream.HEXMAP[index] = -48 + index;
        }
        for (int index = 65; index <= 70; index++) {
            HexInputStream.HEXMAP[index] = -55 + index;
        }
        for (int index = 97; index <= 102; index++) {
            HexInputStream.HEXMAP[index] = -87 + index;
        }
    }

    public HexInputStream(final InputStream in) {
        super(in);
    }

    private int convertRaw(final int raw) throws IOException {
        final int mapped = HexInputStream.HEXMAP[raw];
        if (mapped < 0) { throw new IOException("unsupported hexchar" + raw); }
        return mapped;
    }

    @Override
    public synchronized void mark(final int readlimit) {
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read() throws IOException {
        final int hexRawPart1 = super.read();
        if (hexRawPart1 == -1) { return -1; }
        final int hexRawPart2 = super.read();
        if (hexRawPart2 == -1) { throw new EOFException("incomplete hex"); }
        final int hexPart1 = this.convertRaw(hexRawPart1);
        final int hexPart2 = this.convertRaw(hexRawPart2);
        final int ret = hexPart1 * 16 + hexPart2;
        return ret;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        /* taken from InputStream */
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) { return 0; }

        int c = this.read();
        if (c == -1) { return -1; }
        int index = 0;
        b[off + index++] = (byte) c;
        for (; index < len; index++) {
            c = this.read();
            if (c == -1) {
                break;
            }
            b[off + index] = (byte) c;
        }
        return index;
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public long skip(final long n) throws IOException {
        return super.skip(n);
    }

}
