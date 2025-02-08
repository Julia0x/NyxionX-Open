package me.nyxion.module.impl.render;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class ChamsModule extends Module {
    private final ColorSetting visibleColor = new ColorSetting("Visible Color", "Color for visible parts", new Color(255, 0, 0, 255));
    private final ColorSetting hiddenColor = new ColorSetting("Hidden Color", "Color for hidden parts", new Color(255, 0, 0, 100));
    private final BooleanSetting colored = new BooleanSetting("Colored", "Use custom colors", true);
    private final BooleanSetting throughWalls = new BooleanSetting("Through Walls", "See through walls", true);
    private final BooleanSetting showInvis = new BooleanSetting("Show Invisible", "Show invisible entities", true);

    public ChamsModule() {
        super("Chams", "See entities through walls", Category.RENDER);
        setKeyBind(Keyboard.KEY_NONE);
        
        addSetting(visibleColor);
        addSetting(hiddenColor);
        addSetting(colored);
        addSetting(throughWalls);
        addSetting(showInvis);
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        if (!isEnabled()) return;
        
        Entity entity = event.entity;
        if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) return;
        if (!showInvis.getValue() && entity.isInvisible()) return;

        if (throughWalls.getValue()) {
            GlStateManager.disableDepth();
        }
        
        if (colored.getValue()) {
            Color hidden = hiddenColor.getValue();
            GL11.glColor4f(hidden.getRed() / 255F,
                          hidden.getGreen() / 255F,
                          hidden.getBlue() / 255F,
                          hidden.getAlpha() / 255F);
        }
    }

    @SubscribeEvent
    public void onRenderLivingPost(RenderLivingEvent.Post event) {
        if (!isEnabled()) return;
        
        Entity entity = event.entity;
        if (!(entity instanceof EntityPlayer) || entity == mc.thePlayer) return;
        if (!showInvis.getValue() && entity.isInvisible()) return;

        if (colored.getValue()) {
            Color visible = visibleColor.getValue();
            GL11.glColor4f(visible.getRed() / 255F,
                          visible.getGreen() / 255F,
                          visible.getBlue() / 255F,
                          visible.getAlpha() / 255F);
        }

        if (throughWalls.getValue()) {
            GlStateManager.enableDepth();
        }
    }
}