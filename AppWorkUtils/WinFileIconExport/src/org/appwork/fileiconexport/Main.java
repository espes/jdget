package org.appwork.fileiconexport;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.appwork.jna.winapi.kernel32.EnumResourceNamesCallback;
import org.appwork.jna.winapi.kernel32.HGLOBAL;
import org.appwork.jna.winapi.kernel32.Kernel;
import org.appwork.jna.winapi.structs.IconInfo;
import org.appwork.jna.winapi.user32.User;
import org.appwork.utils.Application;
import org.appwork.utils.logging.Log;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HICON;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.HRSRC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class Main {

    public static BufferedImage getImageByHICON(final int width, final int height, final HANDLE hicon, final BITMAPINFOHEADER info) {
        final IconInfo iconinfo = new org.appwork.jna.winapi.structs.IconInfo();

        try {
            // GDI32 g32 = GDI32.INSTANCE;

            // get icon information

            if (!User.I.GetIconInfo(new HICON(hicon.getPointer()), iconinfo)) { return null; }
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
                if (info.biBitCount < 32) {
                    final Memory maskBitsMem = new Memory(nBits);
                    // // Extract the mask bitmap
                    GDI32.INSTANCE.GetDIBits(dc, iconinfo.hbmMask, 0, height, maskBitsMem, bmi, WinGDI.DIB_PAL_COLORS);
                    // g32.GetDIBits(dc, iconinfo.hbmMask, 0, size,
                    // maskBitsMem,
                    // bmi,
                    // // GDI32.DIB_RGB_COLORS);
                    final int[] maskBits = maskBitsMem.getIntArray(0, width * height);
                    // // // Copy the mask alphas into the color bits
                    for (int i = 0; i < colorBits.length; i++) {
                        colorBits[i] = colorBits[i] | (maskBits[i] != 0 ? 0 : 0xFF000000);
                    }
                }
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

    public static void main(final String[] args) {
        // System.out.println(System.getenv("windir"));
        //
        final HWND h = User32.INSTANCE.FindWindow(null, "Rechner");

        String dll = null;
        dll = "C:\\Windows\\system32\\User32.dll";
        // dll =
        // "C:\\Users\\thomas\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe";
        // dll = "C:\\Program Files\\JDownloader\\JDownloader.exe";
        // dll = "C:\\Windows\\system32\\Shell32.dll";
        final String NameID = new File(dll).getName();
        final HMODULE lib = Kernel.I.LoadLibraryExA(dll, null, Kernel.LOAD_LIBRARY_AS_DATAFILE);

        Kernel.I.EnumResourceNamesA(lib, "#14", new EnumResourceNamesCallback() {

            @Override
            /**
             * __in_opt HMODULE hModule,
            __in LPCTSTR lpszType,
            __in LPTSTR lpszName,
            __in LONG_PTR lParam
             */
            public boolean callback(final HMODULE module, final Pointer type, final int identifier, final Pointer data) {
                // final byte[] ba = name.getByteArray(0, 1);
                final HRSRC resourceHandle;
                String iconID = null;
                if ((identifier & 0xffff0000) > 0) {
                    // String pointer
                    final String iconName = new Pointer(identifier).getString(0);
                    iconID = "Name" + iconName;
                    System.out.println("Icon Name: " + iconName);

                } else {
                    System.out.println("Icon ID: " + identifier);
                    iconID = "ID" + identifier;

                }
                resourceHandle = Kernel.I.FindResourceA(module, new Pointer(identifier), "#14");
                final HGLOBAL glob = Kernel.I.LoadResource(module, resourceHandle);
                final Pointer pIconGroup = Kernel.I.LockResource(glob);

                final GRPICONDIR iconGroup = new GRPICONDIR(pIconGroup);
                final GRPICONDIRENTRY[] entries = new GRPICONDIRENTRY[iconGroup.idCount];
                for (int i = 0; i < iconGroup.idCount; i++) {
                    final Pointer entriesPointer = new Pointer(Pointer.nativeValue(pIconGroup) + iconGroup.size() + i * 14);
                    entries[i] = new GRPICONDIRENTRY(entriesPointer);
                    // if (first.bHeight >= 16 && first.bHeight == first.bWidth)
                    // {
                    int width = entries[i].bWidth;
                    int height = entries[i].bHeight;
                    if (width == 0) {
                        width = 256;
                    }
                    if (height == 0) {
                        height = 256;
                    }
                    System.out.println(i + " - " + width + " - " + entries[i].dwBytesInRes);

                    // System.out.println(first);
                    // }
                    if (entries[i].wBitCount != 32) {
                        // continue;
                    }
                    final HRSRC hRsrc = Kernel.I.FindResourceA(module, new Pointer(entries[i].nID), "#3");
                    final HGLOBAL hGlobal = Kernel.I.LoadResource(module, hRsrc);
                    final Pointer pIcon = Kernel.I.LockResource(hGlobal);

                    final BITMAPINFOHEADER info = new BITMAPINFOHEADER(pIcon);

                    // final ICONIMAGE iconImage = new ICONIMAGE(pIcon,new
                    // Pointer(Pointer.nativeValue(pIcon)+info.size()));
                    final HICON hicon = User.I.CreateIconFromResourceEx(pIcon, entries[i].dwBytesInRes, true, 0x00030000, width, height, 0);
                    //
                    System.out.println(info);
                    if (hicon != null) {
                        final BufferedImage image = Main.getImageByHICON(width, height, hicon, info);
                        // try {
                        // Dialog.getInstance().showConfirmDialog(0, iconID,
                        // "INFO",
                        // new ImageIcon(image), null, null);
                        // } catch (final DialogClosedException e) {
                        // Log.exception(Level.WARNING, e);
                        //
                        // } catch (final DialogCanceledException e) {
                        // Log.exception(Level.WARNING, e);
                        //
                        // }

                        final File saveTo = Application.getTempResource("icons/" + NameID + "_" + iconID + "_" + (i + 1) + ".png");

                        saveTo.delete();
                        saveTo.getParentFile().mkdirs();
                        try {
                            ImageIO.write(image, "png", saveTo);
                            System.out.println(saveTo);
                        } catch (final IOException e) {
                            Log.exception(Level.WARNING, e);

                        }
                    }
                    // System.out.println(iconImage);

                }
                // final Structure[] list = new
                // GRPICONDIRENTRY(entriesPointer).toArray(10);
                // final GRPICONDIRENTRY[] groupEntry = GRPICONDIRENTRY.list(,
                // iconGroup.idCount);
                // if(0x0000FFFF)
                // final Pointer p = name.getPointer();
                // p.getString(2);
                // final String str = name.getPointer()

                // final Class nt = name.nativeType();
                // final byte b = name.getByte(0);
                // // System.out.println(" - " + str);
                // final int in = name.getInt(0);
                // final String str = name.getString(0);
                // System.out.println(str);
                return true;
            }

        }, 1l);
        Kernel.I.FreeLibrary(lib);

        // System.out.println("No Exception");
        // try {
        // final FileIconExporter x = FileIconExporter.getInstance();
        // final BufferedImage b =
        // FileIconExporter.getIcon("C:\\Users\\thomas\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe",
        // 1027688, 512, 512);
        // // final BufferedImage b = x.export("html");
        // Dialog.getInstance().showConfirmDialog(0, "title", "icon", new
        // ImageIcon(b), null, null);
        // } catch (final DialogClosedException e) {
        // Log.exception(Level.WARNING, e);
        //
        // } catch (final DialogCanceledException e) {
        // Log.exception(Level.WARNING, e);
        //
        // } catch (final Throwable e) {
        // Log.exception(Level.WARNING, e);
        //
        // }

    }
}
