package org.appwork;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import jd.http.Browser;
import jd.parser.Regex;

import org.appwork.resources.AWUTheme;
import org.appwork.storage.JSonStorage;
import org.appwork.storage.TypeRef;
import org.appwork.storage.jackson.JacksonMapper;

public class Stater {
    static {
        // USe Jacksonmapper in this project
        JSonStorage.setMapper(new JacksonMapper());
        AWUTheme.I().setPath("/");

    }

    private static long count(final java.util.List<HashMap<String, String>> data, final String key, final String regex) {
        long ret = 0;
        for (final HashMap<String, String> d : data) {
            if (new Regex(d.get(key), regex).matches()) {
                ret++;
            }

        }
        return ret;
    }

    private static long getTimestamp(final int day, final int month, final int year, final int h, final int m) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("PST"));
        cal.set(year, month - 1, day, h, m, 0);

        return cal.getTimeInMillis() / 1000;
    }

    public static void main(final String[] args) throws IOException, InterruptedException {

        final long start = Stater.getTimestamp(18, 8, 2011, 0, 0);
        final long end = Stater.getTimestamp(20, 8, 2011, 0, 0);
        Stater.print(start, end);
        // Stater.printDayInHours(start);

        // System.out.println("ignores: " + ignores + " " + 100 * ignores /
        // totals + "%");
        Thread.sleep(10000);

    }

    private static void print(final long start, final long end) throws IOException {
        final String sql = "http://update0.jdownloader.org/php/stats146738.php?start=" + start + "&end=" + end;
        final String dat = new Browser().getPage(sql);

        final java.util.List<HashMap<String, String>> data = JSonStorage.restoreFromString(dat, new TypeRef<ArrayList<HashMap<String, String>>>() {
        }, null);
        final SimpleDateFormat df = new SimpleDateFormat("dd.MM.yy - HH:mm");
        System.err.println(df.format(new Date(start * 1000)) + " - " + df.format(new Date(end * 1000)));

        if (data == null) {
            System.err.println("NULL");
            return;
        }
        final long offers = Stater.count(data, "do", "offer");
        final long ignores = Stater.count(data, "do", "ignore");
        final long bundleInstall = Stater.count(data, "do", "bundleok");
        final long failed = Stater.count(data, "do", "bundlefail");
        final long total = Stater.count(data, "do", "screen");
        final long inits = Stater.count(data, "do", "init");
        long ok = 0;

        final long chrome = Stater.count(data, "browser", "chrome");
        final HashMap<String, Integer> toolbarMap = new HashMap<String, Integer>();
        long toolbar = 0;
        long ai0 = 0;
        long ain0 = 0;
        long failedAfter = 0;
        for (final HashMap<String, String> d : data) {
            if ("AI0".equals(d.get("do"))) {
                ok++;
                final String[] beforeAfter = d.get("comment").split(":");

                if (beforeAfter.length == 2) {
                    if (beforeAfter[0].equals(beforeAfter[1])) {
                        failedAfter++;
                        System.out.println("FAIL " + d.get("comment"));
                    }
                }
                ai0++;
            } else if (d.get("do").startsWith("AI")) {
                ain0++;
                ok++;

            }
            if ("ignore".equals(d.get("do")) && "Toolbar: -".equals(d.get("comment"))) {

            } else if ("ignore".equals(d.get("do")) && d.get("comment").startsWith("Toolbar: ")) {

                toolbar++;
                final String toolbarid = d.get("comment").substring(9);
                Integer num = toolbarMap.get(toolbarid);
                if (num == null) {
                    num = 0;
                }
                toolbarMap.put(toolbarid, num + 1);
            }

        }
        System.err.println("JRE Downloads: " + (inits - total) + " - ");
        System.err.println("A0: " + ai0);
        System.err.println("Ask_Failed " + ain0);
        System.err.println("ASk Failed after: " + failedAfter);

        if (failedAfter + ain0 + ai0 > 0) {
            System.err.println("Install Fails: " + 100 * (failedAfter + ain0) / (failedAfter + ain0 + ai0) + "%");
        }
        System.err.println("Total :" + total);
        if (total > 0) {
            System.err.println("Offers: " + offers + " " + 100 * offers / total + "%");
        }
        if (total > 0) {
            System.err.println("Ignored: " + ignores + " " + 100 * ignores / total + "%");
        }

        if (ignores > 0) {
            System.err.println("Chrome: " + 100 * chrome / ignores + "%  " + 100 * chrome / total + "%");
        }
        if (ignores > 0) {
            System.err.println("Toolbar: " + 100 * toolbar / ignores + "%  " + 100 * toolbar / total + "%");

        }
        if (ok + failed > 0) {
            System.err.println("Converted: " + ok + " Rate: " + 100 * ok / offers + "%");
            System.err.println("OK " + ok + " - " + bundleInstall);
        }
        if (total > 0) {
            System.err.println("Total conversion: " + 100 * ok / total + "% " + "(" + 100 * (ok - ain0 - failedAfter) / total + " %)");
        }
        // System.out.println(JSonStorage.toString(toolbarMap));
    }

    private static void printDayInHours(final long start) throws IOException {
        for (int i = 0; i < 24; i++) {
            final long s = start + i * 60 * 60;
            final long e = start + (i + 1) * 60 * 60;
            Stater.print(s, e);
        }

    }
}
