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
public interface ShutdownVetoListener {
    /**
     * Step 2a:<br>
     * Informs listener, that shutdown will be done for sure now. Shutdown will
     * happen immediatelly after this call
     * @param silent TODO
     */
    public void onShutdown(boolean silent);

    /**
     * step 2b: If one or more listeners in step 1 answered with true(veto) all
     * listeners will be informed afterwards that shutdown has been canceled
     * 
     * @param vetos
     */
    public void onShutdownVeto(ShutdownVetoException[] shutdownVetoExceptions);

    /**
     * step 1b:<br>
     * Application requests shutdown. throws ShutdownVetoException if shutdown
     * currently not possible/wanted
     * 
     * @return
     * @throws ShutdownVetoException
     */
    public void onShutdownVetoRequest(ShutdownVetoException[] shutdownVetoExceptions) throws ShutdownVetoException;

    /**
     * step 1a:<br>
     * Application requests shutdown. throws ShutdownVetoException if shutdown
     * currently not possible/wanted
     * 
     * @return
     * @throws ShutdownVetoException
     */
    public void onSilentShutdownVetoRequest(ShutdownVetoException[] shutdownVetoExceptions) throws ShutdownVetoException;

}
