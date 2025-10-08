package com.example.dailymodunlocker.event;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = "dailymodunlocker")
public class RecipeEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static Collection<Recipe<?>> allRecipes = List.of(); // フルレシピのキャッシュ

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        RecipeManager recipeManager = server.overworld().getRecipeManager();

        // 全レシピをキャッシュしておく
        allRecipes = List.copyOf(recipeManager.getRecipes());
        LOGGER.info("[DailyModUnlocker] 全レシピ {} 件をキャッシュしました。", allRecipes.size());

        // 初期制限を適用
        applyRecipeRestrictions(server);
    }

    /** MODの解禁状況を反映してレシピ制御を適用 */
    public static void applyRecipeRestrictions(MinecraftServer server) {
        ModUnlockManager manager = ModUnlockManager.getInstance(server);

        // 解禁済みレシピのみ抽出
        Collection<Recipe<?>> allowed = allRecipes.stream()
                .filter(r -> manager.isUnlocked(r.getId().getNamespace()))
                .collect(Collectors.toList());

        // サーバー全体に適用
        server.overworld().getRecipeManager().replaceRecipes(allowed);
        LOGGER.info("[DailyModUnlocker] レシピを {} 件に制限しました。", allowed.size());

        // プレイヤーごとに同期
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncPlayerRecipes(player, allowed);
        }
    }

    /** プレイヤーのレシピブックを同期 */
    private static void syncPlayerRecipes(ServerPlayer player, Collection<Recipe<?>> allowed) {
        RecipeManager manager = player.serverLevel().getRecipeManager();

        // 全レシピID
        Set<ResourceLocation> allIds = allRecipes.stream()
                .map(Recipe::getId).collect(Collectors.toSet());
        // 有効レシピID
        Set<ResourceLocation> allowedIds = allowed.stream()
                .map(Recipe::getId).collect(Collectors.toSet());

        // 削除対象
        Set<ResourceLocation> toRemove = new HashSet<>(allIds);
        toRemove.removeAll(allowedIds);

        // 追加対象
        Set<ResourceLocation> toAdd = new HashSet<>(allowedIds);
        toAdd.removeIf(id -> player.getRecipeBook().contains(id));

        // 適用
        toRemove.forEach(id -> manager.byKey(id).ifPresent(player.getRecipeBook()::remove));
        toAdd.forEach(id -> manager.byKey(id).ifPresent(r -> player.awardRecipes(List.of(r))));

        LOGGER.info("[DailyModUnlocker] プレイヤー '{}': {} 件追加, {} 件削除。",
                player.getGameProfile().getName(), toAdd.size(), toRemove.size());
    }
}
