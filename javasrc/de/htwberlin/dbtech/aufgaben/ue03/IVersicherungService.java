package de.htwberlin.dbtech.aufgaben.ue03;

/*
  @author Ingo Classen
 */

import de.htwberlin.dbtech.exceptions.*;

import java.math.BigDecimal;
import java.sql.Connection;

public interface IVersicherungService {

    /**
     * Speichert die Verbindung zur Datenbank,
     * damit wir später damit arbeiten können.
     */
    void setConnection(Connection connection);

    /**
     * Fügt eine neue Versicherung (Deckung) zu einem Vertrag hinzu.
     *
     * @param vertragsId     Die Nummer vom Vertrag (also von wem die Versicherung ist).
     * @param deckungsartId  Die Nummer der Versicherungsart (z. B. Glas, Feuer, usw.).
     * @param deckungsbetrag Wie viel Geld abgesichert werden soll.
     *
     * @throws VertragExistiertNichtException Wenn der Vertrag nicht gefunden wird.
     * @throws DeckungsartExistiertNichtException Wenn die Versicherungsart nicht existiert.
     * @throws UngueltigerDeckungsbetragException Wenn der Betrag nicht erlaubt ist.
     * @throws DeckungsartPasstNichtZuProduktException Wenn diese Art nicht zu diesem Vertrag passt.
     * @throws DeckungsartNichtRegelkonformException Wenn Regeln sagen: Diese Art geht hier nicht (z. B. wegen Alter).
     * @throws DeckungspreisNichtVorhandenException Wenn wir keinen Preis für diese Versicherung finden.
     */
    void createDeckung(Integer vertragsId, Integer deckungsartId, BigDecimal deckungsbetrag);

}