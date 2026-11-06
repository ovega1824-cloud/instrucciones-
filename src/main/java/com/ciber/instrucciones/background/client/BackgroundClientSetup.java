package com.ciber.instrucciones.background.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Crea la carpeta de imagenes y restaura el ultimo fondo elegido al abrir el juego. */
@Mod.EventBusSubscriber(modid = "instrucciones", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BackgroundClientSetup {
    private BackgroundClientSetup() {}

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(BackgroundManager::init);
    }
}
