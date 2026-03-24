package net.kayn.apothic_extensions.mixin;

import net.kayn.apothic_extensions.client.AttributesGuiHooks;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class InventoryScreenMixin<T extends AbstractContainerMenu> extends net.minecraft.client.gui.screens.Screen {

    public InventoryScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void apothext$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        EditBox nameBox = AttributesGuiHooks.nameBox;
        if (nameBox == null || !nameBox.visible || !nameBox.isFocused()) return;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            nameBox.setFocused(false);
            cir.setReturnValue(true);
            return;
        }
        nameBox.keyPressed(keyCode, scanCode, modifiers);
        AttributesGuiHooks.refreshData();
        cir.cancel();
    }
}