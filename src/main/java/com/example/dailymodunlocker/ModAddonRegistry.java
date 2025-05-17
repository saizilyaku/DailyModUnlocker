package com.example.dailymodunlocker;

import net.minecraftforge.fml.loading.FMLPaths;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.util.*;

public class ModAddonRegistry {
    private static final Map<String, List<String>> map = new HashMap<>();
    private static final Set<String> addons = new HashSet<>();

    public static void loadConfig() {
        var configPath = FMLPaths.CONFIGDIR.get().resolve("addon_relations.toml");
        try (CommentedFileConfig config = CommentedFileConfig.builder(configPath).build()) {
            config.load();

            for (String parent : config.valueMap().keySet()) {
                List<String> addonList = config.get(parent + ".addons");
                if (addonList != null) {
                    map.put(parent, addonList);
                    addons.addAll(addonList);
                }
            }

            System.out.println("[DailyModUnlocker] アドオン構成を読み込みました: " + map);

        } catch (Exception e) {
            System.err.println("[DailyModUnlocker] addon_relations.toml の読み込みに失敗しました: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static List<String> getAddons(String parent) {
        return map.getOrDefault(parent, List.of());
    }

    public static boolean isAddon(String modid) {
        return addons.contains(modid);
    }
}
