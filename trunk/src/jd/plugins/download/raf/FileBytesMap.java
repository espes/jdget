package jd.plugins.download.raf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.appwork.storage.Storable;

public class FileBytesMap {
    
    private final static class FileBytesMapEntry {
        private final long    begin;
        private volatile long length;
        
        private FileBytesMapEntry(final long begin) {
            this(begin, 0);
        }
        
        private FileBytesMapEntry(final long begin, final long length) {
            if (length < 0) throw new IllegalArgumentException("length is negative");
            this.begin = begin;
            this.length = length;
        }
        
        /**
         * return the begin of the first byte(inclusive)
         * 
         * @return
         */
        private final long getBegin() {
            return begin;
        }
        
        /**
         * return the end of the last byte(inclusive)
         * 
         * @return
         */
        private final long getEnd() {
            return begin + length - 1;
        }
        
        /**
         * return the length of this entry
         * 
         * @return
         */
        private final long getLength() {
            return length;
        }
        
        private final void modifyLength(long length) {
            this.length = Math.max(0, this.length + length);
        }
        
        @Override
        public String toString() {
            return getBegin() + "-" + getEnd() + "=" + getLength();
        }
    }
    
    protected final ArrayList<FileBytesMapEntry>         fileBytesMapEntries = new ArrayList<FileBytesMapEntry>();
    protected final static Comparator<FileBytesMapEntry> sorter              = new Comparator<FileBytesMapEntry>() {
                                                                                 
                                                                                 private int compare(long x, long y) {
                                                                                     return (x < y) ? -1 : ((x == y) ? 0 : 1);
                                                                                 }
                                                                                 
                                                                                 @Override
                                                                                 public int compare(FileBytesMapEntry o1, FileBytesMapEntry o2) {
                                                                                     return compare(o1.getBegin(), o2.getBegin());
                                                                                 }
                                                                             };
    
    protected volatile long                              finalSize           = -1;
    protected volatile long                              markedBytes         = 0;
    
    public static class FileBytesMapViewInterfaceStorable implements Storable {
        protected long finalSize = -1;
        
        public long getFinalSize() {
            return finalSize;
        }
        
        public void setFinalSize(long finalSize) {
            this.finalSize = finalSize;
        }
        
        /**
         * return a list of begin/length entries of the map
         * 
         * @return
         */
        public List<Long[]> getMarkedAreas() {
            return markedAreas;
        }
        
        public void setMarkedAreas(List<Long[]> markedAreas) {
            this.markedAreas = markedAreas;
        }
        
        protected List<Long[]> markedAreas = null;
        
        private FileBytesMapViewInterfaceStorable(/* Storable */) {
        }
        
        public FileBytesMapViewInterfaceStorable(FileBytesMapViewInterface fileBytesMapInfo) {
            this.finalSize = fileBytesMapInfo.getFinalSize();
            markedAreas = new ArrayList<Long[]>(fileBytesMapInfo.getMarkedAreas().length);
            for (long[] markedArea : fileBytesMapInfo.getMarkedAreas()) {
                markedAreas.add(new Long[] { markedArea[0], markedArea[1] });
            }
        }
        
        public FileBytesMapViewInterfaceStorable(FileBytesMap fileBytesMap) {
            synchronized (fileBytesMap) {
                this.finalSize = fileBytesMap.getFinalSize();
                markedAreas = new ArrayList<Long[]>(fileBytesMap.fileBytesMapEntries.size());
                for (int index = 0; index < fileBytesMap.fileBytesMapEntries.size(); index++) {
                    FileBytesMapEntry fileBytesMapEntry = fileBytesMap.fileBytesMapEntries.get(index);
                    markedAreas.add(new Long[] { fileBytesMapEntry.getBegin(), fileBytesMapEntry.getLength() });
                }
            }
        }
        
    }
    
    public static class FileBytesMapView implements FileBytesMapViewInterface {
        protected final long finalSize;
        protected final long marked;
        protected final long size;
        
        /**
         * return a list of begin/length entries of the map
         * 
         * @return
         */
        @Override
        public long[][] getMarkedAreas() {
            return markedAreas;
        }
        
        protected final long[][] markedAreas;
        
        @Override
        public long getFinalSize() {
            return finalSize;
        }
        
        public long getSize() {
            return size;
        }
        
        public long getUnMarkedBytes() {
            return Math.max(0, getSize()) - getMarkedBytes();
        }
        
        public long getMarkedBytes() {
            return marked;
        }
        
        public FileBytesMapView(FileBytesMap fileBytesMap) {
            synchronized (fileBytesMap) {
                this.finalSize = fileBytesMap.getFinalSize();
                markedAreas = new long[fileBytesMap.fileBytesMapEntries.size()][2];
                for (int index = 0; index < fileBytesMap.fileBytesMapEntries.size(); index++) {
                    FileBytesMapEntry fileBytesMapEntry = fileBytesMap.fileBytesMapEntries.get(index);
                    markedAreas[index][0] = fileBytesMapEntry.getBegin();
                    markedAreas[index][1] = fileBytesMapEntry.getLength();
                }
                this.marked = fileBytesMap.getMarkedBytes();
                this.size = fileBytesMap.getSize();
            }
        }
    }
    
    public long getFinalSize() {
        return finalSize;
    }
    
    public synchronized void set(FileBytesMapViewInterface fileBytesMapInfo) {
        reset();
        setFinalSize(fileBytesMapInfo.getFinalSize());
        for (long[] markedArea : fileBytesMapInfo.getMarkedAreas()) {
            mark(markedArea[0], markedArea[1]);
        }
    }
    
    public void setFinalSize(long finalSize) {
        this.finalSize = Math.max(-1, finalSize);
    }
    
    public synchronized void resetMarkedBytesLive() {
        markedBytes = 0;
    }
    
    public synchronized long mark(long markedAreaBegin, long markedAreaLength) {
        if (markedAreaLength <= 0) throw new IllegalArgumentException("invalid length");
        long markedAreaEnd = markedAreaBegin + markedAreaLength - 1;
        for (int index = 0; index < fileBytesMapEntries.size(); index++) {
            FileBytesMapEntry fileBytesMapEntry = fileBytesMapEntries.get(index);
            long fileBytesMapEntryEnd = fileBytesMapEntry.getEnd();
            if ((markedAreaBegin >= fileBytesMapEntry.getBegin()) && (markedAreaBegin <= fileBytesMapEntryEnd)) {
                /* markedAreaBegin is inside fileBytesMapEntry */
                if (markedAreaEnd <= fileBytesMapEntryEnd) {
                    /* markedArea is completely within fileBytesMapEntry */
                    return -markedAreaLength;
                }
                long endOffset = markedAreaEnd - fileBytesMapEntryEnd;
                long lengthOffset = endOffset + 1;
                fileBytesMapEntry.modifyLength(lengthOffset);
                markedBytes += lengthOffset;
                final int nextIndex = index + 1;
                if (nextIndex < fileBytesMapEntries.size()) {
                    /* check overlap of next fileBytesMapEntry */
                    FileBytesMapEntry nextFileBytesMapEntry = fileBytesMapEntries.get(nextIndex);
                    fileBytesMapEntryEnd = fileBytesMapEntry.getEnd();
                    if (fileBytesMapEntryEnd >= nextFileBytesMapEntry.getBegin()) {
                        /* overlapping */
                        fileBytesMapEntries.remove(nextIndex);
                        lengthOffset = nextFileBytesMapEntry.getEnd() - fileBytesMapEntryEnd - nextFileBytesMapEntry.getLength();
                        if (lengthOffset <= 0) {
                            /* nextFileBytesMapEntry is completely within fileBytesMapEntry */
                            markedBytes -= nextFileBytesMapEntry.getLength();
                        } else {
                            markedBytes += lengthOffset;
                            fileBytesMapEntry.modifyLength(lengthOffset);
                        }
                        return lengthOffset;
                    }
                }
                return lengthOffset;
            } else if (markedAreaBegin == fileBytesMapEntry.getEnd() + 1) {
                /* markedAreaBegin continues fileBytesMapEntry */
                fileBytesMapEntry.modifyLength(markedAreaLength);
                markedBytes += markedAreaLength;
                final int nextIndex = index + 1;
                if (nextIndex < fileBytesMapEntries.size()) {
                    /* check overlap of next fileBytesMapEntry */
                    FileBytesMapEntry nextFileBytesMapEntry = fileBytesMapEntries.get(nextIndex);
                    fileBytesMapEntryEnd = fileBytesMapEntry.getEnd();
                    if (fileBytesMapEntryEnd >= nextFileBytesMapEntry.getBegin()) {
                        /* overlapping */
                        fileBytesMapEntries.remove(nextIndex);
                        long lengthOffset = nextFileBytesMapEntry.getEnd() - fileBytesMapEntryEnd - nextFileBytesMapEntry.getLength();
                        if (lengthOffset <= 0) {
                            /* nextFileBytesMapEntry is completely within fileBytesMapEntry */
                            markedBytes -= nextFileBytesMapEntry.getLength();
                        } else {
                            markedBytes += lengthOffset;
                            fileBytesMapEntry.modifyLength(lengthOffset);
                        }
                        return lengthOffset;
                    }
                }
                return markedAreaLength;
            }
        }
        fileBytesMapEntries.add(new FileBytesMapEntry(markedAreaBegin, markedAreaLength));
        markedBytes += markedAreaLength;
        Collections.sort(fileBytesMapEntries, sorter);
        return markedAreaLength;
    }
    
    public synchronized List<Long[]> getUnMarkedAreas() {
        ArrayList<Long[]> ret = new ArrayList<Long[]>();
        for (int index = 0; index < fileBytesMapEntries.size(); index++) {
            FileBytesMapEntry currentMapEntry = fileBytesMapEntries.get(index);
            final long unMarkedBegin = currentMapEntry.getEnd() + 1;
            if (index + 1 < fileBytesMapEntries.size()) {
                /* next entry does exist */
                FileBytesMapEntry nextMapEntry = fileBytesMapEntries.get(index + 1);
                ret.add(new Long[] { unMarkedBegin, nextMapEntry.getBegin() - 1 });
            } else {
                /* this is our last entry */
                long finalSize = getFinalSize();
                if (finalSize >= 0) {
                    if (unMarkedBegin < finalSize) {
                        ret.add(new Long[] { unMarkedBegin, finalSize - 1 });
                    }
                } else {
                    ret.add(new Long[] { unMarkedBegin, -1l });
                }
            }
        }
        if (fileBytesMapEntries.size() == 0) {
            /* nothing marked yet */
            long finalSize = getFinalSize();
            if (finalSize == 0) {
                return ret;
            } else if (finalSize > 0) {
                ret.add(new Long[] { 0l, finalSize - 1 });
            } else {
                ret.add(new Long[] { 0l, -1l });
            }
        }
        return ret;
    }
    
    public synchronized void reset() {
        fileBytesMapEntries.clear();
        markedBytes = 0;
        finalSize = 0;
    }
    
    /**
     * return known size of this map
     * 
     * @return
     */
    public synchronized long getSize() {
        long size = getFinalSize();
        if (size >= 0) return size;
        if (fileBytesMapEntries.size() > 0) {
            //
            long end = fileBytesMapEntries.get(fileBytesMapEntries.size() - 1).getEnd();
            /* array index starts at 0, end is last existing byte, so 0-end=size */
            return end + 1;
        }
        return -1;
    }
    
    /**
     * return size of unmarked area
     * 
     * @return
     */
    public synchronized long getUnMarkedBytes() {
        return Math.max(0, getSize()) - getMarkedBytes();
    }
    
    /**
     * return a "live" size marked area
     * 
     * @return
     */
    public long getMarkedBytesLive() {
        return markedBytes;
    }
    
    /**
     * return size of marked area
     * 
     * @return
     */
    public synchronized long getMarkedBytes() {
        long ret = 0;
        for (int index = 0; index < fileBytesMapEntries.size(); index++) {
            FileBytesMapEntry fileBytesMapEntry = fileBytesMapEntries.get(index);
            ret += fileBytesMapEntry.getLength();
        }
        return ret;
    }
}
