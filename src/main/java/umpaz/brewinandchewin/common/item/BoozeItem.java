package umpaz.brewinandchewin.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoozeItem extends Item {
    private final Supplier<Fluid> fluid;

    public BoozeItem(Supplier<Fluid> fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return this.fluid.get();
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
       if (BrewinAndChewin.getHelper().isEdible(heldStack, player)) {
          if ( player.canEat(BrewinAndChewin.getHelper().getFoodProperties(heldStack, player).canAlwaysEat()) ) {
             player.startUsingItem(hand);
              return InteractionResult.CONSUME;
          }
          else {
              return InteractionResult.FAIL;
          }
       }
       else {
           return ItemUtils.startUsingInstantly(level, player, hand);
       }
    }


    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide()) {
            var tipsy = getConsumeEffects(stack).stream()
                    .filter(effect -> effect.getEffect() == BnCEffects.TIPSY)
                    .findFirst();
            this.affectConsumer(consumer, tipsy.map(MobEffectInstance::getDuration).orElse(0), tipsy.map(MobEffectInstance::getAmplifier).orElse(-1));
        }
        ItemStack containerStack = BrewinAndChewin.getHelper().getCraftingRemainingItem(stack);
        Player player;
        if (BrewinAndChewin.getHelper().isEdible(stack, consumer)) {
            super.finishUsingItem(stack, level, consumer);
        } else {
            player = consumer instanceof Player ? (Player)consumer : null;
            if (player instanceof ServerPlayer) {
                CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, stack);
            }
            if (player != null) {
                player.awardStat(Stats.ITEM_USED.get(this));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
        }
        if (stack.isEmpty()) {
            return containerStack;
        } else {
            if (consumer instanceof Player) {
                player = (Player)consumer;
                if (!((Player)consumer).getAbilities().instabuild && !player.getInventory().add(containerStack)) {
                    player.drop(containerStack, false);
                }
            }
            return stack;
        }
    }

    public static List<MobEffectInstance> getConsumeEffects(ItemStack stack) {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null) {
            return List.of();
        }
        return consumable.onConsumeEffects().stream()
                .filter(ApplyStatusEffectsConsumeEffect.class::isInstance)
                .map(ApplyStatusEffectsConsumeEffect.class::cast)
                .flatMap(effect -> effect.effects().stream())
                .toList();
    }

    //Tipsy Stuff
    public void affectConsumer(LivingEntity consumer, int duration, int potency) {
       if (consumer.hasEffect(BnCEffects.TIPSY)) {
           MobEffectInstance effect = consumer.getEffect(BnCEffects.TIPSY);
           consumer.addEffect(new MobEffectInstance(BnCEffects.TIPSY, effect.getDuration() == -1 ? -1 : effect.getDuration() + duration, Math.min(effect.getAmplifier() + potency + 1, 9), effect.isAmbient(), effect.isVisible(), effect.showIcon()));
       }
    }

    public static final Set<Supplier<Holder<MobEffect>>> RED_EFFECTS = Set.of(() -> BnCEffects.TIPSY, () -> MobEffects.BAD_OMEN);

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag isAdvanced) {
        if (!BrewinAndChewin.getHelper().hasFoodEffectTooltip())
            return;
        List<Component> tooltip = new java.util.ArrayList<>();
        TextUtils.addFoodEffectTooltip(stack, tooltip::add, 1.0F, context.tickRate());
        for (int i = 0; i < tooltip.size(); ++i) {
            Component component = tooltip.get(i);
            if (RED_EFFECTS.stream().anyMatch(holder -> component.contains(Component.translatable(holder.get().value().getDescriptionId())))) {
                tooltip.set(i, component.copy().withStyle(ChatFormatting.RED));
            }
        }
        tooltip.forEach(tooltipAdder);
    }
}
