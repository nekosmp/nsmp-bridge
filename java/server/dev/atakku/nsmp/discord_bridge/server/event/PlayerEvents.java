package dev.atakku.nsmp.discord_bridge.server.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class PlayerEvents {
  public static final Event<PlayerMessage> PLAYER_MESSAGE = EventFactory.createArrayBacked(PlayerMessage.class, callbacks -> (player, msg) -> {
    for (PlayerMessage callback : callbacks) {
      callback.onPlayerMessage(player, msg);
    }
  });

  @FunctionalInterface
  public interface PlayerMessage {
    void onPlayerMessage(ServerPlayerEntity player, SignedMessage msg);
  }

  public static final Event<PlayerDisconnect> PLAYER_DISCONNECT = EventFactory.createArrayBacked(PlayerDisconnect.class, callbacks -> (player, reason) -> {
    for (PlayerDisconnect callback : callbacks) {
      callback.onPlayerDisconnect(player, reason);
    }
  });

  @FunctionalInterface
  public interface PlayerDisconnect {
    void onPlayerDisconnect(ServerPlayerEntity player, Text reason);
  }

  public static final Event<PlayerAdvancement> PLAYER_ADVANCEMENT = EventFactory.createArrayBacked(PlayerAdvancement.class, callbacks -> (player, reason) -> {
    for (PlayerAdvancement callback : callbacks) {
      callback.onPlayerAdvancement(player, reason);
    }
  });

  @FunctionalInterface
  public interface PlayerAdvancement {
    void onPlayerAdvancement(ServerPlayerEntity player, Advancement reason);
  }

  public static final Event<PlayerJoin> PLAYER_JOIN = EventFactory.createArrayBacked(PlayerJoin.class, callbacks -> (player, firstJoin) -> {
    for (PlayerJoin callback : callbacks) {
      callback.onPlayerJoin(player, firstJoin);
    }
  });

  @FunctionalInterface
  public interface PlayerJoin {
    void onPlayerJoin(ServerPlayerEntity player, boolean firstJoin);
  }

  public static final Event<PlayerLeft> PLAYER_LEFT = EventFactory.createArrayBacked(PlayerLeft.class, callbacks -> (player, reason) -> {
    for (PlayerLeft callback : callbacks) {
      callback.onPlayerLeft(player, reason);
    }
  });

  @FunctionalInterface
  public interface PlayerLeft {
    void onPlayerLeft(ServerPlayerEntity player, Text reason);
  }

  public static final Event<PlayerDeath> PLAYER_DEATH = EventFactory.createArrayBacked(PlayerDeath.class, callbacks -> (player, source) -> {
    for (PlayerDeath callback : callbacks) {
      callback.onPlayerDeath(player, source);
    }
  });

  @FunctionalInterface
  public interface PlayerDeath {
    void onPlayerDeath(ServerPlayerEntity player, DamageSource source);
  }
}
