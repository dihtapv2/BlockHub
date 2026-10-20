package de.blockhub.client.mixin;

import de.blockhub.client.feature.Features;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float bh$yaw(float yaw) {
        return Features.FREELOOK.active ? Features.FREELOOK.yaw : yaw;
    }

    @ModifyVariable(method = "setRotation", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private float bh$pitch(float pitch) {
        return Features.FREELOOK.active ? Features.FREELOOK.pitch : pitch;
    }
}
