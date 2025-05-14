package github.flukesupakorn.mixin;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.EntityType;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    private boolean hasLanded = false;
    private static final double MERGE_RADIUS = 5.0; // 3x3x3 area (1 block radius)

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        World world = itemEntity.getWorld();
        
        if (!world.isClient) {
            // Check if item has landed
            if (!hasLanded && itemEntity.isOnGround()) {
                hasLanded = true;
                ItemStack stack1 = itemEntity.getStack();
                if (stack1.isEmpty()) return;

                // Create a 3x3x3 box around the item
                Vec3d pos = itemEntity.getPos();
                Box mergeBox = new Box(
                    pos.x - MERGE_RADIUS, pos.y - MERGE_RADIUS, pos.z - MERGE_RADIUS,
                    pos.x + MERGE_RADIUS, pos.y + MERGE_RADIUS, pos.z + MERGE_RADIUS
                );

                // Find nearby items
                List<ItemEntity> nearbyItems = new ArrayList<>();
                world.getEntitiesByClass(ItemEntity.class, mergeBox, entity -> {
                    if (entity == itemEntity) return false;
                    ItemStack stack2 = entity.getStack();
                    return !stack2.isEmpty() && ItemStack.areItemsEqual(stack1, stack2);
                }).forEach(nearbyItems::add);

                // Process merging
                for (ItemEntity otherEntity : nearbyItems) {
                    if (otherEntity.isRemoved()) continue;
                    
                    ItemStack stack2 = otherEntity.getStack();
                    if (stack2.isEmpty()) continue;

                    // Check if items can be merged
                    if (ItemStack.areItemsEqual(stack1, stack2)) {
                        int totalCount = stack1.getCount() + stack2.getCount();
                        
                        // Create new item stack with combined count (unlimited)
                        ItemStack newStack = stack1.copy();
                        newStack.setCount(totalCount);

                        // Spawn new combined item
                        ItemEntity newEntity = new ItemEntity(world, pos.x, pos.y, pos.z, newStack);
                        newEntity.setVelocity(0, 0.1, 0); // Small upward velocity
                        world.spawnEntity(newEntity);

                        // Remove both original items
                        itemEntity.discard();
                        otherEntity.discard();
                        return; // Exit after first merge
                    }
                }
            }
        }
    }

    // Handle hopper interaction
    @Inject(method = "onPlayerCollision", at = @At("HEAD"), cancellable = true)
    private void onPlayerCollision(CallbackInfo ci) {
        ItemEntity itemEntity = (ItemEntity) (Object) this;
        ItemStack stack = itemEntity.getStack();
        
        // Let vanilla handle the pickup logic
        // The stack count will be automatically reduced as items are picked up
    }
}
