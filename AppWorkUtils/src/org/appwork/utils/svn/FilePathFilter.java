/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.utils.svn
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.utils.svn;

import org.tmatesoft.svn.core.SVNDirEntry;

/**
 * @author Thomas
 * 
 */
public interface FilePathFilter {

    public boolean accept(SVNDirEntry path);
}
