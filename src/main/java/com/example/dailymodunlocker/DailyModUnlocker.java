package com.example.dailymodunlocker;

import com.example.dailymodunlocker.command.UnlockModCommand;
import com.example.dailymodunlocker.config.ModConfig;
import com.example.dailymodunlocker.event.DropEventHandler;
import com.example.dailymodunlocker.event.LoginEventHandler;
import com.example.dailymodunlocker.event.RecipeEventHandler;
import com.example.dailymodunlocker.event.SpawnEventHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DailyModUnlocker.MODID)
public class DailyModUnlocker {
    public static final String MODID = "dailymodunlocker";

    public DailyModUnlocker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);
        modBus.addListener(this::onLoadConfig);
        modBus.addListener(this::onReloadConfig);

        // コンフィグ登録
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.COMMON_SPEC);

        // 通常の Forge イベント登録
        MinecraftForge.EVENT_BUS.register(this);

        // 各種ハンドラの登録
        MinecraftForge.EVENT_BUS.register(new RecipeEventHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnEventHandler());
        MinecraftForge.EVENT_BUS.register(new DropEventHandler());
        MinecraftForge.EVENT_BUS.register(new LoginEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModAddonRegistry.loadConfig(); // コンフィグが読み込まれた後に安全に呼び出す
        });
    }

    private void onLoadConfig(final ModConfigEvent.Loading event) {
        System.out.println("[DailyModUnlocker] コンフィグが読み込まれました。");
    }

    private void onReloadConfig(final ModConfigEvent.Reloading event) {
        System.out.println("[DailyModUnlocker] コンフィグがリロードされました。");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnlockModCommand.register(event.getDispatcher());
    }
}
