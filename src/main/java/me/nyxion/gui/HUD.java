package me.nyxion.gui;

import me.nyxion.Nyxion;
import me.nyxion.module.Module;
import me.nyxion.module.impl.render.HUDModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.*;

public class HUD extends Gui {
    private final Minecraft mc = Minecraft.getMinecraft();
    private static final Color ACCENT_COLOR = new Color(90, 200, 255);
    private final long startTime = System.currentTimeMillis();
    private float animationProgress = 0f;
    private long lastUpdate = System.currentTimeMillis();
    private float rainbowHue = 0f;
    private HUDModule hudModule;
    private final Map<String, Float> moduleAnimations = new HashMap<>();

    public HUD() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        if (hudModule == null) {
            hudModule = (HUDModule) Nyxion.getInstance().getModuleManager().getModuleByName("HUD");
            if (hudModule == null) return;
        }

        render(event.partialTicks);
    }

    private void render(float partialTicks) {
        if (mc == null || mc.gameSettings.showDebugInfo || mc.gameSettings.hideGUI || mc.thePlayer == null) return;

        ScaledResolution sr = new ScaledResolution(mc);
        updateAnimations();

        int yOffset = 5;

        // Draw ArrayList first to ensure proper layering
        if (hudModule.isArrayListEnabled()) {
            drawArrayList(sr);
        }

        // Draw watermark
        if (hudModule.isWatermarkEnabled()) {
            drawWatermark(sr, yOffset);
            yOffset += 25;
        }

        // Draw session info
        if (hudModule.isSessionEnabled()) {
            drawInfoBox(5, yOffset, "§7Session: §f" + formatTime(System.currentTimeMillis() - startTime));
            yOffset += 20;
        }

        // Draw current time
        if (hudModule.isTimeEnabled()) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            drawInfoBox(5, yOffset, "§7Time: §f" + timeFormat.format(new Date()));
            yOffset += 20;
        }

        // Draw game info at bottom
        yOffset = sr.getScaledHeight() - 5;

        // Draw coordinates
        if (hudModule.isCoordsEnabled()) {
            yOffset -= 15;
            String coords = String.format("§7XYZ: §f%d, %d, %d",
                    (int)mc.thePlayer.posX, (int)mc.thePlayer.posY, (int)mc.thePlayer.posZ);
            drawInfoBox(5, yOffset, coords);
        }

        // Draw FPS
        if (hudModule.isFPSEnabled()) {
            yOffset -= 20;
            String fps = mc.debug.split(" fps")[0];
            drawInfoBox(5, yOffset, "§7FPS: §f" + fps);
        }
    }

    private void drawArrayList(ScaledResolution sr) {
        List<Module> enabledModules = new ArrayList<>();
        for (Module module : Nyxion.getInstance().getModuleManager().getModules()) {
            if (module.isEnabled() && !(module instanceof HUDModule)) {
                enabledModules.add(module);
            }
        }

        // Sort modules by name length
        enabledModules.sort((m1, m2) ->
                mc.fontRendererObj.getStringWidth(m2.getName()) -
                        mc.fontRendererObj.getStringWidth(m1.getName()));

        int y = 2;
        float hue = rainbowHue;

        // First pass: calculate total width for background
        int maxWidth = 0;
        for (Module module : enabledModules) {
            int width = mc.fontRendererObj.getStringWidth(module.getName());
            maxWidth = Math.max(maxWidth, width);
        }

        // Draw background for all modules
        if (!enabledModules.isEmpty()) {
            drawRect(sr.getScaledWidth() - maxWidth - 6, 0,
                    sr.getScaledWidth(),
                    y + enabledModules.size() * 11 + 1,
                    new Color(0, 0, 0, 120).getRGB());
        }

        // Second pass: draw modules
        for (Module module : enabledModules) {
            float animation = moduleAnimations.computeIfAbsent(module.getName(), k -> 0f);
            animation = MathHelper.clamp_float(animation + (1f / 8f), 0f, 1f);
            moduleAnimations.put(module.getName(), animation);

            if (animation <= 0f) continue;

            // Calculate x position for right alignment
            int width = mc.fontRendererObj.getStringWidth(module.getName());
            int x = sr.getScaledWidth() - width - 4;

            // Get color based on mode
            Color color;
            switch (hudModule.getArrayListMode()) {
                case "Rainbow":
                    color = Color.getHSBColor(hue, 0.8f, 1f);
                    hue += 0.025f;
                    break;
                case "Fade":
                    color = Color.getHSBColor(rainbowHue, 0.8f, 1f);
                    break;
                default: // Static
                    color = hudModule.getArrayListColor();
                    break;
            }

            // Draw module name with shadow
            int alpha = (int)(animation * 255);
            mc.fontRendererObj.drawStringWithShadow(module.getName(),
                    x, y + 2,
                    new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha).getRGB());

            y += 11;
        }

        // Clean up animations
        moduleAnimations.entrySet().removeIf(entry -> {
            Module module = Nyxion.getInstance().getModuleManager().getModuleByName(entry.getKey());
            if (module == null || !module.isEnabled()) {
                entry.setValue(entry.getValue() - (1f / 8f));
                return entry.getValue() <= 0f;
            }
            return false;
        });
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float delta = (currentTime - lastUpdate) / 1000f;
        lastUpdate = currentTime;

        animationProgress = MathHelper.clamp_float(animationProgress + delta * 2f, 0f, 1f);
        rainbowHue = (rainbowHue + delta * 0.2f) % 1f;
    }

    private void drawWatermark(ScaledResolution sr, int yOffset) {
        // Background
        drawRect(5, yOffset, 105, yOffset + 20, new Color(0, 0, 0, 150).getRGB());

        // Rainbow line
        Color rainbow = Color.getHSBColor(rainbowHue, 0.8f, 1f);
        drawRect(5, yOffset + 20, 105, yOffset + 21, rainbow.getRGB());

        // Client name
        GlStateManager.pushMatrix();
        float scale = 1.5f;
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow("§bNyxion", 5 / scale, (yOffset + 3) / scale, -1);
        GlStateManager.popMatrix();

        // Version
        mc.fontRendererObj.drawStringWithShadow("§7v" + Nyxion.VERSION, 70, yOffset + 9, -1);
    }

    private void drawInfoBox(int x, int y, String text) {
        drawRect(x, y, x + 110, y + 15, new Color(0, 0, 0, 150).getRGB());
        drawRect(x, y, x + 2, y + 15, ACCENT_COLOR.getRGB());
        mc.fontRendererObj.drawStringWithShadow(text, x + 5, y + 4, -1);
    }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        minutes %= 60;
        seconds %= 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}