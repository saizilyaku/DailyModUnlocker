package com.example.dailymodunlocker;

import com.example.dailymodunlocker.config.ModConfig;

import java.util.*;

public class ModAddonRegistry {
    private static final Map<String, List<String>> map = new HashMap<>();
    private static final Set<String> addons = new HashSet<>();

    public static void loadConfig() {
        map.clear();
        addons.clear();

        for (String entry : ModConfig.COMMON.addonRelations.get()) {
            String[] parts = entry.split("=");
            if (parts.length != 2)
                continue;

            String parent = parts[0].trim();
            List<String> addonList = Arrays.asList(parts[1].split(","));
            map.put(parent, addonList);
            addons.addAll(addonList);
        }

        System.out.println("[DailyModUnlocker] アドオン構成をコンフィグから読み込みました: " + map);
    }

    public static List<String> getAddons(String parent) {
        return map.getOrDefault(parent, List.of());
    }

    public static boolean isAddon(String modid) {
        return addons.contains(modid);
    }

    public static Map<String, List<String>> getParentToAddonsMap() {
        return Collections.unmodifiableMap(map);
    }
}
