package reactor;

import reactorapi.*;

public class Dispatcher {
    private static final int DEFAULT_CAPACITY = 10;
    private final int capacity;

    public Dispatcher() {
        this(DEFAULT_CAPACITY);
    }

    public Dispatcher(int capacity) {
        this.capacity = capacity;
        // TODO: Implement Dispatcher(int).
    }

    public void handleEvents() throws InterruptedException {
        // TODO: Implement Dispatcher.handleEvents().
    }

    public Event<?> select() throws InterruptedException {
        throw new UnsupportedOperationException();
        // TODO: Implement Dispatcher.select().
    }

    public void addHandler(EventHandler<?> h) {
        // TODO: Implement Dispatcher.addHandler(EventHandler).
    }

    public void removeHandler(EventHandler<?> h) {
        // TODO: Implement Dispatcher.removeHandler(EventHandler).
    }

    // Add methods and fields as needed.
}
