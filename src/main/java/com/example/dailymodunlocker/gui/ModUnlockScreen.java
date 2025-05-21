package com.example.dailymodunlocker.gui;

import com.example.dailymodunlocker.ModUnlockManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MOD 解禁の状況を表示する画面。
 * 常時解放されているMODは表示対象外とします。
 */
public class ModUnlockScreen extends Screen {

    private final List<IModInfo> displayedMods;
    private final ModUnlockManager manager;

    public ModUnlockScreen() {
        super(Component.literal("MOD解禁状況"));

        Level level = Minecraft.getInstance().level;
        MinecraftServer server = (level != null) ? level.getServer() : null;

        if (server != null) {
            this.manager = ModUnlockManager.getInstance(server);
        } else {
            this.manager = null;
        }

        // 常時解放されているMODは表示しない
        Set<String> hiddenMods = ModUnlockManager.loadAlwaysUnlockedMods();

        this.displayedMods = ModList.get().getMods().stream()
                .filter(info -> !hiddenMods.contains(info.getModId()))
                .sorted((a, b) -> a.getModId().compareToIgnoreCase(b.getModId()))
                .collect(Collectors.toList());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int y = 30;
        for (IModInfo container : displayedMods) {
            if (y > this.height - 20)
                break;

            String modid = container.getModId();
            boolean unlocked = (manager != null) && manager.isUnlocked(modid);
            String display = (unlocked ? "✓ " : "✗ ") + modid;
            int color = unlocked ? 0x00FF00 : 0xFF5555;
            graphics.drawString(this.font, display, 20, y, color);
            y += 10;
        }
        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
