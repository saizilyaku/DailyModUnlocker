package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "dailymodunlocker")
public class RecipeEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ModUnlockManager manager = ModUnlockManager.getInstance(server);

        for (ServerLevel level : server.getAllLevels()) {
            RecipeManager recipeManager = level.getRecipeManager();

            // 全レシピを取得
            Collection<Recipe<?>> allRecipes = recipeManager.getRecipes();

            // 解禁済みのレシピのみ抽出
            List<Recipe<?>> allowedRecipes = allRecipes.stream()
                    .filter(recipe -> manager.isUnlocked(recipe.getId().getNamespace()))
                    .collect(Collectors.toList());

            // byKey / byType の書き換え
            try {
                Field byKeyField = RecipeManager.class.getDeclaredField("byKey");
                Field byTypeField = RecipeManager.class.getDeclaredField("byType");
                byKeyField.setAccessible(true);
                byTypeField.setAccessible(true);

                Map<ResourceLocation, Recipe<?>> byKey = allowedRecipes.stream()
                        .collect(Collectors.toMap(Recipe::getId, Function.identity()));

                Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> byType = new HashMap<>();
                for (Recipe<?> recipe : allowedRecipes) {
                    byType
                            .computeIfAbsent(recipe.getType(), k -> new HashMap<>())
                            .put(recipe.getId(), recipe);
                }

                byKeyField.set(recipeManager, byKey);
                byTypeField.set(recipeManager, byType);

                System.out.println("[DailyModUnlocker] レシピをフィルタリングしました（解禁済みMODのみ）。");

            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("[DailyModUnlocker] レシピ更新中にエラー: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
