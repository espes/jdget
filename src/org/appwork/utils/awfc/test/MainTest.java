/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.awfc.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.awfc.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.appwork.exceptions.WTFException;
import org.appwork.utils.ReusableByteArrayOutputStreamPool;
import org.appwork.utils.ReusableByteArrayOutputStreamPool.ReusableByteArrayOutputStream;
import org.appwork.utils.awfc.AWFCUtils;

/**
 * @author Thomas
 * 
 */
public class MainTest {
    public static void main(final String[] args) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        AWFCUtils utils = new AWFCUtils(bos);

        utils.writeBoolean(true);
        utils.writeShort(10);
        utils.writeShort(32765);
        utils.writeString("HALLO DU");

        long[] testValues = new long[] { Long.MAX_VALUE };
        for (long value : testValues) {
            utils.writeLongOptimized(value);
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        utils = new AWFCUtils(bis);
        int read = 0;
        if (utils.readBoolean() != true) { throw new WTFException(); }
        if (utils.readShort() != 10) { throw new WTFException(); }
        if ((read = utils.readShort()) != 32765) { throw new WTFException("" + read); }
        if (!"HALLO DU".equals(utils.readString())) { throw new WTFException(); }
        for (long value : testValues) {
            long ret = utils.readLongOptimized();
            if (value != ret) { throw new WTFException(); }
        }
        final ReusableByteArrayOutputStream rbos = ReusableByteArrayOutputStreamPool.getReusableByteArrayOutputStream();
        final InputStream is = new InputStream() {

            protected int pos;

            public synchronized int read() {
                return (pos < rbos.size()) ? (rbos.getInternalBuffer()[pos++] & 0xff) : -1;
            }

            @Override
            public synchronized int read(byte b[], int off, int len) {
                if (b == null) {
                    throw new NullPointerException();
                } else if (off < 0 || len < 0 || len > b.length - off) { throw new IndexOutOfBoundsException(); }

                if (pos >= rbos.size()) { return -1; }

                int avail = rbos.size() - pos;
                if (len > avail) {
                    len = avail;
                }
                if (len <= 0) { return 0; }
                System.arraycopy(rbos.getInternalBuffer(), pos, b, off, len);
                pos += len;
                return len;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.io.InputStream#reset()
             */
            @Override
            public synchronized void reset() throws IOException {
                pos = 0;
            }

        };

        AWFCUtils utils2 = new AWFCUtils() {

            @Override
            public InputStream getCurrentInputStream() throws IOException {
                // TODO Auto-generated method stub
                return is;
            }

            @Override
            public OutputStream getCurrentOutputStream() throws IOException {
                return rbos;
            }

        };
        long size = 0;
        long l = 0;
        for (l = 0; l < Integer.MAX_VALUE; l++) {
            rbos.reset();
            is.reset();
            utils2.writeLongOptimized(l);
            if (utils2.readLongOptimized() != l) {
                break;
            }
            size += rbos.size();
            System.out.println(l + " " + size);
        }

    }
}
