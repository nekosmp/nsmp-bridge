package dev.atakku.nsmp.discord_bridge.server.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.atakku.nsmp.discord_bridge.server.event.PlayerEvents;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

@Mixin(PlayerManager.class)
abstract class MixinPlayerManager {
  @Inject(method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
  private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
    PlayerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player, player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME)) < 1);
  }
  
  @Inject(method = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
  private void  checkCanJoin(CallbackInfoReturnable<Text> cir) {
    cir.setReturnValue(Text.literal("You are not whitelisted!\nAsk Atakku#2391 on how to get whitelisted!"));
  }
}
