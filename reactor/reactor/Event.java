package reactor;

import reactorapi.EventHandler;

/**
 * And event that contains the actual event, and the handler function to handle
 * it
 */
public class Event<T> {
    private final T event;
    private final EventHandler<T> handler;

    /**
     * Create a new Event with event data and suitable handler.
     *
     * @param e
     *              the event data
     *
     * @param eh
     *              the handler to handle the event data.
     */
    public Event(T e, EventHandler<T> eh) {
        event = e;
        handler = eh;
    }

    /**
     * Return the data associated with the event
     *
     * @return event
     */
    public T getEvent() {
        return event;
    }

    /**
     * Return the handler associated with the event.
     *
     * @return event
     */
    public EventHandler<T> getHandler() {
        return handler;
    }

    /**
     * Handle the event by passing the data to the handler.handleEvent function
     */
    public void handle() {
        handler.handleEvent(event);
    }
}
