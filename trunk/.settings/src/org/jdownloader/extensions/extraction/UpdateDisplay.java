//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
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

package org.jdownloader.extensions.extraction;

import java.util.TimerTask;

/**
 * Is a {@link TimerTask} for updating the unpacking process. Will be executed
 * every second.
 * 
 * @author botzi
 * 
 */
class UpdateDisplay extends TimerTask {
    private ExtractionEvent event;

    UpdateDisplay(ExtractionController con) {
        event = new ExtractionEvent(con, ExtractionEvent.Type.EXTRACTING);
    }

    @Override
    public void run() {
        ExtractionExtension.getIntance().fireEvent(event);
    }
}