package net.kayn.apothic_extensions.client;

import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "apothic_extensions", value = Dist.CLIENT)
public class AffixCodexInitHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof ReforgingScreen scn)) return;

        AffixCodexGui codex = new AffixCodexGui(scn);
        event.addListener(codex);
        event.addListener(codex.toggleBtn);

        if (AffixCodexGui.wasOpen) {
            codex.toggleVisibility();
        }
    }
}