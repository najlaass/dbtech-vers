package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface IDeckungspreisTDG {

    /**
     * Schaut nach, ob es für diese Versicherungsart und diesen Betrag
     * einen gültigen Preis gibt (also: darf man das gerade kaufen?).
     *
     * @param artId   Die Nummer von der Versicherungsart.
     * @param betrag  Der Betrag, der versichert werden soll.
     * @return true → es gibt einen gültigen Preis dafür (heute).
     *         false → kein Preis oder nicht mehr gültig.
     */
    boolean hasDeckungspreis(int artId, BigDecimal betrag) throws SQLException;
}