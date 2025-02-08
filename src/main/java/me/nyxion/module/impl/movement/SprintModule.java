package me.nyxion.module.impl.movement;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.BooleanSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class SprintModule extends Module {
    private final BooleanSetting omniSprint = new BooleanSetting("Omni", "Sprint in all directions", false);
    private final BooleanSetting keepSprint = new BooleanSetting("Keep Sprint", "Keep sprinting when hitting entities", true);

    public SprintModule() {
        super("Sprint", "Automatically sprints for you", Category.MOVEMENT);
        setKeyBind(Keyboard.KEY_M);
        addSetting(omniSprint);
        addSetting(keepSprint);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.thePlayer.isSneaking() || 
            mc.thePlayer.isCollidedHorizontally || mc.thePlayer.getFoodStats().getFoodLevel() <= 6) {
            return;
        }

        if (omniSprint.getValue()) {
            // Sprint in any direction when moving
            if ((mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0) && 
                !mc.thePlayer.isUsingItem()) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            // Only sprint when moving forward
            if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isUsingItem()) {
                mc.thePlayer.setSprinting(true);
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer != null) {
            mc.thePlayer.setSprinting(false);
        }
    }
}