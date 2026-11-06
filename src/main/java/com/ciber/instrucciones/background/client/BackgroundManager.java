package com.ciber.instrucciones.background.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Maneja la carpeta de imagenes y el fondo activo.
 *
 * El truco: la pantalla original dibuja "instrucciones:textures/gui/background.png".
 * Para cambiar el fondo registramos otra textura (DynamicTexture) bajo ESE MISMO nombre,
 * asi no hace falta modificar ninguna clase original. Para volver al fondo predeterminado
 * se vuelve a registrar el SimpleTexture que lee el png del propio mod.
 */
public final class BackgroundManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Textura que dibuja la pantalla original de instrucciones. */
    public static final ResourceLocation BG =
            new ResourceLocation("instrucciones", "textures/gui/background.png");

    public static final String FOLDER_NAME = "instrucciones_fondos";
    private static final int MAX_WIDTH = 2560;

    private static Path folder;
    private static Path configFile;
    /** Nombre del archivo activo; "" = fondo predeterminado del mod. */
    private static String selected = "";

    private BackgroundManager() {}

    public static void init() {
        folder = FMLPaths.GAMEDIR.get().resolve(FOLDER_NAME);
        configFile = FMLPaths.CONFIGDIR.get().resolve("instrucciones-fondo.json");
        try {
            Files.createDirectories(folder);
            Path readme = folder.resolve("LEEME.txt");
            if (!Files.exists(readme)) {
                Files.writeString(readme,
                        "Pon aqui tus imagenes (.png, .jpg, .jpeg) para usarlas de fondo.\n"
                                + "Luego usa el comando /ins backgraund y elige la imagen en el menu.\n",
                        StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOGGER.warn("[instrucciones] No se pudo crear la carpeta de fondos", e);
        }
        String saved = loadSaved();
        if (!saved.isEmpty()) {
            apply(saved, false);
        }
    }

    public static Path getFolder() {
        return folder;
    }

    public static String getSelected() {
        return selected;
    }

    /** Nombres de las imagenes disponibles en la carpeta, ordenados. */
    public static List<String> list() {
        List<String> result = new ArrayList<>();
        if (folder == null || !Files.isDirectory(folder)) {
            return result;
        }
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(BackgroundManager::isImage)
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .forEach(result::add);
        } catch (IOException e) {
            LOGGER.warn("[instrucciones] No se pudo leer la carpeta de fondos", e);
        }
        return result;
    }

    private static boolean isImage(String name) {
        String n = name.toLowerCase(Locale.ROOT);
        return n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg");
    }

    /** Busca el archivo SOLO entre los de la carpeta (evita rutas raras). */
    private static Path find(String name) {
        for (String candidate : list()) {
            if (candidate.equals(name)) {
                return folder.resolve(candidate);
            }
        }
        return null;
    }

    /** Aplica el fondo "name" ("" = predeterminado). Si la imagen no existe aqui, usa el predeterminado. */
    public static void apply(String name, boolean persist) {
        final String wanted = name == null ? "" : name;
        Minecraft.getInstance().execute(() -> applyNow(wanted, persist));
    }

    private static void applyNow(String wanted, boolean persist) {
        Minecraft mc = Minecraft.getInstance();
        if (!wanted.isEmpty()) {
            Path file = find(wanted);
            if (file != null) {
                try (InputStream in = Files.newInputStream(file)) {
                    NativeImage image = NativeImage.read(in);
                    NativeImage fitted = fit(image);
                    if (fitted != image) {
                        image.close();
                    }
                    mc.getTextureManager().register(BG, new DynamicTexture(fitted));
                    selected = wanted;
                    if (persist) {
                        save();
                    }
                    return;
                } catch (Exception e) {
                    LOGGER.warn("[instrucciones] No se pudo cargar el fondo {}", wanted, e);
                }
            }
            // la imagen no esta en este equipo (o fallo): se muestra el predeterminado sin borrar la preferencia guardada
            mc.getTextureManager().register(BG, new SimpleTexture(BG));
            selected = "";
            return;
        }
        mc.getTextureManager().register(BG, new SimpleTexture(BG));
        selected = "";
        if (persist) {
            save();
        }
    }

    /** Recorta al centro a 16:9 (como el fondo original) y limita el tamano para no gastar memoria. */
    private static NativeImage fit(NativeImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int cw = w;
        int ch = h;
        if ((long) w * 9 > (long) h * 16) {
            cw = h * 16 / 9;
        } else {
            ch = w * 9 / 16;
        }
        int ow = Math.min(cw, MAX_WIDTH);
        int oh = Math.max(1, ow * 9 / 16);
        if (cw == w && ch == h && ow == w && oh == h) {
            return src;
        }
        int x0 = (w - cw) / 2;
        int y0 = (h - ch) / 2;
        NativeImage out = new NativeImage(ow, oh, false);
        for (int y = 0; y < oh; y++) {
            int sy = y0 + (int) ((long) y * ch / oh);
            for (int x = 0; x < ow; x++) {
                int sx = x0 + (int) ((long) x * cw / ow);
                out.setPixelRGBA(x, y, src.getPixelRGBA(sx, sy));
            }
        }
        return out;
    }

    private static String loadSaved() {
        try {
            if (configFile != null && Files.exists(configFile)) {
                JsonObject json = JsonParser.parseString(
                        Files.readString(configFile, StandardCharsets.UTF_8)).getAsJsonObject();
                if (json.has("selected")) {
                    return json.get("selected").getAsString();
                }
            }
        } catch (Exception e) {
            LOGGER.warn("[instrucciones] No se pudo leer instrucciones-fondo.json", e);
        }
        return "";
    }

    private static void save() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("selected", selected);
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.warn("[instrucciones] No se pudo guardar instrucciones-fondo.json", e);
        }
    }
}
