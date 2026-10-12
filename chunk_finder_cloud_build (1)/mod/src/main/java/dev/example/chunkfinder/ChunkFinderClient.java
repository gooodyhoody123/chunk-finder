package dev.example.chunkfinder;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Conservative client-side finder. It only inspects structure starts exposed by
 * chunks already loaded on the client; it does not request chunks or inspect server files.
 */
public final class ChunkFinderClient implements ClientModInitializer {
    private static final FinderState STATE = new FinderState();
    private static final Identifier HUD_ID = Identifier.of("chunk_finder", "overlay");

    private static KeyBinding toggleKey;
    private static KeyBinding radiusUpKey;
    private static KeyBinding radiusDownKey;
    private static KeyBinding villagesKey;
    private static KeyBinding mineshaftsKey;
    private static KeyBinding strongholdsKey;

    private static List<Marker> cachedMarkers = List.of();
    private static int ticksUntilScan;

    @Override
    public void onInitializeClient() {
        toggleKey = register("key.chunk_finder.toggle", GLFW.GLFW_KEY_O);
        radiusDownKey = register("key.chunk_finder.radius_down", GLFW.GLFW_KEY_LEFT_BRACKET);
        radiusUpKey = register("key.chunk_finder.radius_up", GLFW.GLFW_KEY_RIGHT_BRACKET);
        villagesKey = register("key.chunk_finder.filter_villages", GLFW.GLFW_KEY_V);
        mineshaftsKey = register("key.chunk_finder.filter_mineshafts", GLFW.GLFW_KEY_M);
        strongholdsKey = register("key.chunk_finder.filter_strongholds", GLFW.GLFW_KEY_H);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) STATE.enabled = !STATE.enabled;
            while (radiusUpKey.wasPressed()) {
                STATE.radius = Math.min(512, STATE.radius + 32);
                ticksUntilScan = 0;
            }
            while (radiusDownKey.wasPressed()) {
                STATE.radius = Math.max(32, STATE.radius - 32);
                ticksUntilScan = 0;
            }
            while (villagesKey.wasPressed()) {
                STATE.villages = !STATE.villages;
                ticksUntilScan = 0;
            }
            while (mineshaftsKey.wasPressed()) {
                STATE.mineshafts = !STATE.mineshafts;
                ticksUntilScan = 0;
            }
            while (strongholdsKey.wasPressed()) {
                STATE.strongholds = !STATE.strongholds;
                ticksUntilScan = 0;
            }

            if (ticksUntilScan > 0) ticksUntilScan--;
            if (ticksUntilScan == 0) {
                cachedMarkers = client.player != null && client.world != null
                        ? findLoadedMarkers(client) : List.of();
                ticksUntilScan = 10;
            }
        });

        HudElementRegistry.addLast(HUD_ID, (context, tickCounter) -> renderHud(context));
    }

    private static KeyBinding register(String translationKey, int keyCode) {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding(
                translationKey, InputUtil.Type.KEYSYM, keyCode, KeyBinding.Category.MISC));
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden || !STATE.enabled) return;

        int x = 12, y = 12;
        context.fill(x - 5, y - 5, x + 250, y + 162, 0xB9101420);
        context.drawText(client.textRenderer, "Chunk Finder", x, y, 0xFFFFFFFF, true);
        context.drawText(client.textRenderer, "Radius: " + STATE.radius + " blocks", x, y + 13, 0xFFD1D5DB, false);
        context.drawText(client.textRenderer, "O toggle | [ ] radius", x, y + 26, 0xFF9CA3AF, false);
        context.drawText(client.textRenderer, "V villages: " + onOff(STATE.villages)
                + "  M mines: " + onOff(STATE.mineshafts), x, y + 39, 0xFFD1D5DB, false);
        context.drawText(client.textRenderer, "H strongholds: " + onOff(STATE.strongholds), x, y + 52, 0xFFD1D5DB, false);
        context.drawText(client.textRenderer, "Loaded client data only", x, y + 65, 0xFFFFD166, false);
        context.drawText(client.textRenderer, "Markers: " + cachedMarkers.size(), x, y + 78, 0xFFFFFFFF, false);

        int mapX = x + 8, mapY = y + 96, mapSize = 72;
        context.fill(mapX, mapY, mapX + mapSize, mapY + mapSize, 0xCC202938);
        context.drawBorder(mapX, mapY, mapSize, mapSize, 0xFF65758B);
        context.fill(mapX + mapSize / 2 - 2, mapY + mapSize / 2 - 2,
                mapX + mapSize / 2 + 2, mapY + mapSize / 2 + 2, 0xFFFFFFFF);

        double scale = mapSize / (double) (STATE.radius * 2);
        for (Marker marker : cachedMarkers) {
            double dx = marker.pos().getX() - client.player.getX();
            double dz = marker.pos().getZ() - client.player.getZ();
            int mx = mapX + mapSize / 2 + (int) Math.round(dx * scale);
            int mz = mapY + mapSize / 2 + (int) Math.round(dz * scale);
            if (mx < mapX + 2 || mx > mapX + mapSize - 2 || mz < mapY + 2 || mz > mapY + mapSize - 2) continue;
            context.fill(mx - 2, mz - 2, mx + 3, mz + 3, marker.color());
        }

        int lx = mapX + mapSize + 8, ly = mapY + 3;
        drawLegend(context, lx, ly, 0xFF4ADE80, "Village");
        drawLegend(context, lx, ly + 14, 0xFFFBBF24, "Mineshaft");
        drawLegend(context, lx, ly + 28, 0xFF60A5FA, "Stronghold");
        context.drawText(client.textRenderer, "No geode detection", lx, ly + 45, 0xFFFFD166, false);
    }

    private static String onOff(boolean enabled) { return enabled ? "ON" : "OFF"; }

    private static void drawLegend(DrawContext context, int x, int y, int color, String label) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.fill(x, y + 1, x + 6, y + 7, color);
        context.drawText(client.textRenderer, label, x + 10, y, 0xFFFFFFFF, false);
    }

    private static List<Marker> findLoadedMarkers(MinecraftClient client) {
        if (client.player == null || client.world == null) return List.of();
        Map<String, Marker> unique = new LinkedHashMap<>();
        BlockPos playerPos = client.player.getBlockPos();
        ChunkPos center = new ChunkPos(playerPos);
        int chunkRadius = (int) Math.ceil(STATE.radius / 16.0);
        double radiusSquared = (double) STATE.radius * STATE.radius;

        for (int cx = center.x - chunkRadius; cx <= center.x + chunkRadius; cx++) {
            for (int cz = center.z - chunkRadius; cz <= center.z + chunkRadius; cz++) {
                int chunkCenterX = (cx << 4) + 8, chunkCenterZ = (cz << 4) + 8;
                double dx = chunkCenterX - playerPos.getX(), dz = chunkCenterZ - playerPos.getZ();
                if (dx * dx + dz * dz > radiusSquared) continue;
                WorldChunk chunk = client.world.getChunkManager().getWorldChunk(cx, cz);
                if (chunk == null) continue;

                chunk.getStructureStarts().forEach((structure, start) -> {
                    if (start == null || !start.hasChildren()) return;
                    String id = Registries.STRUCTURE.getId(structure).toString();
                    int color = 0;
                    if (STATE.villages && id.contains("village")) color = 0xFF4ADE80;
                    else if (STATE.mineshafts && id.contains("mineshaft")) color = 0xFFFBBF24;
                    else if (STATE.strongholds && id.contains("stronghold")) color = 0xFF60A5FA;
                    if (color != 0) {
                        BlockPos pos = start.getPos();
                        String key = id + "@" + pos.getX() + "," + pos.getZ();
                        unique.putIfAbsent(key, new Marker(pos, color));
                    }
                });
            }
        }
        return new ArrayList<>(unique.values());
    }

    private record Marker(BlockPos pos, int color) {}

    private static final class FinderState {
        boolean enabled = true;
        boolean villages = true;
        boolean mineshafts = true;
        boolean strongholds = true;
        int radius = 128;
    }
}
