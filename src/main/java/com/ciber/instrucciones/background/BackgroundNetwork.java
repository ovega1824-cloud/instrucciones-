package com.ciber.instrucciones.background;

import com.ciber.instrucciones.background.packet.ApplyBackgroundPacket;
import com.ciber.instrucciones.background.packet.OpenMenuPacket;
import com.ciber.instrucciones.background.packet.SelectBackgroundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

/** Canal de red propio para la funcion de fondos (no toca el canal original del mod). */
public final class BackgroundNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("instrucciones", "background"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    /** Ultimo fondo elegido en este servidor (null = nadie ha elegido todavia). Solo en memoria. */
    private static volatile String current = null;

    private BackgroundNetwork() {}

    public static String getCurrent() {
        return current;
    }

    public static void setCurrent(String name) {
        current = name;
    }

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenMenuPacket.class,
                OpenMenuPacket::encode, OpenMenuPacket::decode, OpenMenuPacket::handle);
        CHANNEL.registerMessage(id++, SelectBackgroundPacket.class,
                SelectBackgroundPacket::encode, SelectBackgroundPacket::decode, SelectBackgroundPacket::handle);
        CHANNEL.registerMessage(id++, ApplyBackgroundPacket.class,
                ApplyBackgroundPacket::encode, ApplyBackgroundPacket::decode, ApplyBackgroundPacket::handle);
    }
}
