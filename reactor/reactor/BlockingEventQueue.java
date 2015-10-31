package reactor;

import reactorapi.BlockingQueue;
import java.util.List;
import java.util.ArrayList;

// TODO: check the type
public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
    private final ArrayList<Event<? extends T>> eventList;
    private final int capacity;
    private final Semaphore notEmpty;
    private final Semaphore notFull;

    public BlockingEventQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.eventList = new ArrayList<Event<? extends T>>(capacity);
        this.notEmpty = new Semaphore(0);
        this.notFull  = new Semaphore(capacity);
    }

    @Override
    public int getSize() {
        final int size;

        synchronized (eventList) {
            size = eventList.size();
        }

        return size;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public Event<? extends T> get() throws InterruptedException {
        final Event<? extends T> event;

        notEmpty.acquire();

        synchronized (eventList) {
            event = eventList.remove(0);
        }

        notFull.release();

        return event;
    }

    @Override
    public List<Event<? extends T>> getAll() {
        final ArrayList<Event<? extends T>> returnList;
        final int elements;

        synchronized (eventList) {
            elements = notEmpty.tryAcquireAll();
            returnList = new ArrayList<Event<? extends T>>(elements);

            for (int i=0; i<elements; i++) {
                returnList.add(eventList.remove(0));
            }
        }

        notFull.release(capacity);

        return returnList;
    }

    @Override
    public void put(Event<? extends T> event) throws InterruptedException {
        if (event == null) throw new NullPointerException();

        notFull.acquire();

        synchronized (eventList) {
            eventList.add(event);
        }

        notEmpty.release();
    }
}
