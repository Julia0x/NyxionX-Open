package me.nyxion.gui;

import me.nyxion.Nyxion;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainMenu extends GuiScreen {
    private static final Color ACCENT_COLOR = new Color(90, 200, 255);
    private static final Color BACKGROUND_COLOR = new Color(20, 20, 25);
    private final List<ModernButton> buttons = new ArrayList<>();
    private float animationProgress = 0f;
    private long initTime;

    @Override
    public void initGui() {
        initTime = System.currentTimeMillis();
        buttons.clear();
        int centerX = this.width / 2;
        int startY = this.height / 2;

        // Add buttons
        addButton("Singleplayer", centerX - 100, startY - 20, () ->
                mc.displayGuiScreen(new GuiSelectWorld(this)));

        addButton("Multiplayer", centerX - 100, startY + 10, () ->
                mc.displayGuiScreen(new GuiMultiplayer(this)));

        addButton("Settings", centerX - 100, startY + 40, () ->
                mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)));

        addButton("Quit", centerX - 100, startY + 70, () ->
                mc.shutdown());
    }

    private void addButton(String text, int x, int y, Runnable action) {
        buttons.add(new ModernButton(text, x, y, 200, 20, action));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Draw background
        drawRect(0, 0, width, height, BACKGROUND_COLOR.getRGB());

        // Update animation
        float targetProgress = 1f;
        animationProgress += (targetProgress - animationProgress) * 0.1f;

        // Draw animated particles
        drawParticles();

        // Draw title
        GlStateManager.pushMatrix();
        float scale = 4.0f * animationProgress;
        String title = "NyxionX Client";
        int titleWidth = mc.fontRendererObj.getStringWidth(title);
        float x = (width / 2f) - (titleWidth * scale / 2f);
        float y = height / 4f - (mc.fontRendererObj.FONT_HEIGHT * scale / 2f);

        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawStringWithShadow(title, 0, 0, ACCENT_COLOR.getRGB());
        GlStateManager.popMatrix();

        // Draw version
        String version = "NyxionX " + Nyxion.VERSION;
        drawString(mc.fontRendererObj, version, 5, height - 15,
                new Color(200, 200, 200, (int)(255 * animationProgress)).getRGB());

        // Draw buttons with animation
        for (int i = 0; i < buttons.size(); i++) {
            ModernButton button = buttons.get(i);
            float buttonAnimation = Math.max(0, Math.min(1,
                    (System.currentTimeMillis() - initTime - i * 100) / 500f));
            button.draw(mouseX, mouseY, buttonAnimation * animationProgress);
        }

        // Draw copyright
        String copyright = "Copyright Mojang AB. Do not distribute!";
        drawString(mc.fontRendererObj, copyright, width - mc.fontRendererObj.getStringWidth(copyright) - 5,
                height - 15, new Color(200, 200, 200, (int)(255 * animationProgress)).getRGB());
    }

    private void drawParticles() {
        int particleCount = 50;
        long time = System.currentTimeMillis();

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        for (int i = 0; i < particleCount; i++) {
            float x = (float) ((Math.sin(time * 0.001 + i) + 1) * width / 2);
            float y = (float) ((Math.cos(time * 0.001 + i) + 1) * height / 2);
            float size = (float) (2 + Math.sin(time * 0.002 + i) * 2);

            int alpha = (int) (100 * animationProgress * (0.5 + Math.sin(time * 0.002 + i) * 0.5));
            Color particleColor = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                    ACCENT_COLOR.getBlue(), alpha);

            drawRect((int)(x - size), (int)(y - size), (int)(x + size), (int)(y + size),
                    particleColor.getRGB());
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (ModernButton button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                button.onClick();
                break;
            }
        }
    }

    private class ModernButton {
        private final String text;
        private final int x, y, width, height;
        private final Runnable action;
        private float hoverProgress = 0f;

        public ModernButton(String text, int x, int y, int width, int height, Runnable action) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.action = action;
        }

        public void draw(int mouseX, int mouseY, float alpha) {
            boolean hover = isMouseOver(mouseX, mouseY);
            hoverProgress += (hover ? 1f : -1f) * 0.1f;
            hoverProgress = Math.max(0f, Math.min(1f, hoverProgress));

            // Draw button background
            Color buttonColor = new Color(30, 30, 35, (int)(200 * alpha));
            drawRect(x, y, x + width, y + height, buttonColor.getRGB());

            // Draw hover effect
            if (hoverProgress > 0) {
                int hoverAlpha = (int)(50 * hoverProgress * alpha);
                Color hoverColor = new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                        ACCENT_COLOR.getBlue(), hoverAlpha);
                drawRect(x, y, x + width, y + height, hoverColor.getRGB());
            }

            // Draw button outline
            drawHorizontalLine(x, x + width - 1, y,
                    new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                            ACCENT_COLOR.getBlue(), (int)(100 * alpha)).getRGB());
            drawHorizontalLine(x, x + width - 1, y + height - 1,
                    new Color(ACCENT_COLOR.getRed(), ACCENT_COLOR.getGreen(),
                            ACCENT_COLOR.getBlue(), (int)(100 * alpha)).getRGB());

            // Draw text
            int textColor = new Color(255, 255, 255, (int)(255 * alpha)).getRGB();
            drawCenteredString(mc.fontRendererObj, text, x + width / 2,
                    y + (height - 8) / 2, textColor);
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }

        public void onClick() {
            action.run();
        }
    }
}