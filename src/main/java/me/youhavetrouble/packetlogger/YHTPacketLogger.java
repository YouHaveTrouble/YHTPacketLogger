package me.youhavetrouble.packetlogger;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import me.youhavetrouble.packetlogger.listener.PacketEventsListener;
import me.youhavetrouble.packetlogger.storage.Database;
import me.youhavetrouble.packetlogger.storage.SqliteDatabase;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public final class YHTPacketLogger extends JavaPlugin {

    private static YHTPacketLogger plugin = null;
    private static Database database = null;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketEventsListener());
    }

    @Override
    public void onEnable() {
        plugin = this;
        database = new SqliteDatabase();
        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.close();
        }
    }

    public static @Nullable YHTPacketLogger getInstance() {
        return plugin;
    }

    public static @Nullable Database getDatabase() {
        return database;
    }

}
