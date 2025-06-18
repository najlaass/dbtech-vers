package de.htwberlin.dbtech.aufgaben.ue03.dao;

import java.sql.SQLException;

public interface IDeckungsartTDG {

    /**
     * Schaut nach, ob es eine Deckungsart mit dieser Nummer gibt.
     *
     * @param id Die Nummer von der Deckungsart.
     * @return true → wenn sie gefunden wurde.
     *         false → wenn es sie nicht gibt.
     */
    boolean findDeckungsartByID(int id) throws SQLException;
}