/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.appwork.utils.logging.Log;

/**
 * tiny animation class
 * 
 * @author thomas
 * 
 */
public class SwingAnimator {

    public class AnimatorListener implements ActionListener {
        private int            step;
        private final long     startTime;
        private final Getter   getter;
        private final Setter   setter;
        private final Runnable finalizer;
        private final int      startValue;
        private final int      steps;

        protected AnimatorListener(final Getter getter, final Setter setter, final Runnable finalizer) {
            this.step = 0;
            this.startTime = System.currentTimeMillis();
            this.getter = getter;
            this.setter = setter;
            this.finalizer = finalizer;
            this.steps = SwingAnimator.this.duration / (1000 / SwingAnimator.this.fps);

            this.startValue = getter.getStartValue();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(final ActionEvent e) {
            try {
                this.step++;
                final int cu = (int) (System.currentTimeMillis() - this.startTime);

                if (this.step <= this.steps) {

                    this.setter.set(this.getter.get(this, cu));
                } else {
                    SwingAnimator.this.timer.stop();
                    SwingAnimator.this.timer = null;
                    this.finalizer.run();

                }
            } catch (final Throwable t) {
                Log.exception(t);
                SwingAnimator.this.timer.stop();
                SwingAnimator.this.timer = null;
                this.finalizer.run();
            }

        }

        public int getDuration() {
            return SwingAnimator.this.duration;
        }

        public long getStartTime() {
            return this.startTime;
        }

        public int getStartValue() {
            return this.startValue;
        }

        public int getStep() {
            return this.step;
        }

        public int getSteps() {
            return this.steps;
        }

    }

    public static abstract class Getter {
        /**
         * @param animatorListener
         * @param cu
         * @return
         */
        public abstract int get(final AnimatorListener animatorListener, final int cu);

        /**
         * @return
         */
        public abstract int getStartValue();
    }

    public static abstract class Setter {
        public abstract void set(int i);
    }

    private final int duration;

    private final int fps;

    private Timer     timer;

    /**
     * @param i
     * @param j
     */
    public SwingAnimator(final int duratation, final int fps) {
        this.duration = duratation;
        this.fps = fps;
    }

    /**
     * @param getter
     * @param setter
     * @param runnable
     */
    public synchronized void run(final Getter getter, final Setter setter, final Runnable finalizer) {
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = new Timer(1000 / this.fps, new AnimatorListener(getter, setter, finalizer));
        this.timer.setInitialDelay(0);
        this.timer.setRepeats(true);
        this.timer.start();
    }

}
