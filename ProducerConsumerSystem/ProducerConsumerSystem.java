import java.text.StringCharacterIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ProducerConsumerSystem {

	//Producer implementation
	static class Producer extends Thread {
		private final String producerName;
		//TODO add variables

		final StringCharacterIterator source =
				new StringCharacterIterator(" concurrent programming rocks");

		public Producer(/* data buffer and other arguments of your choise */, String n) {
			this.producerName = n;
			//TODO
		}

		@Override
		public void run() {
			//TODO implement the following
			// - produce data with produce()
			// - append the date to the buffer
			// - loop until the entire source string has been handled
		}

		public Data produce() throws InterruptedException {
			//Producers are asynchronous, simulate arbitrary timing
			//with a randomized sleep timer
			Thread.sleep((long)(Math.random() * 3000));

			//Return the next character in the source string or
			//StringCharacterIterator.DONE if the entire string has been consumed
			return new Data(source.next(), this.producerName);
		}
	}

	//Consumer implementation
	static class Consumer extends Thread {
		//TODO add variables

		public Consumer(/* data buffer and other arguments of your choise */) {
			//TODO
		}

		@Override
		public void run() {
			//TODO implement the following
			// - take data from the buffer
			// - process the data by calling consume()
			// - loop forever (or until all three producers exit)
		}

		private void consume(Data msg) {
			//Processing is simply printing to System.out
			System.out.println(msg.msg + " from " + msg.sender);
		}
	}

	//The producers and consumers exchange data wrapped in an immutable class
	//For this task, data is simply a character of input
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
		//TODO implement the following
		// - create an instance of a data buffer of your choice
		// - create three producers, each with an unique name
		// - create a single consumer
		// - start all of the producers and consumers
	}
}
