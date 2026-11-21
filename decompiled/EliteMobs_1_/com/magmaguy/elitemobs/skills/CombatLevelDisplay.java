package com.magmaguy.elitemobs.skills;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.CombatHealthFormatter;
import com.magmaguy.elitemobs.combatsystem.combattag.PlayerCombatState;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FollowingText;
import com.magmaguy.elitemobs.easyminecraftgoals.thirdparty.BedrockChecker;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;

public class CombatLevelDisplay implements Listener {
   private static final float DEFAULT_Y_TRANSLATION = 0.5F;
   private static final float BEDROCK_Y_TRANSLATION_BONUS = 1.0F;
   private static final int FOLLOWING_TEXT_INTERPOLATION_TICKS = 3;
   private static final Map<UUID, CombatLevelDisplay.PlayerDisplay> playerDisplays = new ConcurrentHashMap<>();
   private static PlayerCombatState combatState;
   private static BukkitTask refreshTask;

   public static void setCombatState(PlayerCombatState state) {
      combatState = state;
   }

   private static boolean shouldShowHealth(Player player) {
      return !player.isDead() && combatState != null && combatState.isInCombat(player.getUniqueId());
   }

   private static String renderText(Player player, boolean showHealth) {
      String identity = SkillsConfig.isWorldExcludedFromClassLabels(player) && PlayerIdentityLabelRenderer.hasClassLabel(player.getUniqueId())
         ? ""
         : PlayerIdentityLabelRenderer.render(player.getUniqueId());
      if (!showHealth) {
         return identity;
      } else {
         AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
         double current = player.getHealth();
         double maximum = maxHealth == null ? current : maxHealth.getValue();
         String health = "&c❤ " + CombatHealthFormatter.format(current) + "/" + CombatHealthFormatter.format(maximum);
         return identity.isBlank() ? health : health + "\n&r" + identity;
      }
   }

   public static void createDisplay(Player player) {
      if (!shouldRender(player)) {
         removeDisplay(player);
      } else if (NMSManager.getAdapter() != null) {
         removeDisplay(player);
         boolean showHealth = shouldShowHealth(player);
         String text = renderText(player, showHealth);
         if (!text.isBlank()) {
            boolean passenger = shouldUsePassenger(player);
            FakeText fakeText = NMSManager.getAdapter()
               .fakeTextBuilder()
               .text(ChatColorConverter.convert(text))
               .billboard(Billboard.CENTER)
               .shadow(true)
               .seeThrough(false)
               .translation(0.0F, passenger ? getDisplayHeight(player) : 0.0F, 0.0F)
               .teleportDuration(passenger ? 0 : 3)
               .viewerFilter(viewer -> canSeeNameTag(player, viewer))
               .build(player.getLocation());
            FollowingText tracking = null;
            if (passenger) {
               fakeText.attachTo(player);
            } else {
               tracking = new FollowingText(
                  fakeText,
                  player,
                  () -> player.getLocation().add(0.0, player.getHeight() + getDisplayHeight(player), 0.0),
                  viewer -> canSeeNameTag(player, viewer)
               );
            }

            playerDisplays.put(player.getUniqueId(), new CombatLevelDisplay.PlayerDisplay(fakeText, tracking, text));
         }
      }
   }

   private static boolean shouldUsePassenger(Player player) {
      return DefaultConfig.isUsePassengerCombatLevelDisplay() && player.getPassengers().isEmpty();
   }

   private static boolean canSeeNameTag(Player player, Player viewer) {
      if (viewer.canSee(player) && !player.isInvisible()) {
         if (player.isSneaking() && viewer.getLocation().distanceSquared(player.getLocation()) >= 1024.0) {
            return false;
         } else {
            Scoreboard scoreboard = viewer.getScoreboard();
            Team playerTeam = scoreboard.getEntryTeam(player.getName());
            if (playerTeam == null) {
               return true;
            } else {
               Team viewerTeam = scoreboard.getEntryTeam(viewer.getName());
               boolean sameTeam = playerTeam.equals(viewerTeam);

               return switch (playerTeam.getOption(Option.NAME_TAG_VISIBILITY)) {
                  case ALWAYS -> true;
                  case NEVER -> false;
                  case FOR_OWN_TEAM -> sameTeam;
                  case FOR_OTHER_TEAMS -> !sameTeam;
                  default -> throw new MatchException(null, null);
               };
            }
         }
      } else {
         return false;
      }
   }

   private static float getDisplayHeight(Player player) {
      return 0.5F + (BedrockChecker.isBedrock(player) ? 1.0F : 0.0F);
   }

   public static void removeDisplay(Player player) {
      CombatLevelDisplay.PlayerDisplay display = playerDisplays.remove(player.getUniqueId());
      if (display != null) {
         display.close();
      }
   }

   public static void updateDisplay(Player player) {
      if (!shouldRender(player)) {
         removeDisplay(player);
      } else {
         String text = renderText(player, shouldShowHealth(player));
         if (text.isBlank()) {
            removeDisplay(player);
         } else {
            CombatLevelDisplay.PlayerDisplay display = playerDisplays.get(player.getUniqueId());
            if (display == null || !display.isValid(player) || display.isPassenger() != shouldUsePassenger(player)) {
               createDisplay(player);
            } else if (!text.equals(display.text)) {
               display.display.setText(ChatColorConverter.convert(text));
               display.text = text;
            }
         }
      }
   }

   public static void updateDisplay(UUID playerUUID) {
      Player player = Bukkit.getPlayer(playerUUID);
      if (player != null && player.isOnline()) {
         updateDisplay(player);
      }
   }

   public static void shutdown() {
      if (refreshTask != null) {
         refreshTask.cancel();
         refreshTask = null;
      }

      combatState = null;

      for (CombatLevelDisplay.PlayerDisplay display : playerDisplays.values()) {
         display.close();
      }

      playerDisplays.clear();
   }

   public static void initialize() {
      shutdown();
      refreshTask = (new BukkitRunnable() {
         public void run() {
            for (Player player : Bukkit.getOnlinePlayers()) {
               CombatLevelDisplay.updateDisplay(player);
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 20L, 5L);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerQuit(PlayerQuitEvent event) {
      removeDisplay(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerBedEnter(PlayerBedEnterEvent event) {
      if (event.useBed() == Result.ALLOW || event.getBedEnterResult() == BedEnterResult.OK) {
         removeDisplay(event.getPlayer());
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
      removeDisplay(event.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      removeDisplay(event.getPlayer());
   }

   private static boolean shouldRender(Player player) {
      if (!player.isOnline() || player.isDead() || player.isSleeping()) {
         return false;
      } else if (!SkillsConfig.isShowCombatLevelDisplay()) {
         return false;
      } else {
         return PlayerIdentityLabelRenderer.hasClassLabel(player.getUniqueId())
            ? true
            : SkillsConfig.isSkillSystemEnabled() && !SkillsConfig.isWorldExcludedFromSkills(player);
      }
   }

   private static final class PlayerDisplay {
      private final FakeText display;
      private final FollowingText tracking;
      private String text;

      private PlayerDisplay(FakeText display, FollowingText tracking, String text) {
         this.display = display;
         this.tracking = tracking;
         this.text = text;
      }

      private boolean isPassenger() {
         return this.tracking == null;
      }

      private boolean isValid(Player player) {
         return this.tracking != null ? this.tracking.isValid() : this.display.isAutoTracked() && player.equals(this.display.getVehicle());
      }

      private void close() {
         if (this.tracking != null) {
            this.tracking.close();
         } else {
            this.display.detach();
            this.display.remove();
         }
      }
   }
}
