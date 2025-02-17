// Copyright 2024 Atakku <https://atakku.dev>
//
// This project is dual licensed under MIT and Apache.

package dev.atakku.fsmp.bridge;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.external.JDAWebhookClient;
import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.vdurmont.emoji.EmojiParser;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import javax.annotation.Nonnull;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dev.atakku.fsmp.bridge.event.PlayerEvents;

import java.net.URL;
import java.util.Random;

import org.apache.commons.io.IOUtils;


public class Bridge implements DedicatedServerModInitializer {
  public static final String MOD_ID = "fsmp-bridge";
  public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

  public static final String CHANNEL_ID = System.getenv("DISCORD_CHANNEL_ID");
  public static final String OWNER = System.getenv("DISCORD_OWNER_ID");
  public static final JDAWebhookClient WEBHOOK = new WebhookClientBuilder(System.getenv("DISCORD_WEBHOOK")).buildJDA();
  public static final JDA JDA = JDABuilder.createDefault(System.getenv("DISCORD_TOKEN")).enableIntents(GatewayIntent.MESSAGE_CONTENT).build();

  private static String CHARSET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_.";

  private static HashMap<UUID, String> NAME_CACHE = new HashMap<>();
  private static HashMap<UUID, String> ID_CACHE = new HashMap<>();
  private static Random R = new Random();

  private static String cacheName(UUID uuid, String name, String id) {
    if (name.length() > 16) {
      name = name.substring(0, 16);
    }
    NAME_CACHE.remove(uuid);
    ID_CACHE.remove(uuid);
    if (NAME_CACHE.containsValue(name)) {
      if (name.length() >= 15) {
        name = name.substring(0, 14);
      }
      return cacheName(uuid, name + CHARSET.charAt(R.nextInt(CHARSET.length())) + CHARSET.charAt(R.nextInt(CHARSET.length())), id);
    }
    NAME_CACHE.put(uuid, name);
    ID_CACHE.put(uuid, id);
    return NAME_CACHE.get(uuid);
  }

  public static String getUserData(UUID uuid, boolean force) {
    if (uuid == null)
      return null;
    if (force || !NAME_CACHE.containsKey(uuid)) {
      try {
        URL url = new URL("https://link.neko.rs/whitelist?uuid=" + uuid.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == 200) {
          String[] data = IOUtils.toString(conn.getInputStream(), "UTF-8").split("\n");
          
          String name = data[0];
          String id = data[1];
          
          Member m = DISCORD_CACHE.get(id);
          if (m != null) {
            String fancy = m.getEffectiveName().replaceAll("[^a-zA-Z0-9_.]", "");
            if (fancy.length() > 1) {
              name = fancy;
            }
          }

          return cacheName(uuid, name, id);
        }
      } catch (Exception ex) {
        Bridge.LOGGER.error(ex.getMessage());
        ex.printStackTrace();
      }
      if (!NAME_CACHE.containsKey(uuid)) {
        NAME_CACHE.put(uuid, null);
      }
    }
    return NAME_CACHE.get(uuid);
  }

  private static Object2ObjectOpenHashMap<String, Member> DISCORD_CACHE = new Object2ObjectOpenHashMap<>();

  @Override
  public void onInitializeServer() {
    LOGGER.info("Initializing FSMP Bridge");
    sendSystemText("🟡 Server is starting");
    ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
      sendSystemText("🟢 Server started");
      JDA.addEventListener(new ListenerAdapter() {
        @Override
        public void onGuildReady(@Nonnull GuildReadyEvent e) {
          e.getGuild().loadMembers().onSuccess(l -> {
            for (Member m: l) {
              DISCORD_CACHE.put(m.getId(), m);
            }
          });
        }
        @Override
        public void onMessageReceived(MessageReceivedEvent e) {
          if (e.getAuthor().getIdLong() == WEBHOOK.getId()) return;
          if (e.getChannel().getId().equals(CHANNEL_ID)) {
            String text = "";
            if (e.getMessage().getMessageReference() != null) {
              Message m = e.getMessage().getMessageReference().getMessage();
              if (m != null)
                text += "Replying to " + m.getAuthor().getEffectiveName() + ": ";
            }
            text += EmojiParser.parseToAliases(e.getMessage().getContentDisplay());
            for (Attachment at : e.getMessage().getAttachments()) {
              boolean nsfw = at.getFileName().startsWith("SPOILER_");
              text += " [[CICode,url=" + at.getUrl() + ",name=" + at.getFileName() + ",nsfw=" + nsfw + "]]";
            }
            broadcastMessage(server, e.getMessage().getAuthor().getEffectiveName(), text);
          }
        }
      });
    });
    ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
      sendSystemText("🔴 Server is stopping");
    });
    ServerLifecycleEvents.SERVER_STOPPED.register((server) -> {
      sendSystemText("🛑 Server stopped");
      JDA.shutdown();
    });
    PlayerEvents.PLAYER_JOIN.register((player, firstJoin) -> {
      if (firstJoin) {
        sendSystemEmbed(new EmbedBuilder()
            .setTitle(String.format("%s joined the game", pingOrFallback(player)))
            .setThumbnail(minotar(player, "armor/bust", 128))
            .setColor(0x8BC34A));
      } else {
        sendSystemText("📥 **%s** joined the game (%s)", pingOrFallback(player), getPlayTime(player));
      }
    });
    PlayerEvents.PLAYER_LEFT.register((player, reason) -> {
      sendSystemText("📤 **%s** left the game (%s)", pingOrFallback(player), reason.getString());
    });
    PlayerEvents.PLAYER_MESSAGE.register((player, msg) -> {
      sendPlayerText(player, parseCustom(msg.getContent().getString()));
    });
    PlayerEvents.PLAYER_ADVANCEMENT.register((player, adv) -> {
      AdvancementDisplay disp = adv.getDisplay();
      switch (disp.getFrame()) {
        case TASK:
          sendSystemText("✨ **%s** has made the advancement **[%s]**", pingOrFallback(player),
              disp.getTitle().getString());
          break;
        case CHALLENGE:
          sendSystemText("🎉 %s has completed the challenge **[%s]**", pingOrFallback(player),
              disp.getTitle().getString());
          break;
        case GOAL:
          sendSystemText("🎊 **%s** has reached the goal **[%s]**", pingOrFallback(player),
              disp.getTitle().getString());
          break;
      }
    });
    PlayerEvents.PLAYER_DEATH.register((player, source) -> {
      Text textDM = player.getDamageTracker().getDeathMessage();
      //if (textDM instanceof TranslatableText) {
      //  TranslatableText tt_dm = (TranslatableText) textDM;
      //  List<String> args = Arrays.stream(tt_dm.getArgs()).map(a -> String.format("**%s**", a instanceof Text ? ((Text) a).getString() : a.toString())).collect(Collectors.toList());
      //  textDM = Text.translatable(tt_dm.getKey(), args.toArray());
      //}
      Map<String, UUID> temp = new Object2ObjectArrayMap<>();
      for(Map.Entry<UUID, String> entry : NAME_CACHE.entrySet()) {
        temp.put(entry.getValue(), entry.getKey());
      }

      ObjectArrayList<String> words = new ObjectArrayList<>(textDM.getString().split(" "));
      words.replaceAll(n -> temp.get(n) != null ? pingOrFallback(temp.get(n), n) : n);
      sendSystemText("💀 %s", String.join(" ", words));
    });
  }

  private static String pingOrFallback(UUID uuid, String fallback) {
    String id = ID_CACHE.get(uuid);
    return id != null ? "<@" + id + ">" : "**" + fallback + "**";
  }

  private static String pingOrFallback(ServerPlayerEntity e) {
    return pingOrFallback(e.getUuid(), e.getEntityName());
  }

  private static String getPlayTime(ServerPlayerEntity player) {
    int pt = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)) / 20;
    int months = pt / 2592000;
    int days = pt % 2592000 / 86400;
    int hours = pt % 86400 / 3600;
    int mins = pt % 3600 / 60;
    int secs = pt % 60;

    if (months > 0)
      return i18n(months, "month");
    if (days > 0)
      return i18n(days, "day");
    if (hours > 0)
      return i18n(hours, "hour");
    if (mins > 0)
      return i18n(mins, "min");
    return i18n(secs, "sec");
  }

  private static String i18n(int num, String name) {
    return num + " " + name + (num > 1 ? "s" : "");
  }

  private static WebhookMessageBuilder getPlayerHook(PlayerEntity pe) {
    return new WebhookMessageBuilder().setAllowedMentions(AllowedMentions.none()).setUsername(pe.getEntityName())
        .setAvatarUrl(minotar(pe, "helm", 128));
  }

  private static String minotar(PlayerEntity pe, String type, int size) {
    return String.format("https://minotar.net/%s/%s/%s.png", type, pe.getUuid().toString(), size);
  }

  private static WebhookMessageBuilder getSystemHook() {
    return new WebhookMessageBuilder().setAllowedMentions(AllowedMentions.none());
  }

  private static final Pattern p = Pattern.compile(":\\w+:");

  public static String parseCustom(String in) {
    return p.matcher(in).replaceAll(match -> {
      String input = match.group();
      List<RichCustomEmoji> list = JDA.getEmojisByName(input.replaceAll(":", ""), false);
      if (list.size() > 0)
        return list.get(0).getAsMention();
      return input;
    });
  }

  public static void broadcastMessage(MinecraftServer server, String src, String text) {
    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
      p.sendChatMessage(SentMessage.of(SignedMessage.ofUnsigned(text)), false, MessageType.params(MessageType.CHAT, p.getWorld().getRegistryManager(), Text.of(src)));
    }
  }

  public static void sendPlayerText(PlayerEntity pe, String text) {
    WEBHOOK.send(getPlayerHook(pe).setContent(text).build());
  }

  public static void sendPlayerText(PlayerEntity pe, String format, Object... args) {
    sendPlayerText(pe, String.format(format, args));
  }

  public static void sendSystemEmbed(EmbedBuilder embed) {
    WEBHOOK.send(getSystemHook().addEmbeds(WebhookEmbedBuilder.fromJDA(embed.build()).build()).build());
  }

  public static void sendSystemText(String text) {
    WEBHOOK.send(getSystemHook().setContent(text).build());
  }

  public static void sendSystemText(String format, Object... args) {
    sendSystemText(String.format(format, args));
  }
}
