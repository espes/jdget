//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.appwork.utils.svn;

import org.appwork.utils.logging.Log;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNInfo;

public class InfoEventHandler implements ISVNInfoHandler {

    public void handleInfo(SVNInfo info) {
        Log.L.fine("-----------------INFO-----------------");
        Log.L.fine("Local Path: " + info.getPath());
        Log.L.fine("URL: " + info.getURL());

        if (info.isRemote() && info.getRepositoryRootURL() != null) {
            Log.L.fine("Repository Root URL: " + info.getRepositoryRootURL());
        }

        if (info.getRepositoryUUID() != null) {
            Log.L.fine("Repository UUID: " + info.getRepositoryUUID());
        }

        Log.L.fine("Revision: " + info.getRevision().getNumber());
        Log.L.fine("Node Kind: " + info.getKind().toString());

        if (!info.isRemote()) {
            Log.L.fine("Schedule: " + (info.getSchedule() != null ? info.getSchedule() : "normal"));
        }

        Log.L.fine("Last Changed Author: " + info.getAuthor());
        Log.L.fine("Last Changed Revision: " + info.getCommittedRevision().getNumber());
        Log.L.fine("Last Changed Date: " + info.getCommittedDate());

        if (info.getPropTime() != null) {
            Log.L.fine("Properties Last Updated: " + info.getPropTime());
        }

        if (info.getKind() == SVNNodeKind.FILE && info.getChecksum() != null) {
            if (info.getTextTime() != null) {
                Log.L.fine("Text Last Updated: " + info.getTextTime());
            }
            Log.L.fine("Checksum: " + info.getChecksum());
        }

        if (info.getLock() != null) {
            if (info.getLock().getID() != null) {
                Log.L.fine("Lock Token: " + info.getLock().getID());
            }

            Log.L.fine("Lock Owner: " + info.getLock().getOwner());
            Log.L.fine("Lock Created: " + info.getLock().getCreationDate());

            if (info.getLock().getExpirationDate() != null) {
                Log.L.fine("Lock Expires: " + info.getLock().getExpirationDate());
            }

            if (info.getLock().getComment() != null) {
                Log.L.fine("Lock Comment: " + info.getLock().getComment());
            }
        }
    }
}
