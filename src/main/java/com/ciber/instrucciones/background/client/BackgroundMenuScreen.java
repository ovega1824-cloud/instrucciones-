package com.ciber.instrucciones.background.client;

import com.ciber.instrucciones.background.BackgroundNetwork;
import com.ciber.instrucciones.background.packet.SelectBackgroundPacket;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;

/** Menu de /ins backgraund: lista de imagenes de la carpeta + vista previa del fondo activo. */
public class BackgroundMenuScreen extends Screen {
    private static final int PER_PAGE = 6;
    private static final int ROW_H = 24;
    private static final int LIST_W = 200;

    private final List<String> entries = new ArrayList<>();
    private int page = 0;
    private String shownSelected = null;

    public BackgroundMenuScreen() {
        super(Component.literal("Fondo de instrucciones"));
    }

    @Override
    protected void init() {
        entries.clear();
        entries.add(""); // predeterminado
        entries.addAll(BackgroundManager.list());
        shownSelected = BackgroundManager.getSelected();

        int pages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        page = Mth.clamp(page, 0, pages - 1);

        int left = this.width / 2 - LIST_W - 10;
        int top = 40;

        for (int i = 0; i < PER_PAGE; i++) {
            int idx = page * PER_PAGE + i;
            if (idx >= entries.size()) {
                break;
            }
            final String name = entries.get(idx);
            boolean isSelected = name.equals(shownSelected);
            String text = name.isEmpty() ? "Predeterminado (del mod)" : this.font.plainSubstrByWidth(name, LIST_W - 24);
            Component label = Component.literal((isSelected ? "» " : "") + text);
            this.addRenderableWidget(Button.builder(label, b -> choose(name))
                    .bounds(left, top + i * ROW_H, LIST_W, 20).build());
        }

        int navY = top + PER_PAGE * ROW_H + 4;
        Button prev = Button.builder(Component.literal("<"), b -> {
            page--;
            this.rebuildWidgets();
        }).bounds(left, navY, 40, 20).build();
        prev.active = page > 0;
        this.addRenderableWidget(prev);

        Button next = Button.builder(Component.literal(">"), b -> {
            page++;
            this.rebuildWidgets();
        }).bounds(left + LIST_W - 40, navY, 40, 20).build();
        next.active = page < pages - 1;
        this.addRenderableWidget(next);

        int bottom = this.height - 28;
        int bw = 100;
        int gap = 6;
        int startX = this.width / 2 - (bw * 3 + gap * 2) / 2;
        this.addRenderableWidget(Button.builder(Component.literal("Abrir carpeta"), b -> {
            if (BackgroundManager.getFolder() != null) {
                Util.getPlatform().openFile(BackgroundManager.getFolder().toFile());
            }
        }).bounds(startX, bottom, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Recargar"), b -> this.rebuildWidgets())
                .bounds(startX + bw + gap, bottom, bw, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cerrar"), b -> this.onClose())
                .bounds(startX + (bw + gap) * 2, bottom, bw, 20).build());
    }

    private void choose(String name) {
        // el servidor valida el permiso y lo reenvia a todos (tambien a este cliente)
        BackgroundNetwork.CHANNEL.sendToServer(new SelectBackgroundPacket(name));
    }

    @Override
    public void tick() {
        super.tick();
        // cuando llega el fondo nuevo, refrescar la marca » de la lista
        if (shownSelected != null && !shownSelected.equals(BackgroundManager.getSelected())) {
            this.rebuildWidgets();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(g);
        g.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);

        int pages = Math.max(1, (entries.size() + PER_PAGE - 1) / PER_PAGE);
        int left = this.width / 2 - LIST_W - 10;
        int navY = 40 + PER_PAGE * ROW_H + 4;
        g.drawCenteredString(this.font, (page + 1) + " / " + pages, left + LIST_W / 2, navY + 6, 0xAAAAAA);

        // vista previa del fondo activo
        int px = this.width / 2 + 10;
        int py = 40;
        int pw = LIST_W;
        int ph = pw * 9 / 16;
        g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, 0xFFFFFFFF);
        g.blit(BackgroundManager.BG, px, py, 0, 0, pw, ph, pw, ph);
        String current = BackgroundManager.getSelected();
        g.drawString(this.font, "Activo: " + (current.isEmpty() ? "Predeterminado" : current),
                px, py + ph + 8, 0xFFFFFF);

        g.drawString(this.font, "Carpeta: " + BackgroundManager.FOLDER_NAME + " (en .minecraft)",
                px, py + ph + 24, 0xAAAAAA);
        g.drawString(this.font, "Formatos: png, jpg", px, py + ph + 36, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
