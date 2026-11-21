package dev.demora.demoralegendaryweapons.commands;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class DemoraMenuCommand implements CommandExecutor {
   private final DemoraLegendaryWeapons plugin;

   public DemoraMenuCommand(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         sender.sendMessage(DemoraLegendaryWeapons.getInstance().getMessageWithPrefix("commands.only-players"));
         return true;
      } else if (!player.isOp()) {
         player.sendMessage(this.plugin.getMessageWithPrefix("no-permission"));
         return true;
      } else {
         this.openWeaponsMenu(player);
         return true;
      }
   }

   public void openWeaponsMenu(Player player) {
      Inventory menu = Bukkit.createInventory(null, 36, DemoraLegendaryWeapons.getInstance().getMessage("commands.menu-legendary-title"));
      menu.setItem(0, this.createMenuItem(this.plugin.getSculkCrossbow().createSculkCrossbow(), "sculk_crossbow"));
      menu.setItem(1, this.createMenuItem(this.plugin.getMidasSword().createMidasSword(), "midas_sword"));
      menu.setItem(2, this.createMenuItem(this.plugin.getMjolnir().createMjolnir(), "mjolnir"));
      menu.setItem(3, this.createMenuItem(this.plugin.getShadowBlade().createShadowBlade(), "shadow_blade"));
      menu.setItem(4, this.createMenuItem(this.plugin.getReaperScythe().createReaperScythe(), "reaper_scythe"));
      menu.setItem(5, this.createMenuItem(this.plugin.getVillagerStaff().createVillagerStaff(), "villager_staff"));
      menu.setItem(6, this.createMenuItem(this.plugin.getDragonKatana().createDragonKatana(), "dragon_katana"));
      menu.setItem(7, this.createMenuItem(this.plugin.getExcalibur().createExcalibur(), "excalibur"));
      menu.setItem(8, this.createMenuItem(this.plugin.getVoidStaff().createVoidStaff(), "void_staff"));
      menu.setItem(9, this.createMenuItem(this.plugin.getMagmaClub().createMagmaClub(), "magma_club"));
      menu.setItem(10, this.createMenuItem(this.plugin.getShrinkRay().createShrinkRay(), "shrink_ray"));
      menu.setItem(11, this.createMenuItem(this.plugin.getChainsawSword().createChainsawSword(), "chainsaw_sword"));
      menu.setItem(12, this.createMenuItem(this.plugin.getWitherSickles().createWitherSickle(), "wither_sickles"));
      menu.setItem(13, this.createMenuItem(this.plugin.getCloudSword().createCloudSword(), "cloud_sword"));
      menu.setItem(14, this.createMenuItem(this.plugin.getHypnosisStaff().createHypnosisStaff(), "hypnosis_staff"));
      menu.setItem(15, this.createMenuItem(this.plugin.getEmeraldBlade().createEmeraldBlade(), "emerald_blade"));
      menu.setItem(16, this.createMenuItem(this.plugin.getGolemHammer().createGolemHammer(), "golem_hammer"));
      menu.setItem(17, this.createMenuItem(this.plugin.getToxicCrossbow().createToxicCrossbow(), "toxic_crossbow"));
      menu.setItem(18, this.createMenuItem(this.plugin.getLichStaff().createLichStaff(), "lich_staff"));
      menu.setItem(19, this.createMenuItem(this.plugin.getArtemisBow().createArtemisBow(), "artemis_bow"));
      menu.setItem(20, this.createMenuItem(this.plugin.getPoseidonTrident().createPoseidonTrident(), "poseidon_trident"));
      menu.setItem(21, this.createMenuItem(this.plugin.getReinforcedElytra().createReinforcedElytra(), "reinforced_elytra"));
      menu.setItem(22, this.createMenuItem(this.plugin.getRavagerHorn().createRavagerHorn(), "ravager_horn"));
      menu.setItem(23, this.createMenuItem(this.plugin.getFreezingChakram().createFreezingChakram(), "freezing_chakram"));
      menu.setItem(24, this.createMenuItem(this.plugin.getSculkweaversLantern().createSculkweaversLantern(), "sculkweavers_lantern"));
      menu.setItem(25, this.createMenuItem(this.plugin.getWarPick().createWarPick(), "war_pick"));
      menu.setItem(26, this.createMenuItem(this.plugin.getBeehiveBlaster().createBeehiveBlaster(), "beehive_blaster"));
      menu.setItem(27, this.createMenuItem(this.plugin.getFuguCannon().createFuguCannon(), "fugu_cannon"));
      player.openInventory(menu);
   }

   private ItemStack createMenuItem(ItemStack weapon, String weaponId) {
      return weapon.clone();
   }

   public boolean isWeaponsMenu(Inventory inventory) {
      return inventory.getSize() == 36
         && inventory.getViewers().size() > 0
         && "§c§lЛегендарные оружия".equals(((HumanEntity)inventory.getViewers().get(0)).getOpenInventory().getTitle());
   }
}
