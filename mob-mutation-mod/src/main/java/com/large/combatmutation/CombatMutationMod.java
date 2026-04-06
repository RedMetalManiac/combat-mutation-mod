package com.large.combatmutation;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CombatMutationMod.MOD_ID)
public final class CombatMutationMod {
    public static final String MOD_ID = "combatmutation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CombatMutationMod(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, CombatMutationConfig.SPEC);
    }
}
