package hangman;

import java.util.List;
import java.util. Random;
import java.net.Socket;

import reactor.Dispatcher;
import hangmanrules.HangmanRules;

/**
 * The server for the hangman game. Kicks off the game, i.e. creates an
 * AcceptHandle, which accepts TCP connections. HangmanServer also provides
 * an interface between the actual hangman game and the event handlers.
 */
public class HangmanServer {
    // allowed characters for the guess
    private static final String ALLOWED_GUESS_CHARS =
        "abcdefghijklmnopqrstuvwxyz";
    // random number generator used to draw a random guess character
    private static final Random random = new Random();

    private final Dispatcher dispatcher;
    private final HangmanRules<TCPTextHandler> game;
    private final AcceptHandler acceptHandler;

    private static final boolean DEBUG = false;

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
     * Create a random lower-case alphabetic character.
     */
    private char getRandomGuess() {
        int n = ALLOWED_GUESS_CHARS.length();
        char guess = ALLOWED_GUESS_CHARS.charAt(random.nextInt(n));
        return guess;
    }

    private boolean validGuess(char guess) {
        return (Character.isLetter(guess) && Character.isLowerCase(guess));
    }

    /**
     * Parses a guess character from a string with the following heuristic:
     *   - if the string is shorter than 1 character, use a random character.
     *   - if the string is longer than 1, find the first valid guess character,
     *     and if no valid character is found, use a random character. Note,
     *     that in this case we don't convert upper case character, but consider
     *     them as invalid as any other invalid character.
     *   - if the string is of length 1, check that it's valid. If not, but it's
     *     a letter, then convert it to lower case. If not letter, use random
     *     character.
     * Note that the 'random' means just a pseudo random character, generated by
     * using the java.util.Random
     *
     * @param guessString
     *              The string to be parsed
     *
     * @return a valid guess character
     */
    public char parseGuessCharacter(String guessString) {
        int length = guessString.length();
        char guess;
        String errorMsg = "";

        if (length < 1) {
            guess = getRandomGuess();
            errorMsg += "Received guess shorter than 1 character. " +
                "Using a random lower-case alphabetic character " +
                "(" + guess + ")";
        } else if (length > 1) {
            errorMsg += "Received guess longer than 1 character. ";
            boolean foundValid = false;

            // Just to make sure we have guess, should always be overwitten.
            guess = guessString.charAt(0);

            for (int i=0; i<length; i++) {
                guess = guessString.charAt(i);
                if (validGuess(guess)) {
                    errorMsg += "using the first valid character. " +
                        "(" + guess + ")";
                    foundValid = true;
                    break;
                }
            }

            if (!foundValid) {
                // The string didn't contain any valid characters. Use random.
                guess = getRandomGuess();
                errorMsg += "None of the characters in the string are valid, " +
                    "using a random lower-case alphabetic character " +
                    "(" + guess + ") instead";
            }
        } else {
            // The string is of length 1. Check that it's valid guess
            guess = guessString.charAt(0);
            if (!validGuess(guess)) {
                errorMsg += "The guess character (" + guess + ") " +
                    "is invalid. Should be a lower-case alphabetic " +
                    "character. ";

                // If the character is invalid, but letter (i.e. upper-case),
                // then convert to lower-case
                if (Character.isLetter(guess)) {
                    guess = Character.toLowerCase(guess);
                    errorMsg += "Converting to lower case.";
                } else {
                    guess = getRandomGuess();
                    errorMsg += "Using a random lower-case alphabetic " +
                        "character (" + guess + ") instead";
                }

            }
        }

        if (DEBUG) System.err.println(errorMsg);

        return guess;
    }

    /**
     * Make a guess for the player. Convert the string input to a valid
     * character before actually guessing. If the string is longer than one
     * character, then use the first character of it. If the string is empty,
     * use a whitespace.
     *
     * @param player
     *              the player who made the guess
     *
     * @param guessString
     *              the string received as a player input
     */
    public void makeGuess(HangmanRules<TCPTextHandler>.Player player,
                          String guessString) {

        char guess = parseGuessCharacter(guessString);

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
