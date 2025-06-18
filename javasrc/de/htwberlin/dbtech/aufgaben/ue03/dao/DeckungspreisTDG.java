package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;

import java.math.BigDecimal;
import java.sql.*;

public class DeckungspreisTDG implements IDeckungspreisTDG {

    // Das ist die Verbindung zur Datenbank
    private final Connection conn;

    // Wenn das Objekt gebaut wird, speichern wir die Verbindung
    public DeckungspreisTDG(Connection conn) {
        this.conn = conn;
    }

    // Diese Methode prüft:
    // Gibt es gerade (heute) einen gültigen Preis für die Deckung?
    @Override
    public boolean hasDeckungspreis(int artId, BigDecimal betrag) throws SQLException {
        String sql = """
            SELECT 1 FROM Deckungspreis
            WHERE Deckungsbetrag_FK IN (
                SELECT ID FROM Deckungsbetrag
                WHERE Deckungsart_FK = ? AND Deckungsbetrag = ?
            )
            AND Gueltig_Von <= CURRENT_DATE
            AND (Gueltig_Bis IS NULL OR Gueltig_Bis >= CURRENT_DATE)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, artId);        // Versicherungsart einsetzen
            ps.setBigDecimal(2, betrag); // Betrag einsetzen
            return ps.executeQuery().next(); // true → Preis existiert und ist heute gültig
        } catch (SQLException e) {
            // Wenn etwas bei der Datenbank schiefläuft, sagen wir Bescheid
            throw new DataException(e);
        }
    }
}