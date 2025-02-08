package me.nyxion.module.impl.render;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import me.nyxion.utils.other.MathUtil;
import me.nyxion.utils.other.TimeUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.awt.Color;

public class TargetHUDModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "HUD style", "Modern", "Modern", "Simple", "Minimal");
    private final NumberSetting x = new NumberSetting("X", "X position", 100, 0, mc.displayWidth, 1);
    private final NumberSetting y = new NumberSetting("Y", "Y position", 100, 0, mc.displayHeight, 1);
    private final ColorSetting backgroundColor = new ColorSetting("Background", "Background color", new Color(0, 0, 0, 160));
    private final ColorSetting accentColor = new ColorSetting("Accent", "Accent color", new Color(90, 200, 255));
    private final NumberSetting fadeTime = new NumberSetting("Fade Time", "Time before HUD fades (seconds)", 2.0, 0.5, 5.0, 0.1);

    private EntityLivingBase target;
    private double healthAnimation = 0.0;
    private final TimeUtil attackTimer = new TimeUtil();
    private double fadeAnimation = 0.0;

    public TargetHUDModule() {
        super("TargetHUD", "Displays information about your target", Category.RENDER);
        setKeyBind(Keyboard.KEY_NONE);

        addSetting(mode);
        addSetting(x);
        addSetting(y);
        addSetting(backgroundColor);
        addSetting(accentColor);
        addSetting(fadeTime);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!isEnabled() || !(event.target instanceof EntityPlayer)) return;

        target = (EntityPlayer) event.target;
        attackTimer.reset();
        fadeAnimation = 1.0;
    }

    @SubscribeEvent
    public void onHurt(LivingHurtEvent event) {
        if (!isEnabled() || event.entity != mc.thePlayer || !(event.source.getEntity() instanceof EntityPlayer)) return;

        target = (EntityPlayer) event.source.getEntity();
        attackTimer.reset();
        fadeAnimation = 1.0;
    }

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (!isEnabled() || event.type != RenderGameOverlayEvent.ElementType.CROSSHAIRS) return;

        // Check if we should hide the HUD
        if (target != null) {
            long timeSinceAttack = attackTimer.elapsed();
            if (timeSinceAttack > fadeTime.getValue() * 1000) {
                fadeAnimation = Math.max(0, fadeAnimation - 0.1);
                if (fadeAnimation <= 0) {
                    target = null;
                    return;
                }
            }
        }

        if (target == null || !target.isEntityAlive()) {
            fadeAnimation = Math.max(0, fadeAnimation - 0.1);
            if (fadeAnimation <= 0) {
                target = null;
                return;
            }
        }

        // Render based on mode
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.color(1, 1, 1, (float)fadeAnimation);

        switch (mode.getValue().toLowerCase()) {
            case "modern":
                renderModernHUD();
                break;
            case "simple":
                renderSimpleHUD();
                break;
            case "minimal":
                renderMinimalHUD();
                break;
        }

        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void renderModernHUD() {
        int width = 150;
        int height = 50;
        double posX = x.getValue();
        double posY = y.getValue();

        // Background with fade
        Color bgColor = backgroundColor.getValue();
        Gui.drawRect((int)posX, (int)posY, (int)(posX + width), (int)(posY + height),
                new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
                        (int)(bgColor.getAlpha() * fadeAnimation)).getRGB());

        // Target name
        mc.fontRendererObj.drawStringWithShadow(target.getName(),
                (float)(posX + 40), (float)(posY + 5), -1);

        // Health bar background
        Gui.drawRect((int)(posX + 40), (int)(posY + 20),
                (int)(posX + width - 5), (int)(posY + 28),
                new Color(0, 0, 0, (int)(100 * fadeAnimation)).getRGB());

        // Animate health
        healthAnimation = MathUtil.interpolate(healthAnimation, target.getHealth(), 0.1);
        double healthPercent = healthAnimation / target.getMaxHealth();

        // Health bar
        Color accentCol = accentColor.getValue();
        Gui.drawRect((int)(posX + 40), (int)(posY + 20),
                (int)(posX + 40 + (width - 45) * healthPercent), (int)(posY + 28),
                new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(),
                        (int)(accentCol.getAlpha() * fadeAnimation)).getRGB());

        // Health text
        String healthText = String.format("%.1f HP", healthAnimation);
        mc.fontRendererObj.drawStringWithShadow(healthText,
                (float)(posX + 40), (float)(posY + 30),
                new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(),
                        (int)(255 * fadeAnimation)).getRGB());

        // Render player model
        GlStateManager.pushMatrix();
        GuiInventory.drawEntityOnScreen((int)posX + 20, (int)posY + 45, 20,
                -mc.thePlayer.rotationYaw, -mc.thePlayer.rotationPitch, target);
        GlStateManager.popMatrix();

        // Distance
        String distance = String.format("%.1f blocks", mc.thePlayer.getDistanceToEntity(target));
        mc.fontRendererObj.drawStringWithShadow(distance,
                (float)(posX + width - mc.fontRendererObj.getStringWidth(distance) - 5),
                (float)(posY + height - 12), -1);
    }

    private void renderSimpleHUD() {
        int width = 120;
        int height = 40;
        double posX = x.getValue();
        double posY = y.getValue();

        // Background with fade
        Color bgColor = backgroundColor.getValue();
        Gui.drawRect((int)posX, (int)posY, (int)(posX + width), (int)(posY + height),
                new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
                        (int)(bgColor.getAlpha() * fadeAnimation)).getRGB());

        // Name and health
        mc.fontRendererObj.drawStringWithShadow(target.getName(),
                (float)(posX + 5), (float)(posY + 5), -1);

        // Health bar
        healthAnimation = MathUtil.interpolate(healthAnimation, target.getHealth(), 0.1);
        double healthPercent = healthAnimation / target.getMaxHealth();

        Gui.drawRect((int)(posX + 5), (int)(posY + 20),
                (int)(posX + width - 5), (int)(posY + 25),
                new Color(0, 0, 0, (int)(100 * fadeAnimation)).getRGB());

        Color accentCol = accentColor.getValue();
        Gui.drawRect((int)(posX + 5), (int)(posY + 20),
                (int)(posX + 5 + (width - 10) * healthPercent), (int)(posY + 25),
                new Color(accentCol.getRed(), accentCol.getGreen(), accentCol.getBlue(),
                        (int)(accentCol.getAlpha() * fadeAnimation)).getRGB());

        // Health text
        String healthText = String.format("%.1f HP", healthAnimation);
        mc.fontRendererObj.drawStringWithShadow(healthText,
                (float)(posX + 5), (float)(posY + 28), -1);
    }

    private void renderMinimalHUD() {
        int width = 100;
        int height = 20;
        double posX = x.getValue();
        double posY = y.getValue();

        // Background with fade
        Color bgColor = backgroundColor.getValue();
        Gui.drawRect((int)posX, (int)posY, (int)(posX + width), (int)(posY + height),
                new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(),
                        (int)(bgColor.getAlpha() * fadeAnimation)).getRGB());

        // Name
        mc.fontRendererObj.drawStringWithShadow(target.getName(),
                (float)(posX + 5), (float)(posY + 5), -1);

        // Health
        String healthText = String.format("%.1f HP", target.getHealth());
        mc.fontRendererObj.drawStringWithShadow(healthText,
                (float)(posX + width - mc.fontRendererObj.getStringWidth(healthText) - 5),
                (float)(posY + 5), accentColor.getValue().getRGB());
    }
}