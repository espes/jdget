package org.appwork.utils.svn;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.appwork.utils.Application;
import org.appwork.utils.Files;
import org.appwork.utils.IO;
import org.appwork.utils.logging.Log;
import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.ISVNReporterBaton;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.wc.ISVNCommitParameters;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

public class Subversion implements ISVNEventHandler {

    /**
     * checks wether logins are correct or not
     * 
     * @param url
     * @param user
     * @param pass
     * @return
     */
    public static boolean checkLogin(final String url, final String user, final String pass) {
        Subversion subversion = null;
        try {
            subversion = new Subversion(url, user, pass);
            return true;
        } catch (final SVNException e) {
        } finally {
            try {
                subversion.dispose();
            } catch (final Throwable e) {
            }
        }
        return false;
    }

    private SVNRepository             repository;
    private SVNURL                    svnurl;
    private ISVNAuthenticationManager authManager;
    private SVNClientManager          clientManager;
    private SVNUpdateClient           updateClient;
    private SVNCommitClient           commitClient;

    private SVNWCClient               wcClient;
    private SVNStatusClient           statusClient;

    public Subversion() {
    }

    public Subversion(final String url) throws SVNException {
        try {
            this.setupType(url);
            this.checkRoot();
        } catch (final SVNException e) {
            this.dispose();
            throw e;
        }
    }

    public Subversion(final String url, final String user, final String pass) throws SVNException {
        try {
            this.setupType(url);
            this.authManager = SVNWCUtil.createDefaultAuthenticationManager(user, pass);

            ((DefaultSVNAuthenticationManager) this.authManager).setAuthenticationForced(true);
            this.repository.setAuthenticationManager(this.authManager);

            this.checkRoot();

        } catch (final SVNException e) {
            this.dispose();
            throw e;
        }
    }

    /**
     * WCClient
     */
    @Override
    public void checkCancelled() throws SVNCancelException {
    }

    public long checkout(final File file, SVNRevision revision, final SVNDepth i) throws SVNException {

        file.mkdirs();

        final SVNUpdateClient updateClient = this.getUpdateClient();

        updateClient.setIgnoreExternals(false);
        if (revision == null) {
            revision = SVNRevision.HEAD;
        }

        return updateClient.doCheckout(this.svnurl, file, revision, revision, i, true);
    }

    private void checkRoot() throws SVNException {
        final SVNNodeKind nodeKind = this.repository.checkPath("", -1);
        if (nodeKind == SVNNodeKind.NONE) {
            final SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "No entry at URL ''{0}''", this.svnurl);
            throw new SVNException(err);
        } else if (nodeKind == SVNNodeKind.FILE) {
            final SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN, "Entry at URL ''{0}'' is a file while directory was expected", this.svnurl);
            throw new SVNException(err);
        }
    }

    /**
     * Cleans up the file or doirectory
     * 
     * @param dstPath
     * @param deleteWCProperties
     * @throws SVNException
     */
    public void cleanUp(final File dstPath, final boolean deleteWCProperties) throws SVNException {
        this.getWCClient().doCleanup(dstPath, deleteWCProperties);
    }

    /**
     * Commits the wholepath and KEEPS locks
     * 
     * @param dstPath
     * @param message
     * @return
     * @throws SVNException
     */
    public SVNCommitInfo commit(final File dstPath, final String message) throws SVNException {
        this.getWCClient().doAdd(dstPath, true, false, true, SVNDepth.INFINITY, false, false);
        Log.L.finer("Create CommitPacket");
        final SVNCommitPacket packet = this.getCommitClient().doCollectCommitItems(new File[] { dstPath }, false, false, SVNDepth.INFINITY, null);
        Log.L.finer("Transfer Package");
        return this.getCommitClient().doCommit(packet, true, false, message, null);

    }

    public void dispose() {
        try {
            this.repository.closeSession();
        } catch (final Throwable e) {
        }
        try {
            this.getClientManager().dispose();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(final String url, final File resource, final SVNRevision head) throws SVNException {
        this.getUpdateClient().doExport(SVNURL.parseURIDecoded(url), resource, head, head, null, true, null);

    }

    public long export(final File file) throws SVNException, IOException {

        Files.deleteRecursiv(file);
        file.mkdirs();

        final ISVNEditor exportEditor = new ExportEditor(file);
        final long rev = this.latestRevision();
        final ISVNReporterBaton reporterBaton = new ExportReporterBaton(rev);

        this.repository.update(rev, null, true, reporterBaton, exportEditor);

        return rev;
    }

    /**
     * Returns all changesets between revision start and end
     * 
     * @param start
     * @param end
     * @return
     * @throws SVNException
     */
    @SuppressWarnings("unchecked")
    public java.util.List<SVNLogEntry> getChangeset(final long start, final long end) throws SVNException {
        final Collection<SVNLogEntry> log = this.repository.log(new String[] { "" }, null, start, end, true, true);

        final java.util.List<SVNLogEntry> list = new ArrayList<SVNLogEntry>();
        list.addAll(log);
        return list;
    }

    private synchronized SVNClientManager getClientManager() {
        if (this.clientManager == null) {
            DefaultSVNOptions options = new DefaultSVNOptions(null, true) {
                private String[] ignorePatterns;
                {
                    ignorePatterns = new String[] {};
                }

                @Override
                public String[] getIgnorePatterns() {

                    return ignorePatterns;
                }

            };
            options.setIgnorePatterns(null);
            this.clientManager = SVNClientManager.newInstance(options, this.authManager);
        }
        return this.clientManager;
    }

    public SVNCommitClient getCommitClient() {

        if (this.commitClient == null) {
            this.commitClient = this.getClientManager().getCommitClient();
            this.commitClient.setEventHandler(this);
            this.commitClient.setCommitParameters(new ISVNCommitParameters() {

                @Override
                public boolean onDirectoryDeletion(final File directory) {
                    return false;
                }

                @Override
                public boolean onFileDeletion(final File file) {
                    return false;
                }

                @Override
                public Action onMissingDirectory(final File file) {
                    return ISVNCommitParameters.DELETE;
                }

                @Override
                public Action onMissingFile(final File file) {
                    return ISVNCommitParameters.DELETE;
                }
            });
        }
        return this.commitClient;

    }

    /**
     * Returns an ArrayLIst with Info for all files found in file.
     * 
     * @param file
     * @return
     */
    public java.util.List<SVNInfo> getInfo(final File file) {
        final java.util.List<SVNInfo> ret = new ArrayList<SVNInfo>();
        try {
            this.getWCClient().doInfo(file, SVNRevision.UNDEFINED, SVNRevision.WORKING, SVNDepth.getInfinityOrEmptyDepth(true), null, new ISVNInfoHandler() {

                @Override
                public void handleInfo(final SVNInfo info) {
                    ret.add(info);
                }

            });
        } catch (final SVNException e) {
            e.printStackTrace();
        }
        return ret;

    }

    public long getRemoteRevision(final String resource) throws SVNException {

        final SVNDirEntry de = this.getRepository().getDir(resource, -1, false, null);
        return de.getRevision();

    }

    /**
     * Return repo for external actions
     * 
     * @return
     */
    public SVNRepository getRepository() {
        return this.repository;
    }

    public long getRevisionNoException(final File resource) throws SVNException {

        try {
            return getRevision(resource);
        } catch (final SVNException e) {
            Log.exception(e);
        }
        return -1;

    }

    public long getRevision(final File resource) throws SVNException {
        final long[] ret = new long[] { -1 };
        this.getWCClient().doInfo(resource, SVNRevision.UNDEFINED, SVNRevision.WORKING, SVNDepth.EMPTY, null, new ISVNInfoHandler() {

            @Override
            public void handleInfo(final SVNInfo info) {
                final long rev = info.getCommittedRevision().getNumber();
                if (rev > ret[0]) {
                    ret[0] = rev;
                }

            }

        });
        return ret[0];
    }

    private SVNStatusClient getStatusClient() {

        if (this.statusClient == null) {
            this.statusClient = this.getClientManager().getStatusClient();
            this.statusClient.setEventHandler(this);
        }

        return this.statusClient;

    }

    public SVNUpdateClient getUpdateClient() {
        if (this.updateClient == null) {
            this.updateClient = this.getClientManager().getUpdateClient();
            this.updateClient.setEventHandler(this);
        }

        return this.updateClient;
    }

    public SVNWCClient getWCClient() {
        if (this.wcClient == null) {
            this.wcClient = this.getClientManager().getWCClient();
            this.wcClient.setEventHandler(this);
        }

        return this.wcClient;
    }

    /**
     * WCClientHanlder
     * 
     * @param event
     * @param progress
     * @throws SVNException
     */
    @Override
    public void handleEvent(final SVNEvent event, final double progress) throws SVNException {
        /* WCCLient */
        final String nullString = " ";
        final SVNEventAction action = event.getAction();
        String pathChangeType = nullString;
        if (action == SVNEventAction.ADD) {
            /*
             * The item is scheduled for addition.
             */
            Log.L.fine("A     " + event.getFile());
            return;
        } else if (action == SVNEventAction.COPY) {
            /*
             * The item is scheduled for addition with history (copied, in other
             * words).
             */
            Log.L.fine("A  +  " + event.getFile());
            return;
        } else if (action == SVNEventAction.DELETE) {
            /*
             * The item is scheduled for deletion.
             */
            Log.L.fine("D     " + event.getFile());
            return;
        } else if (action == SVNEventAction.LOCKED) {
            /*
             * The item is locked.
             */
            Log.L.fine("L     " + event.getFile());
            return;
        } else if (action == SVNEventAction.LOCK_FAILED) {
            /*
             * Locking operation failed.
             */
            Log.L.fine("failed to lock    " + event.getFile());
            return;
        }

        /* Updatehandler */

        if (action == SVNEventAction.UPDATE_ADD) {
            /*
             * the item was added
             */
            pathChangeType = "A";
        } else if (action == SVNEventAction.UPDATE_DELETE) {
            /*
             * the item was deleted
             */
            pathChangeType = "D";
        } else if (action == SVNEventAction.UPDATE_UPDATE) {
            /*
             * Find out in details what state the item is (after having been
             * updated).
             * 
             * Gets the status of file/directory item contents. It is
             * SVNStatusType who contains information on the state of an item.
             */
            final SVNStatusType contentsStatus = event.getContentsStatus();
            if (contentsStatus == SVNStatusType.CHANGED) {
                /*
                 * the item was modified in the repository (got the changes from
                 * the repository
                 */
                pathChangeType = "U";
            } else if (contentsStatus == SVNStatusType.CONFLICTED) {
                /*
                 * The file item is in a state of Conflict. That is, changes
                 * received from the repository during an update, overlap with
                 * local changes the user has in his working copy.
                 */

                pathChangeType = "C";
            } else if (contentsStatus == SVNStatusType.MERGED) {
                /*
                 * The file item was merGed (those changes that came from the
                 * repository did not overlap local changes and were merged into
                 * the file).
                 */
                pathChangeType = "G";
            }
        } else if (action == SVNEventAction.UPDATE_EXTERNAL) {
            /*
             * for externals definitions
             */
            Log.L.fine("Fetching external item into '" + event.getFile().getAbsolutePath() + "'");
            Log.L.fine("External at revision " + event.getRevision());
            return;
        } else if (action == SVNEventAction.UPDATE_COMPLETED) {
            /*
             * Working copy update is completed. Prints out the revision.
             */
            Log.L.fine("At revision " + event.getRevision());
            return;
        }

        /*
         * Status of properties of an item. SVNStatusType also contains
         * information on the properties state.
         */
        final SVNStatusType propertiesStatus = event.getPropertiesStatus();
        String propertiesChangeType = nullString;
        if (propertiesStatus == SVNStatusType.CHANGED) {
            /*
             * Properties were updated.
             */
            propertiesChangeType = "U";
        } else if (propertiesStatus == SVNStatusType.CONFLICTED) {
            /*
             * Properties are in conflict with the repository.
             */
            propertiesChangeType = "C";
        } else if (propertiesStatus == SVNStatusType.MERGED) {
            /*
             * Properties that came from the repository were merged with the
             * local ones.
             */
            propertiesChangeType = "G";
        }

        /*
         * Gets the status of the lock.
         */
        String lockLabel = nullString;
        final SVNStatusType lockType = event.getLockStatus();

        if (lockType == SVNStatusType.LOCK_UNLOCKED) {
            /*
             * The lock is broken by someone.
             */
            lockLabel = "B";
        }
        if (pathChangeType != nullString || propertiesChangeType != nullString || lockLabel != nullString) {
            Log.L.fine(pathChangeType + propertiesChangeType + lockLabel + "       " + event.getFile());
        }

        /*
         * Comitghandler
         */

        if (action == SVNEventAction.COMMIT_MODIFIED) {
            Log.L.fine("Sending   " + event.getFile());
        } else if (action == SVNEventAction.COMMIT_DELETED) {
            Log.L.fine("Deleting   " + event.getFile());
        } else if (action == SVNEventAction.COMMIT_REPLACED) {
            Log.L.fine("Replacing   " + event.getFile());
        } else if (action == SVNEventAction.COMMIT_DELTA_SENT) {
            Log.L.fine("Transmitting file data....");
        } else if (action == SVNEventAction.COMMIT_ADDED) {
            /*
             * Gets the MIME-type of the item.
             */
            final String mimeType = event.getMimeType();
            if (SVNProperty.isBinaryMimeType(mimeType)) {
                /*
                 * If the item is a binary file
                 */
                Log.L.fine("Adding  (bin)  " + event.getFile());
            } else {
                Log.L.fine("Adding         " + event.getFile());
            }
        }

    }

    public long latestRevision() throws SVNException {

        return this.repository.getLatestRevision();
    }

    /**
     * @param filePathFilter
     * @return
     * @throws SVNException
     * @throws InterruptedException
     */
    public List<SVNDirEntry> listFiles(final FilePathFilter filePathFilter, final String path) throws SVNException, InterruptedException {
        final java.util.List<SVNDirEntry> ret = new ArrayList<SVNDirEntry>();
        final Collection entries = this.repository.getDir(path, -1, null, (Collection) null);
        final Iterator iterator = entries.iterator();
        while (iterator.hasNext()) {
            final SVNDirEntry entry = (SVNDirEntry) iterator.next();
            if (Thread.currentThread().isInterrupted()) { throw new InterruptedException(); }
            if (filePathFilter.accept(entry)) {
                entry.setRelativePath((path.equals("") ? "" : path + "/") + entry.getName());
                ret.add(entry);
                System.out.println("/" + (path.equals("") ? "" : path + "/") + entry.getName() + " ( author: '" + entry.getAuthor() + "'; revision: " + entry.getRevision() + "; date: " + entry.getDate() + ")");

            }
            ;
            if (entry.getKind() == SVNNodeKind.DIR) {
                ret.addAll(this.listFiles(filePathFilter, path.equals("") ? entry.getName() : path + "/" + entry.getName()));
            }
        }
        return ret;
    }

    /**
     * Locks a file or directory as long as it it not locked by someone else
     * 
     * @param dstPath
     * @param message
     * @throws SVNException
     */
    public void lock(final File dstPath, final String message) throws SVNException {
        this.getWCClient().doLock(new File[] { dstPath }, false, message);
    }

    public void resolveConflictedFile(final SVNInfo info, final File file, final ResolveHandler handler) throws Exception {
        final String mine = "<<<<<<< .mine";
        final String delim = "=======";
        final String theirs = ">>>>>>> .r";
        String txt = IO.readFileToString(file);
        String pre, post;
        while (true) {
            int mineStart = txt.indexOf(mine);

            if (mineStart < 0) {
                break;
            }
            mineStart += mine.length();
            final int delimStart = txt.indexOf(delim, mineStart);
            final int theirsEnd = txt.indexOf(theirs, delimStart + delim.length());
            int end = theirsEnd + theirs.length();
            while (end < txt.length() && txt.charAt(end) != '\r' && txt.charAt(end) != '\n') {
                end++;
            }

            pre = txt.substring(0, mineStart - mine.length());
            post = txt.substring(end);
            while (pre.endsWith("\r") || pre.endsWith("\n")) {
                pre = pre.substring(0, pre.length() - 1);
            }
            while (post.startsWith("\r") || post.startsWith("\n")) {
                post = post.substring(1);
            }
            pre += "\r\n";
            post = "\r\n" + post;
            if (pre.trim().length() == 0) {
                pre = pre.trim();
            }
            if (post.trim().length() == 0) {
                post = post.trim();
            }
            final String solve = handler.resolveConflict(info, file, txt, mineStart, delimStart, delimStart + delim.length(), theirsEnd);
            if (solve == null) { throw new Exception("Could not resolve"); }
            txt = pre + solve.trim() + post;
        }
        file.delete();
        IO.writeStringToFile(file, txt);

    }

    public void resolveConflicts(final File file, final ResolveHandler handler) throws SVNException {

        this.getWCClient().doInfo(file, SVNRevision.UNDEFINED, SVNRevision.WORKING, SVNDepth.getInfinityOrEmptyDepth(true), null, new ISVNInfoHandler() {

            @Override
            public void handleInfo(final SVNInfo info) {
                final File file = info.getConflictWrkFile();
                if (file != null) {
                    try {
                        Subversion.this.resolveConflictedFile(info, info.getFile(), handler);
                        Subversion.this.getWCClient().doResolve(info.getFile(), SVNDepth.INFINITY, null);
                        Log.L.fine(file + " resolved");
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * Reverts the file or directory
     * 
     * @param dstPath
     * @throws SVNException
     */
    public void revert(final File dstPath) throws SVNException {
        try {

            this.getWCClient().doRevert(new File[] { dstPath }, SVNDepth.INFINITY, null);
        } catch (final Exception e) {
            e.printStackTrace();
            this.cleanUp(dstPath, false);
        }
    }

    private void setupType(final String url) throws SVNException {
        this.svnurl = SVNURL.parseURIDecoded(url);

        if (url.startsWith("http")) {
            DAVRepositoryFactory.setup();
            this.repository = SVNRepositoryFactory.create(this.svnurl);
        } else if (url.startsWith("svn")) {
            SVNRepositoryFactoryImpl.setup();
            this.repository = SVNRepositoryFactory.create(this.svnurl);
        } else {
            FSRepositoryFactory.setup();
            this.repository = SVNRepositoryFactory.create(this.svnurl);
        }
    }

    public void showInfo(final File wcPath, SVNRevision revision, final boolean isRecursive) throws SVNException {
        if (revision == null) {
            revision = SVNRevision.HEAD;
        }

        this.getWCClient().doInfo(wcPath, SVNRevision.UNDEFINED, revision, SVNDepth.getInfinityOrEmptyDepth(isRecursive), null, new InfoEventHandler());
    }

    public void showStatus(final File wcPath, final boolean isRecursive, final boolean isRemote, final boolean isReportAll, final boolean isIncludeIgnored, final boolean isCollectParentExternals) throws SVNException {
        this.getClientManager().getStatusClient().doStatus(wcPath, SVNRevision.HEAD, SVNDepth.fromRecurse(isRecursive), isRemote, isReportAll, isIncludeIgnored, isCollectParentExternals, new StatusEventHandler(isRemote), null);
    }

    /**
     * Unlocks this file only if it is locked by you
     * 
     * @param dstPath
     * @param message
     * @throws SVNException
     */
    public void unlock(final File dstPath) throws SVNException {
        this.getWCClient().doUnlock(new File[] { dstPath }, false);
    }

    /**
     * Updates the repo to file. if there is no repo at file, a checkout is
     * performed
     * 
     * @param file
     * @param revision
     * @throws SVNException
     * @return revision
     */
    public long update(final File file, final SVNRevision revision) throws SVNException {
        return this.update(file, revision, SVNDepth.INFINITY);

    }

    public long update(final File file, SVNRevision revision, SVNDepth i) throws SVNException {
        if (i == null) {
            i = SVNDepth.INFINITY;
        }
        // JDIO.removeDirectoryOrFile(file);
        file.mkdirs();

        final SVNUpdateClient updateClient = this.getUpdateClient();

        updateClient.setIgnoreExternals(false);
        if (revision == null) {
            revision = SVNRevision.HEAD;
        }

        try {

            Log.L.info("SVN Update at " + file + " to Revision " + revision + " depths:" + i + "  " + this.svnurl);
            return updateClient.doUpdate(file, revision, i, false, true);
         
        } catch (final Exception e) {
            Log.L.info(e.getMessage());
            Log.L.info("SVN Checkout at " + file + "  " + this.svnurl);
            return updateClient.doCheckout(this.svnurl, file, revision, revision, i, true);

        } finally {
            Log.L.info("SVN Update finished");
        }
    }

    /**
     * @param string
     * @param string2
     * @param bs
     * @throws IOException
     * @throws SVNException
     */
    public void write(final String path, final String commitmessage, final byte[] content) throws SVNException, IOException {
        this.write(path, commitmessage, new ByteArrayInputStream(content));

    }

    public SVNCommitInfo write(final String path, final String commitmessage, final ByteArrayInputStream is) throws SVNException, IOException {

        final File file = new File(Application.getResource("tmp/svnwrite_" + System.currentTimeMillis()), path);
        this.downloadFile(this.svnurl + (this.svnurl.toString().endsWith("/") ? "" : "/") + path, file, SVNRevision.HEAD);

        final SVNDeltaGenerator generator = new SVNDeltaGenerator();

        final ISVNEditor commitEditor = this.getRepository().getCommitEditor(commitmessage, null);
        try {
            commitEditor.openRoot(-1);
            commitEditor.openFile(path, -1);
            commitEditor.applyTextDelta(path, null);
            final String checksum = generator.sendDelta(path, is, commitEditor, true);
            commitEditor.closeFile(path, checksum);
            commitEditor.closeDir();
            final SVNCommitInfo info = commitEditor.closeEdit();
            return info;
        } finally {
            if (commitEditor != null) {
                commitEditor.abortEdit();
            }

            Files.deleteRecursiv(file.getParentFile());

        }

    }

}
