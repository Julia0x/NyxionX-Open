package me.nyxion.mixins;

import me.nyxion.gui.MainMenu;
import net.minecraft.client.gui.GuiMainMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static me.nyxion.utils.client.IMinecraft.mc;

@Mixin(GuiMainMenu.class)
public class MixinGuiMainMenu {
    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    private void onInitGui(CallbackInfo ci) {
        mc.displayGuiScreen(new MainMenu());
        ci.cancel();
    }
}