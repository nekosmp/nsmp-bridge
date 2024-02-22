package dev.atakku.nsmp.discord_bridge.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.atakku.nsmp.discord_bridge.event.PlayerEvents;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Mixin(ServerPlayNetworkHandler.class)
abstract class MixinServerPlayNetworkHandler {
  @Shadow
  public ServerPlayerEntity player;
  
  @Inject(method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;handleDecoratedMessage(Lnet/minecraft/network/message/SignedMessage;)V", at = @At("HEAD"))
  private void handleMessage(SignedMessage msg, CallbackInfo ci) {
    if (msg.getContent().getString().startsWith("/")) return;
    PlayerEvents.PLAYER_MESSAGE.invoker().onPlayerMessage(player, msg);
  }

  @Inject(method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;disconnect(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
  private void onDisconnect(Text reason, CallbackInfo ci) {
    PlayerEvents.PLAYER_DISCONNECT.invoker().onPlayerDisconnect(player, reason);
  }

  @Inject(method = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;onDisconnected(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
  private void onDisconnected(Text reason, CallbackInfo ci) {
    PlayerEvents.PLAYER_LEFT.invoker().onPlayerLeft(player, reason);
  }
}
