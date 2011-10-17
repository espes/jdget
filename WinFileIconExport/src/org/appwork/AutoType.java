package org.appwork;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import org.appwork.utils.logging.Log;
import org.appwork.utils.os.CrossSystem;

public class AutoType {
    public static void main(final String[] args) {
        while (true) {
            try {
                Thread.sleep(5000);

                java.awt.Robot r = null;

                r = new java.awt.Robot();
                CrossSystem.openURL("http://127.0.0.0");
                Thread.sleep(500);
                AutoType.press(r, KeyEvent.VK_TAB);

                AutoType.press(r, KeyEvent.VK_TAB);

                AutoType.press(r, KeyEvent.VK_TAB);

                AutoType.press(r, KeyEvent.VK_H);
                AutoType.press(r, KeyEvent.VK_A);
                AutoType.press(r, KeyEvent.VK_L);
                AutoType.press(r, KeyEvent.VK_L);
                AutoType.press(r, KeyEvent.VK_O);
                AutoType.press(r, KeyEvent.VK_ENTER);
                return;
            } catch (final AWTException e) {
                e.printStackTrace();
            } catch (final InterruptedException e) {
                Log.exception(Level.WARNING, e);

            }
        }
    }

    private static void press(final Robot r, final int vkTab) {
        r.keyPress(vkTab);
        r.keyRelease(vkTab);
    }
}
