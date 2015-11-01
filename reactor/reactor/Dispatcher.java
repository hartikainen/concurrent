package reactor;

import java.util.HashMap;
import reactorapi.*;

/**
 * A Dispatcher that dispatches events, received synchronously through a
 * {@link BlockingEventQueue}, from {@link Handle}s to
 * {@link EventHandler}s. Events are dispatched by calling the
 * {@link EventHandler}s sequentially.
 */
public class Dispatcher {
    private static final int DEFAULT_CAPACITY = 10;
    private final BlockingEventQueue<Object> eventQueue;
    private final HashMap<EventHandler<? extends Object>,
                          WorkerThread<? extends Object>> workerList;

    /**
     * Create a new Dispatcher with capacity of <code>DEFAULT_CAPACITY</code>
     */
    public Dispatcher() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Create a new Dispatcher with capacity of <code>capacity</code>.
     * Initialize the {@link BlockingEventQueue} with the given capacity, and
     * a {@link HashMap} to store the {@link EventHandler}-{@link WorkerThread}
     * pairs.
     *
     * @param capacity
     *              The capacity to be used for the {@link BlockingEventQueue}
     */
    public Dispatcher(int capacity) {
        this.eventQueue = new BlockingEventQueue<Object>(capacity);
        this.workerList = new HashMap<EventHandler<? extends Object>,
                                      WorkerThread<? extends Object>>();
    }

    /**
     * The main loop of the Dispatcher. Dispatches the events from the
     * eventQueue, to the associated {@link EventHandler} as long as the are
     * handlers in the workerList. Only handle event if the associated
     * {@link EventHandler} is still in the <code>workerList</code>.
     */
    public void handleEvents() throws InterruptedException {
        Event<?> event;
        EventHandler<?> handler;

        while (workerList.size() > 0) {
            event = select();
            handler = event.getHandler();

            if (!workerList.containsKey(handler)) continue;

            event.handle();
        }
    }

    /**
     * Select and event from the {@link BlockingEventQueue}.
     *
     * @return
     *              the first element in the {@link BlockingEventQueue}
     *
     * @throws InterruptedException
     */
    public Event<?> select() throws InterruptedException{
        return eventQueue.get();
    }

    /**
     * Add a new (unregistered) handler to the Dispatcher. Create and start a
     * new WorkerThread, that pushes the events to the
     * {@link BlockingEventQueue}.
     *
     * @param
     *              The handler to be added
     *
     * @throws InterruptedException
     */
    public <T> void addHandler(EventHandler<T> handler) {
        if (handler == null) throw new IllegalArgumentException();

        // Don't allow same handler to be assigned twice
        if (workerList.containsKey(handler)) return;

        WorkerThread<T> thread = new WorkerThread<T>((EventHandler<T>)handler,
                                                     eventQueue);
        workerList.put(handler, thread);
        thread.start();
    }

    /**
     * Remove the given handler from the Dispatcher. Also cancel the associated
     * thread. After the handler is removed, the {@link handleEvents} will not
     * dispatch events associated with the handler.
     *
     * @param
     *              The handler to be removed
     */
    public <T> void removeHandler(EventHandler<T> handler) {
        if (handler == null) throw new IllegalArgumentException();

        WorkerThread<?> thread = workerList.remove(handler);

        if (thread != null) {
            thread.cancelThread();
        }
    }
}
