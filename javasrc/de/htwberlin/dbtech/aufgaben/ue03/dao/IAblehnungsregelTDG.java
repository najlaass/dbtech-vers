package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.math.BigDecimal;
import java.sql.SQLException;

public interface IAblehnungsregelTDG {

    /**
     * Schaut, ob eine Regel sagt:
     * "Diese Versicherung darf man bei diesem Alter und Betrag NICHT machen."
     *
     * @param deckungsartId Die Nummer von der Versicherungsart.
     * @param alter Das Alter vom Kunden.
     * @param betrag Wie viel Geld versichert werden soll.
     * @return true → wenn die Regel zutrifft (also Ablehnung).
     *         false → wenn keine Regel passt (also erlaubt).
     */
    boolean doesRejectionRuleApply(int deckungsartId, int alter, BigDecimal betrag) throws SQLException;
}