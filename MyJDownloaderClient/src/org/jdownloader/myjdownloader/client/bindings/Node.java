package org.jdownloader.myjdownloader.client.bindings;

public interface Node {
    public abstract void setEnabled(final boolean enabled);

    public abstract boolean isEnabled();

    public abstract void setComment(final String comment);

    public abstract String getComment();

    public abstract void setBytesTotal(final long size);

    public abstract long getBytesTotal();

    public abstract void setName(final String name);

    public abstract long getUuid();

    public abstract String getName();

    public abstract void setUuid(final long uuid);

}
