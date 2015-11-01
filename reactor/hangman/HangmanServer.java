package hangman;

import java.net.Socket;

import java.util.List;
import reactor.Dispatcher;
import hangmanrules.HangmanRules;

/**
 * The server for the hangman game. Kicks off the game, i.e. creates an
 * AcceptHandle, which accepts TCP connections. HangmanServer also provides
 * an interface between the actual hangman game and the event handlers.
 */
public class HangmanServer {
    private final Dispatcher dispatcher;
    private final HangmanRules<TCPTextHandler> game;
    private final AcceptHandler acceptHandler;

    public HangmanServer(String word, int tries) {
        this.dispatcher = new Dispatcher();
        this.game = new HangmanRules<TCPTextHandler>(word, tries);
        this.acceptHandler = new AcceptHandler(this);
    }

    /**
     * Add a new {@link TCPTextHandler}, .i.e. a new player connection to the
     * game. Note that, the actual player for the {@link game} is added only
     * after the handler has received a player name as an input.
     *
     * @param socket
     *              the socket for the player input/output.
     */
    public void connect(Socket socket) {
        TCPTextHandler handler = new TCPTextHandler(socket, this);
        dispatcher.addHandler(handler);
    }

    /**
     * Add a new {@link HangmanRules.Player} to the {@link game}. Use the
     * {@link TCPTextHandler} as the data associated to the player. After the
     * player has been added to the game, send a game status as a message to it.
     *
     * @param handler
     *              the handler associated to the player
     *
     * @param name
     *              the name of the player to be added
     */
    public HangmanRules<TCPTextHandler>.Player addPlayer(TCPTextHandler handler,
                                                         String name) {
        HangmanRules<TCPTextHandler>.Player player = game.addNewPlayer(handler,
                                                                        name);
        sendMessage(player, game.getStatus());
        return player;
    }

    /**
     * Disconnects a player from the game, also removing the associated handler.
     *
     * @param player
     *              the player to be disconnected
     */
    public void disconnectPlayer(HangmanRules<TCPTextHandler>.Player player) {
        TCPTextHandler handler = (TCPTextHandler)player.playerData;
        removeTCPTextHandler(handler);
        game.removePlayer(player);
    }

    /**
     * Removes a {@link TCPTextHandler} from the dispatcher. Also closes the
     * {@link TCPTextHandle}.
     *
     * @param handler
     *              handler to be removed
     */
    public void removeTCPTextHandler(TCPTextHandler handler) {
        dispatcher.removeHandler(handler);
        handler.getHandle().close();
    }

    /**
     * Send a message to a player, using the {@link TCPTextHandle} from the
     * {@link TCPTextHandler} associated with the player.
     *
     * @param player
     *              the player to send the message to
     *
     * @param message
     *              the message to be sent
     */
    private void sendMessage(HangmanRules<TCPTextHandler>.Player player,
                             String message) {
        TCPTextHandler handler = (TCPTextHandler)player.playerData;
        TCPTextHandle handle = handler.getHandle();
        handle.write(message);
    }

    /**
     * Broadcasts a message to each player in the {@link game}.
     *
     * @param message
     *              the message to be sent
     */
    private void broadcastMessage(String message) {
        List<HangmanRules<TCPTextHandler>.Player> players = game.getPlayers();

        for (HangmanRules<TCPTextHandler>.Player player : players) {
            sendMessage(player, message);
        }
    }

    /**
     * Make a guess for the player. Convert the string input to a character
     * before actually guessing. If the string is longer than one character,
     * then use the first character of it. If the string is empty, use a
     * whitespace.
     *
     * @param player
     *              the player who made the guess
     *
     * @param guessString
     *              the string received as a player input
     */
    public void makeGuess(HangmanRules<TCPTextHandler>.Player player,
                          String guessString) {
        if (guessString.length() > 1) {
            System.err.println("Received guess longer than one char." +
                               "Using charAt(0), ignoring rest.");
        }

        // Just to make sure we don't call charAt(0) for 0 length string
        char guess = (guessString.length() > 0) ? guessString.charAt(0) : ' ';

        game.makeGuess(guess);

        String message = player.getGuessString(guess);
        broadcastMessage(message);

        if (game.gameEnded()) {
            endGame();
        }
    }

    /**
     * Kick-off the server by adding the {@link acceptHandler} to the dispatcher
     * and start the dispatcher event handle loop.
     */
    public void startGame() {
        dispatcher.addHandler(acceptHandler);

        try {
            dispatcher.handleEvents();
        } catch (InterruptedException ie) {
            return;
        }
    }

    /**
     * Shut down the game and the server. Remove all the player handlers from
     * the dispatcher, close all the handles, and remove players. Also remove
     * the {@link acceptHandler} from the dispatcher, and close its handle.
     *
     * There should be no other handlers in the dispatcher apart from the player
     * handlers and the acceptHandler.
     */
    public void endGame() {
        List<HangmanRules<TCPTextHandler>.Player> players = game.getPlayers();

        for (HangmanRules<TCPTextHandler>.Player player : players) {
            disconnectPlayer(player);
        }

        dispatcher.removeHandler(acceptHandler);
        acceptHandler.getHandle().close();
    }

    /**
     * The main function to start the hangman server. Creates a new
     * {@link HangmanServer} instance with the word and tries parsed from the
     * args, and calls its {@link startGame} function.
     *
     * @param args
     *              the word to be guesses, and the amount of guesses allowed
     */
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
