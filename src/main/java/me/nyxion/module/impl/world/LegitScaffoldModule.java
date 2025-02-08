package me.nyxion.module.impl.world;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.BooleanSetting;
import me.nyxion.module.settings.NumberSetting;
import me.nyxion.utils.other.TimeUtil;
import me.nyxion.utils.player.PlayerUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class LegitScaffoldModule extends Module {
    private final NumberSetting sneakTime = new NumberSetting("Sneak Time", "Time to sneak in ms", 60, 0, 300, 20);
    private final BooleanSetting onlyGround = new BooleanSetting("Only Ground", "Only activate when on ground", true);
    private final BooleanSetting holdSneak = new BooleanSetting("Hold Sneak", "Hold sneak key instead of toggle", false);

    private boolean shouldSneak = false;
    private final TimeUtil sneakTimer = new TimeUtil();

    public LegitScaffoldModule() {
        super("LegitScaffold", "Helps with bridging safely", Category.WORLD);
        setKeyBind(Keyboard.KEY_G);

        addSetting(sneakTime);
        addSetting(onlyGround);
        addSetting(holdSneak);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.currentScreen != null) return;

        // Check if sneak timer has elapsed
        shouldSneak = !sneakTimer.hasReached((int)Math.round(sneakTime.getValue()));

        // Check if player is over air
        if (PlayerUtil.isOverAir() && (!onlyGround.getValue() || mc.thePlayer.onGround) && mc.thePlayer.motionY < 0.1) {
            shouldSneak = true;
            sneakTimer.reset();
        }

        // Handle sneaking based on mode
        if (holdSneak.getValue()) {
            // Hold mode - only sneak when both conditions are met
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) && shouldSneak);
        } else {
            // Toggle mode - sneak when either condition is met
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindSneak) || shouldSneak);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Reset sneak key
        if (mc.thePlayer != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
        shouldSneak = false;
    }
}