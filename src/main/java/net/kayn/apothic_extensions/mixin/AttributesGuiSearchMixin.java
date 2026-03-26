package net.kayn.apothic_extensions.mixin;

import dev.shadowsoffire.attributeslib.client.AttributesGui;
import net.kayn.apothic_extensions.client.AttributesGuiHooks;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = AttributesGui.class, remap = false)
public class AttributesGuiSearchMixin {

    @Shadow protected int leftPos;
    @Shadow @org.spongepowered.asm.mixin.Final protected net.minecraft.client.gui.screens.inventory.InventoryScreen parent;
    @Shadow protected int topPos;
    @Shadow protected boolean open;
    @Shadow protected List<AttributeInstance> data;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void apothext$init(InventoryScreen parent, CallbackInfo ci) {
        AttributesGuiHooks.init((AttributesGui)(Object)this, parent, leftPos, topPos);
    }

    @Inject(method = "toggleVisibility", at = @At("HEAD"))
    private void apothext$closeCodexWhenOpening(CallbackInfo ci) {
        if (!open) {
            for (var child : new java.util.ArrayList<>(parent.children())) {
                if (child instanceof net.kayn.apothic_extensions.client.AffixCodexGui codex
                        && net.kayn.apothic_extensions.client.AffixCodexGui.wasOpen) {
                    codex.toggleVisibility();
                    break;
                }
            }
        }
    }

    @Inject(method = "toggleVisibility", at = @At("RETURN"))
    private void apothext$toggleVisibility(CallbackInfo ci) {
        AttributesGuiHooks.toggleVisibility(open, leftPos, topPos);
    }

    @Inject(method = "render", at = @At("RETURN"), remap = true)
    private void apothext$render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (!open) return;
        if (AttributesGuiHooks.nameBox != null) {
            AttributesGuiHooks.nameBox.setPosition(leftPos + 7, topPos + 3);
            AttributesGuiHooks.nameBox.setVisible(true);
        }
        gfx.fill(leftPos + 6, topPos + 2, leftPos + 111, topPos + 14, 0xFF8B8B8B);
        AttributesGuiHooks.render(gfx, mouseX, mouseY, partialTicks, open);
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void apothext$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        AttributesGuiHooks.mouseClicked(mouseX, mouseY, button);
    }

    @Inject(method = "refreshData", at = @At("RETURN"))
    private void apothext$refreshData(CallbackInfo ci) {
        if (AttributesGuiHooks.nameBox == null) return;
        String search = AttributesGuiHooks.nameBox.getValue().toLowerCase();
        if (search.isEmpty()) return;
        data.removeIf(inst -> !I18n.get(inst.getAttribute().getDescriptionId()).toLowerCase().contains(search));
    }
}