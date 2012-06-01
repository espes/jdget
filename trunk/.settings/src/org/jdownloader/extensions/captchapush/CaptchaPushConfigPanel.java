package org.jdownloader.extensions.captchapush;

import jd.gui.swing.jdgui.views.settings.components.TextInput;

import org.jdownloader.extensions.ExtensionConfigPanel;

public class CaptchaPushConfigPanel extends ExtensionConfigPanel<CaptchaPushExtension> {

    private static final long       serialVersionUID = 1L;

    private final CaptchaPushConfig config;

    private TextInput               brokerHost;
    private TextInput               brokerTopic;

    public CaptchaPushConfigPanel(CaptchaPushExtension extension, CaptchaPushConfig config) {
        super(extension);
        this.config = config;
        initComponents();
        layoutPanel();
    }

    private void initComponents() {
        brokerHost = new TextInput();
        brokerTopic = new TextInput();
    }

    protected void layoutPanel() {
        addPair("Host of the Broker:", null, brokerHost);
        addPair("Topic of the Broker:", null, brokerTopic);
    }

    @Override
    public void save() {
        boolean changes = false;
        if (!brokerHost.getText().equals(config.getBrokerHost())) {
            config.setBrokerHost(brokerHost.getText());
            changes = true;
        }
        if (!brokerTopic.getText().equals(config.getBrokerTopic())) {
            config.setBrokerTopic(brokerTopic.getText());
            changes = true;
        }

        if (changes) showRestartRequiredMessage();
    }

    @Override
    public void updateContents() {
        brokerHost.setText(config.getBrokerHost());
        brokerTopic.setText(config.getBrokerTopic());
    }

}