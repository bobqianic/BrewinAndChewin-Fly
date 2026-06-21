package umpaz.brewinandchewin.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.registry.BnCEffects;
import vectorwing.farmersdelight.common.utility.TextUtils;

import java.util.List;
import java.util.function.Consumer;

public class BnCBucketItem extends BucketItem {
    private static final int DRINK_SOUND_INTERVAL_TICKS = 4;
    private static final float DRINK_SOUND_START_FRACTION = 0.21875F;
    private static final int MAX_DRINK_SOUND_START_TICKS = 7;

    private final Fluid fluid;

    public BnCBucketItem(Fluid fluid, Item.Properties properties) {
        super(fluid, properties);
        this.fluid = fluid;
    }

    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            return consumable.startConsuming(player, stack, hand);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity consumer, ItemStack stack, int remainingUseDuration) {
        Consumable consumable = stack.get(DataComponents.CONSUMABLE);
        if (consumable == null || consumable.animation() != ItemUseAnimation.DRINK) {
            return;
        }

        if (!level.isClientSide() && shouldEmitBucketDrinkSound(consumable, remainingUseDuration)) {
            float pitch = Mth.randomBetween(consumer.getRandom(), 0.9F, 1.0F);
            level.playSound(null, consumer.getX(), consumer.getY(), consumer.getZ(), consumable.sound().value(), consumer.getSoundSource(), 0.5F, pitch);
        }
    }

    private static boolean shouldEmitBucketDrinkSound(Consumable consumable, int remainingUseDuration) {
        int consumeTicks = consumable.consumeTicks();
        int elapsedTicks = consumeTicks - remainingUseDuration;
        int startTicks = Math.min((int) (consumeTicks * DRINK_SOUND_START_FRACTION), MAX_DRINK_SOUND_START_TICKS);
        return elapsedTicks > startTicks && remainingUseDuration % DRINK_SOUND_INTERVAL_TICKS == 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
        if (!level.isClientSide()) {
            List<MobEffectInstance> consumeEffects = BoozeItem.getConsumeEffects(stack);
            consumeEffects.stream()
                    .filter(effect -> effect.getEffect() == BnCEffects.TIPSY)
                    .findFirst()
                    .ifPresent(effect -> stackTipsy(consumer, effect));
            consumeEffects.stream()
                    .filter(effect -> effect.getEffect() == MobEffects.BAD_OMEN)
                    .findFirst()
                    .ifPresent(effect -> stackBadOmen(consumer, effect));
        }

        ItemStack result = super.finishUsingItem(stack, level, consumer);
        if (consumer instanceof Player player && player.getAbilities().instabuild) {
            return result;
        }

        ItemStack emptyBucket = new ItemStack(Items.BUCKET);
        if (result.isEmpty()) {
            return emptyBucket;
        }
        if (consumer instanceof Player player && !player.getInventory().add(emptyBucket)) {
            player.drop(emptyBucket, false);
        }
        return result;
    }

    @Override
    public boolean emptyContents(LivingEntity entity, Level level, BlockPos pos, BlockHitResult result) {
        // BnC fluids are tank-only and have no world block; keep bucket use from placing air.
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag isAdvanced) {
        if (!BrewinAndChewin.getHelper().hasFoodEffectTooltip()) {
            return;
        }

        List<Component> tooltip = new java.util.ArrayList<>();
        TextUtils.addFoodEffectTooltip(stack, tooltip::add, 1.0F, context.tickRate());
        for (int i = 0; i < tooltip.size(); ++i) {
            Component component = tooltip.get(i);
            if (BoozeItem.RED_EFFECTS.stream().anyMatch(holder -> component.contains(Component.translatable(holder.get().value().getDescriptionId())))) {
                tooltip.set(i, component.copy().withStyle(ChatFormatting.RED));
            }
        }
        tooltip.forEach(tooltipAdder);
    }

    private static void stackTipsy(LivingEntity consumer, MobEffectInstance incomingEffect) {
        if (consumer.hasEffect(BnCEffects.TIPSY)) {
            MobEffectInstance existingEffect = consumer.getEffect(BnCEffects.TIPSY);
            consumer.addEffect(new MobEffectInstance(BnCEffects.TIPSY, stackedDuration(existingEffect, incomingEffect),
                    Math.min(existingEffect.getAmplifier() + incomingEffect.getAmplifier() + 1, 9),
                    existingEffect.isAmbient(), existingEffect.isVisible(), existingEffect.showIcon()));
        }
    }

    private static void stackBadOmen(LivingEntity consumer, MobEffectInstance incomingEffect) {
        if (consumer.hasEffect(MobEffects.BAD_OMEN)) {
            MobEffectInstance existingEffect = consumer.getEffect(MobEffects.BAD_OMEN);
            consumer.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, maxDuration(existingEffect, incomingEffect),
                    Math.min(existingEffect.getAmplifier() + incomingEffect.getAmplifier() + 1, 4),
                    existingEffect.isAmbient(), existingEffect.isVisible(), existingEffect.showIcon()));
        }
    }

    private static int stackedDuration(MobEffectInstance existingEffect, MobEffectInstance incomingEffect) {
        if (existingEffect.getDuration() == -1 || incomingEffect.getDuration() == -1) {
            return -1;
        }
        return existingEffect.getDuration() + incomingEffect.getDuration();
    }

    private static int maxDuration(MobEffectInstance existingEffect, MobEffectInstance incomingEffect) {
        if (existingEffect.getDuration() == -1 || incomingEffect.getDuration() == -1) {
            return -1;
        }
        return Math.max(existingEffect.getDuration(), incomingEffect.getDuration());
    }
}
