package rs.neko.smp.discbr.server.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import rs.neko.smp.discbr.server.event.PlayerEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerAdvancementTracker.class)
abstract class MixinPlayerAdvancementTracker {
  @Shadow
  ServerPlayerEntity owner;

  @Inject(method = "Lnet/minecraft/advancement/PlayerAdvancementTracker;grantCriterion(Lnet/minecraft/advancement/Advancement;Ljava/lang/String;)Z", at = @At(value = "INVOKE", target="Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
  private void grantCriterion(Advancement adv, String criterionName, CallbackInfoReturnable<Boolean> cir) {
    if (adv != null && adv.getDisplay() != null && adv.getDisplay().shouldAnnounceToChat()) {
      PlayerEvents.PLAYER_ADVANCEMENT.invoker().onPlayerAdvancement(owner, adv);
    }
  }
}
