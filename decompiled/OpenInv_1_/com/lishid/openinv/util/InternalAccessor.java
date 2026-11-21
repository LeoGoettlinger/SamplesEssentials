package com.lishid.openinv.util;

import com.github.jikoo.openinv.lib.planarwrappers.util.version.BukkitVersions;
import com.github.jikoo.openinv.lib.planarwrappers.util.version.Version;
import com.lishid.openinv.internal.Accessor;
import com.lishid.openinv.internal.IAnySilentContainer;
import com.lishid.openinv.internal.ISpecialEnderChest;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.internal.ISpecialPlayerInventory;
import com.lishid.openinv.internal.PlayerManager;
import com.lishid.openinv.util.lang.LanguageManager;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InternalAccessor {
   private static final boolean PAPER;
   @Nullable
   private Accessor internal;

   public InternalAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
      try {
         this.internal = this.getAccessor(logger, lang);
         if (this.internal != null) {
            InventoryAccess.setProvider(this.internal::get);
         }
      } catch (Exception | NoClassDefFoundError var4) {
         this.internal = null;
         InventoryAccess.setProvider(null);
      }
   }

   @Nullable
   private Accessor getAccessor(@NotNull Logger logger, @NotNull LanguageManager lang) {
      if (!PAPER) {
         if (BukkitVersions.MINECRAFT.equals(Version.of(26, 2))) {
            return new com.github.jikoo.openinv.internal.spigot26_2.InternalAccessor(logger, lang);
         } else {
            return BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 1)) && BukkitVersions.MINECRAFT.lessThanOrEqual(Version.of(26, 1, 2))
               ? new com.github.jikoo.openinv.internal.spigot26_1.InternalAccessor(logger, lang)
               : null;
         }
      } else {
         Version maxSupported = Version.of(26, 2);
         Version minSupported = Version.of(1, 21, 9);
         if (!BukkitVersions.MINECRAFT.greaterThan(maxSupported) && !BukkitVersions.MINECRAFT.lessThan(minSupported)) {
            if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 2))) {
               return new com.lishid.openinv.internal.paper26_2.InternalAccessor(logger, lang);
            } else if (BukkitVersions.MINECRAFT.greaterThanOrEqual(Version.of(26, 1))) {
               return new com.lishid.openinv.internal.paper26_1.InternalAccessor(logger, lang);
            } else {
               return (Accessor)(BukkitVersions.MINECRAFT.equals(Version.of(1, 21, 11))
                  ? new com.lishid.openinv.internal.paper1_21_11.InternalAccessor(logger, lang)
                  : new com.lishid.openinv.internal.paper1_21_10.InternalAccessor(logger, lang));
            }
         } else {
            return null;
         }
      }
   }

   public void reload(ConfigurationSection config) {
      if (this.internal != null) {
         this.internal.reload(config);
      }
   }

   @NotNull
   public String getVersion() {
      return BukkitVersions.MINECRAFT.toString();
   }

   public boolean isSupported() {
      return this.internal != null;
   }

   @NotNull
   public IAnySilentContainer getAnySilentContainer() {
      if (this.internal == null) {
         throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
      } else {
         return this.internal.getAnySilentContainer();
      }
   }

   @Nullable
   public InventoryView openInventory(@NotNull Player player, @NotNull ISpecialInventory inventory, boolean viewOnly) {
      if (this.internal == null) {
         throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
      } else {
         return this.internal.getPlayerManager().openInventory(player, inventory, viewOnly);
      }
   }

   @NotNull
   PlayerManager getPlayerDataManager() {
      if (this.internal == null) {
         throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
      } else {
         return this.internal.getPlayerManager();
      }
   }

   ISpecialEnderChest createEnderChest(Player player) {
      if (this.internal == null) {
         throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
      } else {
         return this.internal.createEnderChest(player);
      }
   }

   ISpecialPlayerInventory createInventory(Player player) {
      if (this.internal == null) {
         throw new IllegalStateException(String.format("Unsupported server version %s!", BukkitVersions.MINECRAFT));
      } else {
         return this.internal.createPlayerInventory(player);
      }
   }

   static {
      boolean paper = false;

      try {
         Class.forName("io.papermc.paper.configuration.GlobalConfiguration");
         paper = true;
      } catch (ClassNotFoundException var2) {
      }

      PAPER = paper;
   }
}
