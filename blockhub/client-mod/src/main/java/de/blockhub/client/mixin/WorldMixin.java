package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {
    /** Wetter ändern (nur clientseitig). */
    @Inject(method = "getRainGradient", at = @At("HEAD"), cancellable = true)
    private void bh$rain(float delta, CallbackInfoReturnable<Float> cir) {
        if (Features.WEATHER.isEnabled() && (Object) this instanceof ClientWorld) cir.setReturnValue(Features.WEATHER.rain());
    }

    @Inject(method = "getThunderGradient", at = @At("HEAD"), cancellable = true)
    private void bh$thunder(float delta, CallbackInfoReturnable<Float> cir) {
        if (Features.WEATHER.isEnabled() && (Object) this instanceof ClientWorld) cir.setReturnValue(Features.WEATHER.thunder());
    }
}
