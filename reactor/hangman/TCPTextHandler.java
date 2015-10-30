package hangman;

import java.net.Socket;
import java.util.*;

import reactorapi.EventHandler;
import hangmanrules.HangmanRules;
import hangmanrules.HangmanRules.Player;

import reactor.Dispatcher;

public class TCPTextHandler implements EventHandler<String>{
    private final TCPTextHandle handle;
    private final HangmanRules rules;
    private final Dispatcher dispatcher;
    private Player player;

    public TCPTextHandler(Socket socket,
                          Dispatcher dispatcher,
                          HangmanRules rules) {

        this.handle = new TCPTextHandle(socket);
        this.rules = rules;
        this.dispatcher = dispatcher;
        this.player = null;
    }

    @Override
    public TCPTextHandle getHandle() {
        return handle;
    }

    @Override
    public void handleEvent(String s) {
        if (s == null) {
            dispatcher.removeHandler(this);
            return;
        }

        s = s.trim();

        if (player == null) {
            createPlayer(s);
        } else {
            makeGuess(s);
        }
    }

    /**
     * Broadcasts a message to each player in the game.
     */
    private void broadcast(String message) {
        TCPTextHandler playerHandler;
        List<?> playersO = rules.getPlayers();

        for (Object o : playersO) {
            Player plr = (Player)o;
            playerHandler = (TCPTextHandler)plr.playerData;
            playerHandler.getHandle().write(message);
        }
    }

    private void closeGame() {
        TCPTextHandler playerHandler;
        List<?> playersO = rules.getPlayers();

        for (Object o : playersO) {
            Player plr = (Player)o;
            playerHandler = (TCPTextHandler)plr.playerData;
            playerHandler.close();
        }
    }

    private void makeGuess(String guessString) {
        if (guessString.length() > 1) {
            System.err.println("Received guess longer than one char." +
                               "Using charAt(0), ignoring rest.");
        }

        // Just to make sure we don't call charAt(0) for 0 length string
        char guess = (guessString.length() > 0) ? guessString.charAt(0) : ' ';

        rules.makeGuess(guess);

        String message = player.getGuessString(guess);
        broadcast(message);

        System.err.println("TODO: remove, making guess");

        if (rules.gameEnded()) {
            closeGame();
        }
    }

    private void createPlayer(String name) {
        player = rules.addNewPlayer(this, name);
        String status = rules.getStatus();
        handle.write(status);
    }

    private void close() {
        handle.close();
        dispatcher.removeHandler(this);
    }
}
