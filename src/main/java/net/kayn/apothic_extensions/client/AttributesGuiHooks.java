package net.kayn.apothic_extensions.client;

import dev.shadowsoffire.attributeslib.client.AttributesGui;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;

public class AttributesGuiHooks {

    public static EditBox nameBox;
    private static AttributesGui ATTRIBUTES_GUI;

    public static void init(AttributesGui gui, InventoryScreen parent, int leftPos, int topPos) {
        ATTRIBUTES_GUI = gui;
        nameBox = new EditBox(
                Minecraft.getInstance().font,
                leftPos + 7, topPos + 3,
                102, 12,
                Component.empty()
        );
        nameBox.setMaxLength(50);
        nameBox.setBordered(false);
        nameBox.setHint(Component.literal("Search...").withStyle(ChatFormatting.WHITE));
        nameBox.visible = false;
        nameBox.setFocused(false);
    }

    public static void toggleVisibility(boolean open, int leftPos, int topPos) {
        if (nameBox == null) return;
        nameBox.setVisible(open);
        nameBox.setPosition(leftPos + 7, topPos + 3);
        if (!open) {
            nameBox.setFocused(false);
            nameBox.setValue("");
        }
    }

    public static void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks, boolean open) {
        if (nameBox != null && open) {
            nameBox.render(gfx, mouseX, mouseY, partialTicks);
        }
    }

    public static void mouseClicked(double mouseX, double mouseY, int button) {
        if (nameBox == null) return;
        if (nameBox.isHovered()) {
            nameBox.setFocused(!nameBox.isFocused());
        } else if (nameBox.isFocused()) {
            nameBox.setFocused(false);
        }
    }

    public static void refreshData() {
        if (ATTRIBUTES_GUI != null) {
            ATTRIBUTES_GUI.refreshData();
        }
    }
}