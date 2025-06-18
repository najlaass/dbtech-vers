package de.htwberlin.dbtech.aufgaben.ue03;

import de.htwberlin.dbtech.exceptions.*;
import de.htwberlin.dbtech.utils.DbCred;
import de.htwberlin.dbtech.utils.DbUnitUtils;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class VersicherungServiceJavaTest {

    // Hiermit können wir Text ins Log schreiben, also zum Nachschauen, was passiert ist.
    private static final Logger L = LoggerFactory.getLogger(VersicherungServiceJavaTest.class);
    private static IDatabaseConnection dbTesterCon = null;

    // Das ist unser Service, mit dem wir Deckungen anlegen wollen.
    private static final IVersicherungService vService = new VersicherungService();

    // Das passiert einmal ganz am Anfang, bevor die Tests starten.
    @BeforeClass
    public static void setUp() {
        L.debug("setUp: start");
        try {
            // Wir verbinden uns mit der Datenbank zum Testen.
            IDatabaseTester dbTester = new JdbcDatabaseTester(DbCred.driverClass, DbCred.url, DbCred.user, DbCred.password,
                    DbCred.schema);
            dbTesterCon = dbTester.getConnection();

            // Wir lesen Testdaten aus CSV-Dateien ein.
            IDataSet datadir = new CsvDataSet(new File("test-data/ue03-04"));
            dbTester.setDataSet(datadir);

            // Die Datenbank wird zuerst geleert, dann füllen wir sie neu mit den Testdaten.
            DatabaseOperation.CLEAN_INSERT.execute(dbTesterCon, datadir);

            // Jetzt sagen wir dem Service, dass er mit dieser Datenbank arbeiten soll.
            vService.setConnection(dbTesterCon.getConnection());
        } catch (Exception e) {
            DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
            throw new RuntimeException(e);
        }
    }

    // Ganz am Ende wird die Verbindung zur Datenbank wieder geschlossen.
    @AfterClass
    public static void tearDown() {
        L.debug("tearDown: start");
        DbUnitUtils.closeDbUnitConnectionQuietly(dbTesterCon);
    }

    /**
     * Wir probieren eine Deckung zu machen – aber der Vertrag gibt’s gar nicht.
     * Da soll ein Fehler kommen.
     */
    @org.junit.Test(expected = VertragExistiertNichtException.class)
    public void createDeckung01() {
        vService.createDeckung(99, 1, BigDecimal.valueOf(0));
    }

    /**
     * Hier versuchen wir eine Deckung mit einer falschen Art.
     * Diese Deckungsart kennt das System gar nicht – also Fehler!
     */
    @org.junit.Test(expected = DeckungsartExistiertNichtException.class)
    public void createDeckung02() {
        vService.createDeckung(5, 99, BigDecimal.valueOf(0));
    }

    /**
     * Die Deckungsart passt nicht zu dem Produkt vom Vertrag.
     * Also: passt nicht zusammen = Fehler.
     */
    @org.junit.Test(expected = DeckungsartPasstNichtZuProduktException.class)
    public void createDeckung03() {
        vService.createDeckung(5, 1, BigDecimal.valueOf(0));
    }

    /**
     * Für diese Deckungsart wissen wir nicht, wie viel Geld erlaubt ist.
     * Deshalb klappt es nicht.
     */
    @org.junit.Test(expected = UngueltigerDeckungsbetragException.class)
    public void createDeckung04() {
        vService.createDeckung(5, 6, BigDecimal.valueOf(0));
    }

    /**
     * Wir wollen zu viel absichern – mehr als erlaubt.
     * System sagt: nö, das geht nicht.
     */
    @org.junit.Test(expected = UngueltigerDeckungsbetragException.class)
    public void createDeckung05() {
        vService.createDeckung(5, 5, BigDecimal.valueOf(2000));
    }

    /**
     * Der Betrag ist okay, aber es gibt keinen Preis dafür.
     * Also weiß das System nicht, was es tun soll.
     */
    @org.junit.Test(expected = DeckungspreisNichtVorhandenException.class)
    public void createDeckung06() {
        vService.createDeckung(5, 5, BigDecimal.valueOf(1500));
    }

    /**
     * Die Deckung ist zwar gültig, aber der Preis dafür gilt nur bis 2018.
     * Der Vertrag ist aus 2019 – zu spät also.
     */
    @org.junit.Test(expected = DeckungspreisNichtVorhandenException.class)
    public void createDeckung07() {
        vService.createDeckung(5, 4, BigDecimal.valueOf(150000));
    }

    /**
     * Der Kunde ist unter 18 Jahre – für ihn darf man keine Haftung machen.
     * Deshalb soll es nicht gehen.
     */
    @org.junit.Test(expected = DeckungsartNichtRegelkonformException.class)
    public void createDeckung08() {
        vService.createDeckung(6, 1, BigDecimal.valueOf(100000000));
    }

    /**
     * Der Kunde ist über 90 Jahre – für ihn darf man keine Todesversicherung machen.
     * Deshalb gibt’s einen Fehler.
     */
    @org.junit.Test(expected = DeckungsartNichtRegelkonformException.class)
    public void createDeckung09() {
        vService.createDeckung(7, 3, BigDecimal.valueOf(100000));
    }

    /**
     * Der Kunde ist über 70 – bei so einem hohen Betrag darf man keine Todesdeckung machen.
     */
    @org.junit.Test(expected = DeckungsartNichtRegelkonformException.class)
    public void createDeckung10() {
        vService.createDeckung(8, 3, BigDecimal.valueOf(200000));
    }

    /**
     * Der Kunde ist über 60 – da darf man keine Todesdeckung mit 300 Tsd machen.
     * Also ist das nicht erlaubt.
     */
    @org.junit.Test(expected = DeckungsartNichtRegelkonformException.class)
    public void createDeckung11() {
        vService.createDeckung(9, 3, BigDecimal.valueOf(300000));
    }

    /**
     * Jetzt machen wir drei Deckungen, die alle richtig sind.
     * Danach schauen wir, ob sie wirklich in der Datenbank stehen.
     */
    @org.junit.Test
    public void createDeckung12() throws Exception {
        Integer[] vertragsIds = new Integer[]{5, 8, 9};
        Integer[] deckungsartIds = new Integer[]{4, 3, 3};
        BigDecimal[] deckungsbetraege = new BigDecimal[]{
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(200000)
        };

        for (int i = 0; i < 3; i++) {
            vService.createDeckung(vertragsIds[i], deckungsartIds[i], deckungsbetraege[i]);
        }

        // Jetzt holen wir die Daten aus der Datenbank
        QueryDataSet databaseDataSet = new QueryDataSet(dbTesterCon);
        String sql = "select * from Deckung where Vertrag_FK in (5, 8, 9) order by Vertrag_FK, Deckungsart_FK";
        databaseDataSet.addTable("Deckung", sql);
        ITable tblDeckung = databaseDataSet.getTable("Deckung");

        // Wir prüfen, ob wirklich 3 Zeilen da sind
        Assert.assertEquals("Falsche Anzahl Zeilen", 3, tblDeckung.getRowCount());

        // Jetzt schauen wir, ob die Werte stimmen
        for (int i = 0; i < 3; i++) {
            Integer vertragsId = ((BigDecimal) tblDeckung.getValue(i, "Vertrag_FK")).intValue();
            Integer deckungsartId = ((BigDecimal) tblDeckung.getValue(i, "Deckungsart_FK")).intValue();
            BigDecimal deckungsbetrag = (BigDecimal) tblDeckung.getValue(i, "Deckungsbetrag");

            Assert.assertEquals("Falsche vertragsId", vertragsIds[i], vertragsId);
            Assert.assertEquals("Falsche deckungsartId", deckungsartIds[i], deckungsartId);
            Assert.assertEquals("Falscher deckungsbetrag", deckungsbetraege[i], deckungsbetrag);
        }
    }
}