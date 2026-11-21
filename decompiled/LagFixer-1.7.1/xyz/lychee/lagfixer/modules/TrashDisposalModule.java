package xyz.lychee.lagfixer.modules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.NonNull;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.Language;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import xyz.lychee.lagfixer.managers.ModuleManager;
import xyz.lychee.lagfixer.managers.SupportManager;
import xyz.lychee.lagfixer.objects.AbstractFork;
import xyz.lychee.lagfixer.objects.AbstractModule;

public class TrashDisposalModule extends AbstractModule implements Runnable, CommandExecutor, Listener {
   private final Map<UUID, Inventory> playerTrashInventories = new HashMap<>();
   private Inventory globalTrashInventory = null;
   private BukkitTask task;
   private boolean global;
   private boolean storeItems;
   private boolean cleanupEnabled;
   private long cleanupInterval;
   private boolean abyssIntegration;
   private boolean forcedTrash;
   private String commandPermission;
   private List<String> commandAliases;

   public TrashDisposalModule(LagFixer plugin, ModuleManager manager) {
      super(
         plugin,
         manager,
         AbstractModule.Impact.LOW,
         "TrashDisposal",
         new String[]{
            "Introduces a trash command to discard items cleanly, reducing world entity clutter.",
            "Performance impact depends on how frequently players use it instead of dropping items.",
            "Allows per-world enforcement to force trash can usage on selected worlds.",
            "Prevents dropping renamed items to block server advertising on selected worlds."
         },
         "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmUwZmQxMDE5OWU4ZTRmY2RhYmNhZTRmODVjODU5MTgxMjdhN2M1NTUzYWQyMzVmMDFjNTZkMThiYjk0NzBkMyJ9fX0="
      );
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public void onItemDrop(PlayerDropItemEvent e) {
      if (this.forcedTrash && this.canContinue(e.getPlayer().getWorld()) && !e.getPlayer().hasPermission("lagfixer.trash.bypass")) {
         e.setCancelled(true);
         Component text = this.getLanguage().getComponent("trash.disabled_drop", true);
         if (text != null) {
            this.getPlugin().getAudiences().player(e.getPlayer()).sendMessage(text);
         }
      }
   }

   @Override
   public void load() throws Exception {
      if (this.global || !this.storeItems) {
         this.playerTrashInventories.clear();
      }

      if (this.forcedTrash) {
         Bukkit.getPluginManager().registerEvents(this, this.getPlugin());
      }

      AbstractFork fork = SupportManager.getInstance().getFork();
      fork.registerCommand(this.getPlugin(), "trash", this.commandAliases, this);
      if (this.cleanupEnabled) {
         this.task = fork.runTimer(true, this, this.cleanupInterval, this.cleanupInterval, TimeUnit.SECONDS);
      }
   }

   @Override
   public boolean loadConfig() {
      this.global = this.getSection().getBoolean("global");
      this.storeItems = this.global || this.getSection().getBoolean("store_items");
      this.cleanupEnabled = this.storeItems && this.getSection().getBoolean("cleanup.enabled");
      if (this.cleanupEnabled) {
         this.cleanupInterval = Math.max(1L, this.getSection().getLong("cleanup.interval"));
      }

      this.abyssIntegration = this.cleanupEnabled && this.getSection().getBoolean("abyss_integration");
      this.forcedTrash = this.getSection().getBoolean("forced_trash");
      this.commandPermission = this.getSection().getString("command.permission");
      this.commandAliases = this.getSection().getStringList("command.aliases");
      return true;
   }

   @Override
   public void disable() {
      HandlerList.unregisterAll(this);
      if (this.task != null) {
         this.task.cancel();
         this.task = null;
      }
   }

   @Override
   public void run() {
      if (this.cleanupEnabled) {
         if (this.global) {
            if (this.globalTrashInventory != null) {
               if (this.abyssIntegration) {
                  this.sendToAbyss(this.globalTrashInventory);
               }

               this.globalTrashInventory.clear();
            }
         } else {
            for (Inventory inv : this.playerTrashInventories.values()) {
               if (this.abyssIntegration) {
                  this.sendToAbyss(inv);
               }

               inv.clear();
            }
         }
      }
   }

   public void sendToAbyss(Inventory inv) {
      WorldCleanerModule worldCleanerModule = this.getManager().get(WorldCleanerModule.class);
      if (worldCleanerModule != null && worldCleanerModule.isLoaded() && worldCleanerModule.isItems_abyss_enabled()) {
         for (ItemStack item : inv.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
               worldCleanerModule.getItems().add(item);
            }
         }
      }
   }

   private Inventory getOrCreateTrash(Player player) {
      if (this.global) {
         if (this.globalTrashInventory == null) {
            String guiName = this.getLanguage().getString("gui.global_name", true);
            return this.globalTrashInventory = Bukkit.createInventory(null, 54, guiName);
         } else {
            return this.globalTrashInventory;
         }
      } else if (this.storeItems) {
         return this.playerTrashInventories.computeIfAbsent(player.getUniqueId(), k -> {
            String guiNamex = this.getLanguage().getString("gui.individual_name", true, Placeholder.unparsed("player", player.getDisplayName()));
            return Bukkit.createInventory(null, 54, guiNamex);
         });
      } else {
         String guiName = this.getLanguage().getString("gui.individual_name", true, Placeholder.unparsed("player", player.getDisplayName()));
         return Bukkit.createInventory(null, 54, guiName);
      }
   }

   public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, String[] args) {
      Component text;
      if (this.commandPermission != null && !this.commandPermission.isEmpty() && !sender.hasPermission(this.commandPermission)) {
         text = Language.getMainValue("no_access", true, Placeholder.unparsed("permission", this.commandPermission));
      } else if (sender instanceof Player player) {
         player.openInventory(this.getOrCreateTrash(player));
         text = this.getLanguage().getComponent("trash.opened", true);
      } else {
         text = Language.getMainValue("player_only", true);
      }

      this.getPlugin().getAudiences().sender(sender).sendMessage(text);
      return true;
   }

   @Generated
   public Map<UUID, Inventory> getPlayerTrashInventories() {
      return this.playerTrashInventories;
   }

   @Generated
   public Inventory getGlobalTrashInventory() {
      return this.globalTrashInventory;
   }

   @Generated
   public BukkitTask getTask() {
      return this.task;
   }

   @Generated
   public boolean isGlobal() {
      return this.global;
   }

   @Generated
   public boolean isStoreItems() {
      return this.storeItems;
   }

   @Generated
   public boolean isCleanupEnabled() {
      return this.cleanupEnabled;
   }

   @Generated
   public long getCleanupInterval() {
      return this.cleanupInterval;
   }

   @Generated
   public boolean isAbyssIntegration() {
      return this.abyssIntegration;
   }

   @Generated
   public boolean isForcedTrash() {
      return this.forcedTrash;
   }

   @Generated
   public String getCommandPermission() {
      return this.commandPermission;
   }

   @Generated
   public List<String> getCommandAliases() {
      return this.commandAliases;
   }
}
