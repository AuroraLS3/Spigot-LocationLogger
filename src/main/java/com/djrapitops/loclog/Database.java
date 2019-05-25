package com.djrapitops.loclog;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class Database {

    private final File dbFile;

    public Database(File dbFile) {
        this.dbFile = dbFile;
    }

    private Connection connection;

    public void init() throws SQLException {
        connection = getNewConnection();
        createTables();
    }

    public void close() throws SQLException {
        if (connection != null) connection.close();
    }

    private Connection getNewConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        return getConnectionFor(dbFile.getAbsolutePath());
    }

    private Connection getConnectionFor(String dbFilePath) throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath + "?journal_mode=WAL");
        } catch (SQLException walNotSupported) {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        }
    }

    private void createTables() throws SQLException {
        String createLocationTableSQL = "CREATE TABLE IF NOT EXISTS locations (" +
                "id integer PRIMARY KEY," +
                "uuid varchar(36) NOT NULL," +
                "t bigint NOT NULL," +
                "x integer NOT NULL," +
                "z integer NOT NULL)";
        try (PreparedStatement statement = connection.prepareStatement(createLocationTableSQL)) {
            statement.execute();
        }

        String createPlacedBlocksTableSQL = "CREATE TABLE IF NOT EXISTS blocks (" +
                "id integer PRIMARY KEY," +
                "uuid varchar(36) NOT NULL," +
                "x integer NOT NULL," +
                "z integer NOT NULL," +
                "event integer NOT NULL," +
                "block varchar(250) NOT NULL)";
        try (PreparedStatement statement = connection.prepareStatement(createPlacedBlocksTableSQL)) {
            statement.execute();
        }
    }

    public void savePlayerLocation(UUID identifier, long time, int x, int z) throws SQLException {
        String sql = "INSERT INTO locations (uuid, t, x, z) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier.toString());
            statement.setLong(2, time);
            statement.setInt(3, x);
            statement.setInt(4, z);
            statement.execute();
        }
    }

    public void savePlayerPlacedBlock(UUID identifier, int x, int z, String blockName) throws SQLException {
        String sql = "INSERT INTO blocks (uuid, x, z, event, block) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier.toString());
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setInt(4, EventType.PLACE);
            statement.setString(5, blockName);
            statement.execute();
        }
    }

    public void savePlayerBrokenBlock(UUID identifier, int x, int z, String blockName) throws SQLException {
        String sql = "INSERT INTO blocks (uuid, x, z, event, block) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identifier.toString());
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setInt(4, EventType.BREAK);
            statement.setString(5, blockName);
            statement.execute();
        }
    }
}