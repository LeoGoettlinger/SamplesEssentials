package de.oliver.fancynpcs;

import com.fancyinnovations.config.ConfigHelper;
import de.oliver.fancynpcs.api.FancyNpcsConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.configuration.file.FileConfiguration;

public class FancyNpcsConfigImpl implements FancyNpcsConfig {
   private String language;
   private boolean skipInvisibleNpcs;
   private boolean disabledInteractionCooldownMessage;
   private boolean muteVersionNotification;
   private boolean enableAutoSave;
   private int autoSaveInterval;
   private int npcUpdateInterval;
   private int npcUpdateVisibilityInterval;
   private int turnToPlayerTrackerInterval;
   private boolean registerCommands;
   private int turnToPlayerDistance;
   private boolean turnToPlayerResetToInitialDirection;
   private int visibilityDistance;
   private int removeNpcsFromPlayerlistDelay;
   private boolean swingArmOnUpdate;
   private String mineskinApiKey;
   private int mineskinTimeout;
   private List<String> blockedCommands;
   private Map<String, Integer> maxNpcsPerPermission;

   public void reload() {
      FancyNpcs.getInstance().reloadConfig();
      FileConfiguration config = FancyNpcs.getInstance().getConfig();
      this.language = (String)ConfigHelper.getOrDefault(config, "language", "default");
      config.setInlineComments("language", List.of("Language to use for translatable messages."));
      this.skipInvisibleNpcs = (Boolean)ConfigHelper.getOrDefault(config, "skip_invisible_npcs", true);
      config.setInlineComments("skip_invisible_npcs", List.of("Whether invisible NPCs should not be sent to the player."));
      this.disabledInteractionCooldownMessage = (Boolean)ConfigHelper.getOrDefault(config, "disable_interaction_cooldown_message", false);
      config.setInlineComments("disable_interaction_cooldown_message", List.of("Whether interaction cooldown messages are disabled."));
      this.muteVersionNotification = (Boolean)ConfigHelper.getOrDefault(config, "mute_version_notification", false);
      config.setInlineComments("mute_version_notification", List.of("Whether version notifications are muted."));
      this.enableAutoSave = (Boolean)ConfigHelper.getOrDefault(config, "enable_autosave", true);
      config.setInlineComments("enable_autosave", List.of("Whether autosave is enabled."));
      this.autoSaveInterval = (Integer)ConfigHelper.getOrDefault(config, "autosave_interval", 15);
      config.setInlineComments("autosave_interval", List.of("The interval at which autosave is performed in minutes."));
      this.npcUpdateInterval = (Integer)ConfigHelper.getOrDefault(config, "npc_update_interval", 60);
      config.setInlineComments(
         "npc_update_interval", List.of("The interval at which the NPC is updated (in seconds). Only if the skin or displayName is a placeholder.")
      );
      this.npcUpdateVisibilityInterval = (Integer)ConfigHelper.getOrDefault(config, "npc_update_visibility_interval", 20);
      config.setInlineComments("npc_update_visibility_interval", List.of("The interval at which the NPC visibility is updated (in ticks)."));
      this.turnToPlayerTrackerInterval = (Integer)ConfigHelper.getOrDefault(config, "turn_to_player_tracker_interval", 3);
      config.setInlineComments("turn_to_player_tracker_interval", List.of("The interval at which the turn-to-player tracker runs (in ticks)."));
      this.registerCommands = (Boolean)ConfigHelper.getOrDefault(config, "register_commands", true);
      config.setInlineComments("register_commands", List.of("Whether the plugin should register its commands."));
      this.turnToPlayerDistance = (Integer)ConfigHelper.getOrDefault(config, "turn_to_player_distance", 5);
      config.setInlineComments("turn_to_player_distance", List.of("The distance at which NPCs turn to the player."));
      this.turnToPlayerResetToInitialDirection = (Boolean)ConfigHelper.getOrDefault(config, "turn_to_player_reset_to_initial_direction", false);
      config.setInlineComments(
         "turn_to_player_reset_to_initial_direction", List.of("Whether direction of NPC should be reset when leaving their turning range.")
      );
      this.visibilityDistance = (Integer)ConfigHelper.getOrDefault(config, "visibility_distance", 20);
      config.setInlineComments("visibility_distance", List.of("The distance at which NPCs are visible."));
      this.mineskinApiKey = (String)ConfigHelper.getOrDefault(config, "mineskin_api_key", "");
      config.setInlineComments(
         "mineskin_api_key",
         List.of("The API key for the MineSkin API. This will be used to load skins faster. You can get an API key at https://mineskin.org/account.")
      );
      this.mineskinTimeout = (Integer)ConfigHelper.getOrDefault(config, "mineskin_timeout", 3000);
      config.setInlineComments("mineskin_timeout", List.of("Timeout for the MineSkin API in milliseconds."));
      this.removeNpcsFromPlayerlistDelay = (Integer)ConfigHelper.getOrDefault(config, "remove_npcs_from_playerlist_delay", 2000);
      config.setInlineComments(
         "remove_npcs_from_playerlist_delay",
         List.of(
            "The delay in milliseconds to remove NPCs from the player list. Increase this value if you have problems with skins not loading correctly when joining or switching worlds. You can set it to -1, if you don't have any npcs using the show_in_tab feature."
         )
      );
      this.swingArmOnUpdate = (Boolean)ConfigHelper.getOrDefault(config, "swing_arm_on_update", true);
      config.setInlineComments("swing_arm_on_update", List.of("Whether NPCs should swing arm on update."));
      this.blockedCommands = (List<String>)ConfigHelper.getOrDefault(config, "blocked_commands", Arrays.asList("op", "ban"));
      config.setInlineComments("blocked_commands", List.of("The commands that are blocked for NPCs in the message."));
      if (!config.isSet("max-npcs")) {
         List<Map<String, Integer>> entries = new ArrayList<>();
         entries.add(Map.of("fancynpcs.max-npcs.5", 5));
         entries.add(Map.of("fancynpcs.max-npcs.10", 10));
         config.set("max-npcs", entries);
         config.setInlineComments("max-npcs", List.of("The maximum number of NPCs per permission. (for the 'player-npcs' feature flag only)"));
         this.maxNpcsPerPermission = new HashMap<>();
         this.maxNpcsPerPermission.put("fancynpcs.max-npcs.5", 5);
         this.maxNpcsPerPermission.put("fancynpcs.max-npcs.10", 10);
      } else {
         this.maxNpcsPerPermission = config.getMapList("max-npcs")
            .stream()
            .flatMap(map -> map.entrySet().stream())
            .collect(Collectors.toMap(entry -> (String)entry.getKey(), entry -> (Integer)entry.getValue()));
      }

      FancyNpcs.getInstance().saveConfig();
   }

   public String getLanguage() {
      return this.language;
   }

   @Override
   public boolean isSkipInvisibleNpcs() {
      return this.skipInvisibleNpcs;
   }

   @Override
   public boolean isInteractionCooldownMessageDisabled() {
      return this.disabledInteractionCooldownMessage;
   }

   @Override
   public boolean isMuteVersionNotification() {
      return this.muteVersionNotification;
   }

   @Override
   public boolean isEnableAutoSave() {
      return this.enableAutoSave;
   }

   @Override
   public int getAutoSaveInterval() {
      return this.autoSaveInterval;
   }

   @Override
   public int getNpcUpdateInterval() {
      return this.npcUpdateInterval;
   }

   @Override
   public int getNpcUpdateVisibilityInterval() {
      return this.npcUpdateVisibilityInterval;
   }

   @Override
   public int getTurnToPlayerTrackerInterval() {
      return this.turnToPlayerTrackerInterval;
   }

   public boolean isRegisterCommands() {
      return this.registerCommands;
   }

   @Override
   public boolean isTurnToPlayerResetToInitialDirection() {
      return this.turnToPlayerResetToInitialDirection;
   }

   @Override
   public int getTurnToPlayerDistance() {
      return this.turnToPlayerDistance;
   }

   @Override
   public boolean setTurnToPlayerDistance(int distance) {
      if (distance <= 0 && distance != -1) {
         return false;
      } else {
         this.turnToPlayerDistance = distance;
         FileConfiguration config = FancyNpcs.getInstance().getConfig();
         config.set("turn_to_player_distance", distance);
         FancyNpcs.getInstance().saveConfig();
         return true;
      }
   }

   @Override
   public int getVisibilityDistance() {
      return this.visibilityDistance;
   }

   @Override
   public int getRemoveNpcsFromPlayerlistDelay() {
      return this.removeNpcsFromPlayerlistDelay;
   }

   @Override
   public boolean isSwingArmOnUpdate() {
      return this.swingArmOnUpdate;
   }

   @Override
   public String getMineSkinApiKey() {
      return this.mineskinApiKey.isEmpty() ? null : this.mineskinApiKey;
   }

   @Override
   public int getMineSkinTimeout() {
      return this.mineskinTimeout;
   }

   @Override
   public List<String> getBlockedCommands() {
      return this.blockedCommands;
   }

   @Override
   public Map<String, Integer> getMaxNpcsPerPermission() {
      return this.maxNpcsPerPermission;
   }
}
