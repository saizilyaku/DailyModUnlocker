package com.example.dailymodunlocker.command;

import com.example.dailymodunlocker.gui.ModUnlockScreen;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OpenUnlockGuiCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("unlockmodgui")
                        .requires(source -> source.hasPermission(0)) // 全プレイヤー実行可能
                        .executes(context -> {
                            Minecraft mc = Minecraft.getInstance();
                            if (mc.player == null) {
                                context.getSource().sendFailure(Component.literal("クライアント側のプレイヤーが見つかりません。"));
                                return 0;
                            }
                            mc.setScreen(new ModUnlockScreen());
                            return 1;
                        })
        );
    }
}
