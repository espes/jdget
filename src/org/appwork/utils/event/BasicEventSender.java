/**
 * 
 */
package org.appwork.utils.event;


/**
 * @author $Author: unknown$
 * 
 */
public class BasicEventSender<E> extends Eventsender<BasicListener<E>, BasicEvent<E>> {

    @Override
    protected void fireEvent(BasicListener<E> listener, BasicEvent<E> event) {
        listener.onEvent(event);

    }

}
