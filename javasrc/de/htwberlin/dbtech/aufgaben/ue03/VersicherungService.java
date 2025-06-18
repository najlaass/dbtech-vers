package de.htwberlin.dbtech.aufgaben.ue03;

import de.htwberlin.dbtech.aufgaben.ue03.dao.*;
import de.htwberlin.dbtech.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class VersicherungService implements IVersicherungService {

    // Das hilft uns beim Ausdrucken von Infos, was gerade passiert.
    private static final Logger L = LoggerFactory.getLogger(VersicherungService.class);

    // Das ist die Verbindung zur Datenbank, damit wir Daten holen und speichern können.
    private Connection connection;

    // Hier sagen wir dem Service, welche Datenbankverbindung er nehmen soll.
    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    // Diese Methode prüft, ob wir eine Verbindung haben – sonst gibt's einen Fehler.
    private Connection useConnection() {
        if (connection == null) {
            throw new DataException("Connection not set"); // Verbindung fehlt
        }
        return connection;
    }

    // Das ist die Hauptmethode: Sie legt eine neue Deckung (Versicherungsschutz) an.
    @Override
    public void createDeckung(Integer vertragsId, Integer deckungsartId, BigDecimal deckungsbetrag) {
        try {
            // Wir holen die Verbindung zur Datenbank
            Connection conn = useConnection();

            // Wir holen uns alle kleinen Helfer, um Daten aus der Datenbank zu holen
            IVertragTDG vertragTDG = new VertragTDG(conn);
            IDeckungsartTDG deckungsartTDG = new DeckungsartTDG(conn);
            IDeckungsbetragTDG deckungsbetragTDG = new DeckungsbetragTDG(conn);
            IDeckungspreisTDG deckungspreisTDG = new DeckungspreisTDG(conn);
            IAblehnungsregelTDG ablehnungsregelTDG = new AblehnungsregelTDG(conn);
            IDeckungTDG deckungTDG = new DeckungTDG(conn);

            // 1. Schauen: Gibt es den Vertrag überhaupt?
            if (!vertragTDG.findVertragByID(vertragsId)) {
                throw new VertragExistiertNichtException(vertragsId);
            }

            // 2. Schauen: Gibt es diese Art von Versicherung?
            if (!deckungsartTDG.findDeckungsartByID(deckungsartId)) {
                throw new DeckungsartExistiertNichtException(deckungsartId);
            }

            // 3. Passt diese Art von Versicherung zu dem Vertrag?
            if (!vertragTDG.isProductMatch(vertragsId, deckungsartId)) {
                throw new DeckungsartPasstNichtZuProduktException(deckungsartId);
            }

            // 4. Ist der Betrag okay? (z. B. nicht zu hoch oder zu niedrig?)
            if (!deckungsbetragTDG.isValidDeckungsbetrag(deckungsartId, deckungsbetrag)) {
                throw new UngueltigerDeckungsbetragException(deckungsbetrag);
            }

            // 5. Gibt es einen Preis dafür?
            if (!deckungspreisTDG.hasDeckungspreis(deckungsartId, deckungsbetrag)) {
                throw new DeckungspreisNichtVorhandenException(deckungsbetrag);
            }

            // 6. Ist die Deckung erlaubt für das Alter vom Kunden?
            int alter = vertragTDG.findKundenAlterByVertragsId(vertragsId);
            if (ablehnungsregelTDG.doesRejectionRuleApply(deckungsartId, alter, deckungsbetrag)) {
                throw new DeckungsartNichtRegelkonformException(deckungsartId);
            }

            // 7. Alles ist okay! Jetzt wird die Deckung gespeichert.
            deckungTDG.addCoverage(vertragsId, deckungsartId, deckungsbetrag);
            L.info("Deckung erfolgreich hinzugefügt: Vertrag={}, Art={}, Betrag={}", vertragsId, deckungsartId, deckungsbetrag);

        } catch (SQLException e) {
            // Wenn etwas bei der Datenbank schiefläuft, sagen wir Bescheid
            throw new DataException("SQL Fehler in createDeckung: " + e.getMessage(), e);
        }
    }
}