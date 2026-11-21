package me.chancesd.pvpmanager.sdutils.command;

import me.chancesd.pvpmanager.sdutils.utils.NCDuration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandArgument {
   private final String name;
   private final String value;
   private Object loadedValue;
   private Throwable loadError;

   public CommandArgument(String name, String value) {
      this.name = name;
      this.value = value;
   }

   public String getName() {
      return this.name;
   }

   public String getValue() {
      return this.value;
   }

   public int getAsInt() {
      try {
         return Integer.parseInt(this.value);
      } catch (NumberFormatException var2) {
         throw new IllegalStateException("Not a valid integer: " + this.value);
      }
   }

   public double getAsDouble() {
      try {
         return Double.parseDouble(this.value);
      } catch (NumberFormatException var2) {
         throw new IllegalStateException("Not a valid number: " + this.value);
      }
   }

   public boolean getAsBoolean() {
      if ("true".equalsIgnoreCase(this.value) || "yes".equalsIgnoreCase(this.value) || "on".equalsIgnoreCase(this.value)) {
         return true;
      } else if (!"false".equalsIgnoreCase(this.value) && !"no".equalsIgnoreCase(this.value) && !"off".equalsIgnoreCase(this.value)) {
         throw new IllegalStateException("Not a valid boolean: " + this.value);
      } else {
         return false;
      }
   }

   public boolean isWildcard() {
      return "*".equals(this.value);
   }

   public boolean isAllPlayers() {
      return this.isWildcard();
   }

   public Player getAsPlayer() {
      return Bukkit.getPlayer(this.value);
   }

   public Player getAsPlayerOrWildcard() {
      return this.isWildcard() ? null : Bukkit.getPlayer(this.value);
   }

   public OfflinePlayer getAsOfflinePlayer() {
      Player onlinePlayer = Bukkit.getPlayer(this.value);
      return (OfflinePlayer)(onlinePlayer != null ? onlinePlayer : Bukkit.getOfflinePlayer(this.value));
   }

   public World getAsWorld() {
      World world = Bukkit.getWorld(this.value);
      if (world == null) {
         throw new IllegalStateException("World not found: " + this.value);
      } else {
         return world;
      }
   }

   public Material getAsMaterial() {
      try {
         return Material.valueOf(this.value.toUpperCase());
      } catch (IllegalArgumentException var2) {
         throw new IllegalStateException("Invalid material: " + this.value);
      }
   }

   public NCDuration getAsDuration() {
      try {
         return NCDuration.parseDuration(this.value);
      } catch (IllegalArgumentException var2) {
         throw new IllegalStateException(var2.getMessage(), var2);
      }
   }

   public <T> T get() {
      if (this.loadedValue != null) {
         return (T)this.loadedValue;
      } else {
         throw new IllegalStateException("No loaded value for argument: " + this.name);
      }
   }

   void setLoadedValue(Object value) {
      this.loadedValue = value;
   }

   void setLoadError(Throwable error) {
      this.loadError = error;
   }

   boolean hasLoadError() {
      return this.loadError != null;
   }

   Throwable getLoadError() {
      return this.loadError;
   }
}
