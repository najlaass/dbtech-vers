package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.SQLException;

public interface IVertragTDG {

    /**
     * Schaut nach, ob es den Vertrag mit dieser Nummer wirklich gibt.
     *
     * @param vertragsId Die Nummer vom Vertrag.
     * @return true, wenn der Vertrag gefunden wurde – sonst false.
     */
    boolean findVertragByID(int vertragsId) throws SQLException;

    /**
     * Prüft, ob die Versicherungsart zu dem Vertrag passt.
     * Also: Gehört die Deckungsart zum gleichen Produkt wie im Vertrag?
     *
     * @param vertragsId     Die Nummer vom Vertrag.
     * @param deckungsartId  Die Nummer von der Versicherungsart.
     * @return true, wenn es passt – sonst false.
     */
    boolean isProductMatch(int vertragsId, int deckungsartId) throws SQLException;

    /**
     * Holt das Alter vom Kunden, der den Vertrag hat.
     *
     * @param vertragsId Die Nummer vom Vertrag.
     * @return Das Alter vom Kunden in Jahren.
     */
    int findKundenAlterByVertragsId(int vertragsId) throws SQLException;
}