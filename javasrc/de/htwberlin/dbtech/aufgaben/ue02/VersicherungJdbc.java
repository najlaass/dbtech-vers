package de.htwberlin.dbtech.aufgaben.ue02;


/*
  @author Ingo Classen
 */

import de.htwberlin.dbtech.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * VersicherungJdbc
 */
public class VersicherungJdbc implements IVersicherungJdbc {
    private static final Logger L = LoggerFactory.getLogger(VersicherungJdbc.class);
    private Connection connection;

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @SuppressWarnings("unused")
    private Connection useConnection() {
        if (connection == null) {
            throw new DataException("Connection not set");
        }
        return connection;
    }

    @Override
    public List<String> kurzBezProdukte() {
        L.info("start");
        String sqlString = "select kurzBez from produkt order by id";
        List<String> produktAbkuerzungen = new ArrayList<>();
        try {
            Statement s = useConnection().createStatement();
            ResultSet r = s.executeQuery(sqlString);
            while (r.next()) {
                String abkuerzung = r.getString("kurzBez");
                produktAbkuerzungen.add(abkuerzung);
                System.out.println(abkuerzung);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        L.info("ende");
        return produktAbkuerzungen;
    }

    @Override
    public Kunde findKundeById(Integer id) throws KundeExistiertNichtException {
        L.info("id: " + id);
        String sqlString = "SELECT * FROM kunde WHERE id = ?";
        try {
            PreparedStatement ps = useConnection().prepareStatement(sqlString);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Kunde kunde = new Kunde();
                kunde.setId(rs.getInt("id"));
                kunde.setName(rs.getString("name"));
                LocalDate geburtsdatum = rs.getDate("geburtsdatum").toLocalDate();
                kunde.setGeburtsdatum(geburtsdatum);

                L.info("ende");
                return kunde;
            } else {
                L.info("ende");
                throw new KundeExistiertNichtException(id);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Fehler beim Abrufen des Kunden mit ID " + id, e);
        }
    }

    @Override
    public void createVertrag(Integer id, Integer produktId, Integer kundenId, LocalDate versicherungsbeginn) {
        L.info("id: " + id);
        L.info("produktId: " + produktId);
        L.info("kundenId: " + kundenId);
        L.info("versicherungsbeginn: " + versicherungsbeginn);

        try {
            if (!kundeExistiert(kundenId)) {
                throw new KundeExistiertNichtException(kundenId); // ✅ Hier korrigiert
            }

            if (versicherungsbeginn.isBefore(LocalDate.now())) {
                throw new DatumInVergangenheitException(versicherungsbeginn);
            }

            // Berechne das Versicherungsende
            LocalDate versicherungsende = versicherungsbeginn.plusYears(1).minusDays(1);
            L.info("versicherungsende: " + versicherungsende);

            if (vertragExistiert(id)) {
                throw new VertragExistiertBereitsException(id);
            }

            if (!produktExistiert(produktId)) {
                throw new ProduktExistiertNichtException(produktId);
            }

            String sqlInsert = "INSERT INTO vertrag (id, produkt_fk, kunde_fk, versicherungsbeginn, versicherungsende) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = useConnection().prepareStatement(sqlInsert);
            ps.setInt(1, id);
            ps.setInt(2, produktId);
            ps.setInt(3, kundenId);
            ps.setDate(4, java.sql.Date.valueOf(versicherungsbeginn));
            ps.setDate(5, java.sql.Date.valueOf(versicherungsende));
            ps.executeUpdate();

            L.info("Vertrag erfolgreich erstellt.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean vertragExistiert(Integer id) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM vertrag WHERE id = ?";
        PreparedStatement ps = useConnection().prepareStatement(sqlCheck);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    private boolean produktExistiert(Integer produktId) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM produkt WHERE id = ?";
        PreparedStatement ps = useConnection().prepareStatement(sqlCheck);
        ps.setInt(1, produktId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    private boolean kundeExistiert(Integer kundenId) throws SQLException {
        String sqlCheck = "SELECT COUNT(*) FROM kunde WHERE id = ?";
        PreparedStatement ps = useConnection().prepareStatement(sqlCheck);
        ps.setInt(1, kundenId);
        ResultSet rs = ps.executeQuery();
        rs.next();
        return rs.getInt(1) > 0;
    }

    @Override
    public BigDecimal calcMonatsrate(Integer vertragsId) {
        L.info("vertragsId: " + vertragsId);
        String sqlString = "SELECT \n" +
                "    SUM(Deckungspreis.Preis) AS Monatliche_Rate\n" +
                "FROM \n" +
                "    Vertrag\n" +
                "JOIN \n" +
                "    Deckung ON Vertrag.ID = Deckung.Vertrag_FK\n" +
                "JOIN \n" +
                "    Deckungspreis ON Deckung.Deckungsart_FK = Deckungspreis.Deckungsbetrag_FK\n" +
                "WHERE\n" +
                "    Vertrag.ID = ? AND\n" +
                "    Deckungspreis.Gueltig_Von <= Vertrag.Versicherungsbeginn AND\n" +
                "    Deckungspreis.Gueltig_Bis >= Vertrag.Versicherungsbeginn\n";
        BigDecimal monatlicheRate = null;

        try {
            PreparedStatement ps = useConnection().prepareStatement(sqlString);
            ps.setInt(1, vertragsId);
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                throw new VertragExistiertNichtException(vertragsId);
            }

            monatlicheRate = rs.getBigDecimal("Monatliche_Rate");
            if (monatlicheRate == null) {
                monatlicheRate = BigDecimal.ZERO;
            }
            L.info("Monatliche Rate berechnet: " + monatlicheRate);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        L.info("Ende");
        return monatlicheRate;
    }


}