import java.text.StringCharacterIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

// Simple Producer Consumer system simulator
public class ProducerConsumerSystem {

    private static final int PRODUCER_COUNT = 3;

    static class Producer extends Thread {
        private final String producerName;
        private final LinkedBlockingQueue<Data> buffer;

        final StringCharacterIterator source =
            new StringCharacterIterator(" concurrent programming rocks");

        public Producer(LinkedBlockingQueue<Data> buffer, String n) {
            this.buffer = buffer;
            this.producerName = n;
        }

        @Override
        public void run() {
            Data data;

            do {
                try {
                    data = produce();
                } catch (InterruptedException ie) { return; }

                synchronized (buffer) {
                    try {
                        buffer.put(data);
                        buffer.notify();
                    } catch (InterruptedException ie) { return; }
                }
            } while (!data.isEnd);
        }

        public Data produce() throws InterruptedException {
            // Random sleep to simulate asynchronicity
            Thread.sleep((long)(Math.random() * 300));

            // Return the next character in the source string
            // next() returns StringCharacterIterator.DONE
            // if the entire string has been consumed
            return new Data(source.next(), this.producerName);
        }
    }

    static class Consumer extends Thread {
        private final LinkedBlockingQueue<Data> buffer;

        public Consumer(LinkedBlockingQueue<Data> buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            int endCount = 0;
            Data data;

            while (endCount < PRODUCER_COUNT) {
                synchronized (buffer) {
                    try {
                        while (buffer.size() < 1) {
                            buffer.wait();
                        }
                    } catch (InterruptedException ie) { }

                    try {
                        data = buffer.take();
                        if (data.isEnd) {
                            endCount++;
                        } else {
                            consume(data);
                        }
                    } catch (InterruptedException ie) { }
                }
            }
        }

        private void consume(Data msg) {
            // Simply print out to System.out
            System.out.println(msg.msg + " from " + msg.sender);
        }
    }

    // The Data is simply a character
    static final class Data {
        public final char msg;
        public final String sender;
        public final boolean isEnd;

        public Data(char msg, String sender) {
            this.msg = msg;
            this.sender = sender;
            this.isEnd = (msg == StringCharacterIterator.DONE);
        }
    }

    public static void main(String[] args) {
        LinkedBlockingQueue<Data> buffer = new LinkedBlockingQueue<Data>();
        Producer[] producers = new Producer[PRODUCER_COUNT];
        Consumer consumer;;
        String name;

        for (int i=0; i<PRODUCER_COUNT; i++) {
            name = "producer " + i;
            producers[i] = new Producer(buffer, name);
        }

        consumer = new Consumer(buffer);

        for (int i=0; i<PRODUCER_COUNT; i++) {
            producers[i].start();
        }

        consumer.start();
    }
}
