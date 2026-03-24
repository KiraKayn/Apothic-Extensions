package net.kayn.apothic_extensions;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ApothicExtensionsConfig {

    public static ForgeConfigSpec.IntValue CODEX_BUTTON_X;
    public static ForgeConfigSpec.IntValue CODEX_BUTTON_Y;

    public static final ForgeConfigSpec CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("codex_button");
        CODEX_BUTTON_X = builder.defineInRange("x_offset", 158, 0, 500);
        CODEX_BUTTON_Y = builder.defineInRange("y_offset", 8, 0, 500);
        builder.pop();
        CONFIG = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CONFIG);
    }
}