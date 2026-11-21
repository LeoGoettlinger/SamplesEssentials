package com.artillexstudios.axrankmenu.gui;

import com.artillexstudios.axrankmenu.libs.axapi.config.Config;
import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axrankmenu.libs.axapi.utils.NumberUtils;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiAction;
import com.artillexstudios.axrankmenu.libs.gui.guis.BaseGui;
import com.artillexstudios.axrankmenu.libs.gui.guis.GuiItem;
import com.artillexstudios.axrankmenu.utils.ItemBuilderUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuiFrame {
   protected final Config file;
   protected BaseGui gui;
   protected Player player;

   public GuiFrame(Config file, Player player) {
      this.file = file;
      this.player = player;
   }

   public static IntArrayList getSlots(List<String> s) {
      IntArrayList slots = new IntArrayList();

      for (String str : s) {
         if (NumberUtils.isInt(str)) {
            slots.add(Integer.parseInt(str));
         } else if (str.contains("|")) {
            String[] split = str.split("\\|");
            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);

            for (int i = min; i <= max; i += 9) {
               slots.add(i);
            }
         } else {
            String[] split = str.split("-");
            int min = Integer.parseInt(split[0]);
            int max = Integer.parseInt(split[1]);

            for (int i = min; i <= max; i++) {
               slots.add(i);
            }
         }
      }

      return slots;
   }

   public void setGui(BaseGui gui) {
      this.gui = gui;
   }

   @NotNull
   public Config getFile() {
      return this.file;
   }

   protected ItemStack buildItem(@NotNull String key) {
      return this.buildItem(key, Map.of());
   }

   protected ItemStack buildItem(@NotNull String key, Map<String, String> replacements) {
      Section section = this.file.getSection(key);
      if (section.getString("material", null) == null && section.getString("type", null) == null) {
         section = this.file.getSection(key + ".item");
      }

      ItemStack item = ItemBuilderUtil.newBuilder(section, replacements, this.player).get();
      if (section.getOptionalString("texture").isEmpty() && item.getItemMeta() instanceof SkullMeta skullMeta) {
         skullMeta.setOwningPlayer(this.player);
         item.setItemMeta(skullMeta);
      }

      return item;
   }

   protected void createItem(@NotNull String route) {
      this.createItem(route, event -> {}, Map.of());
   }

   protected void createItem(@NotNull String route, @Nullable GuiAction<InventoryClickEvent> action) {
      this.createItem(route, action, Map.of());
   }

   protected void createItem(@NotNull String route, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements) {
      if (this.file.getString(route + ".slot") != null || !this.file.getStringList(route + ".slot").isEmpty()) {
         List<String> slots = this.file.getBackingDocument().getStringList(route + ".slot");
         this.createItem(route, action, replacements, getSlots(slots.isEmpty() ? List.of(this.file.getString(route + ".slot")) : slots));
      }
   }

   protected void createItem(@NotNull String route, @Nullable GuiAction<InventoryClickEvent> action, Map<String, String> replacements, IntArrayList slots) {
      GuiItem guiItem = new GuiItem(this.buildItem(route, replacements), action);
      this.gui.setItem(slots, guiItem);
   }

   protected void createItem(@NotNull String route, @NotNull ItemStack item, @Nullable GuiAction<InventoryClickEvent> action) {
      if (this.file.getSection(route) != null) {
         GuiItem guiItem = new GuiItem(item, action);
         List<String> slots = this.file.getBackingDocument().getStringList(route + ".slot");
         this.gui.setItem(getSlots(slots.isEmpty() ? List.of(this.file.getString(route + ".slot")) : slots), guiItem);
      }
   }

   protected void addItem(@NotNull GuiItem guiItem, @NotNull String route) {
      if (this.file.getSection(route) != null) {
         List<String> slots = this.file.getBackingDocument().getStringList(route + ".slot");
         this.gui.setItem(getSlots(slots.isEmpty() ? List.of(this.file.getString(route + ".slot")) : slots), guiItem);
      }
   }

   protected void extendLore(ItemStack item, String... lore) {
      ItemMeta meta = item.getItemMeta();
      List<String> newLore = new ArrayList<>();
      if (meta.getLore() != null) {
         newLore.addAll(meta.getLore());
      }

      newLore.addAll(StringUtils.formatListToString(Arrays.asList(lore)));
      meta.setLore(newLore);
      item.setItemMeta(meta);
   }
}
