package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {
    /** Zeit ändern (nur clientseitig). */
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void bh$time(CallbackInfoReturnable<Long> cir) {
        if (Features.TIME.isEnabled()) cir.setReturnValue(Features.TIME.ticks());
    }
}
