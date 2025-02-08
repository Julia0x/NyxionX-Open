package me.nyxion.module.impl.combat;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.ModeSetting;
import me.nyxion.utils.other.TimeUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class WTapModule extends Module {
    private int ticks;
    private final TimeUtil wtapTimer = new TimeUtil();
    private final ModeSetting wtapMode = new ModeSetting("Mode", "WTap mode", "WTap",
            "WTap", "STap", "Shift Tap", "Packet", "Legit");

    public WTapModule() {
        super("WTap", "Automatically releases W when attacking", Category.COMBAT);
        setKeyBind(Keyboard.KEY_NONE);
        addSetting(wtapMode);
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!isEnabled()) return;

        if (wtapTimer.hasReached(500L)) {
            wtapTimer.reset();
            ticks = 2;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;

        switch (ticks) {
            case 2:
                switch (wtapMode.getValue().toLowerCase()) {
                    case "wtap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                        break;
                    case "stap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
                        break;
                    case "shift tap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                        break;
                    case "packet":
                        mc.thePlayer.sendQueue.addToSendQueue(
                                new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING)
                        );
                        break;
                    case "legit":
                        mc.thePlayer.setSprinting(false);
                        break;
                }
                ticks--;
                break;

            case 1:
                switch (wtapMode.getValue().toLowerCase()) {
                    case "wtap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),
                                GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
                        break;
                    case "stap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),
                                GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(),
                                GameSettings.isKeyDown(mc.gameSettings.keyBindBack));
                        break;
                    case "shift tap":
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                                GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
                        break;
                    case "packet":
                        mc.thePlayer.sendQueue.addToSendQueue(
                                new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING)
                        );
                        break;
                    case "legit":
                        mc.thePlayer.setSprinting(true);
                        break;
                }
                ticks--;
                break;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        // Reset key states
        if (mc.thePlayer != null) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindForward));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindBack));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(),
                    GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
        }
    }

    public String getDisplayInfo() {
        return wtapMode.getValue();
    }
}