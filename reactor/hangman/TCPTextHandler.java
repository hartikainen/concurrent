package hangman;

import java.net.Socket;

import reactorapi.EventHandler;
import hangmanrules.HangmanRules;

/**
 * An {@link EventHandler} that handles the string input from the
 * {@link Handle}. The actual game functionalities are handled through the
 * {@link HangmanServer}.
 */
public class TCPTextHandler implements EventHandler<String>{
    private final TCPTextHandle handle;
    private final HangmanServer server;
    private HangmanRules<TCPTextHandler>.Player player;

    /**
     * Create a handler that handles the string input from the
     * {@link TCPTextHandle}
     *
     * @param socket
     *            the socket for the TCPTextHandle
     *
     * @param server
     *            the actual server that the inputs are forwarded to.
     */
    public TCPTextHandler(Socket socket,
                          HangmanServer server) {

        this.handle = new TCPTextHandle(socket);
        this.server = server;
        this.player = null;
    }


    /**
     * Returns the {@link TCPTextHandle} associated with the
     * {@link TCPTextHandler}
     */
    @Override
    public TCPTextHandle getHandle() {
        return handle;
    }

    /**
     * Handle the string messages received from the {@link Handle}. The first
     * string input creates a new player to the game, and the subsequent strings
     * are the actual guesses. If the message is null, remove the handler from
     * the game. Also remove the player if it was created.
     *
     * @param s
     *            the message to be handled
     */
    @Override
    public void handleEvent(String s) {
        if (s == null) {
            if (player != null) {
                server.disconnectPlayer(player);
            } else {
                // Case when the handler is closed before the player is created
                server.removeTCPTextHandler(this);
            }
            return;
        }

        if (player == null) {
            player = server.addPlayer(this, s.trim());
        } else {
            server.makeGuess(player, s.trim());
        }
    }
}
