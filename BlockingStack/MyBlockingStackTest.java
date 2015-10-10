public class MyBlockingStackTest {
    private static final int DEFAULT_CAPACITY = 10;

    public static void main(String[] args) {
        final MyBlockingStack<Integer> stack;

        int capacity = (args.length < 1) ? DEFAULT_CAPACITY : Integer.parseInt(args[0]);
        stack = new MyBlockingStack<Integer>(capacity);

        System.out.println("Start");

        Thread thread = new Thread(
            new Runnable() {
                @Override
                synchronized public void run() {
                    try {
                        this.wait(500);
                        stack.push(1);
                        this.wait(500);
                        stack.pop();
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        );

        thread.start();

        try {
            stack.pop();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        stack.size();

        for (int i=2; i<4; i++) {
            try {
                stack.push(i);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            stack.size();
        }

        System.out.println("End");
    }
}
