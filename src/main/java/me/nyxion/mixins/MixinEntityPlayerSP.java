package me.nyxion.mixins;

import me.nyxion.Nyxion;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        String prefix = Nyxion.getInstance().getCommandManager().getPrefix();
        if (message.startsWith(prefix)) {
            Nyxion.getInstance().getCommandManager().executeCommand(message);
            ci.cancel();
        }
    }
}