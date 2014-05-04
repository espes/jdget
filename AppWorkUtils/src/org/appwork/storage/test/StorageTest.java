/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.test
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.test;

import org.appwork.storage.JSonStorage;
import org.appwork.storage.Storage;
import org.appwork.utils.logging.Log;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author thomas
 */
public class StorageTest {

    @Test
    public void defaultTest() {
        try {
            // this test has to be executed several times, because it writes on
            // app exit data to disk and evaluates it on the next start
            final Storage s = JSonStorage.getPlainStorage("org.appwork.storage.test.StorageTest");
            s.put("LONG", Long.MAX_VALUE);
            final long myLong = s.get("LONG", 0l);
            Assert.assertTrue("Restore error", myLong == Long.MAX_VALUE);
            // should convert to -1 int
            final int myInt = s.get("LONG", 0);
            Assert.assertTrue("Restore error", myInt == -1);

            s.put("TINYLONG", 100l);
            final long mytinylong = s.get("TINYLONG", 0l);
            Assert.assertTrue("Restore error", mytinylong == 100l);
            // tiny long to int conversions should work
            final long mytinyint = s.get("TINYLONG", 0);
            Assert.assertTrue("Restore error", mytinyint == 100l);
        } catch (final Exception e) {
            Log.exception(e);
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void NullTest() {
        try {
            // this test has to be executed several times, because it writes on
            // app exit data to disk and evaluates it on the next start
            final Storage s = JSonStorage.getPlainStorage("org.appwork.storage.test.StorageTest");

            s.get("GET", (String) null);
            s.get("JJ", (Integer) null);

            s.put("NULL", (String) null);
            s.put("NULL", "UNNULLER");
            s.put("NOTNULL", "imnotnull");

            // nullit
            s.put("NOTNULL", (String) null);

        } catch (final Exception e) {
            Log.exception(e);
            Assert.fail(e.getMessage());
        }

    }
}
