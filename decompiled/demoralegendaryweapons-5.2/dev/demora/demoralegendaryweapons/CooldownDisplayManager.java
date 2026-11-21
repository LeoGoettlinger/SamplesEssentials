package dev.demora.demoralegendaryweapons;

import dev.demora.demoralegendaryweapons.weapons.freezingchakram.FreezingChakramListener;
import dev.demora.demoralegendaryweapons.weapons.sculkweaverslantern.SculkweaversLanternListener;
import dev.demora.demoralegendaryweapons.weapons.warpick.WarPickListener;
import dev.demora.demoralegendaryweapons.weapons.withersickles.SickleCooldownManager;
import dev.demora.demoralegendaryweapons.weapons.withersickles.rework.AwakenedSickleCooldownManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class CooldownDisplayManager {
   private final DemoraLegendaryWeapons plugin;
   private final Map<String, CooldownDisplayManager.WeaponCooldownChecker> weaponCheckers;

   public CooldownDisplayManager(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
      this.weaponCheckers = new LinkedHashMap<>();
      this.initializeWeaponCheckers();
      this.startUpdateTask();
   }

   private void initializeWeaponCheckers() {
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getSculkCrossbow().createSculkCrossbow(), "sculk_crossbow"),
            player -> this.plugin.getSculkCrossbow().isOnCooldown(player) ? this.plugin.getSculkCrossbow().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getMjolnir().createMjolnir(), "mjolnir"),
            player -> this.plugin.getMjolnir().isOnThrowCooldown(player) ? this.plugin.getMjolnir().getRemainingThrowCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getShadowBlade().createShadowBlade(), "shadow_blade"),
            player -> this.plugin.getShadowBlade().isOnCooldown(player) ? this.plugin.getShadowBlade().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getReaperScythe().createReaperScythe(), "reaper_scythe"),
            player -> this.plugin.getReaperScythe().isOnCooldown(player) ? this.plugin.getReaperScythe().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getVillagerStaff().createVillagerStaff(), "villager_staff"),
            player -> this.plugin.getVillagerStaff().isOnCooldown(player) ? this.plugin.getVillagerStaff().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getDragonKatana().createDragonKatana(), "dragon_katana"),
            player -> this.plugin.getDragonKatana().isOnCooldown(player) ? this.plugin.getDragonKatana().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getAwakenedDragonKatana().createAwakenedDragonKatana(), "awakened_dragon_katana"),
            player -> this.plugin.getAwakenedDragonKatana().isOnCooldown(player) ? this.plugin.getAwakenedDragonKatana().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getExcalibur().createExcalibur(), "excalibur"),
            player -> this.plugin.getExcalibur().isOnCooldown(player) ? this.plugin.getExcalibur().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getVoidStaff().createVoidStaff(), "void_staff"),
            player -> this.plugin.getVoidStaff().isOnPortalCooldown(player) ? this.plugin.getVoidStaff().getRemainingPortalCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getMagmaClub().createMagmaClub(), "magma_club"),
            player -> this.plugin.getMagmaClub().isOnCooldown(player) ? this.plugin.getMagmaClub().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getShrinkRay().createShrinkRay(), "shrink_ray"),
            player -> this.plugin.getShrinkRay().isOnCooldown(player) ? this.plugin.getShrinkRay().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getChainsawSword().createChainsawSword(), "chainsaw_sword"),
            player -> this.plugin.getChainsawSwordListener() != null && this.plugin.getChainsawSwordListener().isOnCurseCooldown(player)
               ? this.plugin.getChainsawSwordListener().getRemainingCurseCooldown(player)
               : 0L
         );
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getWitherSickles().createWitherSickle(), "wither_sickles"), player -> {
         if (this.plugin.getWitherSicklesListener() != null) {
            SickleCooldownManager manager = this.plugin.getWitherSicklesListener().getCooldownManager();
            if (manager != null && manager.isOnCooldown(player)) {
               return manager.getRemainingCooldown(player);
            }
         }

         return 0L;
      });
      this.weaponCheckers
         .put(this.getWeaponName(() -> this.plugin.getAwakenedWitherSickles().createAwakenedWitherSickle(), "awakened_wither_sickles"), player -> {
            if (this.plugin.getAwakenedWitherSicklesListener() != null) {
               AwakenedSickleCooldownManager manager = this.plugin.getAwakenedWitherSicklesListener().getCooldownManager();
               if (manager != null && manager.isOnCooldown(player)) {
                  return manager.getRemainingCooldown(player);
               }
            }

            return 0L;
         });
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getCloudSword().createCloudSword(), "cloud_sword"),
            player -> this.plugin.getCloudSwordListener() != null && this.plugin.getCloudSwordListener().isOnCooldown(player)
               ? this.plugin.getCloudSwordListener().getRemainingCooldown(player)
               : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getHypnosisStaff().createHypnosisStaff(), "hypnosis_staff"),
            player -> this.plugin.getHypnosisStaffListener() != null && this.plugin.getHypnosisStaffListener().isOnCooldown(player)
               ? this.plugin.getHypnosisStaffListener().getRemainingCooldown(player)
               : 0L
         );
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getEmeraldBlade().createEmeraldBlade(), "emerald_blade"), player -> 0L);
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getGolemHammer().createGolemHammer(), "golem_hammer"),
            player -> this.plugin.getGolemHammerListener() != null && this.plugin.getGolemHammerListener().isOnCooldown(player)
               ? this.plugin.getGolemHammerListener().getRemainingCooldown(player)
               : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getToxicCrossbow().createToxicCrossbow(), "toxic_crossbow"),
            player -> this.plugin.getToxicCrossbowListener() != null && this.plugin.getToxicCrossbowListener().isOnCooldown(player)
               ? this.plugin.getToxicCrossbowListener().getRemainingCooldown(player)
               : 0L
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getLichStaff().createLichStaff(), "lich_staff"),
            player -> this.plugin.getLichStaff().isOnCooldown(player) ? this.plugin.getLichStaff().getRemainingCooldown(player) : 0L
         );
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getPoseidonTrident().createPoseidonTrident(), "poseidon_trident"), player -> {
         long throwCooldown = this.plugin.getPoseidonTrident().getRemainingThrowCooldown(player.getUniqueId());
         long riptideCooldown = this.plugin.getPoseidonTrident().getRemainingRiptideCooldown(player.getUniqueId());
         return Math.max(throwCooldown, riptideCooldown);
      });
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getReinforcedElytra().createReinforcedElytra(), "reinforced_elytra"),
            player -> {
               long boost = this.plugin.getReinforcedElytra().isOnBoostCooldown(player.getUniqueId())
                  ? this.plugin.getReinforcedElytra().getRemainingBoostCooldownSeconds(player.getUniqueId())
                  : 0L;
               long landing = this.plugin.getReinforcedElytra().getRemainingExplosionLandingCooldownSeconds(player.getUniqueId());
               return (int)Math.max(boost, landing);
            }
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getReworkElytra().createReworkElytra(), "rework_elytra"),
            player -> {
               long boost = this.plugin.getReworkElytra().isOnBoostCooldown(player.getUniqueId())
                  ? this.plugin.getReworkElytra().getRemainingBoostCooldownSeconds(player.getUniqueId())
                  : 0L;
               long landing = this.plugin.getReworkElytra().getRemainingExplosionLandingCooldownSeconds(player.getUniqueId());
               return (int)Math.max(boost, landing);
            }
         );
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getRavagerHorn().createRavagerHorn(), "ravager_horn"),
            player -> this.plugin.getRavagerHornListener() == null
               ? 0L
               : this.plugin.getRavagerHornListener().getRemainingCooldownSeconds(player.getUniqueId())
         );
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getFreezingChakram().createFreezingChakram(), "freezing_chakram"), player -> {
         FreezingChakramListener chakramListener = this.plugin.getFreezingChakramListener();
         return chakramListener != null ? chakramListener.getRemainingCooldownSeconds(player.getUniqueId()) : 0L;
      });
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getSculkweaversLantern().createSculkweaversLantern(), "sculkweavers_lantern"), player -> {
         SculkweaversLanternListener lanternListener = this.plugin.getSculkweaversLanternListener();
         return lanternListener != null ? lanternListener.getRemainingCooldownSeconds(player.getUniqueId()) : 0L;
      });
      this.weaponCheckers.put(this.getWeaponName(() -> this.plugin.getWarPick().createWarPick(), "war_pick"), player -> {
         WarPickListener listener = this.plugin.getWarPickListener();
         return listener != null ? listener.getRemainingCooldownSeconds(player.getUniqueId()) : 0L;
      });
      this.weaponCheckers
         .put(
            this.getWeaponName(() -> this.plugin.getBeehiveBlaster().createBeehiveBlaster(), "beehive_blaster"),
            player -> this.plugin.getBeehiveBlasterListener() != null
               ? this.plugin.getBeehiveBlasterListener().getRemainingCooldownSeconds(player.getUniqueId())
               : 0L
         );
   }

   private void startUpdateTask() {
      (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               CooldownDisplayManager.this.updateCooldownDisplay(player);
            }
         }
      }).runTaskTimer(this.plugin, 0L, 20L);
   }

   private void updateCooldownDisplay(Player player) {
      List<String> cooldownEntries = new ArrayList<>();

      for (Entry<String, CooldownDisplayManager.WeaponCooldownChecker> entry : this.weaponCheckers.entrySet()) {
         String weaponId = entry.getKey();
         if (this.playerHasWeapon(player, weaponId) && !"void_staff".equals(weaponId)) {
            long remaining = entry.getValue().getRemainingCooldown(player);
            if (remaining > 0L) {
               String weaponName = this.getTranslatedWeaponName(weaponId);
               String formatted = weaponName + " §f- §c" + remaining;
               cooldownEntries.add(formatted);
            }
         }
      }

      StringBuilder actionBar = new StringBuilder();
      if (this.plugin.getLichStaff() != null) {
         ItemStack heldItem = player.getInventory().getItemInMainHand();
         if (this.plugin.getLichStaff().isLichStaff(heldItem)) {
            int ammo = this.plugin.getLichStaff().getAmmo(player);
            if (ammo == 0 && !this.plugin.getLichStaff().isOnCooldown(player)) {
               this.plugin.getLichStaff().reloadAmmo(player);
               ammo = this.plugin.getLichStaff().getAmmo(player);
            }

            int maxAmmo = this.plugin.getLichStaff().getMaxAmmo();

            for (int i = 0; i < maxAmmo; i++) {
               if (i < ammo) {
                  actionBar.append("\ue033");
               } else {
                  actionBar.append("\ue034");
               }
            }

            if (!cooldownEntries.isEmpty()) {
               actionBar.append(" §f| ");
            }
         }
      }

      if (!cooldownEntries.isEmpty()) {
         actionBar.append(String.join(" §f| ", cooldownEntries));
      }

      if (actionBar.length() > 0) {
         player.sendActionBar(LegacyComponentSerializer.legacySection().deserialize(actionBar.toString()));
      }
   }

   private String getWeaponName(Supplier<ItemStack> itemSupplier, String weaponId) {
      return weaponId;
   }

   private String getTranslatedWeaponName(String weaponId) {
      String weaponName = this.plugin.getLanguageManager().getWeaponName(weaponId);
      return weaponName.isEmpty() ? weaponId : weaponName;
   }

   private boolean playerHasWeapon(Player player, String weaponId) {
      if (!"reinforced_elytra".equals(weaponId) && !"rework_elytra".equals(weaponId)) {
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         ItemStack offHand = player.getInventory().getItemInOffHand();
         return this.isWeapon(mainHand, weaponId) || this.isWeapon(offHand, weaponId);
      } else {
         ItemStack chest = player.getInventory().getChestplate();
         return this.isWeapon(chest, weaponId);
      }
   }

   private boolean isWeapon(ItemStack item, String weaponId) {
      if (item != null && item.hasItemMeta()) {
         return switch (weaponId) {
            case "sculk_crossbow" -> this.plugin.getSculkCrossbow().isSculkCrossbow(item);
            case "mjolnir" -> this.plugin.getMjolnir().isMjolnir(item);
            case "shadow_blade" -> this.plugin.getShadowBlade().isShadowBlade(item);
            case "reaper_scythe" -> this.plugin.getReaperScythe().isReaperScythe(item);
            case "villager_staff" -> this.plugin.getVillagerStaff().isVillagerStaff(item);
            case "dragon_katana" -> this.plugin.getDragonKatana().isDragonKatana(item);
            case "awakened_dragon_katana" -> this.plugin.getAwakenedDragonKatana().isAwakenedDragonKatana(item);
            case "excalibur" -> this.plugin.getExcalibur().isExcalibur(item);
            case "void_staff" -> this.plugin.getVoidStaff().isVoidStaff(item);
            case "magma_club" -> this.plugin.getMagmaClub().isMagmaClub(item);
            case "shrink_ray" -> this.plugin.getShrinkRay().isShrinkRay(item);
            case "chainsaw_sword" -> this.plugin.getChainsawSword().isChainsawSword(item);
            case "wither_sickles" -> this.plugin.getWitherSickles().isWitherSickle(item);
            case "awakened_wither_sickles" -> this.plugin.getAwakenedWitherSickles().isAwakenedWitherSickle(item);
            case "cloud_sword" -> this.plugin.getCloudSword().isCloudSword(item);
            case "hypnosis_staff" -> this.plugin.getHypnosisStaff().isHypnosisStaff(item);
            case "emerald_blade" -> this.plugin.getEmeraldBlade().isEmeraldBlade(item);
            case "golem_hammer" -> this.plugin.getGolemHammer().isGolemHammer(item);
            case "toxic_crossbow" -> this.plugin.getToxicCrossbow().isToxicCrossbow(item);
            case "lich_staff" -> this.plugin.getLichStaff().isLichStaff(item);
            case "poseidon_trident" -> this.plugin.getPoseidonTrident().isPoseidonTrident(item);
            case "reinforced_elytra" -> this.plugin.getReinforcedElytra().isReinforcedElytra(item);
            case "rework_elytra" -> this.plugin.getReworkElytra().isReworkElytra(item);
            case "ravager_horn" -> this.plugin.getRavagerHorn().isRavagerHorn(item);
            case "freezing_chakram" -> this.plugin.getFreezingChakram().isFreezingChakram(item);
            case "sculkweavers_lantern" -> this.plugin.getSculkweaversLantern().isSculkweaversLantern(item);
            case "war_pick" -> this.plugin.getWarPick().isWarPick(item);
            case "beehive_blaster" -> this.plugin.getBeehiveBlaster().isBeehiveBlaster(item);
            default -> false;
         };
      } else {
         return false;
      }
   }

   @FunctionalInterface
   private interface WeaponCooldownChecker {
      long getRemainingCooldown(Player player);
   }
}
