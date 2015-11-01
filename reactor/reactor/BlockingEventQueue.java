package reactor;

import reactorapi.BlockingQueue;
import java.util.List;
import java.util.ArrayList;

/**
 * An {@link BlockingQueue} to handle concurrent accesses by using
 * {@link Semaphore}s.
 *
 * @param <T>
 *            type of the contents of the queue
 */
public class BlockingEventQueue<T>
    implements BlockingQueue<Event<? extends T>> {

    private final ArrayList<Event<? extends T>> eventList;
    private final int capacity;
    private final Semaphore notEmpty;
    private final Semaphore notFull;

    /**
     * Create a new {@link BlockingEventQueue}. Initialize the actual list to
     * store the elements, and the {@link Semaphore}s to indicate if the list
     * is full or empty.
     *
     * @param cacpacity
     *              the maximum number of elements to store in the queue.
     */
    public BlockingEventQueue(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.eventList = new ArrayList<Event<? extends T>>(capacity);
        this.notEmpty = new Semaphore(0);
        this.notFull  = new Semaphore(capacity);
    }

    /**
     * Get the size of the queue. Lock the eventList for the to get the size of
     * the {@link eventList}.
     *
     * @return how many objects the queue currently holds
     */
    @Override
    public int getSize() {
        final int size;

        synchronized (eventList) {
            size = eventList.size();
        }

        return size;
    }

    /**
     * Get the capacity of the queue.
     *
     * @return how many objects the queue can hold
     */
    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * Wait until there is atleast one element in the queue, then remove and
     * return the head of the queue. Acquire a notEmpty semaphore to make sure
     * the queue is not empty, and release a notFull semaphore when the object
     * is removed, to notify the threads waiting on the queue not to be full.
     *
     * @return the object at the head of the queue
     * @throws InterruptedException
     */
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

    /**
     * Remove the entire contents of the queue and return it (in the order it
     * would be read by get) as a List. It acquires all the token from the
     * notEmpty semaphore by using tryAcquireAll, which doesn't wait for a token
     * to be available. After all the elements have been removed, release all
     * notFull tokens (up to <code>capacity</code>).
     *
     * @return the entire queue
     */
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

    /**
     * Wait until the queue is not full, and insert an object the end of the
     * queue. Acquire the notFull semaphore to make sure the queue is not full,
     * and release a notEmpty semaphore when the object is inserted, to notify
     * the threads waiting on the queue not to be empty.
     *
     * @throws InterruptedException
     */
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
