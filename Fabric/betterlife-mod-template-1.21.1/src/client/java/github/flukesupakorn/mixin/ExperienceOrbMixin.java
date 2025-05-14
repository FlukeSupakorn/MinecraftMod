package github.flukesupakorn.mixin;

import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

@Mixin(ExperienceOrbEntity.class)
public class ExperienceOrbMixin {
    private boolean hasLanded = false;
    private static final double MERGE_RADIUS = 5.0; // 5 block radius

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ExperienceOrbEntity orbEntity = (ExperienceOrbEntity) (Object) this;
        World world = orbEntity.getWorld();
        
        if (!world.isClient) {
            // Check if orb has landed
            if (!hasLanded && orbEntity.isOnGround()) {
                hasLanded = true;
                int orbAmount = orbEntity.getExperienceAmount();
                if (orbAmount <= 0) return;

                // Create a box around the orb
                Vec3d pos = orbEntity.getPos();
                Box mergeBox = new Box(
                    pos.x - MERGE_RADIUS, pos.y - MERGE_RADIUS, pos.z - MERGE_RADIUS,
                    pos.x + MERGE_RADIUS, pos.y + MERGE_RADIUS, pos.z + MERGE_RADIUS
                );

                // Find nearby orbs
                List<ExperienceOrbEntity> nearbyOrbs = new ArrayList<>();
                world.getEntitiesByClass(ExperienceOrbEntity.class, mergeBox, entity -> {
                    if (entity == orbEntity) return false;
                    return entity.getExperienceAmount() > 0;
                }).forEach(nearbyOrbs::add);

                // Process merging
                for (ExperienceOrbEntity otherOrb : nearbyOrbs) {
                    if (otherOrb.isRemoved()) continue;
                    
                    int otherAmount = otherOrb.getExperienceAmount();
                    if (otherAmount <= 0) continue;

                    // Combine experience amounts
                    int totalAmount = orbAmount + otherAmount;

                    // Spawn new combined orb
                    ExperienceOrbEntity newOrb = new ExperienceOrbEntity(world, pos.x, pos.y, pos.z, totalAmount);
                    newOrb.setVelocity(0, 0.1, 0); // Small upward velocity
                    world.spawnEntity(newOrb);

                    // Remove both original orbs
                    orbEntity.discard();
                    otherOrb.discard();
                    return; // Exit after first merge
                }
            }
        }
    }
} 