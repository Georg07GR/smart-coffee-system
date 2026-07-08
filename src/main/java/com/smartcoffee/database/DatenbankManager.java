package com.smartcoffee.database;

import com.smartcoffee.logic.KaffeeArt;
import com.smartcoffee.logic.Muenze;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Manages all database interactions for the smart coffee application.
 * Handles database connections, schema initialization, order placement,
 * payment logging, and coin inventory persistence using SQLite.
 */
public class DatenbankManager
{

    private final String DB_URL = "jdbc:sqlite:coffee_system.db";
    private Connection connection;


    /**
     * Default constructor used by the production application.
     * Connects to the default persistent SQLite file ("coffee_system.db").
     */
    public DatenbankManager()
    {
        this("jdbc:sqlite:coffee_system.db");
    }


    /**
     * Parameterized constructor allowing database URL injection.
     * Primarily used during automated testing to inject an in-memory SQLite database instance.
     *
     * @param dbUrl The JDBC connection URL for SQLite.
     * @throws RuntimeException If connection or foreign key configuration fails.
     */
    public DatenbankManager(String dbUrl)
    {
        try
        {
            this.connection = DriverManager.getConnection(dbUrl);
            try (Statement stmt = this.connection.createStatement())
            {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeSchema();

        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }



    /**
     * Reads the embedded `schema.sql` file and initializes the database tables.
     * Splits individual statements by semicolons and executes them sequentially.
     *
     * @throws RuntimeException If the schema file cannot be read or execution fails.
     */
    private void initializeSchema()
    {
        try (InputStream is = getClass().getResourceAsStream("/com/smartcoffee/database/schema.sql"))
        {
            if (is == null)
            {
                throw new IOException("Schema file not found");
            }
            String schema = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            List<String> statements = Arrays.asList(schema.split(";"));
            try (Statement stmt = this.connection.createStatement())
            {
                for (String statement : statements)
                {
                    if (!statement.trim().isEmpty())
                    {
                        stmt.execute(statement);
                    }
                }
            }
        } catch (IOException | SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Inserts a new coffee order record into the database.
     *
     * @param kaffee The type of coffee beverage being ordered.
     * @return The auto-generated database primary key (ID) assigned to this specific order.
     * @throws SQLException If an error occurs during insertion or database key retrieval fails.
     */
    public int bestellungSpeichern(KaffeeArt kaffee) throws SQLException
    {
        String sqlQuery = "INSERT INTO Bestellungen (" +
                "kaffeeart, mit_milch, preis, zeitstempel)"
                + "VALUES (?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS))
        {
            pstmt.setString(1, kaffee.getAnzeigeName());
            pstmt.setBoolean(2, kaffee.isMitmilch());
            pstmt.setDouble(3, kaffee.getPreis());
            pstmt.setString(4, formattedDateTime);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys())
            {
                if (rs.next())
                {
                  return rs.getInt(1);
                }
            }
        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        throw new SQLException("Failed to retrieve generated order ID.");
    }


    /**
     * Records an individual coin insertion transaction linked to a specific order.
     *
     * @param bestellId The ID of the associated order from the `Bestellungen` table.
     * @param muenzeTyp The coin denomination inserted.
     * @param anzahl    The quantity of this specific coin inserted.
     * @throws RuntimeException If the database statement execution fails.
     */
    public void zahlungSpeichern(int bestellId, Muenze muenzeTyp, int anzahl)
    {
        String sqlQuery = "INSERT INTO Zahlungen(bestellung_id, muenztyp, anzahl) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery))
        {
            pstmt.setInt(1, bestellId);
            pstmt.setDouble(2, muenzeTyp.getWertInEuro());
            pstmt.setInt(3, anzahl);
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Updates or inserts the complete machine coin inventory tracking into the database.
     * Uses an UPSERT string pattern (`INSERT OR REPLACE`) to handle existing records.
     *
     * @param bestand A map containing coin denominations mapped to their current count inside the machine.
     * @throws RuntimeException If database insertion blocks fail.
     */
    public void muenzbestandSpeichern(Map<Muenze, Integer> bestand)
    {
        String sqlQeury = "INSERT OR REPLACE INTO Muenzbestand (muenztyp, anzahl) VALUES (?, ?)";


        for (Map.Entry<Muenze, Integer> entry : bestand.entrySet())
        {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlQeury))
            {
                pstmt.setDouble(1, entry.getKey().getWertInEuro());
                pstmt.setInt(2, entry.getValue());
                pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Loads the current coin inventory counts from the database into the provided runtime map.
     * If the database table is completely empty, it populates a default starter pack of
     * 10 coins for every available coin denomination and syncs it back to the database.
     *
     * @param bestand The application state map to be filled with database inventory data.
     * @throws RuntimeException If execution queries fail.
     */
    public void muenzbestandLaden(Map<Muenze, Integer> bestand)
    {
        String sqlQuery = "SELECT muenztyp, anzahl FROM Muenzbestand";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery))
        {
            while (rs.next())
            {
                Muenze muenzeTyp = Muenze.fromEuroValue(rs.getDouble("muenztyp"));
                int anzahl = rs.getInt("anzahl");
                bestand.put(muenzeTyp, anzahl);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        // Default Initialization: If no coin data was found, seed the machine with 10 of each coin type
        if (bestand.isEmpty())
        {
            for (Muenze m : Muenze.values())
            {
                bestand.put(m, 10);
            }
        }
        muenzbestandSpeichern(bestand);
    }
}
