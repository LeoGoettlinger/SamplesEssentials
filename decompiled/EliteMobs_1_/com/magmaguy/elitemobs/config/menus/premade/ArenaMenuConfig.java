package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import java.util.List;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ArenaMenuConfig extends MenusConfigFields {
   private static String menuName;
   private static ItemStack playerItem;
   private static int playerItemSlot;
   private static ItemStack spectatorItem;
   private static int spectatorItemSlot;
   private static String invalidArenaMessage;

   public ArenaMenuConfig() {
      super("arena_menu", true);
   }

   @Override
   public void processAdditionalFields() {
      menuName = ConfigurationEngine.setString(this.file, this.fileConfiguration, "menuName", "", true);
      playerItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "playerItem",
         ItemStackGenerator.generateItemStack(Material.DIAMOND_SWORD, "&4Challenge the arena!", List.of("&2Fight in the arena!")),
         true
      );
      playerItemSlot = ConfigurationEngine.setInt(this.fileConfiguration, "playerItemSlot", 6);
      spectatorItem = ConfigurationEngine.setItemStack(
         this.file,
         this.fileConfiguration,
         "spectatorItem",
         ItemStackGenerator.generateItemStack(Material.SPYGLASS, "&aSpectate!", List.of("&2Spectate players in the arena!"), MetadataHandler.signatureID),
         true
      );
      spectatorItemSlot = ConfigurationEngine.setInt(this.fileConfiguration, "spectatorItemSlot", 2);
      invalidArenaMessage = ConfigurationEngine.setString(
         this.file,
         this.fileConfiguration,
         "invalidArenaMessage",
         "&4[EliteMobs] &cInvalid arena name! The arena you are trying to join is not correctly setup.",
         true
      );
   }

   @Generated
   public static String getMenuName() {
      return menuName;
   }

   @Generated
   public static ItemStack getPlayerItem() {
      return playerItem;
   }

   @Generated
   public static int getPlayerItemSlot() {
      return playerItemSlot;
   }

   @Generated
   public static ItemStack getSpectatorItem() {
      return spectatorItem;
   }

   @Generated
   public static int getSpectatorItemSlot() {
      return spectatorItemSlot;
   }

   @Generated
   public static String getInvalidArenaMessage() {
      return invalidArenaMessage;
   }
}
