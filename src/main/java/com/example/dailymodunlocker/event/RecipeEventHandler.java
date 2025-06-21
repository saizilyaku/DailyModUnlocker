package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "dailymodunlocker")
public class RecipeEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        ModUnlockManager manager = ModUnlockManager.getInstance(server);

        for (ServerLevel level : server.getAllLevels()) {
            RecipeManager recipeManager = level.getRecipeManager();

            // Step 1: byKey (f_44006_), byType (f_44007_), recipes (f_44008_)
            Map<ResourceLocation, Recipe<?>> allRecipesByKey = getMap(recipeManager, "f_44006_"); // byKey
            if (allRecipesByKey == null) {
                LOGGER.error("[DailyModUnlocker] RecipeManager の byKey 取得失敗");
                continue;
            }

            // 解禁済みMODに基づいてレシピをフィルタ
            Map<ResourceLocation, Recipe<?>> allowedByKey = allRecipesByKey.entrySet().stream()
                    .filter(entry -> manager.isUnlocked(entry.getKey().getNamespace()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            // byType 再構築
            Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> allowedByType = new HashMap<>();
            for (Recipe<?> recipe : allowedByKey.values()) {
                allowedByType
                        .computeIfAbsent(recipe.getType(), k -> new HashMap<>())
                        .put(recipe.getId(), recipe);
            }

            // 書き換え対象フィールド
            try {
                Field recipesField = ObfuscationReflectionHelper.findField(RecipeManager.class, "f_44008_"); // recipes
                Field byKeyField = ObfuscationReflectionHelper.findField(RecipeManager.class, "f_44006_"); // byKey
                Field byTypeField = ObfuscationReflectionHelper.findField(RecipeManager.class, "f_44007_"); // byType

                recipesField.setAccessible(true);
                byKeyField.setAccessible(true);
                byTypeField.setAccessible(true);

                recipesField.set(recipeManager, allowedByKey);
                byKeyField.set(recipeManager, allowedByKey);
                byTypeField.set(recipeManager, allowedByType);

                LOGGER.info("[DailyModUnlocker] ワールド '{}': レシピを {} 件に制限しました。",
                        level.dimension().location(), allowedByKey.size());
            } catch (Exception e) {
                LOGGER.error("[DailyModUnlocker] レシピ更新中に例外発生: {}", e.getMessage(), e);
            }
        }

        // Step 2: 各プレイヤーの RecipeBook から未解禁MODレシピを除外
        // プレイヤーのレシピブックから未解禁MODのレシピを削除
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            RecipeManager recipeManager = player.serverLevel().getRecipeManager();

            Map<ResourceLocation, Recipe<?>> allRecipes = getMap(recipeManager, "f_44006_");
            if (allRecipes == null)
                continue;

            Set<ResourceLocation> toRemove = allRecipes.keySet().stream()
                    .filter(id -> !manager.isUnlocked(id.getNamespace()))
                    .collect(Collectors.toSet());

            for (ResourceLocation id : toRemove) {
                Recipe<?> recipe = allRecipes.get(id);
                if (recipe != null) {
                    player.getRecipeBook().remove(recipe);
                }
            }

            LOGGER.info("[DailyModUnlocker] プレイヤー '{}': {} 件のレシピをレシピブックから削除しました。",
                    player.getGameProfile().getName(), toRemove.size());
        }

    }

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, Recipe<?>> getMap(RecipeManager manager, String fieldName) {
        try {
            Field field = ObfuscationReflectionHelper.findField(RecipeManager.class, fieldName);
            field.setAccessible(true);
            return (Map<ResourceLocation, Recipe<?>>) field.get(manager);
        } catch (Exception e) {
            LOGGER.error("RecipeManager のフィールド '{}' 取得失敗: {}", fieldName, e.getMessage());
            return null;
        }
    }
}
