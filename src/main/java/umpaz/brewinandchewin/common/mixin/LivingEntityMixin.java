package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.attachment.RagingAttachment;
import umpaz.brewinandchewin.common.registry.BnCParticleTypes;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @WrapWithCondition(method = "tickEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private boolean brewinandchewin$reduceRagingParticlesWhenNoStacks(Level instance, ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        RagingAttachment attachment = BrewinAndChewin.getHelper().getRagingAttachment((LivingEntity)(Object) this);
        if (particleData.getType() == BnCParticleTypes.RAGING_STAGE_1 && (attachment == null || attachment.getStacks() == 0))
            return instance.getRandom().nextInt(12) == 0;
        return true;
    }
}
