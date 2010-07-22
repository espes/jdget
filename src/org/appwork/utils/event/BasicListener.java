/**
 * 
 */
package org.appwork.utils.event;

import java.util.EventListener;

/**
 * @author $Author: unknown$
 * 
 */
public interface BasicListener<E> extends EventListener {

    /**
     * @param event
     */
    abstract public void onEvent(BasicEvent<E> event);

}
