package com.example.freecam.mixin;

import com.example.freecam.Freecam;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
	/**
	 * Mouse look calls Entity#turn on the local player. While freecam is active we
	 * redirect that rotation to the camera entity and leave the real player alone.
	 */
	@Inject(method = "turn", at = @At("HEAD"), cancellable = true)
	private void freecam$turn(double yRot, double xRot, CallbackInfo ci) {
		if (Freecam.handleTurn((Entity) (Object) this, yRot, xRot)) {
			ci.cancel();
		}
	}
}
