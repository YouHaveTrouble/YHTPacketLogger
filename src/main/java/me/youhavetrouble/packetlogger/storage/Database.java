package me.youhavetrouble.packetlogger.storage;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import org.bukkit.entity.Player;

public interface Database {

    void saveMovementPacket(Player player, WrapperPlayClientPlayerFlying packet);

    void close();

}
