package reactor;

import reactorapi.BlockingQueue;
import java.util.List;
import java.util.ArrayList;

// TODO: check the type
public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
    private final ArrayList<Event<? extends T>> eventList;
    private final int capacity;

    // Monitors to be used to wait for the list to be notempty or notFull,
    // when getting or setting events, respectively.
    private final Object notEmpty = new Object();
    private final Object notFull  = new Object();

    private volatile boolean empty = true;
    private volatile boolean full  = false;

    public BlockingEventQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("The length of the queue must be > 0");
        }

        this.capacity = capacity;
        this.eventList = new ArrayList<Event<? extends T>>(capacity);
    }

    // TODO: check the lock!
    public int getSize() {
        int size = 0;

        synchronized (eventList) {
            size = eventList.size();
        }

        return size;
    }

    public int getCapacity() {
        return capacity;
    }

    // TODO: the InterruptedException?
    public Event<? extends T> get() throws InterruptedException {
        final Event<? extends T> event;

        synchronized (eventList) {
            // TODO: Interrupting??
            try {
                while (eventList.size() < 1) { eventList.wait(); }
            } catch (InterruptedException ie) {
                throw ie;
                // TODO: ?
            }

            event = eventList.remove(0);

            if (eventList.size() == capacity - 1) {
                // TODO: check if notify is sufficient
                eventList.notifyAll();
            }
        }

        return event;
    }

    public List<Event<? extends T>> getAll() {
        // synchronized (eventList) {
        //     List<Event<? extends T>> eventList;
        // }
        //return new ArrayList<Event<? extends T>>();
        return null;
    }

    public void put(Event<? extends T> event) throws InterruptedException {
        if (event == null) throw new NullPointerException();

        synchronized (eventList) {
            try {
                while (eventList.size() >= capacity) {
                    eventList.wait();
                }
            } catch (InterruptedException ie) {
                throw ie;
                // TODO : ?
            }

            this.eventList.add(event);

            if (eventList.size() == 1) {
                // TODO: check if notify is sufficient
                eventList.notifyAll();
            }
        }
    }

    // private class MySemaphore {
    //     private int value;

    //     public MySemaphore(int v) {
    //         if (v < 1) {
    //             throw new IllegalArgumentException("Semaphore size must be > 0");
    //         }

    //         this.value = v;
    //     }

    //     public synchronized void release() {
    //         this.value++;
    //         this.notify();
    //     }

    //     public synchronized void acquire() throws InterruptedException {
    //         try {
    //             while(this.value <= 0) {
    //                 this.wait();
    //             }
    //         } catch (InterruptedException ie) {
    //             // TODO: should something else be done here?
    //             throw ie;
    //         }

    //         this.value--;
    //     }
    // }
}
