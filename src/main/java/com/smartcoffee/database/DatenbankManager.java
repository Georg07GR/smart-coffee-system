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

public class DatenbankManager
{

    private final String DB_URL = "jdbc:sqlite:coffee_system.db";
    private Connection connection;

    public DatenbankManager()
    {
        try
        {
            this.connection = DriverManager.getConnection(DB_URL);
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


    public void zahlungSpeichern(int bestellId, Muenze muenzeTyp, int anzahl)
    {
        String sqlQuery = "INSERT INTO Zahlungen (bestellung_id, muenztyp, anzahl) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sqlQuery))
        {
            pstmt.setInt(1, bestellId);
            pstmt.setString(2, muenzeTyp.getWertInCents() + " Ct");
            pstmt.setInt(3, anzahl);
            pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }


    public void muenzbestandSpeichern(Map<Muenze, Integer> bestand)
    {
        String sqlQeury = "INSERT OR REPLACE Muenzbestand (muenztyp, anzahl) VALUES (?, ?)";

        for (Map.Entry<Muenze, Integer> entry : bestand.entrySet())
        {
            try (PreparedStatement pstmt = connection.prepareStatement(sqlQeury))
            {
                pstmt.setString(1, entry.getKey().getWertInCents() + " Ct");
                pstmt.setInt(2, entry.getValue());
                pstmt.executeUpdate();
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }


    public void muenzbestandLaden(Map<Muenze, Integer> bestand)
    {
        String sqlQuery = "SELECT muenztyp, anzahl FROM Muenzbestand";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery))
        {
            while (rs.next())
            {
                Muenze muenzeTyp = Muenze.valueOf(rs.getString("muenztyp").replace(" ", "_").toUpperCase());
                int anzahl = rs.getInt("anzahl");
                bestand.put(muenzeTyp, anzahl);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }












}
