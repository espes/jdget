package jd.http.requests;

import java.util.HashMap;
import java.util.Map;

import jd.http.RequestHeader;
import junit.framework.Assert;

import org.junit.Test;

/**
 * @author wnickl
 */

public class RequestHeaderTest {

    @Test
    public void testIsDominant() {
        RequestHeader header = new RequestHeader();
        Assert.assertFalse(header.isDominant());
        header.setDominant(true);
        Assert.assertTrue(header.isDominant());
        header.setDominant(false);
        Assert.assertFalse(header.isDominant());
    }

    @Test
    public void testPut() {
        RequestHeader header = new RequestHeader();
        header.put("TEST-KEY", "TEST-VALUE");
        Assert.assertEquals(1, header.size());
        String value = header.get("TEST-KEY");
        Assert.assertNotNull(value);
        Assert.assertEquals("TEST-VALUE", value);
    }

    @Test
    public void testPutAll() {
        RequestHeader header = new RequestHeader();
        Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 10; i++) {
            map.put("TEST-" + i, "VALUE-" + i);
        }
        header.putAll(map);
        Assert.assertEquals(10, header.size());
        for (int i = 0; i < 10; i++) {
            String value = header.get("TEST-" + i);
            Assert.assertNotNull(value);
            Assert.assertEquals("VALUE-" + i, value);
        }
    }

    @Test
    public void testContains() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        boolean contains = header.contains("TEST-88");
        Assert.assertTrue(contains);
        contains = header.contains("WRONG");
        Assert.assertFalse(contains);
    }

    @Test
    public void testClear() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        Assert.assertEquals(100, header.size());
        header.clear();
        Assert.assertEquals(0, header.size());
    }

    @Test
    public void testClone() {
        RequestHeader header = new RequestHeader();
        RequestHeader clone = header.clone();
        Assert.assertNotNull(clone);
        Assert.assertEquals(header, clone);
        header.setDominant(true);
        clone = header.clone();
        Assert.assertNotNull(clone);
        Assert.assertEquals(header, clone);
    }

    @Test
    public void testGetKey() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        for (int i = 0; i < 100; i++) {
            String key = header.getKey(i);
            Assert.assertEquals("TEST-" + i, key);
        }
    }

    @Test
    public void testGetValue() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        for (int i = 0; i < 100; i++) {
            String value = header.getValue(i);
            Assert.assertEquals("VALUE-" + i, value);
        }
    }

    @Test
    public void testPutAllWithHeader() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        RequestHeader header2 = new RequestHeader();
        header2.putAll(header);
        Assert.assertEquals(100, header2.size());
    }

    @Test
    public void test() {
        RequestHeader header = new RequestHeader();
        for (int i = 0; i < 100; i++) {
            header.put("TEST-" + i, "VALUE-" + i);
        }
        Assert.assertEquals(100, header.size());
        header.remove("TEST-42");
        Assert.assertEquals(99, header.size());
        Assert.assertFalse(header.contains("TEST-42"));
    }
}

// ---------------------------- Revision History ----------------------------
// $Log$
//
