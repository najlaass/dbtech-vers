package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;

public class VertragTDG implements IVertragTDG {

    // Das ist die Verbindung zur Datenbank.
    private final Connection conn;

    // Wenn wir dieses Objekt bauen, speichern wir die Datenbankverbindung.
    public VertragTDG(Connection conn) {
        this.conn = conn;
    }

    // Guckt nach, ob es den Vertrag mit dieser ID wirklich gibt.
    @Override
    public boolean findVertragByID(int vertragsId) throws SQLException {
        String sql = "SELECT 1 FROM Vertrag WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vertragsId);
            return ps.executeQuery().next(); // true, wenn etwas gefunden wurde
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    // Prüft, ob die Versicherungsart zu dem Produkt im Vertrag passt.
    @Override
    public boolean isProductMatch(int vertragsId, int deckungsartId) throws SQLException {
        String sql = """
            SELECT p1.ID AS VertragProduktID, p2.ID AS DeckungsartProduktID
            FROM Vertrag v
            JOIN Produkt p1 ON v.Produkt_FK = p1.ID
            JOIN Deckungsart d ON d.ID = ?
            JOIN Produkt p2 ON d.Produkt_FK = p2.ID
            WHERE v.ID = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckungsartId);    // Deckungsart einsetzen
            ps.setInt(2, vertragsId);       // Vertrags-ID einsetzen
            ResultSet rs = ps.executeQuery();
            // Passt nur, wenn beide Produkt-IDs gleich sind
            return rs.next() && rs.getInt("VertragProduktID") == rs.getInt("DeckungsartProduktID");
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }

    // Holt das Geburtsdatum vom Kunden zu diesem Vertrag und rechnet aus, wie alt er ist.
    @Override
    public int findKundenAlterByVertragsId(int vertragsId) throws SQLException {
        String sql = """
            SELECT K.Geburtsdatum
            FROM Vertrag V
            JOIN Kunde K ON V.Kunde_FK = K.ID
            WHERE V.ID = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, vertragsId); // Vertrags-ID einsetzen
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Wie alt ist der Kunde heute?
                return Period.between(
                        rs.getDate("Geburtsdatum").toLocalDate(),
                        LocalDate.now()
                ).getYears();
            }
            throw new DataException("Kunde nicht gefunden");
        } catch (SQLException e) {
            throw new DataException(e);
        }
    }
}