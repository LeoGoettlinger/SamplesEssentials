package com.artillexstudios.axtrade.trade;

import com.artillexstudios.axtrade.libs.axapi.config.Config;
import com.artillexstudios.axtrade.libs.axapi.nms.wrapper.ServerPlayerWrapper;
import com.artillexstudios.axtrade.libs.axapi.utils.NumberUtils;
import com.artillexstudios.axtrade.libs.axapi.utils.PlayerTextures;
import com.artillexstudios.axtrade.libs.gui.components.GuiAction;
import com.artillexstudios.axtrade.libs.gui.guis.BaseGui;
import com.artillexstudios.axtrade.libs.gui.guis.GuiItem;
import com.artillexstudios.axtrade.utils.ItemBuilderUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiFrame {
   private static final ItemStack air = new ItemStack(Material.AIR);
   protected final Config file;
   protected BaseGui gui;
   protected Player player;
   protected boolean opened = false;
   protected final Trade trade;

   public GuiFrame(Config file, Player player, Trade trade) {
      this.file = file;
      this.player = player;
      this.trade = trade;
   }

   public void setGui(BaseGui gui) {
      this.gui = gui;

      for (String str : this.file.getBackingDocument().getRoutesAsStrings(false)) {
         this.createItem(str);
      }
   }

   @NotNull
   public Config getFile() {
      return this.file;
   }

   protected ItemStack buildItem(@NotNull String key) {
      return this.file.getSection(key) == null ? air : ItemBuilderUtil.newBuilder(this.file.getSection(key), this.player).get();
   }

   protected ItemStack buildItem(@NotNull String key, Map<String, String> replacements) {
      if (this.file.getSection(key) == null) {
         return air;
      } else {
         PlayerTextures selfTextures = ServerPlayerWrapper.wrap(this.player.getPlayer()).textures();
         PlayerTextures otherTextures = ServerPlayerWrapper.wrap(this.trade.getOtherPlayer(this.player.getPlayer())).textures();
         HashMap<String, String> map = new HashMap<>(replacements);
         map.put("%own-head%", selfTextures.texture() == null ? "" : selfTextures.texture());
         map.put("%partner-head%", otherTextures.texture() == null ? "" : otherTextures.texture());
         map.put("%own-name%", this.player.getPlayer().getName());
         map.put("%partner-name%", this.trade.getOtherPlayer(this.player).getPlayer().getName());
         return ItemBuilderUtil.newBuilder(this.file.getSection(key), map, this.player).get();
      }
   }

   protected void createItem(@NotNull String route) {
      this.createItem(route, event -> event.setCancelled(true), Map.of());
   }

   protected void createItem(@NotNull String route, @Nullable GuiAction<InventoryClickEvent> action) {
      this.createItem(route, action, Map.of());
   }

   protected void createItem(@NotNull String route, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements) {
      this.createItem(route + ".slot", route, action, replacements);
   }

   protected void createItem(@NotNull String slotRoute, String itemRoute, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements) {
      this.createItem(slotRoute, itemRoute, action, replacements, 1);
   }

   protected void createItem(
      @NotNull String slotRoute, String itemRoute, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements, int amount
   ) {
      if (this.file.getString(itemRoute + ".type") != null || this.file.getString(itemRoute + ".material") != null) {
         ItemStack it = this.buildItem(itemRoute, replacements);
         it.setAmount(amount);
         GuiItem guiItem = new GuiItem(it, action);
         if (this.opened) {
            for (int slot : this.getSlots(slotRoute)) {
               this.gui.updateItem(slot, guiItem);
            }
         } else {
            this.gui.setItem(this.getSlots(slotRoute), guiItem);
         }
      }
   }

   protected List<Integer> getSlots(String r) {
      List<Integer> slots = new ArrayList<>();
      if (!this.file.getStringList(r).isEmpty()) {
         for (Object route : this.file.getStringList(r)) {
            if (NumberUtils.isInt(route + "")) {
               slots.add(Integer.parseInt(route + ""));
            } else {
               String[] split = (route + "").split("-");
               int min = Integer.parseInt(split[0]);
               int max = Integer.parseInt(split[1]);

               for (int i = min; i <= max; i++) {
                  slots.add(i);
               }
            }
         }
      } else {
         slots.add(this.file.getInt(r));
      }

      return slots;
   }
}
