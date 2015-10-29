package reactor;

import java.util.ArrayList;
import java.util.HashMap;
import reactorapi.*;

public class Dispatcher {
    private static final int DEFAULT_CAPACITY = 10;
    private final int capacity;
    private final BlockingEventQueue<Event<?>> eventQueue;
    private final ArrayList<EventHandler<?>> handlerList;
    private final HashMap<EventHandler<?>, WorkerThread<?>> workerList;

    public Dispatcher() {
        this(DEFAULT_CAPACITY);
    }

    public Dispatcher(int capacity) {
        this.capacity = capacity;
        this.eventQueue = new BlockingEventQueue<Event<?>>(capacity);
        this.handlerList = new ArrayList<EventHandler<?>>();
        this.workerList = new HashMap<EventHandler<?>, WorkerThread<?>>();
    }

    public void handleEvents() throws InterruptedException {
        Event<?> event;
        EventHandler<?> handler;

        while (handlerList.size() > 0) {
            event = select();
            handler = event.getHandler();

            if (!handlerList.contains(handler)) continue;

            event.handle();
        }
        // TODO: Implement Dispatcher.handleEvents().
    }

    public Event<?> select() throws InterruptedException{
        return eventQueue.get();
        // TODO: Implement Dispatcher.select().
    }

    public <T> void addHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        handlerList.add(h);

        WorkerThread<T> thread = new WorkerThread(h, eventQueue);
        workerList.put(h, thread);

        thread.start();

        // or a null message is received
        // TODO: Implement Dispatcher.addHandler(EventHandler).
    }

    public <T> void removeHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        WorkerThread<T> thread = workerList.remove(h);

        if (thread != null) {
            thread.cancelThread();
        }

        workerList.remove(h);
        // TODO: what if h not in handlerList?
        handlerList.remove(h);
        // TODO: Implement Dispatcher.removeHandler(EventHandler).
    }

    // Add methods and fields as needed.
}
