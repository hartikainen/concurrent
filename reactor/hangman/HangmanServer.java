package hangman;

import reactor.Dispatcher;
import reactorapi.EventHandler;
import hangmanrules.HangmanRules;

public class HangmanServer {
    private final Dispatcher dispatcher;
    private final HangmanRules<TCPTextHandler> rules;

    public HangmanServer(String word, int tries) {
        this.dispatcher = new Dispatcher();
        //this.rules = new HangmanRules<EventHandler<?>>(word, tries);
        this.rules = new HangmanRules<TCPTextHandler>(word, tries);
    }

    public void startGame() {
        AcceptHandler ah = new AcceptHandler(this,
                                             dispatcher,
                                             rules);
        dispatcher.addHandler(ah);

        try {
            dispatcher.handleEvents();
        } catch (InterruptedException ie) {
            return;
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Expected two arguments." +
                               "Usage: HangmanServer <word> <tries>");
            return;
        }

        String word = args[0];
        int tries = Integer.parseInt(args[1]);

        HangmanServer server = new HangmanServer(word, tries);
        server.startGame();
    }
}
