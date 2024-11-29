package me.youhavetrouble.packetlogger.listener;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import me.youhavetrouble.packetlogger.YHTPacketLogger;
import org.bukkit.entity.Player;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PacketEventsListener extends PacketListenerAbstract {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public PacketEventsListener() {
        super(PacketListenerPriority.MONITOR);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        
        switch (event.getPacketType()) {
            case PacketType.Play.Client.PLAYER_POSITION,
                 PacketType.Play.Client.PLAYER_ROTATION,
                 PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION -> {
                Player player = event.getPlayer();
                WrapperPlayClientPlayerFlying wrapper = new WrapperPlayClientPlayerFlying(event);
                if (YHTPacketLogger.getDatabase() != null) {
                    executor.submit(() -> YHTPacketLogger.getDatabase().saveMovementPacket(player, wrapper));
                }
            }
            default -> {}
        }

    }
}
