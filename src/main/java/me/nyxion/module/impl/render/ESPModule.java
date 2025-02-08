package me.nyxion.module.impl.render;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ESPModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "ESP rendering mode", "2D", "2D", "Box", "Outline");
    private final ColorSetting playerColor = new ColorSetting("Player Color", "Color for players", new Color(255, 0, 0, 120));
    private final BooleanSetting showInvis = new BooleanSetting("Show Invisible", "Show invisible entities", true);
    private final BooleanSetting showTeam = new BooleanSetting("Show Team", "Show team members", false);
    private final NumberSetting width = new NumberSetting("Width", "Line width", 2.0, 0.5, 5.0, 0.5);

    public ESPModule() {
        super("ESP", "See entities through walls", Category.RENDER);
        setKeyBind(Keyboard.KEY_NONE);

        addSetting(mode);
        addSetting(playerColor);
        addSetting(showInvis);
        addSetting(showTeam);
        addSetting(width);
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (!isEnabled()) return;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) continue;
            if (!showInvis.getValue() && entity.isInvisible()) continue;

            EntityPlayer player = (EntityPlayer) entity;
            if (!showTeam.getValue() && mc.thePlayer.isOnSameTeam(player)) continue;

            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;

            switch (mode.getValue().toLowerCase()) {
                case "2d":
                    draw2DESP(entity, x, y, z);
                    break;
                case "box":
                    drawBoxESP(entity, x, y, z);
                    break;
                case "outline":
                    drawOutlineESP(entity, x, y, z);
                    break;
            }
        }
    }

    private void draw2DESP(Entity entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY,
                z - mc.getRenderManager().viewerPosZ
        );

        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width.getValue().floatValue());

        Color color = playerColor.getValue();
        GlStateManager.color(
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F,
                color.getAlpha() / 255F
        );

        // Draw box
        AxisAlignedBB bb = new AxisAlignedBB(
                -entity.width / 2, 0, -entity.width / 2,
                entity.width / 2, entity.height, entity.width / 2
        );

        drawOutlinedBoundingBox(bb);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawBoxESP(Entity entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY,
                z - mc.getRenderManager().viewerPosZ
        );

        Color color = playerColor.getValue();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GlStateManager.color(
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F,
                color.getAlpha() / 255F
        );

        AxisAlignedBB bb = new AxisAlignedBB(
                -entity.width / 2, 0, -entity.width / 2,
                entity.width / 2, entity.height, entity.width / 2
        );

        drawFilledBoundingBox(bb);

        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawOutlineESP(Entity entity, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x - mc.getRenderManager().viewerPosX,
                y - mc.getRenderManager().viewerPosY,
                z - mc.getRenderManager().viewerPosZ
        );

        Color color = playerColor.getValue();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(width.getValue().floatValue());

        GlStateManager.color(
                color.getRed() / 255F,
                color.getGreen() / 255F,
                color.getBlue() / 255F,
                color.getAlpha() / 255F
        );

        AxisAlignedBB bb = new AxisAlignedBB(
                -entity.width / 2, 0, -entity.width / 2,
                entity.width / 2, entity.height, entity.width / 2
        );

        drawOutlinedBoundingBox(bb);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void drawOutlinedBoundingBox(AxisAlignedBB bb) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        // Bottom
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();

        // Top
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();

        // Sides
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();

        tessellator.draw();
    }

    private void drawFilledBoundingBox(AxisAlignedBB bb) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

        // Bottom
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();

        // Top
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();

        // Front
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();

        // Back
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();

        // Left
        worldrenderer.pos(bb.minX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.minX, bb.maxY, bb.minZ).endVertex();

        // Right
        worldrenderer.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
        worldrenderer.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();

        tessellator.draw();
    }
}