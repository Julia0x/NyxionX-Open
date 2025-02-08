package me.nyxion.module.impl.combat;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import me.nyxion.module.settings.BooleanSetting;
import me.nyxion.module.settings.ModeSetting;
import me.nyxion.module.settings.NumberSetting;
import me.nyxion.utils.other.AttackUtil;
import me.nyxion.utils.other.TimeUtil;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class AutoClickerModule extends Module {
    private final NumberSetting targetCPS = new NumberSetting("Target CPS", "Base clicks per second", 11, 0, 30, 1);
    private final NumberSetting randomization = new NumberSetting("Randomization", "Random CPS variation", 1.5, 0, 4, 0.1);
    private final ModeSetting presetMode = new ModeSetting("Preset", "Clicking pattern preset", "Legit",
            "Legit", "Butterfly", "Drag", "Jitter", "Custom");
    private final ModeSetting randomizeMode = new ModeSetting("Pattern", "Randomization pattern", "NEW",
            "NEW", "OLD", "EXTRA", "PATTERN1", "PATTERN2", "NONE");
    private final BooleanSetting onlySword = new BooleanSetting("Only Sword", "Only click with sword", false);
    private final ModeSetting autoblockMode = new ModeSetting("Autoblock", "Auto blocking mode", "Off",
            "Off", "Click", "Normal", "BlockHit", "Timed");
    private final BooleanSetting autoblockOnClick = new BooleanSetting("Block On Click", "Only block when clicking", true);
    private final BooleanSetting autoblockOnDanger = new BooleanSetting("Safe Block", "Only block when safe", false);
    private final BooleanSetting triggerBot = new BooleanSetting("TriggerBot", "Auto click on entities", false);
    private final ModeSetting displayMode = new ModeSetting("Display", "Info display mode", "Advanced",
            "Advanced", "Simple", "Off");

    private final TimeUtil leftClickTimer = new TimeUtil();
    private long leftClickDelay = 0L;

    public AutoClickerModule() {
        super("AutoClicker", "Automatically clicks for you", Category.COMBAT);
        setKeyBind(Keyboard.KEY_R);

        addSetting(presetMode);
        addSetting(targetCPS);
        addSetting(randomization);
        addSetting(randomizeMode);
        addSetting(onlySword);
        addSetting(autoblockMode);
        addSetting(autoblockOnClick);
        addSetting(autoblockOnDanger);
        addSetting(triggerBot);
        addSetting(displayMode);

        // Add preset change listener
        presetMode.setOnChange(this::applyPreset);
    }

    private void applyPreset(String preset) {
        switch (preset) {
            case "Legit":
                targetCPS.setValue(9.5);
                randomization.setValue(1.2);
                randomizeMode.setValue("NEW");
                onlySword.setValue(false);
                autoblockMode.setValue("Normal");
                autoblockOnClick.setValue(true);
                autoblockOnDanger.setValue(true);
                triggerBot.setValue(false);
                break;

            case "Butterfly":
                targetCPS.setValue(13.5);
                randomization.setValue(2.0);
                randomizeMode.setValue("PATTERN1");
                onlySword.setValue(true);
                autoblockMode.setValue("BlockHit");
                autoblockOnClick.setValue(true);
                autoblockOnDanger.setValue(false);
                triggerBot.setValue(false);
                break;

            case "Drag":
                targetCPS.setValue(16.0);
                randomization.setValue(1.0);
                randomizeMode.setValue("EXTRA");
                onlySword.setValue(true);
                autoblockMode.setValue("Click");
                autoblockOnClick.setValue(true);
                autoblockOnDanger.setValue(false);
                triggerBot.setValue(false);
                break;

            case "Jitter":
                targetCPS.setValue(12.0);
                randomization.setValue(2.5);
                randomizeMode.setValue("PATTERN2");
                onlySword.setValue(true);
                autoblockMode.setValue("Timed");
                autoblockOnClick.setValue(false);
                autoblockOnDanger.setValue(true);
                triggerBot.setValue(false);
                break;

            case "Custom":
                // Do nothing, keep current values
                break;
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;

        boolean shouldClick = GameSettings.isKeyDown(mc.gameSettings.keyBindAttack) ||
                (triggerBot.getValue() && mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null);

        if (shouldClick && (!onlySword.getValue() || isHoldingSword()) && !mc.playerController.getIsHittingBlock()) {
            if (leftClickTimer.hasReached(leftClickDelay)) {
                leftClickTimer.reset();
                leftClickDelay = updateDelay();
                KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

                if (autoblockMode.getValue().equals("Click") &&
                        (!autoblockOnClick.getValue() || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) &&
                        (mc.thePlayer.hurtTime < 2 || !autoblockOnDanger.getValue())) {
                    KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                }
            }

            handleAutoblock();
        }
    }

    private void handleAutoblock() {
        boolean shouldBlock = false;

        switch (autoblockMode.getValue().toLowerCase()) {
            case "off":
                break;
            case "click":
                shouldBlock = false;
                break;
            case "normal":
                shouldBlock = leftClickTimer.elapsed() > 0.1 * leftClickDelay &&
                        leftClickTimer.elapsed() < 0.65 * leftClickDelay;
                break;
            case "blockhit":
                shouldBlock = leftClickTimer.elapsed() < 0.4 * leftClickDelay;
                break;
            case "timed":
                shouldBlock = mc.thePlayer.hurtTime < 2;
                break;
        }

        shouldBlock = shouldBlock &&
                (mc.thePlayer.hurtTime < 2 || !autoblockOnDanger.getValue()) &&
                (!autoblockOnClick.getValue() || GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem));

        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), shouldBlock);
    }

    private long updateDelay() {
        switch (randomizeMode.getValue().toLowerCase()) {
            case "none":
                return (long) (1000 / targetCPS.getValue());
            case "old":
                return (long) (1000 / AttackUtil.getOldRandomization(targetCPS.getValue().intValue(), randomization.getValue().floatValue()));
            case "new":
                return (long) (1000 / AttackUtil.getNewRandomization(targetCPS.getValue().intValue(), randomization.getValue().floatValue()));
            case "extra":
                return (long) (1000 / AttackUtil.getExtraRandomization(targetCPS.getValue().intValue(), randomization.getValue().floatValue()));
            case "pattern1":
                return (long) (1000 / AttackUtil.getPattern1Randomization(targetCPS.getValue().intValue(), randomization.getValue().floatValue()));
            case "pattern2":
                return (long) (1000 / AttackUtil.getPattern2Randomization(targetCPS.getValue().intValue(), randomization.getValue().floatValue()));
            default:
                return 0L;
        }
    }

    private boolean isHoldingSword() {
        return mc.thePlayer.getCurrentEquippedItem() != null &&
                mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword;
    }

    public String getDisplayInfo() {
        switch (displayMode.getValue()) {
            case "Advanced":
                return presetMode.getValue() + " " + targetCPS.getValue() + ", " + randomization.getValue() + ", " + randomizeMode.getValue();
            case "Simple":
                return presetMode.getValue() + " " + String.valueOf(targetCPS.getValue().intValue());
            default:
                return null;
        }
    }
}