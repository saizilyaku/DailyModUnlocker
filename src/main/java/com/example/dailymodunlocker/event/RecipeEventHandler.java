package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

import java.util.Iterator;

@EventBusSubscriber(modid = "dailymodunlocker")
public class RecipeEventHandler {

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesUpdatedEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null) return;

        ModUnlockManager manager = ModUnlockManager.getInstance(server);
        RecipeManager recipeManager = event.getRecipeManager();

        // コレクションからレシピを除去（直接の除去は不可のため Iterator 使用）
        Iterator<Recipe<?>> iterator = recipeManager.getRecipes().iterator();
        while (iterator.hasNext()) {
            Recipe<?> recipe = iterator.next();
            String modid = recipe.getId().getNamespace();

            if (!manager.isUnlocked(modid)) {
                iterator.remove();
            }
        }
    }
}
