package com.example.freecam;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import org.lwjgl.glfw.GLFW;

public class FreecamClient implements ClientModInitializer {
	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("freecam", "main"));

		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.freecam.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F6, category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (toggleKey.consumeClick()) {
				Freecam.toggle();
			}
			Freecam.tick(mc);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, mc) -> {
			if (Freecam.isActive()) Freecam.disable(false);
		});

		// Block world interaction while detached; hit results would be computed
		// from the camera position, not the player's.
		AttackBlockCallback.EVENT.register((player, level, hand, pos, dir) ->
				Freecam.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
		AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
				Freecam.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
		UseBlockCallback.EVENT.register((player, level, hand, hit) ->
				Freecam.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
		UseEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
				Freecam.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
		UseItemCallback.EVENT.register((player, level, hand) ->
				Freecam.isActive() ? InteractionResult.FAIL : InteractionResult.PASS);
	}
}
