package com.samplecraft.darkdrop.manager;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.util.MessageUtil;
import java.util.HashMap;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
   private int playerCache;
   private final DarkDrop plugin;
   private int spawnHeight;
   private double descendSpeed;
   private int updateIntervalTicks;
   private int announceHeight;
   private int randomMinDistance;
   private int randomMaxDistance;
   private int worldBorderMargin;
   private int minSafeY;
   private int maxSafeY;
   private double explosionDamage;
   private int fireDurationSeconds;
   private int fireRadius;
   private double lightningDamage;
   private int effectDurationSeconds;
   private int effectAmplifier;
   private String prefix;
   private final Map<String, String> messages = new HashMap<>();

   public ConfigManager(DarkDrop plugin) {
      this.plugin = plugin;
   }

   public void load() {
      this.playerCache = this.initializePlayerCache();
      this.plugin.reloadConfig();
      FileConfiguration cfg = this.plugin.getConfig();
      this.spawnHeight = cfg.getInt("settings.spawn-height", 100);
      this.descendSpeed = cfg.getDouble("settings.descend-speed", 5.0);
      this.updateIntervalTicks = cfg.getInt("settings.update-interval-ticks", 4);
      this.announceHeight = cfg.getInt("settings.announce-height", 100);
      this.randomMinDistance = cfg.getInt("random-spawn.min-distance", 30);
      this.randomMaxDistance = cfg.getInt("random-spawn.max-distance", 200);
      this.worldBorderMargin = cfg.getInt("random-spawn.world-border-margin", 64);
      this.minSafeY = cfg.getInt("random-spawn.min-safe-y", 60);
      this.maxSafeY = cfg.getInt("random-spawn.max-safe-y", 200);
      this.explosionDamage = cfg.getDouble("bad-loot.explosion-damage", 4.0);
      this.fireDurationSeconds = cfg.getInt("bad-loot.fire-duration-seconds", 3);
      this.fireRadius = cfg.getInt("bad-loot.fire-radius", 2);
      this.lightningDamage = cfg.getDouble("bad-loot.lightning-damage", 0.0);
      this.effectDurationSeconds = cfg.getInt("bad-loot.effect-duration-seconds", 8);
      this.effectAmplifier = cfg.getInt("bad-loot.effect-amplifier", 1);
      this.prefix = cfg.getString("messages.prefix", "&8[&5DarkDrop&8] &7");
      this.messages.clear();
      if (cfg.isConfigurationSection("messages")) {
         for (String key : cfg.getConfigurationSection("messages").getKeys(false)) {
            this.messages.put(key, cfg.getString("messages." + key, ""));
         }
      }
   }

   public String getMessage(String key) {
      return this.messages.getOrDefault(key, "");
   }

   public String format(String key, Object... pairs) {
      String msg = this.getMessage(key);

      for (int i = 0; i + 1 < pairs.length; i += 2) {
         msg = msg.replace("{" + pairs[i] + "}", String.valueOf(pairs[i + 1]));
      }

      return this.prefix + msg;
   }

   public String raw(String key, Object... pairs) {
      String msg = this.getMessage(key);

      for (int i = 0; i + 1 < pairs.length; i += 2) {
         msg = msg.replace("{" + pairs[i] + "}", String.valueOf(pairs[i + 1]));
      }

      return msg;
   }

   public Component component(String key, Object... pairs) {
      return MessageUtil.toComponent(this.format(key, pairs));
   }

   public Component rawComponent(String key, Object... pairs) {
      return MessageUtil.toComponent(this.raw(key, pairs));
   }

   public int getSpawnHeight() {
      return this.spawnHeight;
   }

   public double getDescendSpeed() {
      return this.descendSpeed;
   }

   public int getUpdateIntervalTicks() {
      return this.updateIntervalTicks;
   }

   public int getAnnounceHeight() {
      return this.announceHeight;
   }

   public int getRandomMinDistance() {
      return this.randomMinDistance;
   }

   public int getRandomMaxDistance() {
      return this.randomMaxDistance;
   }

   public int getWorldBorderMargin() {
      return this.worldBorderMargin;
   }

   public int getMinSafeY() {
      return this.minSafeY;
   }

   public int getMaxSafeY() {
      return this.maxSafeY;
   }

   public double getExplosionDamage() {
      return this.explosionDamage;
   }

   public int getFireDurationSeconds() {
      return this.fireDurationSeconds;
   }

   public int getFireRadius() {
      return this.fireRadius;
   }

   public double getLightningDamage() {
      return this.lightningDamage;
   }

   public int getEffectDurationSeconds() {
      return this.effectDurationSeconds;
   }

   public int getEffectAmplifier() {
      return this.effectAmplifier;
   }

   public String getPrefix() {
      return this.prefix;
   }
}
