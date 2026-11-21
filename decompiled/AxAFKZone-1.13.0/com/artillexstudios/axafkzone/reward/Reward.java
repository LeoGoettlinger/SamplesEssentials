package com.artillexstudios.axafkzone.reward;

import com.artillexstudios.axafkzone.libs.axapi.scheduler.Scheduler;
import com.artillexstudios.axafkzone.libs.axapi.utils.ContainerUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.ItemBuilder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Reward {
   private final List<String> commands;
   private final List<ItemStack> items = new ArrayList<>();
   private final String permission;
   private final int minimumTime;
   private final int maximumTime;
   private final double chance;
   private final String display;

   public Reward(Map<Object, Object> str) {
      List<Map<Object, Object>> items = (List<Map<Object, Object>>)str.get("items");
      if (items != null) {
         for (Map<Object, Object> it : new LinkedList<>(items)) {
            this.items.add(ItemBuilder.create(it).get());
         }
      }

      this.display = (String)str.getOrDefault("display", null);
      this.permission = (String)str.getOrDefault("permission", null);
      this.minimumTime = ((Number)str.getOrDefault("minimum-time", 0)).intValue();
      this.maximumTime = ((Number)str.getOrDefault("maximum-time", Integer.MAX_VALUE)).intValue();
      this.chance = ((Number)str.getOrDefault("chance", 10.0)).doubleValue();
      this.commands = (List<String>)str.getOrDefault("commands", new ArrayList());
   }

   public List<String> getCommands() {
      return this.commands;
   }

   public List<ItemStack> getItems() {
      return this.items;
   }

   public String getPermission() {
      return this.permission;
   }

   public int getMinimumTime() {
      return this.minimumTime;
   }

   public int getMaximumTime() {
      return this.maximumTime;
   }

   public double getChance() {
      return this.chance;
   }

   public String getDisplay() {
      return this.display;
   }

   public void run(Player player) {
      Scheduler.get().run(scheduledTask -> {
         for (String cmd : this.commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()));
         }
      });
      ContainerUtils.INSTANCE.addOrDrop(player.getInventory(), this.items, player.getLocation());
   }

   @Override
   public String toString() {
      return "Reward{commands=" + this.commands + ", items=" + this.items + ", chance=" + this.chance + "}";
   }
}
