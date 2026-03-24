package net.kayn.apothic_extensions;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ApothicExtensions.MOD_ID)
public class ApothicExtensions {

    public static final String MOD_ID = "apothic_extensions";

    public ApothicExtensions() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);

        ApothicExtensionsConfig.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }
}