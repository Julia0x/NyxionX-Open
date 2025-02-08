package me.nyxion.module.impl.movement;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.ModeSetting;
import me.nyxion.module.settings.NumberSetting;
import me.nyxion.utils.player.PlayerUtil;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class SpeedModule extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Speed mode", "Vanilla", "Vanilla", "NCP", "Bhop");
    private final NumberSetting speed = new NumberSetting("Speed", "Movement speed multiplier", 1.5, 1.0, 3.0, 0.1);
    private int stage = 0;

    public SpeedModule() {
        super("Speed", "Increases movement speed", Category.MOVEMENT);
        setKeyBind(Keyboard.KEY_V);
        addSetting(mode);
        addSetting(speed);
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null || mc.thePlayer.isInWater() ||
                mc.thePlayer.isInLava() || mc.thePlayer.isOnLadder()) {
            return;
        }

        switch (mode.getValue().toLowerCase()) {
            case "vanilla":
                handleVanillaSpeed();
                break;
            case "ncp":
                handleNCPSpeed();
                break;
            case "bhop":
                handleBhopSpeed();
                break;
        }
    }

    private void handleVanillaSpeed() {
        if (mc.thePlayer.onGround && isMoving()) {
            double multiplier = speed.getValue() * getSpeedEffect();
            mc.thePlayer.motionX *= multiplier;
            mc.thePlayer.motionZ *= multiplier;
        }
    }

    private void handleNCPSpeed() {
        if (isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                mc.thePlayer.motionX *= 1.1;
                mc.thePlayer.motionZ *= 1.1;
            } else {
                mc.thePlayer.motionY -= 0.0147;
            }
        }
    }

    private void handleBhopSpeed() {
        if (isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                stage = 1;
            } else {
                switch (stage) {
                    case 1:
                        mc.thePlayer.motionY = -0.0943;
                        stage = 2;
                        break;
                    case 2:
                        mc.thePlayer.motionY -= 0.0943;
                        stage = 3;
                        break;
                    case 3:
                        mc.thePlayer.motionY -= 0.0943;
                        stage = 4;
                        break;
                    default:
                        mc.thePlayer.motionY -= 0.0943;
                        stage = 1;
                        break;
                }
            }
            setSpeed(PlayerUtil.getBaseMoveSpeed() * speed.getValue());
        }
    }

    private boolean isMoving() {
        return mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0;
    }

    private double getSpeedEffect() {
        return mc.thePlayer.isPotionActive(Potion.moveSpeed) ?
                1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1) : 1.0;
    }

    private void setSpeed(double speed) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = mc.thePlayer.rotationYaw;

        if (forward == 0 && strafe == 0) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
            return;
        }

        if (forward != 0) {
            if (strafe > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (strafe < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            strafe = 0;
            if (forward > 0) {
                forward = 1;
            } else {
                forward = -1;
            }
        }

        mc.thePlayer.motionX = forward * speed * Math.cos(Math.toRadians(yaw + 90.0f)) +
                strafe * speed * Math.sin(Math.toRadians(yaw + 90.0f));
        mc.thePlayer.motionZ = forward * speed * Math.sin(Math.toRadians(yaw + 90.0f)) -
                strafe * speed * Math.cos(Math.toRadians(yaw + 90.0f));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stage = 0;
    }

    public String getDisplayInfo() {
        return mode.getValue();
    }
}