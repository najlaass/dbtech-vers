package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;

import java.math.BigDecimal;
import java.sql.*;

public class DeckungsbetragTDG implements IDeckungsbetragTDG {

    // Verbindung zur Datenbank, damit wir darin nachschauen können
    private final Connection conn;

    // Wenn wir das Objekt bauen, speichern wir die Verbindung
    public DeckungsbetragTDG(Connection conn) {
        this.conn = conn;
    }

    // Diese Methode schaut nach:
    // Ist dieser Betrag für diese Versicherungsart erlaubt?
    @Override
    public boolean isValidDeckungsbetrag(int artId, BigDecimal betrag) throws SQLException {
        String sql = "SELECT 1 FROM Deckungsbetrag WHERE Deckungsart_FK = ? AND Deckungsbetrag = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, artId);        // Die Versicherungsart einsetzen
            ps.setBigDecimal(2, betrag); // Den Betrag einsetzen
            return ps.executeQuery().next(); // true → Betrag ist gültig
        } catch (SQLException e) {
            // Wenn beim Nachschauen ein Fehler passiert, sagen wir Bescheid
            throw new DataException(e);
        }
    }
}