package github.flukesupakorn.itemstacking;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class ItemStackingHandler {
    private static final int MERGE_DELAY_TICKS = 20; // 1 second
    private static final double MERGE_RADIUS = 1.0; // 1 block radius

    public static void tick(ItemEntity itemEntity) {
        if (itemEntity.age < MERGE_DELAY_TICKS) {
            return;
        }

        World world = itemEntity.getWorld();
        if (world.isClient) {
            return;
        }

        Box searchBox = itemEntity.getBoundingBox().expand(MERGE_RADIUS);
        List<ItemEntity> nearbyItems = world.getEntitiesByClass(ItemEntity.class, searchBox, 
            entity -> entity != itemEntity && 
                     entity.isAlive() && 
                     entity.getStack().getItem() == itemEntity.getStack().getItem());

        for (ItemEntity nearbyItem : nearbyItems) {
            if (nearbyItem.age < MERGE_DELAY_TICKS) {
                continue;
            }

            ItemStack stack1 = itemEntity.getStack();
            ItemStack stack2 = nearbyItem.getStack();

            if (stack1.getItem() == stack2.getItem()) {
                int totalCount = stack1.getCount() + stack2.getCount();
                stack1.setCount(totalCount);
                nearbyItem.discard();
                break;
            }
        }
    }
} 