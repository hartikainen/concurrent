package hangman;

import java.net.Socket;
import java.io.IOException;

import reactorapi.EventHandler;

/**
 * An {@link EventHandler} that handles the game connections.
 */
public class AcceptHandler implements EventHandler<Socket> {
    AcceptHandle handle;
    HangmanServer server;

    /**
     * Create a handler that handler the socket input from the
     * {@link AcceptHandle}. The functionalities are handled through the
     * {@link HangmanServer}.
     *
     * @param server
     *            the hangman server to connect the players to.
     */
    public AcceptHandler(HangmanServer server) {
        try {
            this.handle = new AcceptHandle();
        } catch (IOException ie) {
            return;
        }

        this.server = server;
    }

    /**
     * Returns the {@link AcceptHandle} associated with the
     * {@link AcceptHandler}
     */
    @Override
    public AcceptHandle getHandle() {
        return handle;
    }

    /**
     * Handle the socket input received from the {@link Handle}. If the socket
     * sends null, end the hangman game. Otherwise connect a new player to the
     * server.
     *
     * @param socket
     *            the socket to be connected
     */
    @Override
    public void handleEvent(Socket socket) {
        if (socket == null) {
            server.endGame();
        }

        server.connect(socket);
    }
}
