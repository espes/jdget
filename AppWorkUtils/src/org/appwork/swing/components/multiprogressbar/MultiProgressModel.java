/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.swing.components.multiprogressbar
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.swing.components.multiprogressbar;

/**
 * @author Thomas
 * 
 */
public class MultiProgressModel {

    private long    maximum;
    private Range[] ranges;

    public long getMaximum() {
        return maximum;
    }

    public void setMaximum(long maximum) {
        this.maximum = maximum;
        getEventSender().fireEvent(event);
    }

    public Range[] getRanges() {
        return ranges;
    }

    public void setRanges(Range[] ranges) {
        this.ranges = ranges;
        getEventSender().fireEvent(event);
    }

    private MultiProgressModelEvent       event;
    private MultiProgressModelEventSender eventSender;

    /**
     * @param max
     */
    public MultiProgressModel(long max, Range... ranges) {
        event = new MultiProgressModelEvent(this);
        eventSender = new MultiProgressModelEventSender();
        this.maximum = max;
        this.ranges = ranges;
    }

    public MultiProgressModelEventSender getEventSender() {
        return eventSender;
    }

}
