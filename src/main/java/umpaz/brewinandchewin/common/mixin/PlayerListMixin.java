package umpaz.brewinandchewin.common.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import umpaz.brewinandchewin.common.access.ChatPlayerListAccess;

@Mixin(PlayerList.class)
public class PlayerListMixin implements ChatPlayerListAccess {
    @Shadow @Final private MinecraftServer server;
    @Unique
    private Component brewinandchewin$originalChatMessage;

    // Modify on server then unmodify for client for chat links.
    @ModifyVariable(method = "broadcastChatMessage(Lnet/minecraft/network/chat/PlayerChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/network/chat/ChatType$Bound;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;verifyChatTrusted(Lnet/minecraft/network/chat/PlayerChatMessage;)Z"), argsOnly = true)
    public PlayerChatMessage brewinandchewin$sendOriginalChatMessageToClients(PlayerChatMessage message, @Local(argsOnly = true) ChatType.Bound boundChatType) {
        if (brewinandchewin$originalChatMessage != null) {
            server.logChatMessage(message.decoratedContent(), boundChatType, "Modified by Tipsy");
            PlayerChatMessage retValue = !message.signedContent().equals(brewinandchewin$originalChatMessage.getString()) ? message.withUnsignedContent(brewinandchewin$originalChatMessage) : message.removeUnsignedContent();
            brewinandchewin$originalChatMessage = null;
            return retValue;
        }
        return message;
    }

    @Override
    public void brewinandchewin$setOriginalMessage(Component message) {
        brewinandchewin$originalChatMessage = message;
    }
}
