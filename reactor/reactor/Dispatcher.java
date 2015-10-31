package reactor;

import java.util.ArrayList;
import java.util.HashMap;
import reactorapi.*;

public class Dispatcher {
    private static final int DEFAULT_CAPACITY = 10;
    private final BlockingEventQueue<Object> eventQueue;
    //private final ArrayList<EventHandler<? extends Object>> handlerList;
    private final HashMap<EventHandler<? extends Object>,
                          WorkerThread<? extends Object>> workerList;

    public Dispatcher() {
        this(DEFAULT_CAPACITY);
    }

    public Dispatcher(int capacity) {
        this.eventQueue = new BlockingEventQueue<Object>(capacity);
        //this.handlerList = new ArrayList<EventHandler<? extends Object>>();
        this.workerList = new HashMap<EventHandler<? extends Object>,
                                      WorkerThread<? extends Object>>();
    }

    public <T> void handleEvents() throws InterruptedException {
        Event<?> event;
        EventHandler<?> handler;

        while (workerList.size() > 0) {
            //while (handlerList.size() > 0) {
            event = select();
            handler = event.getHandler();

            if (!workerList.containsKey(handler)) continue;

            event.handle();
        }
        // TODO: Implement Dispatcher.handleEvents().
    }

    public Event<?> select() throws InterruptedException{
        return eventQueue.get();
    }

    public <T> void addHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        // handlerList.add(h);

        WorkerThread<T> thread = new WorkerThread<T>((EventHandler<T>)h, eventQueue);
        workerList.put(h, thread);
        thread.start();

        // or a null message is received
        // TODO: Implement Dispatcher.addHandler(EventHandler).
    }

    public <T> void removeHandler(EventHandler<T> h) {
        if (h == null) throw new IllegalArgumentException();

        WorkerThread<?> thread = workerList.remove(h);

        if (thread != null) {
            thread.cancelThread();
        }

        // TODO: what if h not in handlerList?
        //handlerList.remove(h);
        // TODO: Implement Dispatcher.removeHandler(EventHandler).
    }

    // Add methods and fields as needed.
}
