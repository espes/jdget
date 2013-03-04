/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.logging2
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.logging2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LogSink extends Logger {

    protected java.util.List<WeakReference<LogSource>> logSources     = new ArrayList<WeakReference<LogSource>>();
    protected FileHandler                              fileHandler    = null;
    protected ConsoleHandler                           consoleHandler = null;
    protected Logger                                   parent         = null;

    protected LogSink(final String name) {
        this(name, (String) null);
        this.setLevel(Level.ALL);
    }

    protected LogSink(final String name, final String resourceBundleName) {
        super(name, resourceBundleName);
    }

    @Override
    public void addHandler(final Handler handler) throws SecurityException {
        if (handler == null) { return; }
        super.addHandler(handler);
        if (this.fileHandler == null && handler instanceof FileHandler) {
            this.fileHandler = (FileHandler) handler;
        } else if (this.consoleHandler == null && handler instanceof ConsoleHandler) {
            this.consoleHandler = (ConsoleHandler) handler;
            final java.util.List<LogSource> sources = this.getLogSources();
            for (final LogSource source : sources) {
                source.removeHandler(this.consoleHandler);
                source.addHandler(this.consoleHandler);
            }
        }
    }

    protected void addLogSource(final LogSource source) {
        if (source == null) { return; }
        synchronized (this.logSources) {
            this.logSources.add(new WeakReference<LogSource>(source));
            source.setParent(this);
            if (this.consoleHandler != null) {
                source.removeHandler(this.consoleHandler);
                source.addHandler(this.consoleHandler);
            }
        }
    }

    protected synchronized void close() {
        try {
            this.flushSources(true);
            if (this.fileHandler != null) {
                super.removeHandler(this.fileHandler);
            }
        } finally {
            try {
                this.fileHandler.close();
            } catch (final Throwable e) {
            } finally {
                this.fileHandler = null;
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            this.close();
        } finally {
            super.finalize();
        }
    }

    protected void flushSources(final boolean finalFlush) {
        for (final LogSource source : this.getLogSources()) {
            if (source.isAllowTimeoutFlush() || finalFlush && source.isFlushOnFinalize()) {
                source.flush();
            }
        }
    }

    protected java.util.List<LogSource> getLogSources() {
        final java.util.List<LogSource> sources = new ArrayList<LogSource>();
        synchronized (this.logSources) {
            final Iterator<WeakReference<LogSource>> it = this.logSources.iterator();
            while (it.hasNext()) {
                final WeakReference<LogSource> next = it.next();
                final LogSource item = next.get();
                if (item == null || item.isClosed()) {
                    it.remove();
                    continue;
                } else {
                    sources.add(item);
                }
            }
        }
        return sources;
    }

    @Override
    public Logger getParent() {
        return this.parent;
    }

    protected boolean hasLogSources() {
        return this.getLogSources().size() > 0;
    }

    @Override
    public void removeHandler(final Handler handler) throws SecurityException {
        if (handler == null) { return; }
        super.removeHandler(handler);
        if (this.fileHandler != null && this.fileHandler == handler) {
            this.close();
        } else if (this.consoleHandler != null && handler == this.consoleHandler) {
            final java.util.List<LogSource> sources = this.getLogSources();
            for (final LogSource source : sources) {
                source.removeHandler(this.consoleHandler);
            }
            this.consoleHandler = null;
        }
    }

    @Override
    public void setParent(final Logger parent) {
        this.parent = parent;
    }

}
