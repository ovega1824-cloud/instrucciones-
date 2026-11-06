package com.ciber.instrucciones.background.client;

import net.minecraft.client.Minecraft;

/** Puntos de entrada del lado cliente (los llaman los paquetes via DistExecutor). */
public final class BackgroundClientHooks {
    private BackgroundClientHooks() {}

    public static void openMenu() {
        Minecraft.getInstance().setScreen(new BackgroundMenuScreen());
    }

    public static void apply(String name) {
        BackgroundManager.apply(name, true);
    }
}
