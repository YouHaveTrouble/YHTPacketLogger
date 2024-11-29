package me.youhavetrouble.packetlogger.storage;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.youhavetrouble.packetlogger.YHTPacketLogger;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class SqliteDatabase implements Database {

    private final HikariDataSource dataSource;

    public SqliteDatabase() {
        File dataFolder = new File("plugins/packetlogger/data");
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                throw new RuntimeException("Failed to create data folder");
            }
        }
        HikariConfig config = new HikariConfig();
        config.setPoolName("DataSQLitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:plugins/packetlogger/data/data.db");
        config.setConnectionTestQuery("PRAGMA journal_mode=WAL");
        config.setMaxLifetime(60000);
        config.setMaximumPoolSize(500);
        dataSource = new HikariDataSource(config);

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("""
                            CREATE TABLE IF NOT EXISTS movement_packets (
                                id VARCHAR(36) NOT NULL PRIMARY KEY,
                                player_id TEXT NOT NULL,
                                x DOUBLE,
                                y DOUBLE,
                                z DOUBLE,
                                yaw DOUBLE,
                                pitch DOUBLE,
                                on_ground BOOLEAN,
                                protocol_version INT,
                                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            )
                    """);
        } catch (SQLException e) {
        }

    }

    @Override
    public void saveMovementPacket(Player player, WrapperPlayClientPlayerFlying packet) {

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO movement_packets (
                                                  id, player_id, x, y, z, yaw, pitch, on_ground,
                                                  protocol_version)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);

            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, player.getUniqueId().toString());
            statement.setDouble(3, packet.getLocation().getX());
            statement.setDouble(4, packet.getLocation().getY());
            statement.setDouble(5, packet.getLocation().getZ());
            statement.setDouble(6, packet.getLocation().getYaw());
            statement.setDouble(7, packet.getLocation().getPitch());
            statement.setBoolean(8, packet.isOnGround());
            statement.setInt(9, packet.getClientVersion().getProtocolVersion());
            statement.execute();
        } catch (SQLException e) {
            YHTPacketLogger plugin = YHTPacketLogger.getInstance();
            if (plugin == null) return;
            plugin.getSLF4JLogger().warn("Failed to save packet", e);
        }

    }

    @Override
    public void close() {
        dataSource.close();
    }


}
