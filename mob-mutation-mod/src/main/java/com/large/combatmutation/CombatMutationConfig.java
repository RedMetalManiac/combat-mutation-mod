package com.large.combatmutation;

import net.minecraftforge.common.ForgeConfigSpec;

public final class CombatMutationConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.IntValue HEAL_DELAY_SECONDS = BUILDER
            .comment("How long a damaged mob must stay alive without taking more damage before it heals and ends the encounter.")
            .defineInRange("healDelaySeconds", 10, 1, 300);

    private static final ForgeConfigSpec.IntValue MAX_MUTATIONS = BUILDER
            .comment("Maximum number of times a mob can mutate.")
            .defineInRange("maxMutations", 5, 1, 100);

    private static final ForgeConfigSpec.DoubleValue BONUS_HEALTH_PER_MUTATION = BUILDER
            .comment("Extra max health granted by each mutation.")
            .defineInRange("bonusHealthPerMutation", 4.0D, 0.0D, 1024.0D);

    private static final ForgeConfigSpec.DoubleValue BONUS_ATTACK_PER_MUTATION = BUILDER
            .comment("Extra attack damage granted by each mutation when the mob has an attack attribute.")
            .defineInRange("bonusAttackPerMutation", 1.5D, 0.0D, 256.0D);

    private static final ForgeConfigSpec.DoubleValue BONUS_ARMOR_PER_MUTATION = BUILDER
            .comment("Extra armor granted by each mutation when the mob has an armor attribute.")
            .defineInRange("bonusArmorPerMutation", 1.0D, 0.0D, 256.0D);

    private static final ForgeConfigSpec.DoubleValue SPEED_MULTIPLIER_PER_MUTATION = BUILDER
            .comment("Extra movement speed granted by each mutation as a multiplicative bonus. 0.08 means +8% per mutation.")
            .defineInRange("speedMultiplierPerMutation", 0.08D, 0.0D, 10.0D);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private CombatMutationConfig() {
    }

    public static long healDelayTicks() {
        return HEAL_DELAY_SECONDS.get().longValue() * 20L;
    }

    public static int maxMutations() {
        return MAX_MUTATIONS.get();
    }

    public static double healthBonusPerMutation() {
        return BONUS_HEALTH_PER_MUTATION.get();
    }

    public static double attackBonusPerMutation() {
        return BONUS_ATTACK_PER_MUTATION.get();
    }

    public static double armorBonusPerMutation() {
        return BONUS_ARMOR_PER_MUTATION.get();
    }

    public static double speedMultiplierPerMutation() {
        return SPEED_MULTIPLIER_PER_MUTATION.get();
    }
}
