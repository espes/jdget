package org.appwork.swing.components.multiprogressbar;

import javax.swing.JComponent;

public class MultiProgressBar extends JComponent {
    /**
     * @see #getUIClassID
     */
    private static final String        UI_CLASS_ID = "MultiProgressBarUI";
    private MultiProgressModel         model;
    private MultiProgressModelListener listener;
    private MultiProgressEventSender   eventSender;
    public MultiProgressEventSender getEventSender() {
        return eventSender;
    }

    private MultiProgressEvent         changeEvent;

    public MultiProgressBar(long max) {
        this(new MultiProgressModel(max));

    }

    /**
     * Resets the UI property to a value from the current look and feel.
     * 
     * @see JComponent#updateUI
     */
    @Override
    public void updateUI() {
        // final CircleProgressBarUI newUI = (CircleProgressBarUI)
        // UIManager.getUI(this);
        this.setUI(new BasicMultiProgressModelUI());
    }

    public synchronized void setModel(final MultiProgressModel model) {
        if (this.model == model) { return; }
        if (this.model != null) {
            model.getEventSender().removeListener(this.listener);
        }
        this.listener = new MultiProgressModelListener() {

            @Override
            public void onChanged() {
                eventSender.fireEvent(changeEvent);
            }

        };
        model.getEventSender().addListener(this.listener);
        this.model = model;

        this.repaint();

    }

    public void setUI(final MultiProgressBarUI ui) {
        super.setUI(ui);

    }

    /**
     * @param multiProgressModel
     */
    public MultiProgressBar(MultiProgressModel multiProgressModel) {
        eventSender = new MultiProgressEventSender();
        changeEvent = new MultiProgressEvent(this);
        setModel(multiProgressModel);
        this.updateUI();
    }

    /**
     * 
     */
    public MultiProgressModel getModel() {
        return model;
    }
}
