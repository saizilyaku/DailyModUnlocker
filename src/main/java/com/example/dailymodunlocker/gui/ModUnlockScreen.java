package com.example.dailymodunlocker.gui;

import com.example.dailymodunlocker.ClientUnlockState;
import com.example.dailymodunlocker.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * MOD 解禁の状況を表示する画面。
 * 常時解放されているMODは表示対象外とします。
 */
public class ModUnlockScreen extends Screen {

    private static final int LIST_TOP = 30;
    private static final int LIST_BOTTOM_MARGIN = 14;
    private static final int ROW_HEIGHT = 12;

    private final List<IModInfo> displayedMods;
    private int scrollOffset;

    public ModUnlockScreen() {
        super(Component.literal("MOD解禁状況"));

        // 常時解放されているMODは表示しない
        Set<String> hiddenMods = new HashSet<>(ModConfig.COMMON.alwaysUnlockedMods.get());

        this.displayedMods = ModList.get().getMods().stream()
                .filter(info -> !hiddenMods.contains(info.getModId()))
                .sorted((a, b) -> a.getModId().compareToIgnoreCase(b.getModId()))
                .collect(Collectors.toList());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        int visibleRows = getVisibleRows();
        int maxScroll = Math.max(0, displayedMods.size() - visibleRows);
        scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

        graphics.enableScissor(12, LIST_TOP, this.width - 12, listBottom);
        for (int row = 0; row < visibleRows; row++) {
            int modIndex = scrollOffset + row;
            if (modIndex >= displayedMods.size()) {
                break;
            }

            IModInfo container = displayedMods.get(modIndex);
            String modid = container.getModId();
            boolean unlocked = ClientUnlockState.isUnlocked(modid);
            String display = (unlocked ? "✓ " : "✗ ") + modid;
            int color = unlocked ? 0x00FF00 : 0xFF5555;
            graphics.drawString(this.font, display, 20, LIST_TOP + row * ROW_HEIGHT, color);
        }
        graphics.disableScissor();

        if (maxScroll > 0) {
            int trackHeight = listBottom - LIST_TOP;
            int thumbHeight = Math.max(12, trackHeight * visibleRows / displayedMods.size());
            int thumbTravel = trackHeight - thumbHeight;
            int thumbY = LIST_TOP + (thumbTravel * scrollOffset / maxScroll);
            graphics.fill(this.width - 10, LIST_TOP, this.width - 7, listBottom, 0xFF555555);
            graphics.fill(this.width - 10, thumbY, this.width - 7, thumbY + thumbHeight, 0xFFFFFFFF);
        }

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int listBottom = this.height - LIST_BOTTOM_MARGIN;
        if (mouseX < 12 || mouseX > this.width - 12 || mouseY < LIST_TOP || mouseY > listBottom) {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }

        int maxScroll = Math.max(0, displayedMods.size() - getVisibleRows());
        if (maxScroll == 0 || delta == 0) {
            return false;
        }

        scrollOffset = Mth.clamp(scrollOffset - (int) Math.signum(delta) * 3, 0, maxScroll);
        return true;
    }

    private int getVisibleRows() {
        return Math.max(1, (this.height - LIST_TOP - LIST_BOTTOM_MARGIN) / ROW_HEIGHT);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
