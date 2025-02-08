package me.nyxion.mixins;

import me.nyxion.Nyxion;
import me.nyxion.events.impl.ChatEvent;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {
    @Inject(method = "func_146234_a", at = @At("HEAD"), cancellable = true)
    private void onChatMessage(IChatComponent message, int chatLineId, CallbackInfo ci) {
        if (message == null) return;
        
        String text = message.getUnformattedText();
        ChatEvent event = new ChatEvent(text);
        Nyxion.getInstance().getEventManager().call(event);
        
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}