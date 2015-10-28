package reactor;

import reactorapi.*;

public class WorkerThread<T> extends Thread {
    private final EventHandler<T> handler;
    private final BlockingEventQueue<Object> queue;
    private boolean running = true;

    // Additional fields are allowed.

    public WorkerThread(EventHandler<T> eh, BlockingEventQueue<Object> q) {
        handler = eh;
        queue = q;
    }

    public void run() {
        while (running) {
            T data = handler.getHandle().read();
            Event<T> event = new Event<T>(data, handler);

            try {
                queue.put(event);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (data == null) break;
        }
        // TODO: Implement WorkerThread.run().
    }

    public void cancelThread() {
        running = false;
        // TODO: Implement WorkerThread.cancelThread().
    }
}
