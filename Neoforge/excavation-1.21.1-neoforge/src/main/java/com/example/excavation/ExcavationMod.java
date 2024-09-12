package com.example.excavation;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(ExcavationMod.MODID)
public class ExcavationMod {

    public static final String MODID = "excavation";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExcavationMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register common setup
        modEventBus.addListener(this::commonSetup);

        // Register client-side setup
        modEventBus.addListener(this::onClientSetup);

        // Register KeyBindings
        KeyBindings.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup for Excavation Mod");
    }

    // Correct method to handle client setup
    private void onClientSetup(FMLClientSetupEvent event) {
        // Registering tick handler manually using NeoForge's event system
        Minecraft.getInstance().execute(() -> {
            // Register the client tick event manually
            // Add logic here to register client tick handlers
        });
    }

    // Manually handle client ticks (you may need to check NeoForge's event system for the correct way)
    private void onClientTick() {
        // Handle key binding toggle, particle spawning, and HUD rendering on each client tick
        KeyBindings.onClientTick();
        ExcavationHudRenderer.renderHud();
        ExcavationParticleHandler.onClientTick();
    }
}
