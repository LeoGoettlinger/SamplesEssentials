package me.chancesd.pvpmanager.player.nametag;

import java.util.UUID;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Conf;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

public class BukkitNameTag extends NameTag {
   private Team inCombat;
   private Team pvpOnTeam;
   private Team pvpOffTeam;
   private Team previousTeam;
   private String previousTeamName;
   @NotNull
   private final String combatTeamID = "PVP-" + this.processPlayerID(this.pvPlayer.getUUID());
   private final Scoreboard scoreboard = this.pvPlayer.getPlayer().getScoreboard();
   private static final String PVPOFF = "PvPOff";
   private static final String PVPON = "PvPOn";
   private static final String HEALTHOBJ = "PvP_Health";
   private Objective health;

   public BukkitNameTag(CombatPlayer p) {
      super(p);
      this.setup();
   }

   private void setup() {
      this.setupCombatTeam();
      if (Conf.TOGGLE_NAMETAG_ENABLED.asBool()) {
         this.pvpOnTeam = this.setupToggleTeam(this.pvpOnPrefix, "PvPOn");
         this.pvpOffTeam = this.setupToggleTeam(this.pvpOffPrefix, "PvPOff");
         this.setPvP(this.pvPlayer.hasPvPEnabled());
      }

      if (Conf.HEALTH_BELOW_NAME.asBool() && (this.health == null || this.scoreboard.getObjective("PvP_Health") == null)) {
         if (this.scoreboard.getObjective("PvP_Health") != null) {
            this.health = this.scoreboard.getObjective("PvP_Health");
         } else if (MCVersion.isAtLeast(MCVersion.V1_19)) {
            this.health = this.scoreboard.registerNewObjective("PvP_Health", Criteria.HEALTH, Conf.HEALTH_BELOW_NAME_SYMBOL.asString(), RenderType.HEARTS);
            this.health.setDisplaySlot(DisplaySlot.BELOW_NAME);
         } else {
            this.health = this.scoreboard.registerNewObjective("PvP_Health", "health");
            this.health.setDisplayName(Conf.HEALTH_BELOW_NAME_SYMBOL.asString());
            this.health.setDisplaySlot(DisplaySlot.BELOW_NAME);
         }
      }
   }

   private Team registerTeam(String teamID) {
      synchronized (this.scoreboard) {
         return this.scoreboard.getTeam(teamID) != null ? this.scoreboard.getTeam(teamID) : this.scoreboard.registerNewTeam(teamID);
      }
   }

   private void addToTeam(Team team, String entry) {
      synchronized (this.scoreboard) {
         team.addEntry(entry);
      }
   }

   private void removeFromTeam(Team team, String entry) {
      synchronized (this.scoreboard) {
         team.removeEntry(entry);
      }
   }

   private void setupCombatTeam() {
      if (!this.combatPrefix.isEmpty() || !this.combatSuffix.isEmpty()) {
         this.inCombat = this.registerTeam(this.combatTeamID);
         Log.debug("Creating combat team with name " + this.combatTeamID);
         this.inCombat.setPrefix(this.combatPrefix.replace("%pvpmanager_combat_timeleft%s", "").replace("§8(§7s§8) ", ""));
         this.inCombat.setSuffix(this.combatSuffix.replace(" §f%pvpmanager_player_health%§c❤", ""));
         this.setTeamColor(this.inCombat, this.combatPrefix);
      }
   }

   private void setTeamColor(Team team, String teamPrefix) {
      if (MCVersion.isAtLeast(MCVersion.V1_13)) {
         ChatColor nameColor = this.getLastColor(teamPrefix);
         if (nameColor != null) {
            team.setColor(nameColor);
         }
      }
   }

   private Team setupToggleTeam(String teamPrefix, @NotNull String teamName) {
      if (teamPrefix.isEmpty()) {
         return null;
      } else {
         Team toggleTeam = this.registerTeam(teamName);
         toggleTeam.setCanSeeFriendlyInvisibles(false);
         toggleTeam.setPrefix(teamPrefix);
         this.setTeamColor(toggleTeam, teamPrefix);
         return toggleTeam;
      }
   }

   private String processPlayerID(UUID uuid) {
      String idResult = uuid.toString().replace("-", "");
      return idResult.startsWith("000000000000") ? idResult.substring(17, 29) : idResult.substring(0, 12);
   }

   private ChatColor getLastColor(String string) {
      String lastColors = ChatColor.getLastColors(string);
      return lastColors.isEmpty() ? null : ChatColor.getByChar(lastColors.replace("§", ""));
   }

   @Override
   public final synchronized void setInCombat() {
      this.storePreviousTeam();

      try {
         if (this.inCombat != null) {
            this.addToTeam(this.inCombat, this.pvPlayer.getName());
         }
      } catch (IllegalStateException var2) {
         Log.severe("Failed to add player to combat team");
         Log.info("This warning can be ignored but if it happens often it means one of your plugins is removing PvPManager teams and causing a conflict");
      }
   }

   private void storePreviousTeam() {
      Team team = this.scoreboard.getEntryTeam(this.pvPlayer.getName());
      if (team != null && !team.equals(this.inCombat)) {
         this.previousTeam = team;
         this.previousTeamName = team.getName();
      }
   }

   @Override
   public final synchronized void restoreNametag() {
      try {
         if (this.previousTeamName != null && this.scoreboard.getTeam(this.previousTeamName) != null) {
            this.addToTeam(this.previousTeam, this.pvPlayer.getName());
         } else if (this.inCombat != null) {
            this.removeFromTeam(this.inCombat, this.pvPlayer.getName());
         }
      } catch (IllegalStateException var5) {
         Log.warning("Error restoring nametag for: " + this.pvPlayer.getName(), var5);
      } finally {
         this.previousTeamName = null;
      }
   }

   @Override
   public final synchronized void setPvP(boolean state) {
      if (state) {
         if (this.pvpOnTeam == null) {
            this.restoreNametag();
         } else {
            this.addToTeam(this.pvpOnTeam, this.pvPlayer.getName());
         }
      } else if (this.pvpOffTeam == null) {
         this.restoreNametag();
      } else {
         this.addToTeam(this.pvpOffTeam, this.pvPlayer.getName());
      }
   }

   @Override
   public synchronized void cleanup() {
      if (this.inCombat != null) {
         try {
            synchronized (this.scoreboard) {
               if (this.teamExists(this.combatTeamID)) {
                  Log.debug("Unregistering team: " + this.inCombat.getName());
                  this.inCombat.unregister();
                  this.inCombat = null;
               }
            }
         } catch (IllegalStateException var4) {
            Log.severe("Team was already unregistered for player: " + this.pvPlayer.getName(), var4);
         }
      }
   }

   private boolean teamExists(String teamID) {
      return this.scoreboard.getTeam(teamID) != null;
   }
}
