package com.artillexstudios.axrankmenu.gui.impl;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.gui.GuiFrame;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import com.artillexstudios.axrankmenu.libs.gui.builder.gui.BaseGuiBuilder;
import com.artillexstudios.axrankmenu.libs.gui.components.GuiType;
import com.artillexstudios.axrankmenu.libs.gui.guis.Gui;
import com.artillexstudios.axrankmenu.rank.Rank;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RankGui extends GuiFrame {
   private static final Set<RankGui> openMenus = Collections.newSetFromMap(new WeakHashMap<>());
   private final Player player;
   private final Gui gui;

   public RankGui(@NotNull Player player) {
      super(AxRankMenu.RANKS, player);
      this.player = player;
      GuiType guiType = GuiType.valueOf(AxRankMenu.RANKS.getString("type", "CHEST"));
      BaseGuiBuilder<?, ?> builder;
      if (guiType == GuiType.CHEST) {
         builder = Gui.gui().rows(AxRankMenu.RANKS.getInt("rows", 1));
      } else {
         builder = Gui.gui(guiType);
      }

      this.gui = (Gui)builder.disableAllInteractions().title(StringUtils.format(AxRankMenu.RANKS.getString("title"))).create();
      this.gui.setCloseGuiAction(event -> openMenus.remove(this));
      this.setGui(this.gui);
   }

   public void open() {
      for (String str : this.file.getBackingDocument().getRoutesAsStrings(false)) {
         this.createItem(str);
      }

      for (String route : AxRankMenu.RANKS.getBackingDocument().getRoutesAsStrings(false)) {
         if (AxRankMenu.RANKS.getString(route + ".rank", null) != null) {
            Rank rank = new Rank(AxRankMenu.RANKS.getSection(route), this.player);
            if (rank.getGroup() == null) {
               Bukkit.getConsoleSender()
                  .sendMessage(
                     StringUtils.formatToString(
                        "&#FF0000[AxRankMenu] The group %group% does not exist!".replace("%group%", AxRankMenu.RANKS.getString(route + ".rank", "---"))
                     )
                  );
            } else {
               super.addItem(rank.getItem(), route);
            }
         }
      }

      if (openMenus.contains(this)) {
         this.gui.update();
      } else {
         openMenus.add(this);
         this.gui.open(this.player);
      }
   }

   public static Set<RankGui> getOpenMenus() {
      return openMenus;
   }
}
