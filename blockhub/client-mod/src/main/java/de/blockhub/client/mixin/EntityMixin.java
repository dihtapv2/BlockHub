package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    /** Freelook: Mausbewegung dreht nur die Kamera, nicht den Spieler. */
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void bh$freelook(double dx, double dy, CallbackInfo ci) {
        if (Features.FREELOOK.active && (Object) this instanceof ClientPlayerEntity) {
            Features.FREELOOK.rotate(dx, dy);
            ci.cancel();
        }
    }
}
