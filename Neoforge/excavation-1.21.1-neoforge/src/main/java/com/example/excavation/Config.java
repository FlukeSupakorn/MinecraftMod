package com.example.excavation;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue ORE_MINING_RANGE_SPEC = BUILDER
            .comment("The maximum range of ore mining")
            .defineInRange("oreMiningRange", 64, 1, 256);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int oreMiningRange;

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            updateValues();
        }
    }

    private static void updateValues() {
        oreMiningRange = ORE_MINING_RANGE_SPEC.get();
    }
}
