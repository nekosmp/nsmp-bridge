// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import java.net.SocketAddress;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.atakku.fsmp.bridge.Bridge;
import dev.atakku.fsmp.bridge.event.PlayerEvents;

@Mixin(PlayerManager.class)
abstract class MixinPlayerManager {
 @Inject(method = "Lnet/minecraft/server/PlayerManager;checkCanJoin(Ljava/net/SocketAddress;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/text/Text;", at = @At("HEAD"), cancellable = true)
  private void checkCanJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
    if (Bridge.getUserData(profile.getId(), true) != null) {
      return;
    }
    cir.setReturnValue(Text.literal("You need to link your account on https://link.neko.rs and be verified on fem.place to play on this server"));
    cir.cancel();
  }

  @Inject(method = "Lnet/minecraft/server/PlayerManager;onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
  private void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
    PlayerEvents.PLAYER_JOIN.invoker().onPlayerJoin(player, player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.LEAVE_GAME)) < 1);
  }
}
