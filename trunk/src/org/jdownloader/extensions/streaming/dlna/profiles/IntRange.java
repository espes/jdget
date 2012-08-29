package org.jdownloader.extensions.streaming.dlna.profiles;

public class IntRange {

    private final int min;

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    private final int max;

    public IntRange(int value) {
        this(value, value);
    }

    public String toString() {
        return min + "-" + max;
    }

    public IntRange(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public boolean contains(int bitrate) {
        return bitrate >= min && bitrate <= max;
    }

}
