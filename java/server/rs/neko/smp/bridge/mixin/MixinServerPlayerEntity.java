// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package rs.neko.smp.bridge.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameRules;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rs.neko.smp.bridge.event.PlayerEvents;

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
