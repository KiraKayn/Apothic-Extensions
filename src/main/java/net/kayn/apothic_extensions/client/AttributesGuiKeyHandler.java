package net.kayn.apothic_extensions.client;

import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AttributesGuiKeyHandler {

    @SubscribeEvent
    public static void onCharTyped(ScreenEvent.CharacterTyped event) {
        EditBox nameBox = AttributesGuiHooks.nameBox;
        if (nameBox == null || !nameBox.visible || !nameBox.isFocused()) return;
        // charTyped handles actual character input - cancel the event so nothing else processes it
        nameBox.charTyped(event.getCodePoint(), event.getModifiers());
        AttributesGuiHooks.refreshData();
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onScreenClosing(ScreenEvent.Closing event) {
        EditBox nameBox = AttributesGuiHooks.nameBox;
        if (nameBox != null) {
            nameBox.setFocused(false);
        }
    }
}