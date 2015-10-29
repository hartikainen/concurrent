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
        EventHandle<T> handle = handler.getHandle();

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
        // TODO: Implement WorkerThread.run().
    }

    public void cancelThread() {
        running = false;
        interrupt();
        // TODO: Implement WorkerThread.cancelThread().
    }
}
