package com.example.excavation;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyBindings {

    public static final Logger LOGGER = LoggerFactory.getLogger("ExcavationMod");

    public static final KeyMapping TOGGLE_ORE_MINING = new KeyMapping(
            "key.excavation.toggle_ore_mining",
            GLFW.GLFW_KEY_M,  // Default key binding
            "key.categories.excavation"  // Key category
    );

    public static boolean oreMiningEnabled = true;  // Initially set to false

    public static void register() {
        // Register the key binding
        Minecraft.getInstance().options.keyMappings =
                addKeyMapping(Minecraft.getInstance().options.keyMappings, TOGGLE_ORE_MINING);

        LOGGER.info("Key binding for Ore Mining registered successfully.");
    }

    private static KeyMapping[] addKeyMapping(KeyMapping[] original, KeyMapping keyMapping) {
        KeyMapping[] newMappings = new KeyMapping[original.length + 1];
        System.arraycopy(original, 0, newMappings, 0, original.length);
        newMappings[original.length] = keyMapping;
        return newMappings;
    }

    public static void onClientTick() {
        if (TOGGLE_ORE_MINING.consumeClick()) {
            oreMiningEnabled = !oreMiningEnabled;

            // Log to console when key is pressed
            LOGGER.info("M key pressed: Ore Mining is now " + (oreMiningEnabled ? "Enabled" : "Disabled"));

            // Send chat message to player when key is pressed
            Minecraft.getInstance().player.sendSystemMessage(
                    Component.literal("Ore Mining: " + (oreMiningEnabled ? "Enabled" : "Disabled"))
            );

            // Call the HUD to update the toggle time
            ExcavationHudRenderer.updateLastToggleTime();
        }
    }
}
