package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;

import java.math.BigDecimal;
import java.sql.*;

public class DeckungTDG implements IDeckungTDG {

    // Das ist unsere Verbindung zur Datenbank
    private final Connection connection;

    // Wenn das Objekt erstellt wird, merken wir uns die Verbindung
    public DeckungTDG(Connection connection) {
        this.connection = connection;
    }

    // Diese Methode fügt eine neue Deckung in die Datenbank ein.
    // Also: Eine Versicherung mit Vertrag, Art und Betrag wird gespeichert.
    @Override
    public void addCoverage(int vertragId, int artId, BigDecimal betrag) throws SQLException {
        String sql = "INSERT INTO Deckung (Vertrag_FK, Deckungsart_FK, Deckungsbetrag) VALUES (?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, vertragId);     // Vertragsnummer einfügen
            ps.setInt(2, artId);         // Versicherungsart einfügen
            ps.setBigDecimal(3, betrag); // Betrag einfügen
            ps.executeUpdate();          // In Datenbank schreiben
        } catch (SQLException e) {
            // Wenn was schiefgeht, sagen wir Bescheid
            throw new DataException(e);
        }
    }
}