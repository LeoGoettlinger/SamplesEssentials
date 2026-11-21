package com.artillexstudios.axafkzone.zones;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.config.Config;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axafkzone.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axafkzone.libs.axapi.packet.wrapper.clientbound.ClientboundClearTitlesWrapper;
import com.artillexstudios.axafkzone.libs.axapi.serializers.Serializers;
import com.artillexstudios.axafkzone.libs.axapi.utils.ActionBar;
import com.artillexstudios.axafkzone.libs.axapi.utils.BossBar;
import com.artillexstudios.axafkzone.libs.axapi.utils.Cooldown;
import com.artillexstudios.axafkzone.libs.axapi.utils.MessageUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.Title;
import com.artillexstudios.axafkzone.reward.Reward;
import com.artillexstudios.axafkzone.selection.Region;
import com.artillexstudios.axafkzone.utils.RandomUtils;
import com.artillexstudios.axafkzone.utils.TimeUtils;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class Zone {
   private final ConcurrentHashMap<Player, Integer> zonePlayers = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Player, BossBar> bossbars = new ConcurrentHashMap<>();
   private final LinkedList<Reward> rewards = new LinkedList<>();
   private final Cooldown<Player> cooldown = Cooldown.createSynchronized();
   private final MessageUtils msg;
   private final String name;
   private final Config settings;
   private Region region;
   private int ticks = 0;
   private int rewardSeconds;
   private int rollAmount;

   public Zone(String name, Config settings) {
      this.name = name;
      this.settings = settings;
      this.msg = new MessageUtils(settings.getBackingDocument(), "prefix", AxAFKZone.CONFIG.getBackingDocument());
      this.reload();
   }

   public void tick() {
      boolean runChecks = ++this.ticks % 20 == 0;
      Set<Player> players = this.region.getPlayersInZone();
      Iterator<Entry<Player, Integer>> it = this.zonePlayers.entrySet().iterator();

      while (it.hasNext()) {
         Player player = it.next().getKey();
         if (!player.isOnline()) {
            players.remove(player);
            this.leave(player, it);
         } else if (!players.contains(player)) {
            this.leave(player, it);
         } else {
            if (runChecks) {
               int newTime = this.zonePlayers.get(player) + 1;
               this.zonePlayers.put(player, newTime);
               if (newTime != 0 && newTime % this.rewardSeconds == 0) {
                  this.giveRewards(player, newTime);
                  if (AxAFKZone.CONFIG.getBoolean("reset-after-reward", false)) {
                     this.zonePlayers.put(player, 0);
                  }
               }

               this.sendTitle(player);
               this.sendActionbar(player);
               this.updateBossbar(player);
            }

            players.remove(player);
         }
      }

      int ipLimit = AxAFKZone.CONFIG.getInt("zone-per-ip-limit", -1);

      for (Player player : players) {
         if (!this.cooldown.hasCooldown(player)) {
            if (ipLimit != -1 && this.countIPAccounts(player) >= ipLimit) {
               AxAFKZone.MESSAGEUTILS.sendLang(player, "zone.ip-limit");
               this.cooldown.addCooldown(player, 3000L);
            } else {
               this.enter(player);
            }
         }
      }
   }

   private int countIPAccounts(Player player) {
      InetSocketAddress address = player.getAddress();
      int count = 0;
      if (address == null) {
         return count;
      } else if (player.hasPermission("axafkzone.bypass.iplimit")) {
         return count;
      } else {
         for (Player pl : this.zonePlayers.keySet()) {
            if (!pl.hasPermission("axafkzone.bypass.iplimit")) {
               InetSocketAddress address2 = pl.getAddress();
               if (address2 != null && address2.getAddress().equals(address.getAddress())) {
                  count++;
               }
            }
         }

         return count;
      }
   }

   private void enter(Player player) {
      BossBar bossBar = this.bossbars.remove(player);
      if (bossBar != null) {
         bossBar.remove();
      }

      this.msg.sendLang(player, "messages.entered", Map.of("%time%", TimeUtils.fancyTime(this.rewardSeconds * 1000L)));
      this.zonePlayers.put(player, 0);
      Section section;
      if ((section = this.settings.getSection("in-zone.bossbar")) != null) {
         bossBar = BossBar.create(
            StringUtils.format(section.getString("name").replace("%time%", TimeUtils.fancyTime(this.timeUntilNext(player)))),
            1.0F,
            BossBar.Color.valueOf(section.getString("color").toUpperCase()),
            BossBar.Style.parse(section.getString("style"))
         );
         bossBar.show(player);
         this.bossbars.put(player, bossBar);
      }

      this.sendTitle(player);
      this.sendActionbar(player);
   }

   private void leave(Player player, Iterator<Entry<Player, Integer>> it) {
      if (player.isOnline()) {
         this.msg.sendLang(player, "messages.left", Map.of("%time%", TimeUtils.fancyTime(this.zonePlayers.get(player).intValue() * 1000L)));
      }

      it.remove();
      BossBar bossBar = this.bossbars.remove(player);
      if (bossBar != null) {
         bossBar.remove();
      }

      this.removeTitle(player);
   }

   private void sendTitle(Player player) {
      String zoneTitle = this.settings.getString("in-zone.title", null);
      String zoneSubTitle = this.settings.getString("in-zone.subtitle", null);
      if (zoneTitle != null && !zoneTitle.isBlank() || zoneSubTitle != null && !zoneSubTitle.isBlank()) {
         Title title = Title.create(
            (Component)(zoneTitle == null
               ? Component.empty()
               : StringUtils.format(zoneTitle.replace("%time%", TimeUtils.fancyTime(this.timeUntilNext(player))))),
            (Component)(zoneSubTitle == null
               ? Component.empty()
               : StringUtils.format(zoneSubTitle.replace("%time%", TimeUtils.fancyTime(this.timeUntilNext(player))))),
            0,
            25,
            0
         );
         title.send(player);
      }
   }

   private void removeTitle(Player player) {
      ServerPlayerWrapper wrapper = ServerPlayerWrapper.wrap(player);
      wrapper.sendPacket(new ClientboundClearTitlesWrapper(true));
   }

   private void sendActionbar(Player player) {
      String zoneActionbar = this.settings.getString("in-zone.actionbar", null);
      if (zoneActionbar != null && !zoneActionbar.isBlank()) {
         ActionBar.send(player, StringUtils.format(zoneActionbar.replace("%time%", TimeUtils.fancyTime(this.timeUntilNext(player)))));
      }
   }

   private void updateBossbar(Player player) {
      BossBar bossBar = this.bossbars.get(player);
      if (bossBar != null) {
         Integer time = this.zonePlayers.get(player);
         if (time != null) {
            int barDirection = AxAFKZone.CONFIG.getInt("bossbar-direction", 0);
            float calculated = (float)(time % this.rewardSeconds) / (this.rewardSeconds - 1);
            bossBar.progress(Math.clamp(barDirection == 0 ? 1.0F - calculated : calculated, 0.0F, 1.0F));
            Section section;
            if ((section = this.settings.getSection("in-zone.bossbar")) != null) {
               bossBar.title(StringUtils.format(section.getString("name").replace("%time%", TimeUtils.fancyTime(this.timeUntilNext(player)))));
            }
         }
      }
   }

   private void giveRewards(Player player, int newTime) {
      List<Reward> rewardList = this.rollAndGiveRewards(player);
      if (!rewardList.isEmpty()) {
         if (!this.settings.getStringList("messages.reward").isEmpty()) {
            String prefix = AxAFKZone.CONFIG.getString("prefix");
            boolean first = true;

            for (String string : this.settings.getStringList("messages.reward")) {
               if (first) {
                  string = prefix + string;
                  first = false;
               }

               if (string.contains("%reward%")) {
                  for (Reward reward : rewardList) {
                     player.sendMessage(
                        StringUtils.formatToString(
                           string, Map.of("%reward%", Optional.ofNullable(reward.getDisplay()).orElse("---"), "%time%", TimeUtils.fancyTime(newTime * 1000L))
                        )
                     );
                  }
               } else {
                  player.sendMessage(StringUtils.formatToString(string, Map.of("%time%", TimeUtils.fancyTime(newTime * 1000L))));
               }
            }
         }
      }
   }

   public long timeUntilNext(Player player) {
      Integer time = this.zonePlayers.get(player);
      return time == null ? -1L : this.rewardSeconds * 1000L - time % this.rewardSeconds * 1000L;
   }

   public List<Reward> rollAndGiveRewards(Player player) {
      List<Reward> rewardList = new ArrayList<>();
      if (this.rewards.isEmpty()) {
         return rewardList;
      } else {
         int time = this.zonePlayers.getOrDefault(player, 0);
         Map<Reward, Double> chances = new HashMap<>();

         for (Reward reward : this.rewards) {
            String permission = reward.getPermission();
            if ((permission == null || permission.isBlank() || player.hasPermission(permission))
               && time >= reward.getMinimumTime()
               && time <= reward.getMaximumTime()) {
               chances.put(reward, reward.getChance());
            }
         }

         if (chances.isEmpty()) {
            return rewardList;
         } else {
            for (int i = 0; i < this.rollAmount; i++) {
               Reward sel = RandomUtils.randomValue(chances);
               rewardList.add(sel);
               sel.run(player);
            }

            return rewardList;
         }
      }
   }

   public boolean reload() {
      if (!this.settings.reload()) {
         return false;
      } else {
         this.region = new Region(
            Serializers.LOCATION.deserialize(this.settings.getString("zone.location1")),
            Serializers.LOCATION.deserialize(this.settings.getString("zone.location2")),
            this
         );
         this.rewardSeconds = this.settings.getInt("reward-time-seconds", 180);
         this.rollAmount = this.settings.getInt("roll-amount", 1);
         this.rewards.clear();

         for (Map<Object, Object> map : this.settings.getMapList("rewards")) {
            Reward reward = new Reward(map);
            this.rewards.add(reward);
         }

         return true;
      }
   }

   public void disable() {
      for (BossBar bossBar : this.bossbars.values()) {
         bossBar.remove();
      }
   }

   public void setRegion(Region region) {
      this.region = region;
      this.settings.set("zone.location1", Serializers.LOCATION.serialize(region.getCorner1()));
      this.settings.set("zone.location2", Serializers.LOCATION.serialize(region.getCorner2()));
      this.settings.save();
   }

   public String getName() {
      return this.name;
   }

   public Config getSettings() {
      return this.settings;
   }

   public Region getRegion() {
      return this.region;
   }

   public int getTicks() {
      return this.ticks;
   }
}
