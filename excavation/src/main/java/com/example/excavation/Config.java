package com.example.excavation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ExcavationMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue ORE_MINING_RANGE_SPEC = BUILDER
            .comment("The maximum range of ore mining")
            .defineInRange("oreMiningRange", 64, 1, 256);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static int oreMiningRange;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        updateValues();
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        updateValues();
    }

    private static void updateValues() {
        oreMiningRange = ORE_MINING_RANGE_SPEC.get();
    }
}
