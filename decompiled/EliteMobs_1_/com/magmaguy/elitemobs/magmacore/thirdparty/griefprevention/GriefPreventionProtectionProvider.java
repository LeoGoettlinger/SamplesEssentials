package com.magmaguy.elitemobs.magmacore.thirdparty.griefprevention;

import com.magmaguy.elitemobs.magmacore.location.RegionProtectionProvider;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GriefPreventionProtectionProvider implements RegionProtectionProvider {
   private final Object dataStore;
   private final MethodHandle getClaimAt;
   private final MethodHandle allowBuild;

   public GriefPreventionProtectionProvider() throws ReflectiveOperationException {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("GriefPrevention");
      if (plugin == null) {
         throw new IllegalStateException("GriefPrevention is not loaded");
      } else {
         Field dataStoreField = plugin.getClass().getField("dataStore");
         this.dataStore = dataStoreField.get(plugin);
         if (this.dataStore == null) {
            throw new IllegalStateException("GriefPrevention dataStore is null");
         } else {
            Class<?> claimClass = Class.forName("me.ryanhamshire.GriefPrevention.Claim");
            MethodType type = MethodType.methodType(claimClass, Location.class, boolean.class, claimClass);
            this.getClaimAt = MethodHandles.lookup().findVirtual(this.dataStore.getClass(), "getClaimAt", type);
            this.allowBuild = MethodHandles.lookup().findVirtual(claimClass, "allowBuild", MethodType.methodType(String.class, Player.class, Material.class));
         }
      }
   }

   @Override
   public boolean isProtected(Location location) {
      try {
         Object claim = (Object)this.getClaimAt.invoke((Object)this.dataStore, (Location)location, (boolean)true, (Void)null);
         return claim != null;
      } catch (Throwable var3) {
         throw new IllegalStateException("GriefPrevention claim lookup failed", var3);
      }
   }

   @Override
   public boolean canBuild(Player player, Location location) {
      try {
         Object claim = (Object)this.getClaimAt.invoke((Object)this.dataStore, (Location)location, (boolean)true, (Void)null);
         return claim == null || (Object)this.allowBuild.invoke((Object)claim, (Player)player, (Material)Material.STONE) == null;
      } catch (Throwable var4) {
         return false;
      }
   }

   @Override
   public String providerName() {
      return "GriefPrevention";
   }
}
