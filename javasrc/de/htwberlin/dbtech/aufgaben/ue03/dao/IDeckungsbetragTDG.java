package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface IDeckungsbetragTDG {

    /**
     * Prüft, ob dieser Betrag für diese Versicherungsart erlaubt ist.
     *
     * @param artId   Die Nummer der Versicherungsart.
     * @param betrag  Der Betrag, der versichert werden soll.
     * @return true → wenn der Betrag gültig ist.
     *         false → wenn der Betrag nicht erlaubt ist.
     */
    boolean isValidDeckungsbetrag(int artId, BigDecimal betrag) throws SQLException;
}