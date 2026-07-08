package com.smartcoffee.database;

import com.smartcoffee.logic.KaffeeArt;
import com.smartcoffee.logic.Muenze;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests to verify the correctness of SQLite schema creation
 * and CRUD operations in DatenbankManager. Runs against an in-memory DB.
 */
public class DatenbankManagerTest {
    private DatenbankManager dbManager;

    @BeforeEach
    public void setUp() {
        // Initialize databases in-memory (wiped clean before each test)
        dbManager = new DatenbankManager("jdbc:sqlite::memory:");
    }

    @Test
    public void testSchemaInitializationAndDefaultCoins() {
        Map<Muenze, Integer> inventory = new EnumMap<>(Muenze.class);
        dbManager.muenzbestandLaden(inventory);

        // Verify that the database initially loads the default 10 coins of each type
        assertFalse(inventory.isEmpty(), "Loaded coin inventory should not be empty");
        for (Muenze m : Muenze.values()) {
            assertEquals(10, inventory.get(m), "Should initialize with 10 coins for: " + m.getAnzeigeName());
        }
    }

    @Test
    public void testBestellungSpeichern() throws SQLException {
        // Act: Save a new order in the database
        int orderId = dbManager.bestellungSpeichern(KaffeeArt.ESPRESSO);

        // Assert: Ensure a valid auto-incremented ID was returned
        assertTrue(orderId > 0, "Saved order should return a valid positive database ID");
    }

    @Test
    public void testZahlungSpeichern() throws SQLException {
        // Arrange: Insert an order first to satisfy foreign key constraints
        int orderId = dbManager.bestellungSpeichern(KaffeeArt.CAPPUCCINO);

        // Act & Assert: Save a coin payment against this order (should not throw exceptions)
        assertDoesNotThrow(() -> {
            dbManager.zahlungSpeichern(orderId, Muenze.EURO_1, 2);
        }, "Saving payment should succeed under valid foreign key relationship");
    }

    @Test
    public void testMuenzbestandSpeichernAndLaden() {
        // Arrange: Create a custom inventory map
        Map<Muenze, Integer> customInventory = new EnumMap<>(Muenze.class);
        for (Muenze m : Muenze.values()) {
            customInventory.put(m, 5); // Set all counts to 5
        }

        // Act: Save this custom inventory, then load it back into a new map
        dbManager.muenzbestandSpeichern(customInventory);

        Map<Muenze, Integer> loadedInventory = new EnumMap<>(Muenze.class);
        dbManager.muenzbestandLaden(loadedInventory);

        // Assert: Loaded counts should match what was saved (5 coins)
        for (Muenze m : Muenze.values()) {
            assertEquals(5, loadedInventory.get(m), "Loaded quantity should match the saved value of 5");
        }
    }
}