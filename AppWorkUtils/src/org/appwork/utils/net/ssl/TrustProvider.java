/**
 * Copyright (c) 2009 - 2010 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.net.ssl
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ssl;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;

/**
 * This SSL Trustprovider accepts all ssl certificates
 * 
 * @author $Author: unknown$
 * 
 */
public final class TrustProvider extends Provider {

    /**
     * 
     */
    private static final long serialVersionUID = 3537609645240163218L;

    private final static String PROVIDER_NAME = "AppWTrust";

    private static String origAlgorithm;

    /**
     * @return the {@link TrustProvider#providerName}
     * @see TrustProvider#providerName
     */
    public static String getProviderName() {
        return PROVIDER_NAME;
    }

    /**
     * @return the {@link TrustProvider#providerDescription}
     * @see TrustProvider#providerDescription
     */
    public static String getProviderDescription() {
        return PROVIDER_DESCRIPTION;
    }

    private final static String PROVIDER_DESCRIPTION = "AppWork TrustProvider";

    public TrustProvider() {
        super(PROVIDER_NAME, 0.1D, PROVIDER_DESCRIPTION);
        init();

    }

    private void init() {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                put("TrustManagerFactory." + CryptServiceProvider.getAlgorithm(), CryptServiceProvider.class.getName());
                return null;
            }
        });
    }

    /**
     * Registers the Trustprovider
     */
    public static void register() {
        if (Security.getProvider(PROVIDER_NAME) == null) {
            // saves old status to be able to restore it
            Security.insertProviderAt(new TrustProvider(), 2);
            origAlgorithm = System.getProperty("ssl.TrustManagerFactory.algorithm");
            Security.setProperty("ssl.TrustManagerFactory.algorithm", CryptServiceProvider.getAlgorithm());
        }
    }

    /**
     * restores the old algorithm
     */
    public static void unRegister() {
        if (origAlgorithm != null) {
            Security.setProperty("ssl.TrustManagerFactory.algorithm", origAlgorithm);
            origAlgorithm = null;
            Security.removeProvider(getProviderName());

        }
    }

}
