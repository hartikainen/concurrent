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
p     *
     * @return
     *              the first element in the {@link BlockingEventQueue}
     */
    public Event<?> select() throws InterruptedException{
        return eventQueue.get();
    }

    /**
     * Add a new handler to the Dispatcher. Create and start a new
     * WorkerThread, that pushes the events to the {@link BlockingEventQueue}
     *
     * @param
     *              The handler to be added
     */
    public <T> void addHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        WorkerThread<T> thread = new WorkerThread<T>((EventHandler<T>)h, eventQueue);
        workerList.put(h, thread);
        thread.start();
    }

    /**
     * Remove the given handler from the Dispatcher. Also cancel the associated
     * thread.
     *
     * @param
     *              The handler to be removed
     */
    public <T> void removeHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        WorkerThread<?> thread = workerList.remove(h);

        if (thread != null) {
            thread.cancelThread();
        }
    }
}
