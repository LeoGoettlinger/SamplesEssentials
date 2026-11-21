package dev.hercoon.starterKit

import java.util.ArrayList
import java.util.HashMap
import java.util.Locale
import kotlin.jvm.internal.SourceDebugExtension
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.PluginCommand
import org.bukkit.command.TabCompleter
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

@SourceDebugExtension(["SMAP\nStarterKit.kt\nKotlin\n*S Kotlin\n*F\n+ 1 StarterKit.kt\ndev/hercoon/starterKit/StarterKit\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,168:1\n1915#2,2:169\n812#2,12:171\n1915#2,2:183\n777#2:185\n873#2,2:186\n1586#2:188\n1661#2,3:189\n777#2:192\n873#2,2:193\n*S KotlinDebug\n*F\n+ 1 StarterKit.kt\ndev/hercoon/starterKit/StarterKit\n*L\n62#1:169,2\n135#1:171,12\n145#1:183,2\n162#1:185\n162#1:186,2\n164#1:188\n164#1:189,3\n164#1:192\n164#1:193,2\n*E\n"])
public class StarterKit : JavaPlugin, Listener, TabCompleter {
   public open fun onEnable() {
      this.worldEvents = this.registerWorldEvents();
      this.getLogger().info("=================================");
      this.getLogger().info("       StarterKit v2 Loaded      ");
      this.getLogger().info("=================================");
      this.getLogger().info("    Armor & Slot Support Fix     ");
      this.getLogger().info("   Author: Hercoon               ");
      this.getLogger().info("=================================");
      this.saveDefaultConfig();
      this.getServer().getPluginManager().registerEvents(this, this as Plugin);
      val var10000: PluginCommand = this.getCommand("sk");
      if (var10000 != null) {
         var10000.setTabCompleter(this);
      }
   }

   @EventHandler
   public fun onPlayerJoin(event: PlayerJoinEvent) {
      if (!event.getPlayer().hasPlayedBefore()) {
         val var10001: Player = event.getPlayer();
         this.giveKit(var10001, true);
      }
   }

   private fun giveKit(player: Player, forceSlots: Boolean) {
      val var10000: ConfigurationSection = this.getConfig().getConfigurationSection("starterkit.items");
      if (var10000 != null) {
         val var18: PlayerInventory = player.getInventory();
         val inv: PlayerInventory = var18;

         for (java.lang.String slotKey : var10000.getKeys(false)) {
            val var19: Int = StringsKt.toIntOrNull(slotKey);
            if (var19 != null) {
               val slot: Int = var19;
               val var20: ItemStack = this.getConfig().getItemStack("starterkit.items.$slot");
               if (var20 != null) {
                  if (forceSlots) {
                     this.setSlotDirectly(player, slot, var20);
                  } else {
                     val currentItemInSlot: ItemStack = inv.getItem(slot);
                     if (currentItemInSlot != null && !currentItemInSlot.getType().isAir()) {
                        val var21: HashMap = inv.addItem(new ItemStack[]{var20});

                        val `$this$forEach$iv`: java.lang.Iterable;
                        for (Object element$iv : $this$forEach$iv) {
                           player.getWorld().dropItemNaturally(player.getLocation(), `element$iv` as ItemStack);
                        }
                     } else {
                        this.setSlotDirectly(player, slot, var20);
                     }
                  }
               }
            }
         }

         player.updateInventory();
      }
   }

   private fun setSlotDirectly(player: Player, slot: Int, item: ItemStack) {
      val var10000: PlayerInventory = player.getInventory();
      switch (slot) {
         case 36:
            var10000.setBoots(item);
            break;
         case 37:
            var10000.setLeggings(item);
            break;
         case 38:
            var10000.setChestplate(item);
            break;
         case 39:
            var10000.setHelmet(item);
            break;
         case 40:
            var10000.setItemInOffHand(item);
            break;
         default:
            var10000.setItem(slot, item);
      }
   }

   private fun removeKit(player: Player) {
      val var10000: ConfigurationSection = this.getConfig().getConfigurationSection("starterkit.items");
      if (var10000 != null) {
         for (java.lang.String slotKey : var10000.getKeys(false)) {
            val var8: Int = StringsKt.toIntOrNull(slotKey);
            if (var8 != null) {
               val var9: ItemStack = this.getConfig().getItemStack("starterkit.items.${var8}");
               if (var9 != null) {
                  player.getInventory().removeItem(new ItemStack[]{var9});
               }
            }
         }

         player.updateInventory();
      }
   }

   public open fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
      if (!sender.hasPermission("starterkit.admin")) {
         sender.sendMessage("${ChatColor.RED}No permission!");
         return true;
      } else if (args.length != 0 && !StringsKt.equals(args[0], "help", true)) {
         var var10000: java.lang.String = args[0].toLowerCase(Locale.ROOT);
         switch (var10000.hashCode()) {
            case -934641255:
               if (var10000.equals("reload")) {
                  this.reloadConfig();
                  sender.sendMessage("${ChatColor.GREEN}✔ Configuration reloaded!");
               }

               return true;
            case -934610812:
               if (!var10000.equals("remove")) {
                  return true;
               }
               break;
            case 3173137:
               if (!var10000.equals("give")) {
                  return true;
               }
               break;
            case 94756189:
               if (var10000.equals("clone")) {
                  if (sender !is Player) {
                     return true;
                  }

                  this.getConfig().set("starterkit.items", null);
                  val var27: PlayerInventory = (sender as Player).getInventory();
                  val targets: PlayerInventory = var27;
                  var isGive: Int = 0;

                  for (int $this$forEach$iv = var27.getSize(); i < $this$forEach$iv; i++) {
                     val `$i$f$forEach`: ItemStack = targets.getItem(isGive);
                     if (`$i$f$forEach` != null && `$i$f$forEach`.getType().isItem()) {
                        this.getConfig().set("starterkit.items.$isGive", `$i$f$forEach`);
                        switch (i) {
                           case 36:
                              this.getConfig().setInlineComments("starterkit.items.$isGive", CollectionsKt.listOf("Boots"));
                              break;
                           case 37:
                              this.getConfig().setInlineComments("starterkit.items.$isGive", CollectionsKt.listOf("Leggings"));
                              break;
                           case 38:
                              this.getConfig().setInlineComments("starterkit.items.$isGive", CollectionsKt.listOf("Chestplate"));
                              break;
                           case 39:
                              this.getConfig().setInlineComments("starterkit.items.$isGive", CollectionsKt.listOf("Helmet"));
                              break;
                           case 40:
                              this.getConfig().setInlineComments("starterkit.items.$isGive", CollectionsKt.listOf("Off-hand"));
                           default:
                        }
                     }
                  }

                  this.saveConfig();
                  sender.sendMessage("${ChatColor.GREEN}✔ Kit saved! Armor and Off-hand will now equip automatically.");
               }

               return true;
            default:
               return true;
         }

         if (args.length < 2) {
            return false;
         } else {
            var var16: java.util.List;
            try {
               val var28: java.util.List = Bukkit.selectEntities(sender, args[1]);
               val var17: java.lang.Iterable = var28;
               val `destination$iv$iv`: java.util.Collection = new ArrayList();

               for (Object element$iv$iv : $this$filterIsInstance$iv) {
                  if (var13 is Player) {
                     `destination$iv$iv`.add(var13);
                  }
               }

               var16 = `destination$iv$iv` as java.util.List;
            } catch (var14: Exception) {
               val var21: Player = Bukkit.getPlayer(args[1]);
               var16 = if (var21 != null) CollectionsKt.listOf(var21) else CollectionsKt.emptyList();
            }

            if (var16.isEmpty()) {
               sender.sendMessage("${ChatColor.RED}No players found.");
            } else {
               var10000 = args[0].toLowerCase(Locale.ROOT);
               val var18: Boolean = var10000 == "give";

               val var20: java.lang.Iterable;
               for (Object element$iv : var20) {
                  val var25: Player = var24 as Player;
                  if (var18) {
                     this.giveKit(var25, false);
                  } else {
                     this.removeKit(var25);
                  }
               }

               sender.sendMessage("${ChatColor.GREEN}✔ Kit action completed.");
            }

            return true;
         }
      } else {
         this.sendHelp(sender);
         return true;
      }
   }

   private fun sendHelp(sender: CommandSender) {
      sender.sendMessage("${ChatColor.DARK_GRAY}--- ${ChatColor.GREEN}StarterKit v2 ${ChatColor.DARK_GRAY}---");
      sender.sendMessage("${ChatColor.YELLOW}/sk clone ${ChatColor.GRAY}- Saves your inv + auto-equips armor");
      sender.sendMessage("${ChatColor.YELLOW}/sk give <target> ${ChatColor.GRAY}- Safety-first delivery");
      sender.sendMessage("${ChatColor.YELLOW}/sk remove <target> ${ChatColor.GRAY}- Remove kit items");
      sender.sendMessage("${ChatColor.YELLOW}/sk reload ${ChatColor.GRAY}- Reload config");
   }

   public open fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
      if (args.length == 1) {
         val var19: java.lang.Iterable = CollectionsKt.listOf(new java.lang.String[]{"clone", "give", "remove", "reload", "help"});
         val var23: java.util.Collection = new ArrayList();

         for (Object element$iv$iv : $this$filter$iv) {
            val var31: java.lang.String = var29 as java.lang.String;
            val var34: java.lang.String = args[0].toLowerCase(Locale.ROOT);
            if (StringsKt.startsWith$default(var31, var34, false, 2, null)) {
               var23.add(var29);
            }
         }

         return var23 as MutableList<java.lang.String>;
      } else if (args.length == 2 && (args[0] == "give" || args[0] == "remove")) {
         val var10000: java.util.Collection = CollectionsKt.listOf(new java.lang.String[]{"@a", "@s", "@p", "@r"});
         val var10001: java.util.Collection = Bukkit.getOnlinePlayers();
         var var16: java.lang.Iterable = var10001;
         var `destination$iv$iv`: java.util.Collection = new ArrayList(CollectionsKt.collectionSizeOrDefault(var10001, 10));

         for (Object item$iv$iv : $this$filter$iv) {
            `destination$iv$iv`.add((`element$iv$iv` as Player).getName());
         }

         var16 = CollectionsKt.plus(var10000, `destination$iv$iv` as java.util.List);
         `destination$iv$iv` = new ArrayList();

         for (Object element$iv$ivx : $this$filter$iv) {
            if (StringsKt.startsWith(`element$iv$ivx` as java.lang.String, args[1], true)) {
               `destination$iv$iv`.add(`element$iv$ivx`);
            }
         }

         return `destination$iv$iv` as MutableList<java.lang.String>;
      } else {
         return CollectionsKt.emptyList();
      }
   }
}
