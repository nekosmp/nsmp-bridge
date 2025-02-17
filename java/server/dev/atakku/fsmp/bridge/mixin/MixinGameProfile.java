// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge.mixin;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.atakku.fsmp.bridge.Bridge;

@Mixin(GameProfile.class)
abstract public class MixinGameProfile {
  @Mutable
  @Accessor(remap = false)
  abstract public void setName(String name);

  @Inject(method = "<init>(Ljava/util/UUID;Ljava/lang/String;)V", at = @At("RETURN"), remap = false)
  private void init(UUID id, String name, CallbackInfo ci) {
    String data = Bridge.getUserData(id, false);
    if (data != null) {
      this.setName(data);
    }
  }
}
