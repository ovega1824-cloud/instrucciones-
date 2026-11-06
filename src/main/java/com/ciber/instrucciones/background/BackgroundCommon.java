package com.ciber.instrucciones.background;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/** Registra la red de los fondos al iniciar el mod (cliente y servidor). */
@Mod.EventBusSubscriber(modid = "instrucciones", bus = Mod.EventBusSubscriber.Bus.MOD)
public final class BackgroundCommon {
    private BackgroundCommon() {}

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BackgroundNetwork::register);
    }
}
