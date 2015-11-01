package reactor;

import reactorapi.EventHandler;
import reactorapi.Handle;

public class WorkerThread<T> extends Thread {
    private final EventHandler<T> handler;
    private final BlockingEventQueue<Object> queue;
    private boolean running = true;

    /**
     * Create a new WorkerThread.
     *
     * @param eh
     *              The {@link EventHandler} whose {@link Handle} is read.
     *
     * @param q
     *              The {@link BlockingEventQueue} to push the events to.
     */
    public WorkerThread(EventHandler<T> eh, BlockingEventQueue<Object> q) {
        this.handler = eh;
        this.queue = q;
    }

    /**
     * Loop until the thread is cancelled, or a null event is received. Read the
     * data from the handle, create and event, and push it to the queue.
     */
    @Override
    public void run() {
        Handle<T> handle = handler.getHandle();
        Event<T> event;
        T data;

        while (running) {
            data = handle.read();
            event = new Event<T>(data, handler);

            try {
                queue.put(event);
            } catch (InterruptedException e) {
                return;
            }

            if (data == null) break;
        }
    }

    /**
     * Set the value of running to <code>false</code>, and interrupt the thread
     * to make sure that the <code>queue.put</code> in the {@link #run()}
     * will not get locked.
     */
    public void cancelThread() {
        running = false;
        interrupt();
    }
}
