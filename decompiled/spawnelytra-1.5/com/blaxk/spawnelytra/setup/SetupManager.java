package com.blaxk.spawnelytra.setup;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.integration.BedrockSupport;
import com.blaxk.spawnelytra.util.DisplayNames;
import com.blaxk.spawnelytra.util.MessageUtil;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class SetupManager implements Listener {
   private final Main plugin;
   private final Map<UUID, SetupSession> sessions = new HashMap<>();

   public SetupManager(Main plugin) {
      this.plugin = plugin;
   }

   public boolean isInSetup(Player player) {
      return this.sessions.containsKey(player.getUniqueId());
   }

   public void start(Player player) {
      if (this.isInSetup(player)) {
         MessageUtil.send(player, "setup_already_running");
      } else {
         SetupSession session = new SetupSession(this.plugin, player);
         this.sessions.put(player.getUniqueId(), session);
         session.begin();
      }
   }

   public void exit(Player player, boolean silent) {
      SetupSession session = this.sessions.remove(player.getUniqueId());
      if (session != null) {
         session.end();
         if (!silent) {
            MessageUtil.send(player, "setup_cancelled");
         }
      } else if (!silent) {
         MessageUtil.send(player, "setup_not_running");
      }
   }

   public void setPosition(Player player, int index, Location location) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session == null) {
         MessageUtil.send(player, "setup_not_running");
      } else {
         if (index == 1) {
            session.setPos1(location);
         } else if (index == 2) {
            session.setPos2(location);
         }
      }
   }

   public void selectActivationMode(Player player, String mode) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session == null) {
         MessageUtil.send(player, "setup_not_running");
      } else {
         String normalized = mode.toLowerCase(Locale.ROOT);
         switch (normalized) {
            case "double_jump":
            case "auto":
            case "sneak_jump":
            case "f_key":
               session.setActivationMode(normalized);
               MessageUtil.send(player, "setup_activation_mode_set", Placeholder.unparsed("value", DisplayNames.activationMode(normalized)));
               break;
            default:
               MessageUtil.send(player, "setup_invalid_mode");
         }
      }
   }

   public void toggleBoostActivatedMessage(Player player) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session == null) {
         MessageUtil.send(player, "setup_not_running");
      } else {
         boolean newValue = !session.isShowBoostActivated();
         session.setShowBoostActivated(newValue);
         String valueLocalized = MessageUtil.plain(newValue ? "state_on" : "state_off");
         MessageUtil.send(player, "setup_toggled_boost_activated", Placeholder.unparsed("value", valueLocalized));
      }
   }

   public void togglePressToBoostMessage(Player player) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session == null) {
         MessageUtil.send(player, "setup_not_running");
      } else {
         boolean newValue = !session.isShowPressToBoost();
         session.setShowPressToBoost(newValue);
         String valueLocalized = MessageUtil.plain(newValue ? "state_on" : "state_off");
         MessageUtil.send(player, "setup_toggled_press_to_boost", Placeholder.unparsed("value", valueLocalized));
      }
   }

   public void save(Player player) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session == null) {
         MessageUtil.send(player, "setup_not_running");
      } else if (!session.hasBothPositions()) {
         MessageUtil.send(player, "setup_missing_positions");
      } else {
         Location p1 = session.getPos1();
         Location p2 = session.getPos2();
         String worldName = Objects.requireNonNull(p1.getWorld()).getName();
         String path = "worlds." + worldName;
         ConfigurationSection worldSec = this.plugin.getConfig().getConfigurationSection(path);
         if (worldSec == null) {
            this.plugin.getConfig().createSection(path);
         }

         this.plugin.getConfig().set(path + ".enabled", true);
         this.plugin.getConfig().set(path + ".spawn_area.mode", "advanced");
         this.plugin.getConfig().set(path + ".spawn_area.area_type", "rectangular");
         this.plugin.getConfig().set(path + ".spawn_area.x", p1.getX());
         this.plugin.getConfig().set(path + ".spawn_area.y", p1.getY());
         this.plugin.getConfig().set(path + ".spawn_area.z", p1.getZ());
         this.plugin.getConfig().set(path + ".spawn_area.x2", p2.getX());
         this.plugin.getConfig().set(path + ".spawn_area.y2", p2.getY());
         this.plugin.getConfig().set(path + ".spawn_area.z2", p2.getZ());
         if (session.getActivationMode() != null) {
            this.plugin.getConfig().set(path + ".activation_mode", session.getActivationMode());
         }

         this.plugin.getConfig().set("messages.show_boost_activated", session.isShowBoostActivated());
         this.plugin.getConfig().set("messages.show_press_to_boost", session.isShowPressToBoost());
         this.plugin.saveConfig();
         player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.8F, 1.2F);
         MessageUtil.send(player, "setup_saved");
         SchedulerUtil.runAtEntityLater(this.plugin, player, 2L, this.plugin::reload);
      }
   }

   public void showOptions(Player player) {
      SetupSession session = this.sessions.get(player.getUniqueId());
      if (session != null && session.hasBothPositions()) {
         MessageUtil.send(player, "setup_options_header");
         String current = session.getActivationMode();
         if (current == null) {
            String worldName = player.getWorld().getName();
            current = this.plugin.getConfig().getString("worlds." + worldName + ".activation_mode", "double_jump");
         }

         if (BedrockSupport.isBedrockPlayer(player)) {
            this.showOptionsPlain(player, session, current);
         } else {
            String text = "<#91f251>"
               + this.option(
                  "/spawnelytra setup mode double_jump",
                  DisplayNames.activationMode("double_jump"),
                  "activation_mode_hover_double_jump",
                  "double_jump".equalsIgnoreCase(current)
               )
               + " "
               + this.option(
                  "/spawnelytra setup mode auto", DisplayNames.activationMode("auto"), "activation_mode_hover_auto", "auto".equalsIgnoreCase(current)
               )
               + " "
               + this.option(
                  "/spawnelytra setup mode sneak_jump",
                  DisplayNames.activationMode("sneak_jump"),
                  "activation_mode_hover_sneak_jump",
                  "sneak_jump".equalsIgnoreCase(current)
               )
               + " "
               + this.option(
                  "/spawnelytra setup mode f_key", DisplayNames.activationMode("f_key"), "activation_mode_hover_f_key", "f_key".equalsIgnoreCase(current)
               );
            MessageUtil.sendRaw(player, MiniMessage.miniMessage().deserialize(text));
            boolean showBoost = session.isShowBoostActivated();
            boolean showPress = session.isShowPressToBoost();
            String boostLabel = MessageUtil.plain("setup_toggle_boost_label");
            String pressLabel = MessageUtil.plain("setup_toggle_press_label");
            String toggles = "<#5db3ff>"
               + this.labeledToggle(boostLabel, "/spawnelytra setup toggle boost", showBoost)
               + " "
               + this.labeledToggle(pressLabel, "/spawnelytra setup toggle press", showPress);
            MessageUtil.sendRaw(player, MiniMessage.miniMessage().deserialize(toggles));
            String saveLabel = MessageUtil.plain("setup_actions_save_label");
            String saveHover = MessageUtil.plain("setup_actions_save_hover");
            String exitLabel = MessageUtil.plain("setup_actions_exit_label");
            String exitHover = MessageUtil.plain("setup_actions_exit_hover");
            String actions = "<#ffd166>[<click:run_command:'/spawnelytra setup save'><hover:show_text:'<#ffd166>"
               + saveHover
               + "'>"
               + saveLabel
               + "</hover></click>] <#aaa8a8>[<click:run_command:'/spawnelytra setup exit'><hover:show_text:'<#fd5e5e>"
               + exitHover
               + "'>"
               + exitLabel
               + "</hover></click>]";
            MessageUtil.sendRaw(player, MiniMessage.miniMessage().deserialize(actions));
         }
      }
   }

   private void showOptionsPlain(Player player, SetupSession session, String currentMode) {
      MessageUtil.sendRaw(
         player,
         MiniMessage.miniMessage()
            .deserialize(
               "<#91f251>/spawnelytra setup mode <#ffd166>double_jump<#aaa8a8> | <#ffd166>auto<#aaa8a8> | <#ffd166>sneak_jump<#aaa8a8> | <#ffd166>f_key <#aaa8a8>(current: <#91f251>"
                  + currentMode
                  + "<#aaa8a8>)"
            )
      );
      String onLabel = MessageUtil.plain("state_on");
      String offLabel = MessageUtil.plain("state_off");
      String boostLabel = MessageUtil.plain("setup_toggle_boost_label");
      String pressLabel = MessageUtil.plain("setup_toggle_press_label");
      String boostState = session.isShowBoostActivated() ? "<#91f251>" + onLabel : "<#fd5e5e>" + offLabel;
      String pressState = session.isShowPressToBoost() ? "<#91f251>" + onLabel : "<#fd5e5e>" + offLabel;
      MessageUtil.sendRaw(
         player, MiniMessage.miniMessage().deserialize("<#5db3ff>" + boostLabel + ": " + boostState + " <#aaa8a8>- /spawnelytra setup toggle boost")
      );
      MessageUtil.sendRaw(
         player, MiniMessage.miniMessage().deserialize("<#5db3ff>" + pressLabel + ": " + pressState + " <#aaa8a8>- /spawnelytra setup toggle press")
      );
      MessageUtil.sendRaw(player, MiniMessage.miniMessage().deserialize("<#ffd166>/spawnelytra setup save <#aaa8a8>• <#fd5e5e>/spawnelytra setup exit"));
   }

   private String option(String cmd, String label, String hoverKey, boolean selected) {
      String hoverText = MessageUtil.plain(hoverKey);
      String open = "[<click:run_command:'" + cmd + "'><hover:show_text:'" + hoverText + "'>";
      String core = selected ? "<underlined>" + label + "</underlined>" : label;
      return open + core + "</hover></click>]";
   }

   private String labeledToggle(String label, String cmd, boolean on) {
      String onLabel = MessageUtil.plain("state_on");
      String offLabel = MessageUtil.plain("state_off");
      String toggleHover = MessageUtil.plain("toggle_hover");
      String state = on ? "<#91f251>" + onLabel : "<#fd5e5e>" + offLabel;
      return label + ": [<click:run_command:'" + cmd + "'><hover:show_text:'<#5db3ff>" + toggleHover + "'>" + state + "</hover></click>]";
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      SetupSession session = this.sessions.remove(p.getUniqueId());
      if (session != null) {
         session.end();
      }
   }

   public void stopAll() {
      for (Player p : Bukkit.getOnlinePlayers()) {
         SetupSession session = this.sessions.remove(p.getUniqueId());
         if (session != null) {
            session.end();
         }
      }
   }
}
