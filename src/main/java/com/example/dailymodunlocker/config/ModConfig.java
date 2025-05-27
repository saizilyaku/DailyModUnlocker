package com.example.dailymodunlocker.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();

        generateDefaultConfigIfMissing();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue unlockAddonsWithParent;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> addonRelations;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> alwaysUnlockedMods;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            unlockAddonsWithParent = builder
                    .comment("親MOD解放時にアドオンも同時に解放するか")
                    .define("unlockAddonsWithParent", true);

            addonRelations = builder
                    .comment("親MOD=アドオン の形式で依存関係を指定します")
                    .defineListAllowEmpty("addonRelations", Collections.emptyList(), o -> o instanceof String);

            alwaysUnlockedMods = builder
                    .comment("常に解放されるMODのIDリスト")
                    .defineListAllowEmpty(
                            "alwaysUnlockedMods",
                            Arrays.asList("minecraft", "forge", "jei", "dailymodunlocker", "optifine",
                                    "oculus", "rubidium", "sodium", "cloth_config", "architectury", "modmenu"),
                            o -> o instanceof String);

            builder.pop();
        }
    }

    private static void generateDefaultConfigIfMissing() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get().resolve("dailymodunlocker-common.toml");
            if (Files.exists(configPath))
                return;

            Files.createDirectories(configPath.getParent());

            Map<String, List<String>> relations = detectAddonRelations();

            List<String> relationEntries = relations.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                    .collect(Collectors.toList());

            List<String> alwaysUnlocked = Arrays.asList("minecraft", "forge", "jei", "dailymodunlocker", "optifine",
                    "oculus", "rubidium", "sodium", "cloth_config", "architectury", "modmenu");

            CommentedFileConfig config = CommentedFileConfig.builder(configPath)
                    .autosave()
                    .build();

            config.load();

            config.set("general.unlockAddonsWithParent", true);
            config.set("general.addonRelations", relationEntries);
            config.set("general.alwaysUnlockedMods", alwaysUnlocked);

            config.save();
            config.close();
        } catch (Exception e) {
            System.err.println("設定ファイルの初期生成に失敗しました: " + e.getMessage());
        }
    }

    private static Map<String, List<String>> detectAddonRelations() {
        Map<String, List<String>> result = new HashMap<>();
        var modList = ModList.get().getMods();

        for (var mod : modList) {
            String parent = mod.getModId();
            List<String> deps = mod.getDependencies().stream()
                    .map(dep -> dep.getModId())
                    .filter(dep -> !dep.equals("minecraft") && !dep.equals(parent))
                    .collect(Collectors.toList());

            if (!deps.isEmpty()) {
                result.put(parent, deps);
            }
        }

        return result;
    }
}
