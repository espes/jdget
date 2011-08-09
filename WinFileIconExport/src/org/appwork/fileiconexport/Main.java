package org.appwork.fileiconexport;

import java.awt.image.BufferedImage;
import java.util.logging.Level;

import javax.swing.ImageIcon;

import org.appwork.utils.logging.Log;
import org.appwork.utils.swing.dialog.Dialog;
import org.appwork.utils.swing.dialog.DialogCanceledException;
import org.appwork.utils.swing.dialog.DialogClosedException;

public class Main {

    public static void main(final String[] args) {
        // System.out.println(System.getenv("windir"));
        //
        // final HWND h = User32.INSTANCE.FindWindow(null, "Rechner");
        //
        // final WNDENUMPROC test = new WinUser.WNDENUMPROC() {
        // private int counter;
        //
        // {
        // this.counter = 0;
        // }
        //
        // @Override
        // public boolean callback(final HWND hWnd, final Pointer data) {
        // // TODO Auto-generated method stub
        // final char[] ch = new char[100];
        // User32.INSTANCE.GetClassName(hWnd, ch, ch.length);
        // System.out.println(new String(ch));
        // final BufferedImage sh = new Paint().capture(hWnd);
        // if (sh != null) {
        // final File saveto = Application.getResource("tmp/sh/");
        // saveto.mkdirs();
        // try {
        // final StringBuilder sb = new StringBuilder();
        // sb.append(new String(ch).trim());
        // sb.append("_");
        // sb.append(this.counter++);
        // sb.append(".png");
        // // final String filename = + "_" + this.counter++ +
        // // ".png";
        // final File dest = new File(saveto, sb.toString());
        // ImageIO.write(sh, "png", dest);
        // } catch (final IOException e) {
        // Log.exception(Level.WARNING, e);
        //
        // }
        // }
        // return true;
        // }
        //
        // };
        // User32.INSTANCE.EnumWindows(test, new Pointer(1l));

        try {
            final FileIconExporter x = FileIconExporter.getInstance();
            final BufferedImage b = FileIconExporter.getIcon("C:\\Users\\thomas\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe", 1, 64, 64);
            // final BufferedImage b = x.export("html");
            Dialog.getInstance().showConfirmDialog(0, "title", "icon", new ImageIcon(b), null, null);
        } catch (final DialogClosedException e) {
            Log.exception(Level.WARNING, e);

        } catch (final DialogCanceledException e) {
            Log.exception(Level.WARNING, e);

        } catch (final Throwable e) {
            Log.exception(Level.WARNING, e);

        }

    }
}
