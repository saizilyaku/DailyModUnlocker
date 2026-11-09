package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

@EventBusSubscriber(modid = "dailymodunlocker")
public class SpawnEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (entityId == null)
            return;

        String modid = entityId.getNamespace();

        if (!ModUnlockManager.getInstance(serverLevel.getServer()).isUnlocked(modid)) {
            event.setCanceled(true);
        }
    }
}
