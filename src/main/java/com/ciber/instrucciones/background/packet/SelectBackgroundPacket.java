package com.ciber.instrucciones.background.packet;

import com.ciber.instrucciones.background.BackgroundNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/** Cliente -> servidor: el admin eligio un fondo ("" = el predeterminado del mod). */
public class SelectBackgroundPacket {
    private final String name;

    public SelectBackgroundPacket(String name) {
        this.name = name == null ? "" : name;
    }

    public static void encode(SelectBackgroundPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 256);
    }

    public static SelectBackgroundPacket decode(FriendlyByteBuf buf) {
        return new SelectBackgroundPacket(buf.readUtf(256));
    }

    public static void handle(SelectBackgroundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null || !sender.hasPermissions(2)) {
                return;
            }
            String name = msg.name;
            // nunca aceptar rutas: solo nombres de archivo simples
            if (name.contains("/") || name.contains("\\") || name.contains("..")) {
                return;
            }
            BackgroundNetwork.setCurrent(name);
            BackgroundNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new ApplyBackgroundPacket(name));
        });
        ctx.get().setPacketHandled(true);
    }
}
