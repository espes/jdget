/**
 * 
 */
package org.appwork.utils.event;

/**
 * @author $Author: unknown$
 * 
 */
@Deprecated
public class BasicEventSender<E> extends Eventsender<BasicListener<E>, BasicEvent<E>> {

    @Override
    protected void fireEvent(final BasicListener<E> listener, final BasicEvent<E> event) {
        listener.onEvent(event);

    }

}
