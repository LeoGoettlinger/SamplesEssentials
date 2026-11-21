package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.lib.xseries.XEntityType;
import de.theredend2000.advancedhunt.platform.PlatformAccess;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkManager {
   private final Main plugin;
   private final Set<UUID> fireworkUUIDs;
   private final Random random;

   public FireworkManager(Main plugin) {
      this.plugin = plugin;
      this.fireworkUUIDs = ConcurrentHashMap.newKeySet();
      this.random = plugin.getRandom();
   }

   public void spawnFireworkRocket(Location location) {
      if (this.plugin.getConfig().getBoolean("fireworks.enabled", true)) {
         if (location.getWorld() != null) {
            Firework firework = (Firework)location.getWorld().spawnEntity(location, Objects.requireNonNull(XEntityType.FIREWORK_ROCKET.get()));
            boolean silent = this.plugin.getConfig().getBoolean("fireworks.silent", false);
            PlatformAccess.get().setFireworkSilent(firework, silent);
            FireworkMeta meta = firework.getFireworkMeta();
            meta.setPower(this.getRandomPower());
            meta.addEffect(this.buildEffect());
            firework.setFireworkMeta(meta);
            UUID fireworkId = firework.getUniqueId();
            this.fireworkUUIDs.add(fireworkId);
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.fireworkUUIDs.remove(fireworkId), 100L);
         }
      }
   }

   private FireworkEffect buildEffect() {
      ConfigurationSection effectsSec = this.plugin.getConfig().getConfigurationSection("fireworks.effects");
      if (effectsSec == null) {
         return FireworkEffect.builder().flicker(true).trail(true).with(Type.BALL).withColor(Collections.singletonList(Color.WHITE)).build();
      } else {
         Builder builder = FireworkEffect.builder()
            .flicker(effectsSec.getBoolean("flicker", true))
            .trail(effectsSec.getBoolean("trail", true))
            .with(this.getRandomType(effectsSec));
         builder.withColor(this.getColors("fireworks.colors"));
         if (this.plugin.getConfig().getBoolean("fireworks.fade-colors.enabled", true)) {
            builder.withFade(this.getColors("fireworks.fade-colors"));
         }

         return builder.build();
      }
   }

   private int getRandomPower() {
      int min = this.plugin.getConfig().getInt("fireworks.power.min", 1);
      int max = this.plugin.getConfig().getInt("fireworks.power.max", 2);
      return this.random.nextInt(Math.max(1, max - min + 1)) + min;
   }

   private Type getRandomType(ConfigurationSection effectsSec) {
      List<String> types = effectsSec.getStringList("types");
      if (types.isEmpty()) {
         return Type.BALL;
      } else {
         String randomType = types.get(this.random.nextInt(types.size()));

         try {
            return Type.valueOf(randomType.toUpperCase());
         } catch (IllegalArgumentException var5) {
            this.plugin.getLogger().warning("Invalid typ: " + randomType + " - using BALL!");
            return Type.BALL;
         }
      }
   }

   private List<Color> getColors(String path) {
      ConfigurationSection colorSec = this.plugin.getConfig().getConfigurationSection(path);
      if (colorSec == null) {
         return Collections.singletonList(Color.WHITE);
      } else {
         List<String> colorNames = colorSec.getStringList("list");
         List<Color> colors = new ArrayList<>();

         for (String colorName : colorNames) {
            try {
               Color color = (Color)Color.class.getField(colorName.toUpperCase()).get(null);
               colors.add(color);
            } catch (Exception var8) {
               this.plugin.getLogger().warning("Invalid color: " + colorName + " - skipped!");
            }
         }

         if (colors.isEmpty()) {
            colors.add(Color.WHITE);
         }

         return Collections.singletonList(colors.get(this.random.nextInt(colors.size())));
      }
   }

   public Set<UUID> getFireworkUUIDs() {
      return this.fireworkUUIDs;
   }
}
