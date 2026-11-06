package com.ciber.instrucciones.background.packet;

import com.ciber.instrucciones.background.client.BackgroundClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> clientes: aplica el fondo con ese nombre (si el cliente tiene la imagen). */
public class ApplyBackgroundPacket {
    private final String name;

    public ApplyBackgroundPacket(String name) {
        this.name = name == null ? "" : name;
    }

    public static void encode(ApplyBackgroundPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 256);
    }

    public static ApplyBackgroundPacket decode(FriendlyByteBuf buf) {
        return new ApplyBackgroundPacket(buf.readUtf(256));
    }

    public static void handle(ApplyBackgroundPacket msg, Supplier<NetworkEvent.Context> ctx) {
        final String name = msg.name;
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BackgroundClientHooks.apply(name)));
        ctx.get().setPacketHandled(true);
    }
}
