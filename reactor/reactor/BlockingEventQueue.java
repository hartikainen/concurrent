package reactor;

import reactorapi.BlockingQueue;
import java.util.List;
import java.util.ArrayList;

// TODO: check the type
public class BlockingEventQueue<T> implements BlockingQueue<Event<? extends T>> {
    private final ArrayList<Event<? extends T>> eventList;
    private final int capacity;

    public BlockingEventQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.eventList = new ArrayList<Event<? extends T>>(capacity);
    }

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

    public Event<? extends T> get() throws InterruptedException {
        final Event<? extends T> event;

        synchronized (eventList) {
            while (eventList.size() < 1) {
                eventList.wait();
            }

            event = eventList.remove(0);

            eventList.notifyAll();
        }

        return event;
    }

    public List<Event<? extends T>> getAll() {
        final ArrayList<Event<? extends T>> returnList;

        synchronized (eventList) {
            returnList = new ArrayList<Event<? extends T>>(eventList);
            eventList.clear();

            eventList.notifyAll();
        }

        return returnList;
    }

    public void put(Event<? extends T> event) throws InterruptedException {
        if (event == null) throw new NullPointerException();

        synchronized (eventList) {
            while (eventList.size() >= capacity) {
                eventList.wait();
            }

            eventList.add(event);

            eventList.notifyAll();
        }
    }
}
