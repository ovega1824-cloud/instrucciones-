package com.ciber.instrucciones.background;

import com.ciber.instrucciones.background.packet.ApplyBackgroundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/** Cuando entra un jugador, recibe el fondo que se haya elegido en este servidor. */
@Mod.EventBusSubscriber(modid = "instrucciones")
public final class BackgroundServerEvents {
    private BackgroundServerEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        String current = BackgroundNetwork.getCurrent();
        if (current != null && event.getEntity() instanceof ServerPlayer player) {
            BackgroundNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new ApplyBackgroundPacket(current));
        }
    }
}
