package me.nyxion.module.impl.render;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class BlockOverlayModule extends Module {
    private final ColorSetting overlayColor = new ColorSetting("Overlay Color", "Color of block overlay", new Color(255, 255, 255, 120));
    private final NumberSetting lineWidth = new NumberSetting("Line Width", "Width of outline", 2.0, 0.5, 5.0, 0.5);
    private final BooleanSetting fill = new BooleanSetting("Fill", "Fill the block overlay", true);
    private final BooleanSetting outline = new BooleanSetting("Outline", "Show block outline", true);
    private final NumberSetting expandAmount = new NumberSetting("Expand", "Expand the overlay", 0.002, 0.0, 0.02, 0.001);

    public BlockOverlayModule() {
        super("BlockOverlay", "Customize block selection overlay", Category.RENDER);
        setKeyBind(Keyboard.KEY_NONE);
        
        addSetting(overlayColor);
        addSetting(lineWidth);
        addSetting(fill);
        addSetting(outline);
        addSetting(expandAmount);
    }

    @SubscribeEvent
    public void onDrawBlockHighlight(DrawBlockHighlightEvent event) {
        if (!isEnabled()) return;

        MovingObjectPosition target = mc.objectMouseOver;
        if (target == null || target.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return;

        event.setCanceled(true);

        BlockPos pos = target.getBlockPos();
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        
        if (block == null || block.isAir(mc.theWorld, pos)) return;

        // Get block bounds
        AxisAlignedBB bb = block.getSelectedBoundingBox(mc.theWorld, pos)
                .expand(expandAmount.getValue(), expandAmount.getValue(), expandAmount.getValue())
                .offset(-mc.getRenderManager().viewerPosX,
                       -mc.getRenderManager().viewerPosY,
                       -mc.getRenderManager().viewerPosZ);

        // Setup GL state
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glLineWidth(lineWidth.getValue().floatValue());

        Color color = overlayColor.getValue();
        float red = color.getRed() / 255F;
        float green = color.getGreen() / 255F;
        float blue = color.getBlue() / 255F;
        float alpha = color.getAlpha() / 255F;

        // Draw fill
        if (fill.getValue()) {
            GlStateManager.disableDepth();
            GlStateManager.color(red, green, blue, alpha * 0.5F);
            drawFilledBox(bb);
            GlStateManager.enableDepth();
        }

        // Draw outline
        if (outline.getValue()) {
            GlStateManager.color(red, green, blue, alpha);
            RenderGlobal.drawSelectionBoundingBox(bb);
        }

        // Reset GL state
        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawFilledBox(AxisAlignedBB bb) {
        GL11.glBegin(GL11.GL_QUADS);
        
        // Bottom
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);

        // Top
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);

        // Front
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);

        // Back
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);

        // Left
        GL11.glVertex3d(bb.minX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.minX, bb.minY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.minX, bb.maxY, bb.minZ);

        // Right
        GL11.glVertex3d(bb.maxX, bb.minY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.minZ);
        GL11.glVertex3d(bb.maxX, bb.maxY, bb.maxZ);
        GL11.glVertex3d(bb.maxX, bb.minY, bb.maxZ);

        GL11.glEnd();
    }
}