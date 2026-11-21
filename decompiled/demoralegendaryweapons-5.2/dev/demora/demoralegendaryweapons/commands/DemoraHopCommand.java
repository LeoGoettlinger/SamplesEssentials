package dev.demora.demoralegendaryweapons.commands;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DemoraHopCommand implements CommandExecutor, TabCompleter {
   private final DemoraLegendaryWeapons plugin;
   private final List<String> weaponNames = Arrays.asList(
      "sculk_crossbow",
      "midas_sword",
      "mjolnir",
      "shadow_blade",
      "reaper_scythe",
      "villager_staff",
      "dragon_katana",
      "excalibur",
      "void_staff",
      "magma_club",
      "shrink_ray",
      "chainsaw_sword",
      "wither_sickles",
      "awakened_wither_sickles",
      "cloud_sword",
      "hypnosis_staff",
      "emerald_blade",
      "golem_hammer",
      "toxic_crossbow",
      "lich_staff",
      "artemis_bow",
      "poseidon_trident",
      "reinforced_elytra",
      "ravager_horn",
      "freezing_chakram",
      "sculkweavers_lantern",
      "war_pick",
      "beehive_blaster",
      "fugu_cannon"
   );

   public DemoraHopCommand(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("demoralegendaryweapons.give")) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("no-permission"));
         return true;
      } else if (args.length == 0) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.demorahop-usage"));
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.demorahop-weapons-list").replace("{weapons}", String.join(", ", this.weaponNames)));
         return true;
      } else if (args[0].equalsIgnoreCase("give")) {
         return this.handleGiveCommand(sender, args);
      } else if (args[0].equalsIgnoreCase("reload")) {
         return this.handleReloadCommand(sender);
      } else if (args[0].equalsIgnoreCase("lang")) {
         return this.handleLangCommand(sender, args);
      } else {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.demorahop-unknown"));
         return true;
      }
   }

   private boolean handleGiveCommand(CommandSender sender, String[] args) {
      if (args.length < 3) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.demorahop-usage"));
         return true;
      } else {
         String weaponName = args[1].toLowerCase();
         String playerName = args[2];
         if (!this.weaponNames.contains(weaponName)) {
            sender.sendMessage(this.plugin.getMessageWithPrefix("weapon-not-found").replace("{weapon}", weaponName));
            return true;
         } else {
            Player target = Bukkit.getPlayer(playerName);
            if (target == null) {
               sender.sendMessage(this.plugin.getMessageWithPrefix("player-not-found").replace("{player}", playerName));
               return true;
            } else {
               ItemStack weapon = this.createWeapon(weaponName);
               if (weapon != null) {
                  target.getInventory().addItem(new ItemStack[]{weapon});
                  sender.sendMessage(this.plugin.getMessageWithPrefix("weapon-given").replace("{weapon}", weaponName).replace("{player}", target.getName()));
               }

               return true;
            }
         }
      }
   }

   private boolean handleReloadCommand(CommandSender sender) {
      if (!sender.hasPermission("demoralegendaryweapons.reload")) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("no-permission"));
         return true;
      } else {
         this.plugin.reloadPluginConfig();
         sender.sendMessage(this.plugin.getMessageWithPrefix("config-reloaded"));
         return true;
      }
   }

   private boolean handleLangCommand(CommandSender sender, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(this.plugin.getMessageWithPrefix("commands.language-only-players"));
         return true;
      } else if (!player.isOp()) {
         player.sendMessage(this.plugin.getMessageWithPrefix("commands.language-no-op"));
         return true;
      } else if (args.length == 1) {
         this.plugin.getLanguageManager().showLanguageSelection(player);
         return true;
      } else {
         String language = args[1].toLowerCase();
         switch (language) {
            case "ru":
               this.plugin.getLanguageManager().setLanguage("ru");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            case "en":
               this.plugin.getLanguageManager().setLanguage("en");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            case "vn":
               this.plugin.getLanguageManager().setLanguage("vn");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            case "de":
               this.plugin.getLanguageManager().setLanguage("de");
               this.plugin.getLanguageManager().removePlayerNeedingSelection(player.getUniqueId());
               player.sendMessage("§8[§6DemoraLegendary§8] §r" + this.plugin.getLanguageManager().getMessage("language-selection.selected"));
               break;
            default:
               player.sendMessage(this.plugin.getMessageWithPrefix("commands.language-available"));
         }

         return true;
      }
   }

   private ItemStack createWeapon(String weaponName) {
      return switch (weaponName) {
         case "sculk_crossbow" -> this.plugin.getSculkCrossbow().createSculkCrossbow();
         case "midas_sword" -> this.plugin.getMidasSword().createMidasSword();
         case "mjolnir" -> this.plugin.getMjolnir().createMjolnir();
         case "shadow_blade" -> this.plugin.getShadowBlade().createShadowBlade();
         case "reaper_scythe" -> this.plugin.getReaperScythe().createReaperScythe();
         case "villager_staff" -> this.plugin.getVillagerStaff().createVillagerStaff();
         case "dragon_katana" -> this.plugin.getDragonKatana().createDragonKatana();
         case "excalibur" -> this.plugin.getExcalibur().createExcalibur();
         case "void_staff" -> this.plugin.getVoidStaff().createVoidStaff();
         case "magma_club" -> this.plugin.getMagmaClub().createMagmaClub();
         case "shrink_ray" -> this.plugin.getShrinkRay().createShrinkRay();
         case "chainsaw_sword" -> this.plugin.getChainsawSword().createChainsawSword();
         case "wither_sickles" -> this.plugin.getWitherSickles().createWitherSickle();
         case "awakened_wither_sickles" -> this.plugin.getAwakenedWitherSickles().createAwakenedWitherSickle();
         case "cloud_sword" -> this.plugin.getCloudSword().createCloudSword();
         case "hypnosis_staff" -> this.plugin.getHypnosisStaff().createHypnosisStaff();
         case "emerald_blade" -> this.plugin.getEmeraldBlade().createEmeraldBlade();
         case "golem_hammer" -> this.plugin.getGolemHammer().createGolemHammer();
         case "toxic_crossbow" -> this.plugin.getToxicCrossbow().createToxicCrossbow();
         case "lich_staff" -> this.plugin.getLichStaff().createLichStaff();
         case "artemis_bow" -> this.plugin.getArtemisBow().createArtemisBow();
         case "poseidon_trident" -> this.plugin.getPoseidonTrident().createPoseidonTrident();
         case "reinforced_elytra" -> this.plugin.getReinforcedElytra().createReinforcedElytra();
         case "ravager_horn" -> this.plugin.getRavagerHorn().createRavagerHorn();
         case "freezing_chakram" -> this.plugin.getFreezingChakram().createFreezingChakram();
         case "sculkweavers_lantern" -> this.plugin.getSculkweaversLantern().createSculkweaversLantern();
         case "war_pick" -> this.plugin.getWarPick().createWarPick();
         case "beehive_blaster" -> this.plugin.getBeehiveBlaster().createBeehiveBlaster();
         case "fugu_cannon" -> this.plugin.getFuguCannon().createFuguCannon();
         default -> null;
      };
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         completions.add("give");
         if (sender.hasPermission("demoralegendaryweapons.reload")) {
            completions.add("reload");
         }

         if (sender.hasPermission("demoralegendaryweapons.language") && sender instanceof Player && sender.isOp()) {
            completions.add("lang");
         }
      } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
         completions.addAll(this.weaponNames);
      } else if (args.length == 2 && args[0].equalsIgnoreCase("lang")) {
         completions.addAll(Arrays.asList("ru", "en", "vn", "de"));
      } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
         for (Player player : Bukkit.getOnlinePlayers()) {
            completions.add(player.getName());
         }
      }

      return completions.stream().filter(completion -> completion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).toList();
   }
}
