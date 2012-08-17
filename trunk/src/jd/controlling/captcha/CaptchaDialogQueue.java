package jd.controlling.captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import jd.controlling.IOPermission;

import org.appwork.utils.event.queue.Queue;
import org.appwork.utils.event.queue.QueueAction;

public class CaptchaDialogQueue extends Queue {

    private final static CaptchaDialogQueue INSTANCE = new CaptchaDialogQueue();

    public static CaptchaDialogQueue getInstance() {
        return INSTANCE;
    }

    private CaptchaDialogQueueEntry currentItem = null;

    private CaptchaDialogQueue() {
        super("CaptchaDialogQueue");
    }

    public CaptchaResult addWait(final CaptchaDialogQueueEntry item) {
        IOPermission io = item.getIOPermission();
        if (io != null && !io.isCaptchaAllowed(item.getHost().getTld())) return null;
        CaptchaEventSender.getInstance().fireEvent(new CaptchaTodoEvent(item.getCaptchaController()));
        CaptchaResult result = null;
        try {
            if (item.isFinished()) {
                result = item.getResult();
            } else {
                result = super.addWait(item);
            }
        } finally {
            CaptchaEventSender.getInstance().fireEvent(new CaptchaFinishEvent(item.getCaptchaController()));
        }
        return result;
    }

    public CaptchaDialogQueueEntry getCurrentQueueEntry() {
        return this.currentItem;
    }

    @Override
    protected <T extends Throwable> void startItem(final QueueAction<?, T> item, final boolean callExceptionhandler) throws T {
        this.currentItem = (CaptchaDialogQueueEntry) item;
        try {
            super.startItem(item, callExceptionhandler);
        } finally {
            this.currentItem = null;
        }
    }

    public List<CaptchaDialogQueueEntry> getJobs() {
        java.util.List<CaptchaDialogQueueEntry> ret = new ArrayList<CaptchaDialogQueueEntry>();
        synchronized (this.queueLock) {
            CaptchaDialogQueueEntry cur = currentItem;
            if (cur != null) {
                ret.add(cur);
            }
            for (final QueuePriority prio : this.prios) {
                ListIterator<QueueAction<?, ? extends Throwable>> li = this.queue.get(prio).listIterator();
                while (li.hasNext()) {
                    QueueAction<?, ? extends Throwable> next = li.next();
                    if (next instanceof CaptchaDialogQueueEntry) {
                        ret.add((CaptchaDialogQueueEntry) next);
                    }
                }
            }
        }
        return ret;
    }

    public CaptchaDialogQueueEntry getCaptchabyID(long id) {
        synchronized (this.queueLock) {
            for (final QueuePriority prio : this.prios) {
                ListIterator<QueueAction<?, ? extends Throwable>> li = this.queue.get(prio).listIterator();
                while (li.hasNext()) {
                    QueueAction<?, ? extends Throwable> next = li.next();
                    if (next instanceof CaptchaDialogQueueEntry) {
                        if (((CaptchaDialogQueueEntry) next).getID().getID() == id) { return (CaptchaDialogQueueEntry) next; }
                    }
                }
            }
            CaptchaDialogQueueEntry cur = currentItem;
            if (cur != null && cur.getID().getID() == id) { return cur; }
        }
        return null;
    }
}
