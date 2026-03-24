package net.kayn.apothic_extensions.mixin;

import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingScreen;
import net.kayn.apothic_extensions.client.AffixCodexGui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReforgingScreen.class)
public class ReforgingRenderMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void apothext$renderCodexTooltip(GuiGraphics gfx, int mouseX, int mouseY, float pt, CallbackInfo ci) {
        ReforgingScreen screen = (ReforgingScreen)(Object)this;
        for (var child : screen.children()) {
            if (child instanceof AffixCodexGui codex && codex.isOpen()) {
                codex.renderLateTooltip(gfx, mouseX, mouseY);
                break;
            }
        }
    }
}