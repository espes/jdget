package org.appwork.fileiconexport;

import java.awt.image.BufferedImage;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.GDI32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HBITMAP;
import com.sun.jna.platform.win32.WinDef.HDC;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinGDI;
import com.sun.jna.platform.win32.WinGDI.BITMAPINFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public class Paint {

    public static void main(final String[] args) {
        new Paint();
    }

    BufferedImage image;

    public Paint() {
        // final HWND hWnd = User32.INSTANCE.FindWindow(null, "Rechner");
        // this.image = this.capture(hWnd);
        //
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // this.pack();
        // this.setExtendedState(Frame.MAXIMIZED_BOTH);
        // this.setVisible(true);
    }

    public BufferedImage capture(final HWND hWnd) {

        final HDC hdcWindow = User32.INSTANCE.GetDC(hWnd);
        final HDC hdcMemDC = GDI32.INSTANCE.CreateCompatibleDC(hdcWindow);

        final RECT bounds = new RECT();
        User32Extra.INSTANCE.GetClientRect(hWnd, bounds);

        final int width = bounds.right - bounds.left;
        final int height = bounds.bottom - bounds.top;
        if (width * height <= 0) { return null; }
        final HBITMAP hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcWindow, width, height);

        final HANDLE hOld = GDI32.INSTANCE.SelectObject(hdcMemDC, hBitmap);
        GDI32Extra.INSTANCE.BitBlt(hdcMemDC, 0, 0, width, height, hdcWindow, 0, 0, WinGDIExtra.SRCCOPY);

        GDI32.INSTANCE.SelectObject(hdcMemDC, hOld);
        GDI32.INSTANCE.DeleteDC(hdcMemDC);

        final BITMAPINFO bmi = new BITMAPINFO();
        bmi.bmiHeader.biWidth = width;
        bmi.bmiHeader.biHeight = -height;
        bmi.bmiHeader.biPlanes = 1;
        bmi.bmiHeader.biBitCount = 32;
        bmi.bmiHeader.biCompression = WinGDI.BI_RGB;

        final Memory buffer = new Memory(width * height * 4);
        GDI32.INSTANCE.GetDIBits(hdcWindow, hBitmap, 0, height, buffer, bmi, WinGDI.DIB_RGB_COLORS);

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, buffer.getIntArray(0, width * height), 0, width);

        GDI32.INSTANCE.DeleteObject(hBitmap);
        User32.INSTANCE.ReleaseDC(hWnd, hdcWindow);

        return image;

    }

}