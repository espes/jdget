//jDownloader - Downloadmanager
//Copyright (C) 2011  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "deleteme" }, urls = { "REGEXNOTUSEDHASHDASHDASHDHAahahahahdahsdyayah" }, flags = { 0 })
public class HideMyLinksNet extends SflnkgNt {

    // TODO: DELETE WHEN JD2 goes stable.

    public HideMyLinksNet(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final GeneralSafelinkingHandling gsh = new GeneralSafelinkingHandling(br, param, getHost());
        gsh.startUp();
        try {
            gsh.decrypt();
        } catch (final DecrypterException e) {
            final String errormessage = e.getMessage();
            if ("offline".equals(errormessage)) { return decryptedLinks; }
            throw e;
        }
        decryptedLinks = gsh.getDecryptedLinks();

        return decryptedLinks;
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return false;
    }

}