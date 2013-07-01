/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.shutdown
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.shutdown;

/**
 * @author thomas
 * 
 */
public class ShutdownRunableEvent extends ShutdownEvent {

    private final Runnable runnable;

    /**
     * @param singleAppInstance
     */
    public ShutdownRunableEvent(final Runnable runnable) {
        this.runnable = runnable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.appwork.shutdown.ShutdownEvent#run()
     */
    @Override
    public void onShutdown(final ShutdownRequest shutdownRequest) {
        runnable.run();
    }

}
