package reactor;

/**
 * General (weak) semaphore.
 *
 */
public class Semaphore {
    private int value;

    /**
     * Create a general (weak) semaphore
     *
     * @param value
     *            the capacity of the semaphore
     */
    public Semaphore(int value) {
        if (value < 0) throw new IllegalArgumentException();
        this.value = value;
    }

    public synchronized void acquire() throws InterruptedException {
        while (value < 1) {
            wait();
        }
        value--;
    }

    public synchronized int tryAcquireAll() {
        int oldValue = value;
        value = 0;
        return oldValue;
    }

    public synchronized void release() {
        value++;
        notify();
    }

    public synchronized void release(int target) {
        value = target;
        notifyAll();
    }
}
