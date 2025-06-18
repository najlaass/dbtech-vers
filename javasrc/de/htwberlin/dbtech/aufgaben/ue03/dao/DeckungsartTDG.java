package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;

import java.sql.*;

public class DeckungsartTDG implements IDeckungsartTDG {

    // Das ist unsere Verbindung zur Datenbank
    private final Connection conn;

    // Wenn man das Objekt erstellt, speichern wir die Verbindung
    public DeckungsartTDG(Connection conn) {
        this.conn = conn;
    }

    // Diese Methode prüft: Gibt es diese Deckungsart wirklich in der Datenbank?
    @Override
    public boolean findDeckungsartByID(int id) throws SQLException {
        String sql = "SELECT 1 FROM Deckungsart WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id); // Wir setzen die gesuchte ID ein
            return ps.executeQuery().next(); // true, wenn etwas gefunden wurde
        } catch (SQLException e) {
            // Wenn was schiefgeht, sagen wir Bescheid
            throw new DataException(e);
        }
    }
}