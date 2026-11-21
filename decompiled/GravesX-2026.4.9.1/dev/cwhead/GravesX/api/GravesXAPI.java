package dev.cwhead.GravesX.api;

import com.ranull.graves.Graves;
import dev.cwhead.GravesX.api.addon.AddonAPI;
import dev.cwhead.GravesX.api.grave.GraveCreationAPI;
import dev.cwhead.GravesX.api.grave.GraveManagementAPI;
import dev.cwhead.GravesX.api.inventory.InventoryAPI;
import dev.cwhead.GravesX.api.permission.PermissionAPI;
import dev.cwhead.GravesX.api.skin.SkinAPI;
import dev.cwhead.GravesX.api.util.UtilAPI;
import dev.cwhead.GravesX.api.world.LocationAPI;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Experimental;

public class GravesXAPI {
   private final Graves plugin;
   public final GraveCreationAPI gravesCreate;
   public final GraveManagementAPI gravesManage;
   public final LocationAPI world;
   public final InventoryAPI inventory;
   public final SkinAPI skin;
   public final AddonAPI addon;
   public final PermissionAPI permission;
   public final UtilAPI util;

   public GravesXAPI(@NotNull Graves plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.world = new LocationAPI(plugin);
      this.util = new UtilAPI(plugin, this.world);
      this.inventory = new InventoryAPI(plugin, this.util);
      this.skin = new SkinAPI();
      this.addon = new AddonAPI(plugin);
      this.permission = new PermissionAPI(plugin);
      this.gravesManage = new GraveManagementAPI(plugin);
      this.gravesCreate = new GraveCreationAPI(plugin, this.world, this.util, this.gravesManage);
   }

   @NotNull
   public LocationAPI getLocationAPI() {
      return this.world;
   }

   @NotNull
   public UtilAPI getUtilAPI() {
      return this.util;
   }

   @NotNull
   public InventoryAPI getInventoryAPI() {
      return this.inventory;
   }

   @NotNull
   public SkinAPI getSkinAPI() {
      return this.skin;
   }

   @NotNull
   public AddonAPI getAddonAPI() {
      return this.addon;
   }

   @NotNull
   public PermissionAPI getPermissionAPI() {
      return this.permission;
   }

   @NotNull
   public GraveManagementAPI getGravesManagementAPI() {
      return this.gravesManage;
   }

   @NotNull
   public GraveCreationAPI getGravesCreationAPI() {
      return this.gravesCreate;
   }

   @Experimental
   @NotNull
   public Graves plugin() {
      return this.plugin;
   }
}
