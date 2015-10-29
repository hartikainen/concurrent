package reactor;

import reactorapi.EventHandler;
import reactorapi.Handle;

public class WorkerThread<T> extends Thread {
    private final EventHandler<T> handler;
    private final BlockingEventQueue<Object> queue;
    private boolean running = true;

    public WorkerThread(EventHandler<T> eh, BlockingEventQueue<Object> q) {
        handler = eh;
        queue = q;
    }

    /**
     * Read the data from the handle, create and event, and push it to the queue
     */
    public void run() {
        Handle<T> handle = handler.getHandle();

        while (running) {
            T data = handle.read();
            Event<T> event = new Event<T>(data, handler);

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
