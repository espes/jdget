/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.remotecall.server
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.remotecall.server;

import org.appwork.storage.Storable;

/**
 * @author thomas
 * 
 */
public class Requestor implements Storable {
    private String serviceName;
    private String routine;
    private String parameter;

    public Requestor() {
        // required for Storable
    }

    /**
     * @param serviceName
     * @param routine
     * @param serialise
     */
    public Requestor(String serviceName, String routine, String parameter) {
       this.serviceName=serviceName;
       this.routine=routine;
       this.parameter=parameter;
    }

    /**
     * @param host
     */
    public Requestor(String host) {
       remoteAddress=host;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getRoutine() {
        return routine;
    }

    public void setRoutine(String routine) {
        this.routine = routine;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String toString() {
        return remoteAddress+"@"+serviceName+"/"+routine+"?"+parameter;
    }

    public String remoteAddress;

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

}
