package me.usainsrht.ujobs.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.models.Job;
import me.usainsrht.ujobs.models.PlayerJobData;
import me.usainsrht.ujobs.utils.ProgressBarUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecoration.State;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class MainJobGUI implements JobGUI {
   private final UJobsPlugin plugin;
   public Inventory inventory;
   public UUID uuid;
   public static final NamespacedKey jobKey = new NamespacedKey("ujobs", "job_id");

   public MainJobGUI(UJobsPlugin plugin, UUID uuid) {
      this.plugin = plugin;
      this.uuid = uuid;
      int rows = (int)Math.ceil(plugin.getJobManager().getJobs().size() / 7.0F) + 2;
      Component title = plugin.getMiniMessage().deserialize(plugin.getConfig().getString("gui.title"));
      this.inventory = Bukkit.createInventory(this, rows * 9, title);
      String blankMaterial = plugin.getConfig().getString("gui.blank_material", null);
      if (blankMaterial != null && !blankMaterial.isEmpty() && !blankMaterial.equalsIgnoreCase("air")) {
         ItemStack blankItem = new ItemStack(Material.matchMaterial(blankMaterial));
         blankItem.editMeta(meta -> meta.setHideTooltip(true));

         for (int i = 0; i < this.inventory.getSize(); i++) {
            this.inventory.setItem(i, blankItem);
         }
      }

      if (uuid == null) {
         plugin.getLogger().warning("MainJobGUI was opened with a null UUID.");
      } else {
         if (plugin.getStorage().isCached(uuid)) {
            PlayerJobData playerJobData = plugin.getStorage().getCached(uuid);
            if (playerJobData != null) {
               this.fill(plugin, playerJobData);
            }
         } else {
            plugin.getStorage().load(uuid).thenAccept(data -> {
               Player player = Bukkit.getPlayer(uuid);
               if (player != null && player.isOnline()) {
                  plugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> this.fill(plugin, data), () -> {});
               }
            });
         }
      }
   }

   public void fill(UJobsPlugin plugin, PlayerJobData playerJobData) {
      int i = 1;
      int baseSlot = 9;

      for (Job job : plugin.getJobManager().getJobs().values()) {
         if (i > 7) {
            baseSlot = 11;
         }

         if (i > 14) {
            baseSlot = 13;
         }

         String jobId = job.getId();
         PlayerJobData.JobStats jobStats = playerJobData.getJobStats(jobId);
         double exp = jobStats.getExp();
         int level = jobStats.getLevel();
         double nextExp = job.calculateExpForLevel(level);
         double totalMoney = jobStats.getTotalMoney();
         String progress = ProgressBarUtil.getProgressBar(exp, nextExp, 25, "", "|", "<color:dark_gray>", "|");
         TextColor primaryColor = job.getFirstColor();
         TextColor secondaryColor = job.getLastColor();
         Set<TagResolver> placeholderSet = new HashSet<>();
         placeholderSet.add(Placeholder.component("job", job.getName()));
         placeholderSet.add(Formatter.number("level", level));
         placeholderSet.add(Formatter.number("next_level", level + 1));
         placeholderSet.add(Formatter.number("exp", exp));
         placeholderSet.add(Formatter.number("next_exp", nextExp));
         placeholderSet.add(Formatter.number("total_money", totalMoney));
         placeholderSet.add(Placeholder.parsed("progress", progress));
         placeholderSet.add(Placeholder.parsed("money_symbol", plugin.getConfig().getString("symbols.money", "$")));
         placeholderSet.add(Placeholder.parsed("exp_symbol", plugin.getConfig().getString("symbols.exp", "xp")));
         placeholderSet.add(Placeholder.styling("primary", new StyleBuilderApplicable[]{primaryColor}));
         placeholderSet.add(Placeholder.styling("secondary", new StyleBuilderApplicable[]{secondaryColor}));
         int position = plugin.getLeaderboardManager().getPosition(playerJobData.getUuid(), job);
         String positionText = position == -1 ? plugin.getConfig().getString("leaderboard.calculate_top", "100") + "+" : String.valueOf(position + 1);
         placeholderSet.add(Placeholder.unparsed("position", positionText));
         TagResolver[] placeholders = placeholderSet.toArray(new TagResolver[0]);
         ItemStack item = new ItemStack(job.getIcon());
         item.editMeta(
            meta -> {
               meta.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ATTRIBUTES});
               Component name = plugin.getMiniMessage()
                  .deserialize(plugin.getConfig().getString("gui.jobitem.name"), placeholders)
                  .decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
               meta.displayName(name);
               List<Component> lore = new ArrayList<>();
               plugin.getConfig().getStringList("gui.jobitem.lore").forEach(line -> {
                  Component component = plugin.getMiniMessage().deserialize(line, placeholders).decorationIfAbsent(TextDecoration.ITALIC, State.FALSE);
                  lore.add(component);
               });
               meta.lore(lore);
               meta.getPersistentDataContainer().set(jobKey, PersistentDataType.STRING, jobId);
            }
         );
         int slot = baseSlot + i;
         this.inventory.setItem(slot, item);
         i++;
      }
   }

   public void open(Player player) {
      player.openInventory(this.inventory);
   }

   @NotNull
   public Inventory getInventory() {
      return this.inventory;
   }

   @Override
   public void onClick(InventoryClickEvent e) {
      e.setCancelled(true);
      if (e.getWhoClicked() instanceof Player player) {
         ItemStack itemStack = e.getCurrentItem();
         if (itemStack != null && itemStack.getType() != Material.AIR) {
            if (itemStack.getItemMeta() != null && itemStack.getItemMeta().getPersistentDataContainer().has(jobKey)) {
               UUID viewerUuid = this.uuid != null ? this.uuid : player.getUniqueId();
               if (e.getClick().isLeftClick()) {
                  new LeaderboardGUI(this.plugin, viewerUuid).open(player);
               } else if (e.getClick().isRightClick()) {
                  new JobInfoGUI(this.plugin).open(player);
               }
            }
         }
      }
   }

   @Override
   public void onBottomClick(InventoryClickEvent e) {
   }

   @Override
   public void onDrag(InventoryDragEvent e) {
      e.setCancelled(true);
   }

   @Override
   public void onOpen(InventoryOpenEvent e) {
   }

   @Override
   public void onClose(InventoryCloseEvent e) {
   }

   @Generated
   public UJobsPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }
}
