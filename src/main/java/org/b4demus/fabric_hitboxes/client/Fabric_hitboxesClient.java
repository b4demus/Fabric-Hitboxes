package org.b4demus.fabric_hitboxes.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Fabric_hitboxesClient implements ClientModInitializer {

    private static KeyBinding increaseHitboxKey;
    private static KeyBinding decreaseHitboxKey;


    private static final Map<UUID, Float> playerHitboxWidthScale = new HashMap<>();

    @Override
    public void onInitializeClient() {
        increaseHitboxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fabric_hitboxes.increase",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.fabric_hitboxes"
        ));

        decreaseHitboxKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.fabric_hitboxes.decrease",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "category.fabric_hitboxes"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            UUID selfUUID = client.player.getUuid();
            float currentScale = playerHitboxWidthScale.getOrDefault(selfUUID, 1.0f);
            boolean changed = false;

            if (increaseHitboxKey.wasPressed()) {
                currentScale += 0.05f;
                changed = true;
            } else if (decreaseHitboxKey.wasPressed()) {
                currentScale = Math.max(0.1f, currentScale - 0.05f);
                changed = true;
            }

            if (changed) {
                playerHitboxWidthScale.put(selfUUID, currentScale);
            }

            for (PlayerEntity player : client.world.getPlayers()) {
                if (player == null) continue;
                float scale = playerHitboxWidthScale.getOrDefault(player.getUuid(), 1.0f);

                if (!player.getUuid().equals(selfUUID)) {
                    playerHitboxWidthScale.put(player.getUuid(), currentScale);
                    applyHitboxWidth(player, currentScale);
                }
            }
        });
    }

    private void applyHitboxWidth(PlayerEntity player, float widthScale) {
        float baseWidth = player.getWidth();
        float height = player.getHeight();
        float newWidth = baseWidth * widthScale;

        Box targetBox = new Box(
                player.getX() - newWidth / 2,
                player.getY(),
                player.getZ() - newWidth / 2,
                player.getX() + newWidth / 2,
                player.getY() + height,
                player.getZ() + newWidth / 2
        );

        player.setBoundingBox(targetBox);
    }
}
