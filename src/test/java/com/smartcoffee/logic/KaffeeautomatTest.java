package com.smartcoffee.logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Kaffeeautomat class.
 * Employs a testable subclass to mock the random grinder failure.
 */
public class KaffeeautomatTest {
    private TestableKaffeeautomat automat;

    /**
     * A helper subclass of Kaffeeautomat that overrides the random
     * grinder failure method to make tests predictable.
     */
    private static class TestableKaffeeautomat extends Kaffeeautomat {
        private boolean grinderSollKaputtGehen = false;

        public void setGrinderSollKaputtGehen(boolean status) {
            this.grinderSollKaputtGehen = status;
        }

        @Override
        boolean istMahlwerkKaputt() {
            // Override random math and return our mock value
            return grinderSollKaputtGehen;
        }
    }

    @BeforeEach
    public void setUp() {
        automat = new TestableKaffeeautomat();
    }

    @Test
    public void testGetraenkZubereitenSuccess() {
        // Arrange: default machine starts at 2000g beans, 200g milk
        automat.setGrinderSollKaputtGehen(false);

        // Act: Prepare Espresso (no milk)
        boolean ergebnis = automat.getraenkZubereiten(KaffeeArt.ESPRESSO);

        // Assert
        assertTrue(ergebnis, "Espresso brewing should succeed");
        assertEquals(1975, automat.getKaffeeBestand(), "Coffee beans should decrease by 25g");
        assertEquals(200, automat.getMilchBestand(), "Milk should remain unchanged");
        assertEquals(1, automat.getTassen(), "Tassen counter should increment to 1");
        assertEquals(AutomatenStatus.BEREIT, automat.getStatus(), "Status should be BEREIT");
        assertFalse(automat.getTransaktionsHistorie().isEmpty(), "Transaction history should not be empty");
    }

    @Test
    public void testGetraenkZubereitenWithMilk() {
        automat.setGrinderSollKaputtGehen(false);

        // Act: Prepare Cappuccino (requires milk)
        boolean ergebnis = automat.getraenkZubereiten(KaffeeArt.CAPPUCCINO);

        // Assert
        assertTrue(ergebnis, "Cappuccino brewing should succeed");
        assertEquals(1975, automat.getKaffeeBestand(), "Coffee beans should decrease by 25g");
        assertEquals(190, automat.getMilchBestand(), "Milk should decrease by 10g");
        assertEquals(1, automat.getTassen(), "Tassen counter should be 1");
    }

    @Test
    public void testInsufficientIngredients() {
        automat.setGrinderSollKaputtGehen(false);

        // Act: Drain the coffee beans by brewing in a loop until it runs out
        while (automat.getraenkZubereiten(KaffeeArt.ESPRESSO)) {
            // loop continues until beans are empty (less than 25g)
        }

        // Act: Try to brew one more cup
        boolean ergebnis = automat.getraenkZubereiten(KaffeeArt.ESPRESSO);

        // Assert
        assertFalse(ergebnis, "Brewing should fail when coffee beans are empty");
        assertEquals(AutomatenStatus.ZUTATEN_FEHLEN, automat.getStatus(), "Status should be ZUTATEN_FEHLEN");
    }

    @Test
    public void testGrinderFailureSimulation() {
        // Arrange: Force the grinder to break on the next brew
        automat.setGrinderSollKaputtGehen(true);

        // Act: Try to brew coffee
        boolean ergebnis = automat.getraenkZubereiten(KaffeeArt.ESPRESSO);

        // Assert
        assertFalse(ergebnis, "Brewing should fail because grinder is broken");
        assertEquals(AutomatenStatus.DEFEKT, automat.getStatus(), "Status should change to DEFEKT");
        assertTrue(automat.getTransaktionsHistorie().stream()
                .anyMatch(log -> log.contains("DEFEKT")), "Defect should be logged in history");
    }

    @Test
    public void testAuffuellen() {
        // Arrange: Drain ingredients until out of stock
        while (automat.getraenkZubereiten(KaffeeArt.ESPRESSO)) { }
        assertEquals(AutomatenStatus.ZUTATEN_FEHLEN, automat.getStatus());

        // Act: Refill the machine
        automat.auffuellen();

        // Assert
        assertEquals(2000, automat.getKaffeeBestand(), "Beans should be reset to 2000g");
        assertEquals(200, automat.getMilchBestand(), "Milk should be reset to 200g");
        assertEquals(AutomatenStatus.BEREIT, automat.getStatus(), "Status should be reset to BEREIT");
    }
}