package com.example.dailymodunlocker.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ModConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {
        public final ForgeConfigSpec.BooleanValue unlockAddonsWithParent;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.comment("DailyModUnlocker 設定");
            builder.push("general");

            unlockAddonsWithParent = builder
                    .comment("親MOD解放時にアドオンも同時に解放するか")
                    .define("unlockAddonsWithParent", true);

            builder.pop();
        }
    }
}
