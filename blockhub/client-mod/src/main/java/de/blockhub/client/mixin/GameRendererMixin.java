package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    /** Kein Hurtcam. */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void bh$noHurtCam(CallbackInfo ci) {
        if (Features.NO_HURT_CAM.isEnabled()) ci.cancel();
    }

    /** Kein View-Bobbing. */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void bh$noBob(CallbackInfo ci) {
        if (Features.NO_BOB.isEnabled()) ci.cancel();
    }

    /** Zoom. */
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void bh$zoom(CallbackInfoReturnable<Double> cir) {
        if (Features.ZOOM.isEnabled()) cir.setReturnValue(cir.getReturnValue() * Features.ZOOM.multiplier());
    }
}
