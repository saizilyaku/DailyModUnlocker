package com.example.dailymodunlocker;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side copy of the unlock state received from the logical server. */
public final class ClientUnlockState {

    private static final Set<String> UNLOCKED_MODS = ConcurrentHashMap.newKeySet();

    private ClientUnlockState() {
    }

    public static void setUnlockedMods(Set<String> modIds) {
        UNLOCKED_MODS.clear();
        UNLOCKED_MODS.addAll(modIds);
    }

    public static boolean isUnlocked(String modId) {
        return UNLOCKED_MODS.contains(modId);
    }
}
