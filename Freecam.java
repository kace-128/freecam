package com.example.freecam;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class Freecam {
	private static final double BASE_SPEED = 0.6;   // blocks per tick
	private static final double SPRINT_MULT = 3.0;

	private static boolean active;
	private static RemotePlayer camera;
	private static ClientInput savedInput;

	private Freecam() {}

	public static boolean isActive() {
		return active;
	}

	public static void toggle() {
		if (active) {
			disable(true);
		} else {
			enable();
		}
	}

	private static void enable() {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		ClientLevel level = mc.level;
		if (player == null || level == null) return;

		RemotePlayer cam = new RemotePlayer(level, player.getGameProfile());
		cam.copyPosition(player);
		syncOld(cam);

		// Swap the player's input for an empty one so the real player stands still.
		savedInput = player.input;
		player.input = new ClientInput() {};

		camera = cam;
		active = true;
		mc.setCameraEntity(cam);
		player.displayClientMessage(Component.literal("Freecam: ON"), true);
	}

	public static void disable(boolean announce) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if (player != null) {
			if (savedInput != null) player.input = savedInput;
			mc.setCameraEntity(player);
			if (announce) player.displayClientMessage(Component.literal("Freecam: OFF"), true);
		}

		savedInput = null;
		camera = null;
		active = false;
	}

	/** Called every client tick. */
	public static void tick(Minecraft mc) {
		if (!active) return;

		LocalPlayer player = mc.player;
		if (player == null || mc.level == null || camera == null
				|| camera.level() != mc.level || !player.isAlive()) {
			disable(false);
			return;
		}

		syncOld(camera);

		var o = mc.options;
		double forward = (o.keyUp.isDown() ? 1 : 0) - (o.keyDown.isDown() ? 1 : 0);
		double strafe = (o.keyLeft.isDown() ? 1 : 0) - (o.keyRight.isDown() ? 1 : 0);
		double vertical = (o.keyJump.isDown() ? 1 : 0) - (o.keyShift.isDown() ? 1 : 0);

		float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
		double sin = Mth.sin(yaw);
		double cos = Mth.cos(yaw);

		// Facing yaw 0 = +Z. Forward = (-sin, cos); left = (cos, sin).
		double dx = -sin * forward + cos * strafe;
		double dz = cos * forward + sin * strafe;

		double speed = BASE_SPEED * (o.keySprint.isDown() ? SPRINT_MULT : 1.0);
		Vec3 move = new Vec3(dx, vertical, dz);
		if (move.lengthSqr() > 1.0E-7) {
			move = move.normalize().scale(speed);
			camera.setPos(camera.getX() + move.x, camera.getY() + move.y, camera.getZ() + move.z);
		}
	}

	/** Returns true if the rotation was consumed by the freecam camera. */
	public static boolean handleTurn(Entity entity, double yRot, double xRot) {
		if (!active || camera == null) return false;
		Minecraft mc = Minecraft.getInstance();
		if (entity != mc.player) return false;

		float dy = (float) yRot * 0.15F;
		float dx = (float) xRot * 0.15F;
		float oldPitch = camera.getXRot();
		float oldYaw = camera.getYRot();

		camera.setYRot(oldYaw + dy);
		camera.setXRot(Mth.clamp(oldPitch + dx, -90.0F, 90.0F));
		camera.yRotO += camera.getYRot() - oldYaw;
		camera.xRotO += camera.getXRot() - oldPitch;
		return true;
	}

	private static void syncOld(Entity e) {
		e.xo = e.getX();
		e.yo = e.getY();
		e.zo = e.getZ();
		e.xRotO = e.getXRot();
		e.yRotO = e.getYRot();
	}
}
