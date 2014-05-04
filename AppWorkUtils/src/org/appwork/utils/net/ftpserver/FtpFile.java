/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.net.ftpserver
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.net.ftpserver;

/**
 * @author thomas
 * 
 */
public class FtpFile {

    protected String      name;
    private final long    size;
    protected long        lastModified;
    private final boolean isDirectory;
    private String        owner = "unknown";

    private String        group = "unknown";

    /**
     * @param name
     * @param length
     * @param directory
     */
    public FtpFile(final String name, final long length, final boolean directory, final long lastMod) {
        this.name = name;
        this.size = length;
        this.isDirectory = directory;
        this.lastModified = lastMod;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return this.group;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public String getName() {
        return this.name;
    }

    public String getOwner() {
        return this.owner;
    }

    public long getSize() {
        return this.size;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner(final String owner) {
        this.owner = owner;
    }

}
