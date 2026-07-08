package com.smartcoffee.database;

import org.sqlite.SQLiteConfig;

import javax.imageio.IIOException;
import javax.management.DescriptorRead;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

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














}
