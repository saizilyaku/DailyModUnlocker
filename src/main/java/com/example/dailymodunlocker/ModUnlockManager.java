package com.example.dailymodunlocker;

import net.minecraftforge.fml.ModList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ModUnlockManager extends SavedData {

    private static final String DATA_NAME = "dailymodunlocker_unlocks";

    private static Set<String> alwaysUnlockedMods = new HashSet<>();

    static {
        loadAlwaysUnlockedMods();
    }

    public static Set<String> loadAlwaysUnlockedMods() {
        Path configDir = Path.of("config", "dailymodunlocker");
        Path filePath = configDir.resolve("always_unlocked_mods.txt");

        try {
            if (!Files.exists(configDir))
                Files.createDirectories(configDir);

            if (!Files.exists(filePath)) {
                List<String> defaults = List.of(
                        "minecraft", "forge",
                        "optifine", "oculus", "rubidium",
                        "sodium", "cloth_config", "architectury", "modmenu");
                Files.write(filePath, defaults);
            }

            alwaysUnlockedMods = Files.readAllLines(filePath).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toSet());

            System.out.println("[DailyModUnlocker] 常時解禁MOD: " + alwaysUnlockedMods);

        } catch (IOException e) {
            System.err.println("[DailyModUnlocker] 例外MOD読み込み失敗: " + e.getMessage());
        }
        return alwaysUnlockedMods;
    }

    private final Set<String> unlockedMods = new HashSet<>();
    private LocalDate lastUnlockDate = null;

    private static ModUnlockManager instance;

    public ModUnlockManager() {
        super();
    }

    public static ModUnlockManager getInstance(MinecraftServer server) {
        if (instance == null) {
            instance = server.overworld().getDataStorage().computeIfAbsent(ModUnlockManager::load,
                    ModUnlockManager::new, DATA_NAME);
        }
        return instance;
    }

    private static ModUnlockManager load(CompoundTag tag) {
        ModUnlockManager manager = new ModUnlockManager();
        ListTag list = tag.getList("UnlockedMods", 8); // 8 = StringTag
        for (int i = 0; i < list.size(); i++) {
            manager.unlockedMods.add(list.getString(i));
        }

        if (tag.contains("LastUnlockDate")) {
            String dateStr = tag.getString("LastUnlockDate");
            manager.lastUnlockDate = LocalDate.parse(dateStr);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (String modid : unlockedMods) {
            list.add(StringTag.valueOf(modid));
        }
        tag.put("UnlockedMods", list);
        if (lastUnlockDate != null) {
            tag.putString("LastUnlockDate", lastUnlockDate.toString()); // 例: "2025-05-15"
        }
        return tag;
    }

    public boolean isUnlocked(String modid) {
        return alwaysUnlockedMods.contains(modid) || unlockedMods.contains(modid);
    }

    public void unlockMod(String modid) {
        if (unlockedMods.add(modid)) {
            setDirty();
        }
    }

    public void unlockModWithAddons(String modid) {
        if (unlockedMods.add(modid)) {
            setDirty();
        }

        List<? extends net.minecraftforge.forgespi.language.IModInfo> allMods = ModList.get().getMods();

        List<String> addonMods = allMods.stream()
                .filter(info -> info.getDependencies().stream().anyMatch(dep -> dep.getModId().equals(modid)))
                .map(info -> info.getModId())
                .filter(id -> !isUnlocked(id))
                .collect(Collectors.toList());

        for (String addon : addonMods) {
            unlockedMods.add(addon);
        }

        if (!addonMods.isEmpty()) {
            System.out.println("[DailyModUnlocker] " + modid + " に関連するアドオンも解禁: " + addonMods);
            setDirty();
        }
    }

    public Set<String> getAllMods() {
        return ModList.get().getMods().stream()
                .map(info -> info.getModId())
                .collect(Collectors.toSet());
    }

    public Set<String> getUnlockedMods() {
        return Collections.unmodifiableSet(unlockedMods);
    }

    public void tryUnlockNewMod(MinecraftServer server) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (lastUnlockDate != null && !lastUnlockDate.isBefore(today)) {
            return;
        }

        List<String> lockedMods = getAllMods().stream()
                .filter(id -> !isUnlocked(id))
                .collect(Collectors.toList());

        if (lockedMods.isEmpty()) {
            return;
        }

        Collections.shuffle(lockedMods);
        String toUnlock = lockedMods.get(0);

        unlockModWithAddons(toUnlock);
        lastUnlockDate = today;
        setDirty();

        server.sendSystemMessage(Component.literal(
                "[DailyModUnlocker] MOD '" + toUnlock + "' および関連アドオンが日替わりで解禁されました。"));
    }

    public LocalDate getLastUnlockDate() {
        return lastUnlockDate;
    }
}
