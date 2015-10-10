import java.util.ArrayList;

public class MyBlockingStack<T> implements BlockingStack<T> {
    private int capacity;
    private final ArrayList<T> stack;

    private final Object notEmpty = new Object();
    private final Object notFull  = new Object();

    public MyBlockingStack(int capacity) {
        this.capacity = capacity;
        this.stack = new ArrayList<T>(capacity);
    }

    public void push(T object) throws InterruptedException {
        if (object == null) throw new IllegalArgumentException();

        System.out.println("In push method");

        synchronized (stack) {
            while (stack.size() >= capacity) {
                System.out.println("Queue full, waiting...");
                try { stack.wait(); }
                catch (InterruptedException ie) {
                    throw ie;
                    //TODO: what should be done?
                }
            }

            stack.add(0, object);
            System.out.println("Pushed: " + object);

            if (stack.size() == 1) {
                stack.notifyAll();
            }
        }
    }

    public T pop() throws InterruptedException {
        final T object;

        System.out.println("In pop method");

        synchronized (stack) {
            while (stack.size() < 1) {
                System.out.println("Queue empty, waiting...");
                // TODO: Handle notEmpty and notFull with sempahores
                try { stack.wait(); }
                catch (InterruptedException ie) {
                    throw ie;
                    //TODO: what should be done?
                }
            }

            object = stack.remove(0);
            System.out.println("Popped: " + object);

            if (stack.size() == capacity - 1) {
                stack.notifyAll();
            }
        }

        return object;
    }

    public int size() {
        final int size;

        synchronized (stack) {
            size = stack.size();
        }
        System.out.println("Stack size: " + size);

        return size;
    }
}
