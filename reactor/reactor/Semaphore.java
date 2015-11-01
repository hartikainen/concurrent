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

    /**
     * Wait until at least one token is available, and then acquire it, and
     * reduce the value.
     *
     * @throws InterruptedException
     */
    public synchronized void acquire() throws InterruptedException {
        while (value < 1) {
            wait();
        }
        value--;
    }

    /**
     * Acquire all the token from the Semaphore, without waiting.
     *
     * @return the amount of tokens acquired.
     */
    public synchronized int tryAcquireAll() {
        int oldValue = value;
        value = 0;
        return oldValue;
    }

    /**
     * Release a token from the Semaphore, and notify the waiting threads.
     */
    public synchronized void release() {
        value++;
        notify();
    }

    /**
     * Release tokens up to a target number.
     *
     * @param target
     *              The maximum number of token to release.
     */
    public synchronized void release(int target) {
        value = target;
        notifyAll();
    }
}
