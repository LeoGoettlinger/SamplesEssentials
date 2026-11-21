package ch.njol.skript.bukkitutil;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Task;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public abstract class PlayerUtils {
   private static final Set<Player> inventoryUpdateList = Collections.synchronizedSet(new HashSet<>());

   public static void updateInventory(@Nullable Player player) {
      if (player != null) {
         inventoryUpdateList.add(player);
      }
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public static Collection<? extends Player> getOnlinePlayers() {
      return ImmutableList.copyOf(Bukkit.getOnlinePlayers());
   }

   public static boolean canEat(Player p, Material food) {
      GameMode gm = p.getGameMode();
      if (gm != GameMode.CREATIVE && gm != GameMode.SPECTATOR) {
         boolean edible = food.isEdible();
         if (!edible) {
            return false;
         } else {
            return p.getFoodLevel() < 20 || switch (food) {
               case GOLDEN_APPLE, CHORUS_FRUIT -> true;
               default -> false;
            };
         }
      } else {
         return false;
      }
   }

   public static int getLevelXP(int level) {
      if (level <= 15) {
         return 2 * level + 7;
      } else {
         return level <= 30 ? 5 * level - 38 : 9 * level - 158;
      }
   }

   public static int getCumulativeXP(int level) {
      if (level <= 15) {
         return level * level + 6 * level;
      } else {
         return level <= 30 ? (int)(2.5 * (level * level) - 40.5 * level + 360.0) : (int)(4.5 * (level * level) - 162.5 * level + 2220.0);
      }
   }

   public static int getTotalXP(int level, double progress) {
      return (int)(getCumulativeXP(level) + getLevelXP(level) * progress);
   }

   public static int getTotalXP(Player player) {
      return getTotalXP(player.getLevel(), player.getExp());
   }

   public static void setTotalXP(Player player, int experience) {
      int level = 0;
      if (experience < 0) {
         experience = 0;
      }

      while (experience >= getLevelXP(level)) {
         experience -= getLevelXP(level);
         level++;
      }

      player.setLevel(level);
      player.setExp((float)experience / getLevelXP(level));
   }

   static {
      new Task(Skript.getInstance(), 1L, 1L) {
         @Override
         public void run() {
            for (Player p : PlayerUtils.inventoryUpdateList) {
               p.updateInventory();
            }

            PlayerUtils.inventoryUpdateList.clear();
         }
      };
   }
}
