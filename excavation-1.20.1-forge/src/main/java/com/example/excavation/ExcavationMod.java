package com.example.excavation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.logging.LogUtils;

import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExcavationMod.MODID)
public class ExcavationMod {
    public static final String MODID = "excavation";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ExcavationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ExcavationParticleHandler.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        KeyBindings.register();
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!KeyBindings.oreMiningEnabled) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Set<BlockPos> visited = new HashSet<>();

        ItemStack tool = event.getPlayer().getMainHandItem();

        if (OreUtils.isOre(state)) {
            mineAdjacentBlocks(level, pos, state, visited, 0, Config.oreMiningRange, tool, event);
        }
    }

    private void mineAdjacentBlocks(ServerLevel level, BlockPos pos, BlockState state, Set<BlockPos> visited, int depth, int range, ItemStack tool, BlockEvent.BreakEvent event) {
        if (depth > range) return;

        visited.add(pos);

        // Check all 26 possible adjacent positions, including diagonals
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue; // Skip the current block itself
                    BlockPos adjacentPos = pos.offset(dx, dy, dz);
                    BlockState adjacentState = level.getBlockState(adjacentPos);

                    if (!visited.contains(adjacentPos) && adjacentState.getBlock() == state.getBlock()) {
                        breakBlockWithEnchantments(level, adjacentPos, adjacentState, tool, event);
                        mineAdjacentBlocks(level, adjacentPos, state, visited, depth + 1, range, tool, event);
                    }
                }
            }
        }
    }

    private void breakBlockWithEnchantments(ServerLevel level, BlockPos pos, BlockState state, ItemStack tool, BlockEvent.BreakEvent event) {
        // Check for Silk Touch enchantment
        boolean silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;

        // Check for Fortune enchantment
        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);

        List<ItemStack> drops;
        if (silkTouch) {
            drops = List.of(new ItemStack(state.getBlock().asItem()));
        } else {
            // Use the default loot table and apply Fortune manually if needed
            drops = Block.getDrops(state, level, pos, null, event.getPlayer(), tool);
            if (fortuneLevel > 0) {
                drops = applyFortune(drops, fortuneLevel);
            }
        }

        for (ItemStack drop : drops) {
            Block.popResource(level, pos, drop);
        }

        tool.hurtAndBreak(1, event.getPlayer(), (player) -> {
            player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
        });

        level.removeBlock(pos, false);
    }

    private List<ItemStack> applyFortune(List<ItemStack> drops, int fortuneLevel) {
        // Implement logic to apply Fortune enchantment effects to the drops
        // This is a simplified example. Adjust as needed for your specific use case.
        // Note: Minecraft's internal loot system handles Fortune effects, so this may vary based on actual implementation.
        return drops;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("setOreMiningRange")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("range", IntegerArgumentType.integer(1, 256))
                    .executes(context -> {
                        int range = IntegerArgumentType.getInteger(context, "range");
                        Config.oreMiningRange = range;
                        context.getSource().sendSuccess((Supplier<Component>) () -> Component.literal("Ore mining range set to " + range), true);
                        return 1;
                    }))
        );
    }
}
