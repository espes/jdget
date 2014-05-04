package org.jdownloader.extensions.streaming.mediaarchive.prepare.ffmpeg;

import org.appwork.storage.Storable;
import org.appwork.utils.logging2.LogSource;
import org.jdownloader.extensions.streaming.mediaarchive.prepare.AudioStream;
import org.jdownloader.extensions.streaming.mediaarchive.prepare.VideoStream;
import org.jdownloader.logging.LogController;

public class Stream implements Storable {
    public Stream(/* Storable */) {

    }

    private static final LogSource LOGGER = LogController.getInstance().getLogger(Stream.class.getName());
    private int                    channels;
    private StreamTags             tags;

    public StreamTags getTags() {
        return tags;
    }

    public void setTags(StreamTags tags) {
        this.tags = tags;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    private String codec_name;
    private String codec_long_name;

    public String getCodec_long_name() {
        return codec_long_name;
    }

    public void setCodec_long_name(String codec_long_name) {
        this.codec_long_name = codec_long_name;
    }

    public String getCodec_name() {
        return codec_name;
    }

    public void setCodec_name(String codec_name) {
        this.codec_name = codec_name;
    }

    private String sample_aspect_ratio;

    public String getSample_aspect_ratio() {
        return sample_aspect_ratio;
    }

    public void setSample_aspect_ratio(String sample_aspect_ratio) {
        this.sample_aspect_ratio = sample_aspect_ratio;
    }

    public String getCodec_time_base() {
        return codec_time_base;
    }

    public void setCodec_time_base(String codec_time_base) {
        this.codec_time_base = codec_time_base;
    }

    public String getIs_avc() {
        return is_avc;
    }

    public void setIs_avc(String is_avc) {
        this.is_avc = is_avc;
    }

    private String codec_time_base;
    private String codec_tag_string;

    public String getCodec_tag_string() {
        return codec_tag_string;
    }

    public void setCodec_tag_string(String codec_tag_string) {
        this.codec_tag_string = codec_tag_string;
    }

    private String is_avc;

    public int getIndex() {
        return index;
    }

    private int level;

    public int getLevel() {
        return level;
    }

    private String r_frame_rate;

    public String getR_frame_rate() {
        return r_frame_rate;
    }

    public void setR_frame_rate(String r_frame_rate) {
        this.r_frame_rate = r_frame_rate;
    }

    public String getAvg_frame_rate() {
        return avg_frame_rate;
    }

    public void setAvg_frame_rate(String avg_frame_rate) {
        this.avg_frame_rate = avg_frame_rate;
    }

    public String getDisplay_aspect_ratio() {
        return display_aspect_ratio;
    }

    public void setDisplay_aspect_ratio(String display_aspect_ratio) {
        this.display_aspect_ratio = display_aspect_ratio;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    private String avg_frame_rate;
    private String display_aspect_ratio;
    private String profile;

    public void setLevel(int level) {
        this.level = level;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCodec_type() {
        return codec_type;
    }

    public void setCodec_type(String codec_type) {
        this.codec_type = codec_type;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBit_rate() {
        return bit_rate;
    }

    public void setBit_rate(String bit_rate) {
        this.bit_rate = bit_rate;
    }

    private int    index;
    private int    width;
    private int    height;
    private String codec_type;
    private String sample_rate;

    public String getSample_rate() {
        return sample_rate;
    }

    public void setSample_rate(String sample_rate) {
        this.sample_rate = sample_rate;
    }

    private String duration;
    private String bit_rate;

    public int parseDuration() {
        try {
            if (duration == null) return -1;
            return (int) Double.parseDouble(getDuration());
        } catch (Throwable e) {
            LOGGER.info(getDuration());
            LOGGER.log(e);
            return -1;
        }
    }

    public int parseBitrate() {
        try {
            if (bit_rate == null) return -1;
            return (int) Integer.parseInt(getBit_rate());
        } catch (Throwable e) {
            LOGGER.info(getBit_rate());
            LOGGER.log(e);
            return -1;
        }
    }

    public int parseSamplingRate() {
        try {
            if (sample_rate == null) return -1;
            return (int) Integer.parseInt(getSample_rate());
        } catch (Throwable e) {
            LOGGER.info(getSample_rate());
            LOGGER.log(e);
            return -1;
        }

    }

    public int[] parseFrameRate() {

        try {
            if (avg_frame_rate == null) return null;
            String[] sp = avg_frame_rate.split("/");
            if (sp.length == 2) {
                return new int[] { Integer.parseInt(sp[0]), Integer.parseInt(sp[1]) };
            } else {
                return new int[] { Integer.parseInt(sp[0]), 1 };
            }
        } catch (Throwable e) {
            LOGGER.info(avg_frame_rate);
            LOGGER.log(e);
            return null;
        }
    }

    public int[] parsePixelAspectRatio() {

        try {
            if (sample_aspect_ratio == null) return null;
            String[] sp = sample_aspect_ratio.split(":");
            if (sp.length == 2) {
                return new int[] { Integer.parseInt(sp[0]), Integer.parseInt(sp[1]) };
            } else {
                return new int[] { Integer.parseInt(sp[0]), 1 };
            }
        } catch (Throwable e) {
            LOGGER.info(sample_aspect_ratio);
            LOGGER.log(e);
            return null;
        }
    }

    public AudioStream toAudioStream() {
        if ("audio".equalsIgnoreCase(getCodec_type())) {
            AudioStream as = new AudioStream();
            as.setCodec(getCodec_name());
            as.setCodecDescription(getCodec_long_name());
            as.setCodecTag(getCodec_tag_string());
            as.setBitrate(parseBitrate());
            as.setSamplingRate(parseSamplingRate());
            as.setDuration(parseDuration());
            as.setChannels(getChannels());
            as.setIndex(getIndex());
            return as;
        } else {
            return null;
        }
    }

    public VideoStream toVideoStream() {
        if ("video".equalsIgnoreCase(getCodec_type())) {
            VideoStream as = new VideoStream();
            as.setCodec(getCodec_name());
            as.setBitrate(parseBitrate());
            as.setCodecDescription(getCodec_long_name());
            as.setCodecTag(getCodec_tag_string());
            as.setFrameRate(parseFrameRate());
            as.setProfileTags(getProfile());
            as.setPixelAspectRatio(parsePixelAspectRatio());
            as.setDuration(parseDuration());
            as.setIndex(getIndex());
            as.setWidth(getWidth());
            as.setIndex(getIndex());
            as.setHeight(getHeight());
            return as;
        } else {
            return null;
        }

    }

}
