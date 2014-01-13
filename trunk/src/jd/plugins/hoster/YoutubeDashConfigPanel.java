package jd.plugins.hoster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import jd.gui.swing.jdgui.views.settings.components.Checkbox;
import jd.gui.swing.jdgui.views.settings.components.ComboBox;
import jd.gui.swing.jdgui.views.settings.components.MultiComboBox;
import jd.gui.swing.jdgui.views.settings.components.ProxyInput;
import jd.gui.swing.jdgui.views.settings.components.TextInput;
import jd.gui.swing.jdgui.views.settings.panels.advanced.AdvancedConfigTableModel;
import jd.gui.swing.jdgui.views.settings.panels.advanced.AdvancedTable;
import jd.plugins.PluginConfigPanelNG;
import jd.plugins.decrypter.YoutubeVariant;
import jd.plugins.hoster.YoutubeDashV2.YoutubeConfig;
import jd.plugins.hoster.YoutubeDashV2.YoutubeConfig.GroupLogic;
import jd.plugins.hoster.YoutubeDashV2.YoutubeConfig.IfUrlisAVideoAndPlaylistAction;

import org.appwork.storage.config.annotations.AboutConfig;
import org.appwork.storage.config.handler.BooleanKeyHandler;
import org.appwork.storage.config.handler.KeyHandler;
import org.appwork.storage.config.handler.StringKeyHandler;
import org.jdownloader.gui.IconKey;
import org.jdownloader.gui.settings.Pair;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.images.NewTheme;
import org.jdownloader.plugins.config.PluginJsonConfig;
import org.jdownloader.settings.advanced.AdvancedConfigEntry;

public class YoutubeDashConfigPanel extends PluginConfigPanelNG {

    private AdvancedConfigTableModel model;
    private YoutubeConfig            cf;
    private Pair<MultiVariantBox>    videoMp4;
    private Pair<MultiVariantBox>    videoWebm;
    private Pair<MultiVariantBox>    videoFlv;
    private Pair<MultiVariantBox>    videoGp3;
    private Pair<MultiVariantBox>    video3D;
    private Pair<MultiVariantBox>    audioPair;
    private Pair<MultiVariantBox>    videoPair;
    private Pair<Checkbox>           subtitles;
    private Pair<MultiVariantBox>    extraVideoMp4;
    private Pair<MultiVariantBox>    extraVideoWebm;
    private Pair<MultiVariantBox>    extraVideoFlv;
    private Pair<MultiVariantBox>    extraVideoGp3;
    private Pair<MultiVariantBox>    extraVideo3D;
    private Pair<MultiVariantBox>    extraAudio;
    private Pair<MultiVariantBox>    extraVideo;
    private boolean                  setting = false;

    public class MultiVariantBox extends MultiComboBox<YoutubeVariant> {

        private YoutubeDashConfigPanel panel;

        public MultiVariantBox(YoutubeDashConfigPanel youtubeDashConfigPanel, ArrayList<YoutubeVariant> list) {
            super(list.toArray(new YoutubeVariant[] {}));
            this.panel = youtubeDashConfigPanel;

        }

        @Override
        public void onChanged() {
            super.onChanged();
            panel.save();
        }

        @Override
        protected String getLabel(int i, YoutubeVariant sc) {
            if (i == 0) {
                return _GUI._.YoutubeDashConfigPanel_MultiVariantBox_getLabel_(sc.getName()) + " (" + _GUI._.YoutubeDashConfigPanel_getLabel_best() + ")";
            } else if (i == getValues().size() - 1) { return _GUI._.YoutubeDashConfigPanel_MultiVariantBox_getLabel_(sc.getName()) + " (" + _GUI._.YoutubeDashConfigPanel_getLabel_worst() + ")"; }
            return _GUI._.YoutubeDashConfigPanel_MultiVariantBox_getLabel_(sc.getName());
        }

        @Override
        protected String getLabel(List<YoutubeVariant> list) {
            if (list.size() == 0) return super.getLabel(list);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (sb.length() > 0) {
                    if (i == list.size() - 1) {
                        sb.append(" & ");
                    } else {
                        sb.append(", ");
                    }
                }

                sb.append(list.get(i).getQualityExtension());

            }

            sb.append(" ");
            sb.append(list.get(0).getFileExtension());
            sb.append("-");
            if (list.size() > 1) {
                sb.append("Videos");
            } else {
                sb.append("Video");
            }
            return "(" + list.size() + "/" + getValues().size() + ") " + sb.toString();
        }

    }

    public YoutubeDashConfigPanel(String description) {
        ;
        cf = PluginJsonConfig.get(YoutubeConfig.class);

        addStartDescription(description);

        addPair(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_if_link_contains_video_and_playlist(), null, null, new ComboBox<IfUrlisAVideoAndPlaylistAction>(cf._getStorageHandler().getKeyHandler("LinkIsVideoAndPlaylistUrlAction", KeyHandler.class), IfUrlisAVideoAndPlaylistAction.values(), null));
        addPair(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_grouping(), null, null, new ComboBox<GroupLogic>(cf._getStorageHandler().getKeyHandler("GroupLogic", KeyHandler.class), GroupLogic.values(), null));
        addPair(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_https(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("PreferHttpsEnabled", BooleanKeyHandler.class), null));

        HashSet<String> dupe = new HashSet<String>();
        ArrayList<YoutubeVariant> videoFLV = new ArrayList<YoutubeVariant>();
        ArrayList<YoutubeVariant> videoMP4 = new ArrayList<YoutubeVariant>();
        ArrayList<YoutubeVariant> videoWEBM = new ArrayList<YoutubeVariant>();
        ArrayList<YoutubeVariant> videoGP3 = new ArrayList<YoutubeVariant>();
        ArrayList<YoutubeVariant> video3D = new ArrayList<YoutubeVariant>();

        ArrayList<YoutubeVariant> audio = new ArrayList<YoutubeVariant>();
        ArrayList<YoutubeVariant> image = new ArrayList<YoutubeVariant>();
        for (YoutubeVariant ytv : YoutubeVariant.values()) {
            if (!dupe.add(ytv.getTypeId())) continue;
            switch (ytv.getGroup()) {
            case AUDIO:
                audio.add(ytv);
                break;
            case IMAGE:
                image.add(ytv);
                break;
            case VIDEO:
                if (ytv.name().startsWith("MP4")) {
                    videoMP4.add(ytv);

                } else if (ytv.name().startsWith("FLV")) {
                    videoFLV.add(ytv);

                } else if (ytv.name().startsWith("THREEGP")) {
                    videoGP3.add(ytv);

                } else {
                    videoWEBM.add(ytv);

                }

                break;
            case VIDEO_3D:
                video3D.add(ytv);
                break;

            }
        }
        Comparator<YoutubeVariant> comp = new Comparator<YoutubeVariant>() {

            @Override
            public int compare(YoutubeVariant o1, YoutubeVariant o2) {
                return new Double(o2.getQualityRating()).compareTo(new Double(o1.getQualityRating()));
            }
        };
        Collections.sort(videoMP4, comp);
        Collections.sort(video3D, comp);
        Collections.sort(videoFLV, comp);
        Collections.sort(videoGP3, comp);
        Collections.sort(videoMP4, comp);
        Collections.sort(videoWEBM, comp);
        Collections.sort(audio, comp);
        Collections.sort(image, comp);
        // Collections.sort(videoWEBM,comp );
        addHeader(_GUI._.YoutubeDashConfigPanel_allowedtypoes(), NewTheme.I().getIcon(IconKey.ICON_MEDIAPLAYER, 18));
        videoMp4 = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_mp4(), null, null, new MultiVariantBox(this, videoMP4));
        videoWebm = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_webm(), null, null, new MultiVariantBox(this, videoWEBM));
        videoFlv = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_flv(), null, null, new MultiVariantBox(this, videoFLV));
        videoGp3 = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_gp3(), null, null, new MultiVariantBox(this, videoGP3));
        this.video3D = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_3D(), null, null, new MultiVariantBox(this, video3D));
        audioPair = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_audio(), null, null, new MultiVariantBox(this, audio));
        videoPair = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_image(), null, null, new MultiVariantBox(this, image));
        subtitles = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_subtitles(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("SubtitlesEnabled", BooleanKeyHandler.class), null));

        addHeader(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_filename_pattern_header(), NewTheme.I().getIcon(IconKey.ICON_FILE, 18));
        addHtmlDescription(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_filename_desc(), false);
        addPair(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_filename_pattern(), null, null, new TextInput(cf._getStorageHandler().getKeyHandler("FilenamePattern", StringKeyHandler.class)));

        addHeader(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_proxy_header(), NewTheme.I().getIcon(IconKey.ICON_PROXY, 18));
        Pair<Checkbox> checkbox = addPair(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_userproxy(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("ProxyEnabled", BooleanKeyHandler.class), null));

        Pair<ProxyInput> proxy = addPair("", null, null, new ProxyInput(cf._getStorageHandler().getKeyHandler("Proxy", KeyHandler.class)));
        checkbox.getComponent().setDependencies(proxy.getComponent());
        addHeader(_GUI._.YoutubeDashConfigPanel_links(), NewTheme.I().getIcon(IconKey.ICON_LIST, 18));
        addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_best_video(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("CreateBestVideoVariantLinkEnabled", BooleanKeyHandler.class), null));
        addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_best_audio(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("CreateBestAudioVariantLinkEnabled", BooleanKeyHandler.class), null));
        addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_best_3d(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("CreateBest3DVariantLinkEnabled", BooleanKeyHandler.class), null));
        addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_best_subtitle(), null, null, new Checkbox(cf._getStorageHandler().getKeyHandler("CreateBestSubtitleVariantLinkEnabled", BooleanKeyHandler.class), null));

        addHtmlDescription(_GUI._.YoutubeDashConfigPanel_YoutubeDashConfigPanel_extra_desc(), false);
        extraVideoMp4 = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_mp4(), null, null, new MultiVariantBox(this, videoMP4));
        extraVideoWebm = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_webm(), null, null, new MultiVariantBox(this, videoWEBM));
        extraVideoFlv = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_flv(), null, null, new MultiVariantBox(this, videoFLV));
        extraVideoGp3 = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_gp3(), null, null, new MultiVariantBox(this, videoGP3));
        extraVideo3D = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_3D(), null, null, new MultiVariantBox(this, video3D));
        extraAudio = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_audio(), null, null, new MultiVariantBox(this, audio));
        extraVideo = addPair(_GUI._.YoutubeDashConfigPanel_allowedtypoes_image(), null, null, new MultiVariantBox(this, image));
        addHeader("DEBUG ONLY... This Plugin is not finished yet", NewTheme.I().getIcon(IconKey.ICON_WARNING, 18));
        add(new AdvancedTable(model = new AdvancedConfigTableModel("YoutubeConfig") {
            @Override
            public void refresh(String filterText) {
                _fireTableStructureChanged(register(), true);
            }
        }));
        model.refresh("Youtube");
    }

    public static void main(String[] args) {

    }

    public ArrayList<AdvancedConfigEntry> register() {
        ArrayList<AdvancedConfigEntry> configInterfaces = new ArrayList<AdvancedConfigEntry>();
        HashMap<KeyHandler, Boolean> map = new HashMap<KeyHandler, Boolean>();

        for (KeyHandler m : cf._getStorageHandler().getMap().values()) {

            if (map.containsKey(m)) continue;

            if (m.getAnnotation(AboutConfig.class) != null) {
                if (m.getSetter() == null) {
                    throw new RuntimeException("Setter for " + m.getGetter().getMethod() + " missing");
                } else if (m.getGetter() == null) {
                    throw new RuntimeException("Getter for " + m.getSetter().getMethod() + " missing");
                } else {
                    synchronized (configInterfaces) {
                        configInterfaces.add(new AdvancedConfigEntry(cf, m));
                    }
                    map.put(m, true);
                }
            }

        }

        return configInterfaces;
    }

    @Override
    public void reset() {
    }

    @Override
    public void save() {
        if (setting) return;
        HashSet<String> blacklistSet = new HashSet<String>();
        getBlacklist(blacklistSet, video3D.getComponent());
        getBlacklist(blacklistSet, audioPair.getComponent());
        getBlacklist(blacklistSet, videoFlv.getComponent());
        getBlacklist(blacklistSet, videoGp3.getComponent());
        getBlacklist(blacklistSet, videoPair.getComponent());
        getBlacklist(blacklistSet, videoMp4.getComponent());
        getBlacklist(blacklistSet, videoWebm.getComponent());
        cf.setBlacklistedVariants(blacklistSet.toArray(new String[] {}));

        HashSet<String> whitelist = new HashSet<String>();

        getWhitelist(whitelist, extraVideo3D.getComponent());
        getWhitelist(whitelist, extraAudio.getComponent());
        getWhitelist(whitelist, extraVideoFlv.getComponent());
        getWhitelist(whitelist, extraVideoGp3.getComponent());
        getWhitelist(whitelist, extraVideo.getComponent());
        getWhitelist(whitelist, extraVideoMp4.getComponent());
        getWhitelist(whitelist, extraVideoWebm.getComponent());
        cf.setExtraVariants(whitelist.toArray(new String[] {}));
    }

    /**
     * @param list
     * @param blacklistSet
     */
    public void getWhitelist(HashSet<String> whitelist, MultiVariantBox list) {
        for (YoutubeVariant v : list.getValues()) {
            if (list.isItemSelected(v)) {
                whitelist.add(v.getTypeId());
            }
        }
    }

    /**
     * @param list
     * @param blacklistSet
     */
    public void getBlacklist(HashSet<String> blacklistSet, MultiVariantBox list) {
        for (YoutubeVariant v : list.getValues()) {
            if (!list.isItemSelected(v)) {
                blacklistSet.add(v.getTypeId());
            }
        }
    }

    @Override
    public void updateContents() {
        // ifVideoAndPlaylistCombo.getComponent().setSelectedItem(cf.getLinkIsVideoAndPlaylistUrlAction());
        setting = true;
        try {
            String[] blacklist = cf.getBlacklistedVariants();
            HashSet<String> blacklistSet = new HashSet<String>();
            if (blacklist != null) {
                for (String b : blacklist) {

                    blacklistSet.add(b);
                }
            }

            setBlacklist(blacklistSet, video3D.getComponent());
            setBlacklist(blacklistSet, audioPair.getComponent());
            setBlacklist(blacklistSet, videoFlv.getComponent());
            setBlacklist(blacklistSet, videoGp3.getComponent());
            setBlacklist(blacklistSet, videoPair.getComponent());
            setBlacklist(blacklistSet, videoMp4.getComponent());
            setBlacklist(blacklistSet, videoWebm.getComponent());
            HashSet<String> extraSet = new HashSet<String>();
            String[] extra = cf.getExtraVariants();
            if (extra != null) {
                for (String b : extra) {

                    extraSet.add(b);
                }
            }

            setWhitelist(extraSet, extraVideo3D.getComponent());
            setWhitelist(extraSet, extraAudio.getComponent());
            setWhitelist(extraSet, extraVideoFlv.getComponent());
            setWhitelist(extraSet, extraVideoGp3.getComponent());
            setWhitelist(extraSet, extraVideo.getComponent());
            setWhitelist(extraSet, extraVideoMp4.getComponent());
            setWhitelist(extraSet, extraVideoWebm.getComponent());
        } finally {
            setting = false;
        }
        //
    }

    public void setWhitelist(HashSet<String> whitelist, MultiVariantBox list) {

        ArrayList<YoutubeVariant> vList = new ArrayList<YoutubeVariant>();

        for (YoutubeVariant v : list.getValues()) {
            if (whitelist.contains(v.getTypeId())) {
                vList.add(v);
            }

        }
        list.setSelectedItems(vList);
    }

    /**
     * @param blacklistSet
     * @param list
     */
    public void setBlacklist(HashSet<String> blacklistSet, MultiVariantBox list) {

        ArrayList<YoutubeVariant> vList = new ArrayList<YoutubeVariant>();

        for (YoutubeVariant v : list.getValues()) {
            if (!blacklistSet.contains(v.getTypeId())) {
                vList.add(v);
            }

        }
        list.setSelectedItems(vList);
    }

}
