package com.skyblockexp.ezrtp.config.gui;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public final class GuiWorldOption {
   private final RandomTeleportSettings settings;
   private final ItemStack iconTemplate;
   private final int slot;
   private final String permission;
   private final boolean requireCacheEnabled;
   private final int minimumCached;
   private final String rawDisplayName;
   private final List<String> rawLore;

   GuiWorldOption(
      RandomTeleportSettings settings,
      ItemStack iconTemplate,
      int slot,
      String permission,
      boolean requireCacheEnabled,
      int minimumCached,
      String rawDisplayName,
      List<String> rawLore
   ) {
      this.settings = settings;
      this.iconTemplate = iconTemplate;
      this.slot = slot;
      this.permission = permission == null ? "" : permission;
      this.requireCacheEnabled = requireCacheEnabled;
      this.minimumCached = minimumCached;
      this.rawDisplayName = rawDisplayName;
      this.rawLore = rawLore != null ? Collections.unmodifiableList(new ArrayList<>(rawLore)) : Collections.emptyList();
   }

   public RandomTeleportSettings getSettings() {
      return this.settings;
   }

   public ItemStack createIcon() {
      return this.iconTemplate.clone();
   }

   public int getSlot() {
      return this.slot;
   }

   public String getPermission() {
      return this.permission;
   }

   public boolean isRequireCacheEnabled() {
      return this.requireCacheEnabled;
   }

   public int getMinimumCached() {
      return this.minimumCached;
   }

   public String getRawDisplayName() {
      return this.rawDisplayName;
   }

   public List<String> getRawLore() {
      return this.rawLore;
   }
}
