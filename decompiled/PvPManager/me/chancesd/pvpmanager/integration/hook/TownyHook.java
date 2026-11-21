package me.chancesd.pvpmanager.integration.hook;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.metadata.BooleanDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import java.util.UUID;
import me.chancesd.pvpmanager.integration.BaseDependency;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.ForceToggleDependency;
import me.chancesd.pvpmanager.integration.type.RegionDependency;
import me.chancesd.pvpmanager.player.ProtectionType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownyHook extends BaseDependency implements ForceToggleDependency, RegionDependency {
   private final TownyAPI townyAPI;
   private final boolean pushbackPeaceful;
   private final BooleanDataField peaceSetting = new BooleanDataField("siegewar_peaceSetting", false);
   private final StringDataField siegeAttackerUUID = new StringDataField("siegewar_attackerUUID", "");
   private final BooleanDataField hasSiege = new BooleanDataField("siegewar_hasSiege", false);

   public TownyHook(Hook hook) {
      super(hook);
      this.townyAPI = TownyAPI.getInstance();
      this.pushbackPeaceful = this.getConfigBoolean("Pushback on Peaceful", false);
   }

   @Override
   public boolean shouldDisable(Player player) {
      Resident resident = this.townyAPI.getResident(player);
      if (resident == null) {
         return false;
      } else {
         Town town = resident.getTownOrNull();
         return town != null && town.hasActiveWar();
      }
   }

   @Override
   public boolean shouldDisable(Player attacker, Player defender, ProtectionType reason) {
      return this.shouldDisable(attacker) && this.shouldDisable(defender) || this.isAtWarWithNation(attacker, defender);
   }

   @Override
   public boolean canAttackAt(Player player, Location location) {
      if (!this.pushbackPeaceful) {
         return this.townyAPI.isPVP(location);
      } else {
         Town town = this.townyAPI.getTown(location);
         return town == null || !this.isNeutral(town);
      }
   }

   private boolean isAtWarWithNation(Player attacker, Player defender) {
      Resident attackerResident = this.townyAPI.getResident(attacker);
      Resident defenderResident = this.townyAPI.getResident(defender);
      if (attackerResident != null && defenderResident != null) {
         Town attackerTown = attackerResident.getTownOrNull();
         Town defenderTown = defenderResident.getTownOrNull();
         if (attackerTown == null || defenderTown == null) {
            return false;
         } else if (this.hasSiege(attackerTown)) {
            return this.isFromAttackingNation(attackerTown, defenderResident);
         } else {
            return this.hasSiege(defenderTown) ? this.isFromAttackingNation(defenderTown, attackerResident) : false;
         }
      } else {
         return false;
      }
   }

   private boolean isFromAttackingNation(Town siegedTown, Resident residentToCheck) {
      Nation nationToCheck = residentToCheck.getNationOrNull();
      if (nationToCheck == null) {
         return false;
      } else {
         String attackerUUIDString = this.getAttackerUUID(siegedTown);
         if (attackerUUIDString != null && !attackerUUIDString.isEmpty()) {
            UUID attackerUUID = UUID.fromString(attackerUUIDString);
            Nation attackingNation = this.townyAPI.getNation(attackerUUID);
            return attackingNation != null && attackingNation.equals(nationToCheck);
         } else {
            return false;
         }
      }
   }

   private boolean isNeutral(Town town) {
      return town.hasMeta(this.peaceSetting.getKey()) ? MetaDataUtil.getBoolean(town, this.peaceSetting) : town.isNeutral();
   }

   private boolean hasSiege(Town town) {
      return town.hasMeta(this.hasSiege.getKey()) ? MetaDataUtil.getBoolean(town, this.hasSiege) : false;
   }

   private String getAttackerUUID(Town town) {
      return town.hasMeta(this.siegeAttackerUUID.getKey()) ? MetaDataUtil.getString(town, this.siegeAttackerUUID) : null;
   }
}
