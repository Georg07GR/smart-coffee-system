package com.smartcoffee.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MünzwechslerTest
{

    private Münzwechsler wechsler;


    @BeforeEach
    public void setUp()
    {
        wechsler = new Münzwechsler();
    }

    @Test
    public void testExactPayment()
    {
        List<Muenze> eingeworfen = List.of(Muenze.EURO_1, Muenze.CENT_50);
        WechselgeldErgebnis ergebnis = wechsler.wechselgeldBerechnen(150,eingeworfen);

        assertTrue(ergebnis.erfolgreich(), "Transaction should be marked successful");
        assertTrue(ergebnis.ausgegebeneMuenzen().isEmpty(), "No change should be given for exact payment");
        assertEquals(ZahlungsMeldung.ERFOLGREICH.getText(), ergebnis.meldung().getText(), "Message should indicate successful transaction");

        assertEquals(11,wechsler.getMuenzBestand().get(Muenze.EURO_1));
        assertEquals(11,wechsler.getMuenzBestand().get(Muenze.CENT_50));
    }


    @Test
    public void testPaymentWithChange()
    {
        List<Muenze> eingeworfen = List.of(Muenze.EURO_2);
        WechselgeldErgebnis ergebnis = wechsler.wechselgeldBerechnen(150,eingeworfen);

        assertTrue(ergebnis.erfolgreich(), "Transaction should be marked successful");
        assertEquals(1, ergebnis.ausgegebeneMuenzen().size(), "One coin should be given as change");
        assertEquals(Muenze.CENT_50, ergebnis.ausgegebeneMuenzen().get(0), "Change should be a 50 cent coin");
        assertEquals(ZahlungsMeldung.ERFOLGREICH_WECHSELGELD.getText(), ergebnis.meldung().getText(), "Message should indicate successful transaction with change");

        assertEquals(11,wechsler.getMuenzBestand().get(Muenze.EURO_2));
        assertEquals(9,wechsler.getMuenzBestand().get(Muenze.CENT_50));
    }


    @Test
    public void testInsufficientPayment()
    {
        List<Muenze> eingeworfen = List.of(Muenze.EURO_1);
        WechselgeldErgebnis ergebnis = wechsler.wechselgeldBerechnen(150,eingeworfen);

        assertFalse(ergebnis.erfolgreich(), "Transaction should be marked successful");
        assertEquals(1, ergebnis.ausgegebeneMuenzen().size(), "One coin should be returned as change");
        assertEquals(Muenze.EURO_1, ergebnis.ausgegebeneMuenzen().get(0), "Returned coin should be the 1 Euro coin");
        assertEquals(ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD, ergebnis.meldung(), "Message should indicate insufficient payment");

        assertEquals(10,wechsler.getMuenzBestand().get(Muenze.EURO_1));
    }

    @Test
    public void testNoChangeAvailableRollback()
    {
        for (Muenze m: Muenze.values())
            if (m.getWertInCents() < 100)
            {
                wechsler.setMuenzenBestand(m, 0);
            }

        List<Muenze> eingeworfen = List.of(Muenze.EURO_2);

        WechselgeldErgebnis ergebnis = wechsler.wechselgeldBerechnen(150, eingeworfen);



        assertFalse(ergebnis.erfolgreich(), "Transaction should be marked unsuccessful due to insufficient change");
        assertEquals(1, ergebnis.ausgegebeneMuenzen().size(), "One coin should be returned as change");
        assertEquals(Muenze.EURO_2, ergebnis.ausgegebeneMuenzen().get(0), "Returned coin should be the 2 Euro coin");
        assertEquals(ZahlungsMeldung.FEHLER_KEIN_WECHSELGELD, ergebnis.meldung(), "Message should indicate insufficient change available");

        assertEquals(10, wechsler.getMuenzBestand().get(Muenze.EURO_2));
    }
}
