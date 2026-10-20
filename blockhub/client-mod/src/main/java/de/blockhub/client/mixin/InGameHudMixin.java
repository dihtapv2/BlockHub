package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    /** Vanilla-Fadenkreuz ausblenden, wenn das eigene aktiv ist. */
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void bh$crosshair(CallbackInfo ci) {
        if (Features.CROSSHAIR.active()) ci.cancel();
    }
}
