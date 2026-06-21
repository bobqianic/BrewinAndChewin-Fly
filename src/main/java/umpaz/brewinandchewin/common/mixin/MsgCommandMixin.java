package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.commands.MsgCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import umpaz.brewinandchewin.BrewinAndChewin;
import umpaz.brewinandchewin.common.network.clientbound.MakeNextPlayerChatTipsyClientboundPacket;
import umpaz.brewinandchewin.common.registry.BnCEffects;

import java.util.Collection;

@Mixin(MsgCommand.class)
public class MsgCommandMixin {
    @Inject(method = "sendMessage", at = @At("HEAD"))
    private static void brewinandchewin$generateRng(CommandSourceStack source,
                                                    Collection<ServerPlayer> targets,
                                                    PlayerChatMessage message,
                                                    CallbackInfo ci,
                                                    @Share("seed") LocalLongRef seed) {
        if (source.getPlayer() == null || !source.getPlayer().hasEffect(BnCEffects.TIPSY))
            return;

        seed.set(source.getPlayer().getRandom().nextLong());
    }

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/CommandSourceStack;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"))
    private static void brewinandchewin$sendTipsyTellMessageSender(CommandSourceStack source,
                                                                   Collection<ServerPlayer> targets,
                                                                   PlayerChatMessage message,
                                                                   CallbackInfo ci,
                                                                   @Share("seed") LocalLongRef seed) {
        if (source.getPlayer() == null || !source.getPlayer().hasEffect(BnCEffects.TIPSY))
            return;

        BrewinAndChewin.getHelper().sendClientbound(source.getPlayer(), new MakeNextPlayerChatTipsyClientboundPacket(source.getPlayer().getEffect(BnCEffects.TIPSY).getAmplifier(), seed.get(), 0));
    }

    @Inject(method = "sendMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;sendChatMessage(Lnet/minecraft/network/chat/OutgoingChatMessage;ZLnet/minecraft/network/chat/ChatType$Bound;)V"))
    private static void brewinandchewin$sendTipsyTellMessageReceiver(CommandSourceStack source,
                                                                     Collection<ServerPlayer> targets,
                                                                     PlayerChatMessage message,
                                                                     CallbackInfo ci,
                                                                     @Local ServerPlayer serverPlayer,
                                                                     @Share("seed") LocalLongRef seed) {
        if (source.getPlayer() == null || !source.getPlayer().hasEffect(BnCEffects.TIPSY))
            return;

        BrewinAndChewin.getHelper().sendClientbound(serverPlayer, new MakeNextPlayerChatTipsyClientboundPacket(source.getPlayer().getEffect(BnCEffects.TIPSY).getAmplifier(), seed.get(), serverPlayer.is(source.getPlayer()) ? targets.size() : 0));
    }
}
