package me.youhavetrouble.packetlogger.storage;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
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
                                protocol_version INT NOT NULL,
                                timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                            )
                    """);
        } catch (SQLException e) {
        }

        try (Connection connection = dataSource.getConnection()) {
            connection.createStatement().execute("""
                            CREATE TABLE IF NOT EXISTS interact_entity_packets (
                                id VARCHAR(36) NOT NULL PRIMARY KEY,
                                player_id TEXT NOT NULL,
                                player_x DOUBLE NOT NULL,
                                player_y DOUBLE NOT NULL,
                                player_z DOUBLE NOT NULL,
                                target_x DOUBLE,
                                target_y DOUBLE,
                                target_z DOUBLE,
                                interaction_type VARCHAR(16),
                                protocol_version INT NOT NULL,
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
    public void saveInteractEntityPacket(Player player, WrapperPlayClientInteractEntity packet) {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO interact_entity_packets (
                         id, player_id, player_x, player_y, player_z, target_x, target_y, target_z, interaction_type, protocol_version
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);

            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, player.getUniqueId().toString());
            statement.setDouble(3, player.getEyeLocation().getX());
            statement.setDouble(4, player.getEyeLocation().getY());
            statement.setDouble(5, player.getEyeLocation().getZ());
            if (packet.getTarget().isEmpty()) {
                statement.setNull(6, java.sql.Types.DOUBLE);
                statement.setNull(7, java.sql.Types.DOUBLE);
                statement.setNull(8, java.sql.Types.DOUBLE);
            } else {
                statement.setDouble(6, packet.getTarget().get().getX());
                statement.setDouble(7, packet.getTarget().get().getY());
                statement.setDouble(8, packet.getTarget().get().getZ());
            }
            statement.setString(9, packet.getAction().toString());
            statement.setInt(10, packet.getClientVersion().getProtocolVersion());
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
