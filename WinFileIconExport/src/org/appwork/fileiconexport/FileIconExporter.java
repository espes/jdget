package org.appwork.fileiconexport;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

import org.appwork.jna.winapi.structs.IconInfo;
import org.appwork.utils.Application;
import org.appwork.utils.IO;
import org.appwork.utils.Regex;
import org.appwork.utils.logging.Log;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class FileIconExporter {
    private final File                    iconsExt;
    private final File                    iconsDir;
    private final File                    iconsCache;
    private static final FileIconExporter INSTANCE = new FileIconExporter();

    public static BufferedImage getIcon(final String path, final int num, final int width, final int height) throws FileNotFoundException {
        final HMODULE hinst = org.appwork.jna.winapi.kernel32.Kernel.I.LoadLibraryExA(path, null, org.appwork.jna.winapi.kernel32.Kernel.LOAD_LIBRARY_AS_DATAFILE);
        // Kernel32.INSTANCE.e
        // final HMODULE hinst =
        // final int err = Kernel32.INSTANCE.GetLastError();
        if (hinst == null) { throw new FileNotFoundException(path + " could not be loaded"); }
        final HANDLE hicon = com.sun.jna.platform.win32.User32.INSTANCE.LoadImage(hinst, "IDR_MAINFRAME", 1, width, height, 0);
        if (hicon == null) { throw new FileNotFoundException(path + ": No icon #" + num); }
        return getImageByHICON(width, height, hicon);

    }

    public static BufferedImage getImageByHICON(final int width, final int height, final HANDLE hicon) {
        final IconInfo iconinfo = new org.appwork.jna.winapi.structs.IconInfo();

        try {
            // GDI32 g32 = GDI32.INSTANCE;

            // get icon information

            if (!org.appwork.jna.winapi.user32.User.I.GetIconInfo(new HICON(hicon.getPointer()), iconinfo)) { return null; }
            final HWND hwdn = new HWND();
            final HDC dc = User32.INSTANCE.GetDC(hwdn);

            if (dc == null) {

            return null; }
            try {
                final int nBits = width * height * 4;
                // final BitmapInfo bmi = new BitmapInfo(1);

                final Memory colorBitsMem = new Memory(nBits);
                // // Extract the color bitmap
                final BITMAPINFO bmi = new WinGDI.BITMAPINFO();

                bmi.bmiHeader.biWidth = width;
                bmi.bmiHeader.biHeight = -height;
                bmi.bmiHeader.biPlanes = 1;
                bmi.bmiHeader.biBitCount = 32;
                bmi.bmiHeader.biCompression = WinGDI.BI_RGB;
                GDI32.INSTANCE.GetDIBits(dc, iconinfo.hbmColor, 0, height, colorBitsMem, bmi, WinGDI.DIB_RGB_COLORS);
                // g32.GetDIBits(dc, iconinfo.hbmColor, 0, size, colorBitsMem,
                // bmi,
                // GDI32.DIB_RGB_COLORS);
                final int[] colorBits = colorBitsMem.getIntArray(0, width * height);
                // final Memory maskBitsMem = new Memory(nBits);
                // // // Extract the mask bitmap
                // GDI32.INSTANCE.GetDIBits(dc, iconinfo.hbmMask, 0, height,
                // maskBitsMem, bmi, WinGDI.DIB_PAL_COLORS);
                // // g32.GetDIBits(dc, iconinfo.hbmMask, 0, size, maskBitsMem,
                // bmi,
                // // GDI32.DIB_RGB_COLORS);
                // final int[] maskBits = maskBitsMem.getIntArray(0, width *
                // height);
                // // // Copy the mask alphas into the color bits
                // for (int i = 0; i < colorBits.length; i++) {
                // colorBits[i] = colorBits[i] | (maskBits[i] != 0 ? 0 :
                // 0xFF000000);
                // }
                // // Release DC
                // Main.u32.ReleaseDC(0, dc);

                //

                // // Release bitmap handle in icon info
                // g32.DeleteObject(iconinfo.hbmColor); // add
                // g32.DeleteObject(iconinfo.hbmMask); // add

                final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                bi.setRGB(0, 0, width, height, colorBits, 0, height);
                return bi;
            } finally {
                com.sun.jna.platform.win32.User32.INSTANCE.ReleaseDC(hwdn, dc);
            }
        } finally {
            User32.INSTANCE.DestroyIcon(new HICON(hicon.getPointer()));
            GDI32.INSTANCE.DeleteObject(iconinfo.hbmColor);
            GDI32.INSTANCE.DeleteObject(iconinfo.hbmMask);
        }
    }

    public static FileIconExporter getInstance() {
        return FileIconExporter.INSTANCE;
    }

    private FileIconExporter() {
        this.iconsExt = Application.getTempResource("FileIconExporter.exe");
        this.iconsDir = Application.getTempResource("fileicons/");
        this.iconsCache = Application.getTempResource("fileicons/cache/");
        this.iconsCache.mkdirs();
        this.iconsDir.mkdirs();
        if (!this.iconsExt.exists()) {
            this.iconsExt.getParentFile().mkdirs();
            try {
                IO.writeToFile(this.iconsExt, IO.readURL(FileIconExporter.class.getResource("iconsext/iconsext.exe")));
            } catch (final IOException e) {
                Log.exception(Level.WARNING, e);

            }
        }
    }

    public BufferedImage export(final String ext) {

        try {

            final String extClassResult = IO.readInputStreamToString(Runtime.getRuntime().exec("reg query \"HKCR\\." + ext + "\" /ve").getInputStream());
            final String extClass = new Regex(extClassResult, "REG_.+?\\s+(.+)").getMatch(0);
            System.out.println(extClassResult);
            final String iconResult = IO.readInputStreamToString(Runtime.getRuntime().exec("reg query \"HKLM\\SOFTWARE\\CLASSES\\" + extClass.trim() + "\\DefaultIcon\" /ve").getInputStream());
            System.out.println(iconResult);
            String path = this.getValue(iconResult);
            final String windir = System.getenv("windir");
            path = path.replace("%SystemRoot%", windir);
            final String[] iconspath = new Regex(path, "(.+)\\,([+-]?\\d+)").getRow(0);
            int num = 0;
            if (iconspath != null) {
                num = Math.abs(Integer.parseInt(iconspath[1]));
                path = iconspath[0];
            }
            if (path.trim().startsWith("\"")) {
                path = path.trim().substring(1);
            }
            if (path.trim().endsWith("\"")) {
                path = path.trim().substring(0, path.trim().length() - 1);
            }
            if (path.endsWith(".ico") || path.endsWith(".png")) { return this.toImage(path); }

            int lookUpid = num;
            while (lookUpid < 100000) {
                try {
                    System.out.println("# " + lookUpid);
                    return FileIconExporter.getIcon(path, lookUpid, 256, 256);
                } catch (final FileNotFoundException e) {
                    if (num == 0) {
                        lookUpid++;
                        continue;
                    }
                    return null;
                }
            }

        } catch (final IOException e) {
            Log.exception(Level.WARNING, e);

        }
        return null;
    }

    private String getValue(final String iconResult) {
        final String ret = iconResult.split("\\(Standard\\)")[1].trim();
        final int i = ret.indexOf("    ");
        return ret.substring(i + 4);
    }

    private BufferedImage toImage(final String path) {
        System.out.println(path);
        return null;
    }
}
