package com.large.combatmutation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = CombatMutationMod.MOD_ID)
public final class MutationHandler {
    private static final String RECOVERING_KEY = "combatmutation_recovering";
    private static final String HEAL_AT_KEY = "combatmutation_heal_at";
    private static final String PENDING_MUTATION_KEY = "combatmutation_pending_mutation";
    private static final String MUTATION_STAGE_KEY = "combatmutation_mutation_stage";

    private static final UUID HEALTH_MODIFIER_ID = UUID.fromString("fc1116b2-2d5e-4c79-bf43-cf10b6258682");
    private static final UUID ATTACK_MODIFIER_ID = UUID.fromString("1d7cb7fa-ae98-4f69-82d0-6856f7a6a4d1");
    private static final UUID ARMOR_MODIFIER_ID = UUID.fromString("a8cad2b0-c7aa-4e52-a41f-4f4ce9be290d");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("8a8df918-0f13-4f2e-9913-e88d257824af");

    private static final Map<ResourceKey<Level>, Set<UUID>> ACTIVE_MOBS = new HashMap<>();

    private MutationHandler() {
    }

    @SubscribeEvent
    public static void onMobDamaged(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide() || event.getAmount() <= 0.0F) {
            return;
        }

        ServerLevel level = (ServerLevel) mob.level();
        CompoundTag data = mob.getPersistentData();
        data.putBoolean(RECOVERING_KEY, true);
        data.putLong(HEAL_AT_KEY, level.getGameTime() + CombatMutationConfig.healDelayTicks());

        if (countsAsCombatDamage(event.getSource(), mob)) {
            data.putBoolean(PENDING_MUTATION_KEY, true);
        }

        track(level, mob);
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }

        CompoundTag data = mob.getPersistentData();
        int storedStage = data.getInt(MUTATION_STAGE_KEY);
        int clampedStage = clampStage(storedStage);
        if (clampedStage != storedStage) {
            data.putInt(MUTATION_STAGE_KEY, clampedStage);
        }

        if (clampedStage > 0) {
            applyMutationAttributes(mob, clampedStage);
        }

        if (data.getBoolean(RECOVERING_KEY)) {
            track((ServerLevel) event.getLevel(), mob);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide() || !(event.level instanceof ServerLevel level)) {
            return;
        }

        Set<UUID> tracked = ACTIVE_MOBS.get(level.dimension());
        if (tracked == null || tracked.isEmpty()) {
            return;
        }

        long gameTime = level.getGameTime();
        Iterator<UUID> iterator = tracked.iterator();
        while (iterator.hasNext()) {
            UUID uuid = iterator.next();
            Entity entity = level.getEntity(uuid);
            if (!(entity instanceof Mob mob) || !mob.isAlive()) {
                iterator.remove();
                continue;
            }

            CompoundTag data = mob.getPersistentData();
            if (!data.getBoolean(RECOVERING_KEY)) {
                iterator.remove();
                continue;
            }

            long healAt = data.getLong(HEAL_AT_KEY);
            if (healAt <= 0L) {
                clearRecoveryState(data);
                iterator.remove();
                continue;
            }

            if (gameTime < healAt) {
                continue;
            }

            finishEncounter(mob, data);
            iterator.remove();
        }

        if (tracked.isEmpty()) {
            ACTIVE_MOBS.remove(level.dimension());
        }
    }

    private static void finishEncounter(Mob mob, CompoundTag data) {
        int currentStage = clampStage(data.getInt(MUTATION_STAGE_KEY));
        int nextStage = currentStage;

        if (data.getBoolean(PENDING_MUTATION_KEY)) {
            nextStage = clampStage(currentStage + 1);
            if (nextStage > currentStage) {
                data.putInt(MUTATION_STAGE_KEY, nextStage);
                applyMutationAttributes(mob, nextStage);
                CombatMutationMod.LOGGER.debug("Mob {} mutated to stage {}", mob.getEncodeId(), nextStage);
            }
        }

        mob.setHealth(mob.getMaxHealth());
        clearRecoveryState(data);
    }

    private static boolean countsAsCombatDamage(DamageSource source, Mob target) {
        Entity attacker = source.getEntity();
        return attacker != null && attacker != target && (attacker instanceof Player || attacker instanceof Mob);
    }

    private static void track(ServerLevel level, Mob mob) {
        ACTIVE_MOBS.computeIfAbsent(level.dimension(), ignored -> new HashSet<>()).add(mob.getUUID());
    }

    private static int clampStage(int requestedStage) {
        return Math.max(0, Math.min(requestedStage, CombatMutationConfig.maxMutations()));
    }

    private static void clearRecoveryState(CompoundTag data) {
        data.remove(RECOVERING_KEY);
        data.remove(HEAL_AT_KEY);
        data.remove(PENDING_MUTATION_KEY);
    }

    private static void applyMutationAttributes(Mob mob, int stage) {
        applyPermanentModifier(
                mob.getAttribute(Attributes.MAX_HEALTH),
                HEALTH_MODIFIER_ID,
                "combatmutation.max_health",
                stage * CombatMutationConfig.healthBonusPerMutation(),
                AttributeModifier.Operation.ADDITION
        );
        applyPermanentModifier(
                mob.getAttribute(Attributes.ATTACK_DAMAGE),
                ATTACK_MODIFIER_ID,
                "combatmutation.attack_damage",
                stage * CombatMutationConfig.attackBonusPerMutation(),
                AttributeModifier.Operation.ADDITION
        );
        applyPermanentModifier(
                mob.getAttribute(Attributes.ARMOR),
                ARMOR_MODIFIER_ID,
                "combatmutation.armor",
                stage * CombatMutationConfig.armorBonusPerMutation(),
                AttributeModifier.Operation.ADDITION
        );
        applyPermanentModifier(
                mob.getAttribute(Attributes.MOVEMENT_SPEED),
                SPEED_MODIFIER_ID,
                "combatmutation.movement_speed",
                stage * CombatMutationConfig.speedMultiplierPerMutation(),
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );
    }

    private static void applyPermanentModifier(
            AttributeInstance attribute,
            UUID modifierId,
            String modifierName,
            double amount,
            AttributeModifier.Operation operation
    ) {
        if (attribute == null) {
            return;
        }

        attribute.removeModifier(modifierId);

        if (amount > 0.0D) {
            attribute.addPermanentModifier(new AttributeModifier(modifierId, modifierName, amount, operation));
        }
    }
}
