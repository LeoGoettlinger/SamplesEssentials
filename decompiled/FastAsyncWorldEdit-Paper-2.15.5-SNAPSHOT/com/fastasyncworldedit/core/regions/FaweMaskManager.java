package com.fastasyncworldedit.core.regions;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.entity.Player;
import java.util.Locale;

public abstract class FaweMaskManager {
   private final String key;

   public FaweMaskManager(final String plugin) {
      this.key = plugin.toLowerCase(Locale.ROOT);
   }

   public String getKey() {
      return this.key;
   }

   @Override
   public String toString() {
      return this.key;
   }

   public abstract FaweMask getMask(final Player player, FaweMaskManager.MaskType type, boolean isWhitelist);

   public FaweMask getMask(final Player player, FaweMaskManager.MaskType type, boolean isWhitelist, boolean notify) {
      return this.getMask(player, type, isWhitelist);
   }

   public boolean isExclusive() {
      return Settings.settings().REGION_RESTRICTIONS_OPTIONS.EXCLUSIVE_MANAGERS.contains(this.key);
   }

   public static enum MaskType {
      OWNER,
      MEMBER;

      public static FaweMaskManager.MaskType getDefaultMaskType() {
         try {
            return valueOf(Settings.settings().REGION_RESTRICTIONS_OPTIONS.MODE.toUpperCase(Locale.ROOT));
         } catch (Exception var1) {
            return MEMBER;
         }
      }
   }
}
