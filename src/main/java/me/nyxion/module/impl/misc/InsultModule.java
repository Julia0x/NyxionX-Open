package me.nyxion.module.impl.misc;

import me.nyxion.module.Category;
import me.nyxion.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.Random;

public class InsultModule extends Module {
    private final Random random = new Random();
    private long lastInsultTime = 0;
    private static final long INSULT_COOLDOWN = 2000; // 2 seconds cooldown

    private final String[] INSULTS = {
            "L bozo",
            "Get good",
            "Too ez",
            "Skill issue"
    };

    public InsultModule() {
        super("Insult", "Automatically sends insults when killing players", Category.MISC);
        setKeyBind(Keyboard.KEY_NONE);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (!isEnabled() || !(event.target instanceof EntityPlayer)) return;

        EntityPlayer target = (EntityPlayer) event.target;

        if (target.getHealth() <= 0 && System.currentTimeMillis() - lastInsultTime > INSULT_COOLDOWN) {
            String insult = INSULTS[random.nextInt(INSULTS.length)];
            mc.thePlayer.sendChatMessage(insult);
            lastInsultTime = System.currentTimeMillis();
        }
    }
}