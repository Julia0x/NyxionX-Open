package me.nyxion.module.impl.movement;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.ModeSetting;
import me.nyxion.module.settings.NumberSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class StepModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Step mode", "Vanilla", "Vanilla", "NCP");
    private final NumberSetting height = new NumberSetting("Height", "Step height", 1.0, 0.5, 2.5, 0.1);

    public StepModule() {
        super("Step", "Automatically steps up blocks", Category.MOVEMENT);
        setKeyBind(Keyboard.KEY_NONE);
        addSetting(mode);
        addSetting(height);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;

        if (mode.getValue().equals("Vanilla")) {
            mc.thePlayer.stepHeight = height.getValue().floatValue();
        } else if (mode.getValue().equals("NCP")) {
            if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.5F;
        }
    }
}