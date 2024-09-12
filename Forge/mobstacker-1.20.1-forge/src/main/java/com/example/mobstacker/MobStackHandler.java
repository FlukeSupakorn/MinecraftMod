package com.example.mobstacker;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod.EventBusSubscriber(modid = MobStackerMod.MODID)
public class MobStackHandler {

    private static final int STACKING_RADIUS = 5; // Radius within which mobs should stack
    private static final Pattern STACK_COUNT_PATTERN = Pattern.compile(" x(\\d+)$");
    private static final long STACK_DELAY = 2000; // Delay in milliseconds

    private static final Map<Mob, Long> lastStackAttempt = new HashMap<>();

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Mob)) {
            return;
        }

        Mob newMobEntity = (Mob) entity;
        if (!newMobEntity.isAlive() || !canStackEntity(newMobEntity)) {
            return;
        }

        stackMobsInRadius(newMobEntity);
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) entity;
        int count = getMobStackCount(mob);

        if (count > 1) {
            setMobStackCount(mob, count - 1);
            mob.setHealth(mob.getMaxHealth()); // Reset health for the remaining mob
            event.setCanceled(true); // Prevent the mob from actually dying
        }
    }

    @SubscribeEvent
    public static void onMobFed(LivingEntityUseItemEvent.Finish event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof Mob)) {
            return;
        }

        Mob mob = (Mob) entity;
        int count = getMobStackCount(mob);

        if (count > 1) {
            setMobStackCount(mob, count - 1);

            // Create a new mob entity to represent the split-off mob
            Entity newEntity = mob.getType().create(mob.level());
            if (newEntity instanceof Mob) {
                Mob newMob = (Mob) newEntity;
                newMob.moveTo(mob.getX(), mob.getY(), mob.getZ(), mob.getYRot(), mob.getXRot());
                mob.level().addFreshEntity(newMob);
                setMobStackCount(newMob, 1);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        for (Level world : event.getServer().getAllLevels()) {
            world.getEntitiesOfClass(Mob.class, new AABB(
                Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY
            )).forEach(mob -> {
                if (canStackEntity(mob)) {
                    long currentTime = System.currentTimeMillis();
                    if (!lastStackAttempt.containsKey(mob) || (currentTime - lastStackAttempt.get(mob)) >= STACK_DELAY) {
                        lastStackAttempt.put(mob, currentTime);
                        stackMobsInRadius(mob);
                    }
                }
            });
        }
    }

    private static void stackMobsInRadius(Mob mob) {
        List<Mob> nearbyMobs = mob.level().getEntitiesOfClass(Mob.class, new AABB(
            mob.getX() - STACKING_RADIUS, mob.getY() - STACKING_RADIUS, mob.getZ() - STACKING_RADIUS,
            mob.getX() + STACKING_RADIUS, mob.getY() + STACKING_RADIUS, mob.getZ() + STACKING_RADIUS
        ));

        for (Mob nearbyMob : nearbyMobs) {
            if (nearbyMob == mob || !nearbyMob.isAlive() || !canStackEntity(nearbyMob) || isBaby(nearbyMob) || isBaby(mob)) {
                continue;
            }

            if (canStack(mob, nearbyMob)) {
                combineMobs(mob, nearbyMob);
                spawnTntParticles(mob.level(), mob.blockPosition().getX(), mob.blockPosition().getY(), mob.blockPosition().getZ());
                nearbyMob.discard();
                return;
            }
        }
    }

    private static boolean canStack(Mob mob1, Mob mob2) {
        return mob1.getType().equals(mob2.getType());
    }

    private static boolean canStackEntity(Mob mob) {
        return mob.getType() == EntityType.COW || mob.getType() == EntityType.PIG || mob.getType() == EntityType.SHEEP;
    }

    private static boolean isBaby(Mob mob) {
        return (mob instanceof Animal) && ((Animal) mob).isBaby();
    }

    private static void combineMobs(Mob targetMob, Mob sourceMob) {
        int combinedCount = getMobStackCount(targetMob) + getMobStackCount(sourceMob);

        // Update target mob to reflect combined count
        setMobStackCount(targetMob, combinedCount);

        // Optionally update mob's health or other attributes if needed
        targetMob.setHealth(targetMob.getMaxHealth());

        updateMobEntityName(targetMob);
    }

    private static int getMobStackCount(Mob mob) {
        if (mob.getCustomName() != null) {
            Matcher matcher = STACK_COUNT_PATTERN.matcher(mob.getCustomName().getString());
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 1;
    }

    private static void setMobStackCount(Mob mob, int count) {
        String mobName = mob.getType().getDescription().getString();
        if (count > 1) {
            String countString = " x" + count;
            MutableComponent customName = Component.literal(mobName).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFFFF))) // White for mob name
                    .append(Component.literal(countString).withStyle(style -> style.withColor(TextColor.fromRgb(0xFFFF00)))); // Yellow for count
            mob.setCustomName(customName);
            mob.setCustomNameVisible(true);
        } else {
            mob.setCustomName(null); // Remove custom name for single mob
            mob.setCustomNameVisible(false);
        }
    }

    private static void updateMobEntityName(Mob mob) {
        int count = getMobStackCount(mob);
        setMobStackCount(mob, count);
    }

    private static void spawnTntParticles(Level level, double x, double y, double z) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, x, y, z, 10, 0.5, 0.5, 0.5, 0.0);
        }
    }
}
