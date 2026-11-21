package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.messages.EMFConfigReader;
import org.evenmorefish.fish.libs.boostedyaml.YamlDocument;
import org.evenmorefish.fish.libs.boostedyaml.settings.updater.UpdaterSettings;

public class MessageConfig extends ConfigBase {
   private static MessageConfig instance = null;
   private EMFConfigReader messageLoader;
   private final EMFConfigReader defaultMessageLoader;

   public MessageConfig() {
      super("messages.yml", "locales/messages_" + MainConfig.getInstance().getLocale() + ".yml", EvenMoreFish.getInstance(), true);
      instance = this;
      this.messageLoader = new EMFConfigReader(this.getConfig());
      YamlDocument defaults = this.getConfig().getDefaults();
      if (defaults == null) {
         throw new IllegalStateException("MessageConfig has no assigned defaults.");
      } else {
         this.defaultMessageLoader = new EMFConfigReader(defaults);
      }
   }

   public static MessageConfig getInstance() {
      return instance;
   }

   public EMFConfigReader getMessageLoader() {
      return this.messageLoader;
   }

   public EMFConfigReader getDefaultMessageLoader() {
      return this.defaultMessageLoader;
   }

   @Override
   public void reload() {
      super.reload();
      this.messageLoader = new EMFConfigReader(this.getConfig());
   }

   public int getLeaderboardCount() {
      return this.getConfig().getInt("leaderboard.leaderboard-count", 5);
   }

   @Override
   public UpdaterSettings getUpdaterSettings() {
      return UpdaterSettings.builder(super.getUpdaterSettings()).addCustomLogic("1", document -> {
         if (document.contains("bossbar.hour-color")) {
            String hourColor = document.getString("bossbar.hour-color", "<white>");
            String hour = document.getString("bossbar.hour", "h");
            document.set("bossbar.hour", hourColor + "{hour}" + hour);
            document.remove("bossbar.hour-color");
         }

         if (document.contains("bossbar.minute-color")) {
            String minuteColor = document.getString("bossbar.minute-color", "<white>");
            String minute = document.getString("bossbar.minute", "m");
            document.set("bossbar.minute", minuteColor + "{minute}" + minute);
            document.remove("bossbar.minute-color");
         }

         if (document.contains("bossbar.second-color")) {
            String secondColor = document.getString("bossbar.second-color", "<white>");
            String second = document.getString("bossbar.second", "s");
            document.set("bossbar.second", secondColor + "{second}" + second);
            document.remove("bossbar.second-color");
         }
      }).addCustomLogic("1", document -> {
         if (document.contains("prefix")) {
            String prefix = document.getString("prefix");
            String oldRegular = document.getString("prefix-regular");
            document.set("prefix-regular", oldRegular + prefix);
            String oldAdmin = document.getString("prefix-admin");
            document.set("prefix-admin", oldAdmin + prefix);
            String oldError = document.getString("prefix-error");
            document.set("prefix-error", oldError + prefix);
            document.remove("prefix");
         }
      }).addCustomLogic("6", document -> {
         document.move("bossbar.hour", "duration.hour");
         document.move("bossbar.minute", "duration.minute");
         document.move("bossbar.second", "duration.second");
      }).addCustomLogic("7", document -> {
         document.move("toggle-on", "toggle.fishing.on");
         document.move("toggle-off", "toggle.fishing.off");
      }).addCustomLogic("9", document -> {
         document.move("emf-time-remaining", "emf-time-remaining.inactive");
         document.remove("emf-time-remaining-during-comp");
      }).addCustomLogic("10", document -> {
         document.move("leaderboard-largest-fish", "leaderboard.largest-fish");
         document.move("leaderboard-largest-total", "leaderboard.largest-total");
         document.move("leaderboard-most-fish", "leaderboard.most-fish");
         document.move("leaderboard-shortest-fish", "leaderboard.shortest-fish");
         document.move("leaderboard-shortest-total", "leaderboard.shortest-total");
         document.move("single-winner", "leaderboard.single-winner");
         document.move("total-players", "leaderboard.total-players");
         document.move("leaderboard-count", "leaderboard.leaderboard-count");
         document.move("no-winners", "leaderboard.no-winners");
         document.move("no-records", "leaderboard.no-records");
      }).build();
   }
}
