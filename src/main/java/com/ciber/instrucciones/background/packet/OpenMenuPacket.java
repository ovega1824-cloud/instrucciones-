package com.ciber.instrucciones.background.packet;

import com.ciber.instrucciones.background.client.BackgroundClientHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Servidor -> cliente: abre el menu de fondos. */
public class OpenMenuPacket {
    public OpenMenuPacket() {}

    public static void encode(OpenMenuPacket msg, FriendlyByteBuf buf) {}

    public static OpenMenuPacket decode(FriendlyByteBuf buf) {
        return new OpenMenuPacket();
    }

    public static void handle(OpenMenuPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> BackgroundClientHooks.openMenu()));
        ctx.get().setPacketHandled(true);
    }
}
