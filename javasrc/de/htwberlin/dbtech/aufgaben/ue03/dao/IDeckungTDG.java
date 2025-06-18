package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface IDeckungTDG {

    /**
     * Speichert eine neue Deckung (Versicherung) in der Datenbank.
     *
     * @param vertragId Die Nummer vom Vertrag.
     * @param artId     Die Nummer von der Versicherungsart.
     * @param betrag    Wie viel Geld abgesichert werden soll.
     * @throws SQLException Wenn beim Speichern etwas schiefgeht.
     */
    void addCoverage(int vertragId, int artId, BigDecimal betrag) throws SQLException;
}