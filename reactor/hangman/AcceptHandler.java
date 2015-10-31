package hangman;

import java.net.Socket;
import java.io.IOException;

import reactorapi.EventHandler;
import reactor.Dispatcher;
import hangmanrules.HangmanRules;

public class AcceptHandler implements EventHandler<Socket> {
    AcceptHandle handle;
    Dispatcher dispatcher;
    HangmanServer server;
    HangmanRules rules;

    public AcceptHandler(HangmanServer server,
                         Dispatcher dispatcher,
                         HangmanRules rules) {
        try {
            this.handle = new AcceptHandle();
        } catch (IOException ie) {
            return;
        }
        this.server = server;
        this.rules  = rules;
        this.dispatcher = dispatcher;
    }

    @Override
    public AcceptHandle getHandle() {
        return handle;
    }

    @Override
    public void handleEvent(Socket socket) {
        if (socket == null) {
            dispatcher.removeHandler(this);
            return;
        }

        TCPTextHandler handler = new TCPTextHandler(socket,
                                                    dispatcher,
                                                    rules);
        dispatcher.addHandler(handler);
    }
}
