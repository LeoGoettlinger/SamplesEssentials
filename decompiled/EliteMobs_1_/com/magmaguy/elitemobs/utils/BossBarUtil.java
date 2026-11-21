package com.magmaguy.elitemobs.utils;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.playerdata.PlayerItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtil {
   public static HashSet<BossBar> bossBars = new HashSet<>();
   public static ArrayListMultimap<UUID, BossBarUtil.PlayerBrokenItemBar> brokenPlayerItem = ArrayListMultimap.create();

   private BossBarUtil() {
   }

   public static void shutdown() {
      for (BossBarUtil.PlayerBrokenItemBar playerBrokenItemBar : new ArrayList(brokenPlayerItem.values())) {
         playerBrokenItemBar.destroy();
      }

      bossBars.forEach(BossBar::removeAll);
      bossBars.clear();
      brokenPlayerItem.clear();
   }

   public static void DisplayBrokenItemBossBar(PlayerItem.EquipmentSlot equipmentSlot, Player player, String title) {
      UUID playerUUID = player.getUniqueId();
      List<BossBarUtil.PlayerBrokenItemBar> arrayList = brokenPlayerItem.get(playerUUID);
      boolean alreadyExists = false;

      for (BossBarUtil.PlayerBrokenItemBar playerBrokenItemBar : arrayList) {
         if (playerBrokenItemBar.getEquipmentSlot().equals(equipmentSlot)) {
            alreadyExists = true;
            break;
         }
      }

      if (!alreadyExists) {
         brokenPlayerItem.put(playerUUID, new BossBarUtil.PlayerBrokenItemBar(equipmentSlot, player, title, BarColor.RED, BarStyle.SOLID));
      }
   }

   public static void clearPlayer(UUID playerUUID) {
      for (BossBarUtil.PlayerBrokenItemBar playerBrokenItemBar : new ArrayList(brokenPlayerItem.get(playerUUID))) {
         playerBrokenItemBar.destroy();
      }

      brokenPlayerItem.removeAll(playerUUID);
   }

   public static void HideBrokenItemBossBar(PlayerItem.EquipmentSlot equipmentSlot, Player player) {
      UUID playerUUID = player.getUniqueId();
      List<BossBarUtil.PlayerBrokenItemBar> arrayList = brokenPlayerItem.get(playerUUID);
      BossBarUtil.PlayerBrokenItemBar storedPlayerBrokenItemBar = null;

      for (BossBarUtil.PlayerBrokenItemBar playerBrokenItemBar : arrayList) {
         if (playerBrokenItemBar.getEquipmentSlot().equals(equipmentSlot)) {
            storedPlayerBrokenItemBar = playerBrokenItemBar;
            playerBrokenItemBar.destroy();
            break;
         }
      }

      if (storedPlayerBrokenItemBar != null) {
         brokenPlayerItem.remove(playerUUID, storedPlayerBrokenItemBar);
      }
   }

   private static BossBar CreateBossBar(Player player, String title, BarColor barColor, BarStyle barStyle) {
      BossBar bossBar = Bukkit.createBossBar(title, barColor, barStyle, new BarFlag[0]);
      BossBarOrderManager.show(player, bossBar);
      bossBars.add(bossBar);
      return bossBar;
   }

   private static void DestroyBossBar(Player player, BossBar bossBar) {
      BossBarOrderManager.hide(player, bossBar);
      bossBar.removeAll();
      bossBars.remove(bossBar);
   }

   private static class PlayerBrokenItemBar {
      private final PlayerItem.EquipmentSlot equipmentSlot;
      private final BossBar bossBar;
      private final Player player;

      private PlayerBrokenItemBar(PlayerItem.EquipmentSlot equipmentSlot, Player player, String title, BarColor barColor, BarStyle barStyle) {
         this.equipmentSlot = equipmentSlot;
         this.player = player;
         this.bossBar = BossBarUtil.CreateBossBar(player, title, barColor, barStyle);
      }

      private void destroy() {
         BossBarUtil.DestroyBossBar(this.player, this.bossBar);
      }

      @Generated
      public PlayerItem.EquipmentSlot getEquipmentSlot() {
         return this.equipmentSlot;
      }

      @Generated
      public BossBar getBossBar() {
         return this.bossBar;
      }
   }
}
