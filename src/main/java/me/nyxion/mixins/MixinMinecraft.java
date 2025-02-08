package me.nyxion.mixins;

import me.nyxion.Nyxion;
import me.nyxion.gui.ClickGUI;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "startGame", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        System.out.println("[Nyxion] Initialized successfully!");
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
            Minecraft.getMinecraft().displayGuiScreen(new ClickGUI());
        }
    }
}