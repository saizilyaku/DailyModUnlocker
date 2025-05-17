package com.example.dailymodunlocker;

import com.example.dailymodunlocker.command.UnlockModCommand;
import com.example.dailymodunlocker.event.DropEventHandler;
import com.example.dailymodunlocker.event.LoginEventHandler;
import com.example.dailymodunlocker.event.RecipeEventHandler;
import com.example.dailymodunlocker.event.SpawnEventHandler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(DailyModUnlocker.MODID)
public class DailyModUnlocker {
    public static final String MODID = "dailymodunlocker";

    public DailyModUnlocker() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(this);

        // ModUnlockManagerのinitは削除済みのため呼び出し無しに修正
        ModAddonRegistry.loadConfig();

        MinecraftForge.EVENT_BUS.register(new RecipeEventHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnEventHandler());
        MinecraftForge.EVENT_BUS.register(new DropEventHandler());
        MinecraftForge.EVENT_BUS.register(new LoginEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event) {
        // 必要に応じて初期化処理を記述
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        UnlockModCommand.register(event.getDispatcher());
    }
}
