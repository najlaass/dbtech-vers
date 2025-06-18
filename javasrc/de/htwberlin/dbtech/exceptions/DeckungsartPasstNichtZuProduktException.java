package de.htwberlin.dbtech.exceptions;

/**
 * @author Ingo Classen
 */
public class DeckungsartPasstNichtZuProduktException extends VersicherungException {

    public DeckungsartPasstNichtZuProduktException() {
        super();
    }

    public DeckungsartPasstNichtZuProduktException(Integer deckungsartId) {
        super("Die Deckungsart mit ID " + deckungsartId + " passt nicht zum Produkt des Vertrags.");
    }


}
