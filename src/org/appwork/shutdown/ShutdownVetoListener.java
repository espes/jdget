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

import java.util.ArrayList;

/**
 * @author thomas
 * 
 */
public interface ShutdownVetoListener {
    /**
     * Step 2a:<br>
     * Informs listener, that shutdown will be done for sure now. Shutdown will
     * happen immediatelly after this call
     */
    public void onShutdown();

    /**
     * step 1:<br>
     * Application requests shutdown. listeners may answer with true to avoid
     * shutdown.
     * 
     * @return
     * @throws ShutdownVetoException
     */
    public boolean onShutdownRequest() throws ShutdownVetoException;

    /**
     * step 2b: If one or more listeners in steo a answered with true(veto) all
     * listeners will be informed afterwards that shutdown has been canceled
     * 
     * @param vetos
     */
    public void onShutdownVeto(ArrayList<ShutdownVetoException> vetos);
}
