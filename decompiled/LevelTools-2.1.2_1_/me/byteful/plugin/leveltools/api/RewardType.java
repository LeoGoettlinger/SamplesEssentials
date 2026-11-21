package me.byteful.plugin.leveltools.api;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import me.byteful.plugin.leveltools.api.item.LevelToolsItem;
import me.byteful.plugin.leveltools.libs.xseries.XEnchantment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public enum RewardType {
   COMMAND("command", false) {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         Bukkit.dispatchCommand(
            Bukkit.getConsoleSender(),
            String.join(" ", Arrays.copyOfRange(split, 1, split.length)).replace("{player}", player.getName()).replace("%player%", player.getName())
         );
      }
   },
   PLAYER_COMMAND("player-command", false) {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         player.chat(
            "/" + String.join(" ", Arrays.copyOfRange(split, 1, split.length)).replace("{player}", player.getName()).replace("%player%", player.getName())
         );
      }
   },
   PLAYER_OPCOMMAND("player-opcommand", false) {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         boolean isOP = player.isOp();

         try {
            if (!isOP) {
               player.setOp(true);
            }

            PLAYER_COMMAND.apply(tool, split, player);
         } finally {
            player.setOp(isOP);
         }
      }
   },
   ENCHANT("enchant") {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         if (split.length >= 3) {
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);
            if (enchant.isPresent() && NumberUtils.isNumber(split[2])) {
               tool.enchant(enchant.get().getEnchant(), Integer.parseInt(split[2]));
            }
         }
      }
   },
   ENCHANT_2("enchant2") {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         if (split.length >= 3) {
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);
            if (NumberUtils.isNumber(split[2])) {
               int level = Integer.parseInt(split[2]);
               if (enchant.isPresent() && tool.getItemStack().getEnchantmentLevel(Objects.requireNonNull(enchant.get().getEnchant())) < level) {
                  tool.enchant(enchant.get().getEnchant(), level);
               }
            }
         }
      }
   },
   ENCHANT_3("enchant3") {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         if (split.length >= 3) {
            Optional<XEnchantment> enchant = XEnchantment.matchXEnchantment(split[1]);
            if (NumberUtils.isNumber(split[2])) {
               int level = Integer.parseInt(split[2]);
               if (enchant.isPresent()) {
                  int currentLvl = tool.getItemStack().getEnchantmentLevel(Objects.requireNonNull(enchant.get().getEnchant()));
                  tool.enchant(enchant.get().getEnchant(), currentLvl + level);
               }
            }
         }
      }
   },
   ATTRIBUTE("attribute") {
      @Override
      public void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player) {
         if (split.length >= 3) {
            String attribute = split[1];
            if (NumberUtils.isNumber(split[2])) {
               double modifier = Double.parseDouble(split[2]);
               if (StringUtils.countMatches(attribute, "_") >= 2) {
                  attribute = attribute.toLowerCase(Locale.ROOT).replaceFirst("_+", ".").trim();
               }

               tool.modifyAttribute(attribute, modifier);
            }
         }
      }
   };

   @NotNull
   private final String configKey;
   private final boolean shouldUpdate;

   private RewardType(@NotNull String configKey) {
      this.configKey = configKey;
      this.shouldUpdate = true;
   }

   private RewardType(@NotNull String configKey, boolean shouldUpdate) {
      this.configKey = configKey;
      this.shouldUpdate = shouldUpdate;
   }

   @NotNull
   public static Optional<RewardType> fromConfigKey(@NotNull String configKey) {
      for (RewardType value : values()) {
         if (value.configKey.equals(configKey)) {
            return Optional.of(value);
         }
      }

      return Optional.empty();
   }

   public abstract void apply(@NotNull LevelToolsItem tool, @NotNull String[] split, @NotNull Player player);

   @NotNull
   public String getConfigKey() {
      return this.configKey;
   }

   public boolean isShouldUpdate() {
      return this.shouldUpdate;
   }
}
