package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;

@EventBusSubscriber(modid = "dailymodunlocker")
public class DropEventHandler {

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Mob mob) {
            ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
            if (entityId == null) return;

            String modid = entityId.getNamespace();

            MinecraftServer server = entity.getServer();
            if (server == null) return;

            ModUnlockManager manager = ModUnlockManager.getInstance(server);
            if (!manager.isUnlocked(modid)) {
                Iterator<ItemEntity> it = event.getDrops().iterator();
                while (it.hasNext()) {
                    ItemEntity itemEntity = it.next();
                    ItemStack stack = itemEntity.getItem();

                    ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemId != null && itemId.getNamespace().equals(modid)) {
                        it.remove();
                    }
                }
            }
        }
    }
}
