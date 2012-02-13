package org.jdownloader.extensions.omnibox.omni.plugins;

import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import jd.controlling.reconnect.Reconnecter;

import org.jdownloader.extensions.omnibox.omni.Action;
import org.jdownloader.extensions.omnibox.omni.Proposal;
import org.jdownloader.extensions.omnibox.omni.ProposalRequest;
import org.jdownloader.extensions.omnibox.omni.ProposalRequestListener;
import org.jdownloader.extensions.omnibox.omni.Utils;
import org.jdownloader.settings.GeneralSettings;

public class AwesomeReconnectListener implements ProposalRequestListener {
    private enum actionid {
        TURNOFF,
        TURNON,
        RCNOW
    }

    public List<String> getKeywords() {
        return Arrays.asList("reconnect");
    }

    public float getRankingMultiplier() {
        return 0.95f;
    }

    public void performAction(final Action action) {
        switch ((actionid) action.getProposal().getActionID()) {
        case TURNOFF:
            org.jdownloader.settings.staticreferences.CFG_GENERAL.AUTO_RECONNECT_ENABLED.setValue(false);
            break;
        case TURNON:
            org.jdownloader.settings.staticreferences.CFG_GENERAL.AUTO_RECONNECT_ENABLED.setValue(true);
            break;
        case RCNOW:
            Reconnecter.getInstance().forceReconnect();
            break;
        }
    }

    public void requestProposal(final ProposalRequest request) {
        if (request.isParamsEmpty() || request.getParams().startsWith("n")) {
            new Proposal(this, request, new JLabel("Reconnect now."), actionid.RCNOW, Utils.createProposalListElement(this, request.withParams("now")), 0.6f);

        }
        if (request.isParamsEmpty() || request.getParams().startsWith("o")) {
            if (org.jdownloader.settings.staticreferences.CFG_GENERAL.AUTO_RECONNECT_ENABLED.getValue()) {
                new Proposal(this, request, new JLabel("Turn automatic reconnect off"), actionid.TURNOFF, Utils.createProposalListElement(this, request.withParams("off")), 1.0f);
            } else {
                new Proposal(this, request, new JLabel("Turn automatic reconnect on"), actionid.TURNON, Utils.createProposalListElement(this, request.withParams("on")), 1.0f);
            }
        }
    }

}