package de.htwberlin.dbtech.aufgaben.ue03.dao;

import de.htwberlin.dbtech.exceptions.DataException;
import de.htwberlin.dbtech.exceptions.DeckungsartNichtRegelkonformException;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AblehnungsregelTDG implements IAblehnungsregelTDG {

    // Das ist unsere Verbindung zur Datenbank
    private final Connection conn;

    // Wenn wir ein Objekt bauen, speichern wir die Verbindung
    public AblehnungsregelTDG(Connection conn) {
        this.conn = conn;
    }

    // Das ist eine kleine Klasse für Regeln mit Betrag und Alter
    public record Ablehnungsregel(String betragRegel, String alterRegel) {}

    // Holt alle Regeln aus der Datenbank, die zu dieser Deckungsart passen
    private List<Ablehnungsregel> ladeRegeln(int deckungsartId) {
        String sql = "SELECT R_Betrag, R_Alter FROM Ablehnungsregel WHERE Deckungsart_FK = ?";
        List<Ablehnungsregel> regeln = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, deckungsartId); // Wir suchen nach dieser Deckungsart
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                // Für jede Regel speichern wir Betrag-Regel und Alter-Regel
                regeln.add(new Ablehnungsregel(rs.getString("R_Betrag"), rs.getString("R_Alter")));
            }
        } catch (SQLException e) {
            throw new DataException(e); // Wenn was schiefgeht, melden wir das
        }

        return regeln;
    }

    // Schaut, ob eine Ablehnungsregel passt → dann darf die Deckung NICHT gemacht werden
    @Override
    public boolean doesRejectionRuleApply(int deckungsartId, int alter, BigDecimal betrag) {
        for (Ablehnungsregel r : ladeRegeln(deckungsartId)) {
            // Prüfen: passt der Betrag zur Regel? passt das Alter zur Regel?
            boolean betragOk = pruefeRegelOhneException(betrag, r.betragRegel());
            boolean alterOk = pruefeRegelOhneException(BigDecimal.valueOf(alter), r.alterRegel());
            if (betragOk && alterOk) return true; // Wenn beides passt: Regel greift!
        }
        return false; // Keine Regel hat gepasst → alles ok
    }

    // Prüft eine Regel (z. B. "<18" oder ">=100000") → true oder false
    private boolean pruefeRegelOhneException(BigDecimal wert, String regel) {
        if (regel == null || regel.strip().equals("- -")) return true; // Regel ist leer → alles ok
        String regelBereinigt = regel.strip().replace(" ", "").toUpperCase();

        // Wenn das Format nicht stimmt, sagen wir einfach: passt nicht
        if (!regelBereinigt.matches("([<>=!]{1,2})(\\d+)")) {
            return false;
        }

        // Wir trennen Zeichen (z. B. ">=") und die Zahl (z. B. "100")
        String op = regelBereinigt.replaceAll("[0-9]", "");
        BigDecimal rWert = new BigDecimal(regelBereinigt.replaceAll("[^0-9]", ""));

        // Jetzt vergleichen wir je nach Regelzeichen
        return switch (op) {
            case "=" -> wert.compareTo(rWert) == 0;
            case "!=" -> wert.compareTo(rWert) != 0;
            case "<" -> wert.compareTo(rWert) < 0;
            case "<=" -> wert.compareTo(rWert) <= 0;
            case ">" -> wert.compareTo(rWert) > 0;
            case ">=" -> wert.compareTo(rWert) >= 0;
            default -> false; // Falls irgendwas komisch ist
        };
    }
}