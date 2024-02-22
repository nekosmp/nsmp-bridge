package dev.atakku.nsmp.discord_bridge.server.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.atakku.nsmp.discord_bridge.server.event.PlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

@Mixin(ServerPlayerEntity.class)
abstract class MixinServerPlayerEntity {
  @Inject(at = @At("HEAD"), method = "Lnet/minecraft/server/network/ServerPlayerEntity;onDeath(Lnet/minecraft/entity/damage/DamageSource;)V")
  private void onDeath(DamageSource source, CallbackInfo ci) { 
    ServerPlayerEntity spe = (ServerPlayerEntity) (Object) this;
    if (spe.getWorld().getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) {
      PlayerEvents.PLAYER_DEATH.invoker().onPlayerDeath(spe, source);
    }
  }
}
