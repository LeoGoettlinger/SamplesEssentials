package me.usainsrht.ujobs.managers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.models.Action;
import me.usainsrht.ujobs.models.BuiltInActions;
import me.usainsrht.ujobs.models.Job;
import me.usainsrht.ujobs.models.JobInfoLine;
import me.usainsrht.ujobs.models.PlayerJobData;
import me.usainsrht.ujobs.utils.JobExpUtils;
import me.usainsrht.ujobs.yaml.YamlMessage;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class JobManager {
   private final UJobsPlugin plugin;
   private final Map<String, Job> jobs;
   private final Map<Action, Set<String>> actionJobMap;

   public JobManager(UJobsPlugin plugin) {
      this.plugin = plugin;
      this.jobs = new LinkedHashMap<>();
      this.actionJobMap = new HashMap<>();
   }

   public void loadJobs() {
      this.actionJobMap.clear();
      ConfigurationSection jobsSection = this.plugin.getConfigManager().getJobsConfig().getConfigurationSection("jobs");
      if (jobsSection == null) {
         this.plugin.getLogger().warning("No jobs section found in config!");
      } else {
         Map<String, Job> newJobs = new LinkedHashMap<>();

         for (String jobId : jobsSection.getKeys(false)) {
            try {
               Job job = this.loadJob(jobId, jobsSection.getConfigurationSection(jobId));
               if (job != null) {
                  newJobs.put(jobId, job);
               }
            } catch (Exception var6) {
               this.plugin.getLogger().warning("Failed to load job: " + jobId + " - " + var6.getMessage());
               var6.printStackTrace();
            }
         }

         for (Job newJob : newJobs.values()) {
            Job existingJob = this.jobs.get(newJob.getId());
            if (existingJob != null) {
               existingJob.update(newJob);
            } else {
               this.jobs.put(newJob.getId(), newJob);
            }
         }

         this.plugin.getLogger().info("Loaded " + this.jobs.size() + " jobs successfully!");
      }
   }

   private Job loadJob(String jobId, ConfigurationSection jobSection) {
      if (jobSection == null) {
         return null;
      } else {
         String nameText = jobSection.getString("name", jobId);
         Component name = this.plugin.getMiniMessage().deserialize(nameText).compact();
         String iconString = jobSection.getString("icon", "STONE");

         Material icon;
         try {
            icon = Material.valueOf(iconString.toUpperCase());
         } catch (IllegalArgumentException var13) {
            this.plugin.getLogger().warning("Invalid icon material for job " + jobId + ": " + iconString);
            icon = Material.STONE;
         }

         String levelEquation = jobSection.getString("level_equation", "<nextlevel>*<nextlevel>*5");
         YamlMessage levelUpMessage = new YamlMessage(jobSection.get("levelup_message", ""));
         List<String> levelUpCommands = jobSection.getStringList("levelup_commands");
         Job.BossBarConfig bossBarConfig = this.loadBossBarConfig(jobSection.getConfigurationSection("bossbar"));
         Job job = new Job(jobId, name, icon, levelEquation, levelUpMessage, levelUpCommands, bossBarConfig);
         ConfigurationSection actionsSection = jobSection.getConfigurationSection("actions");
         if (actionsSection != null) {
            this.loadJobActions(job, actionsSection);
         }

         return job;
      }
   }

   private Job.BossBarConfig loadBossBarConfig(ConfigurationSection bossBarSection) {
      if (bossBarSection == null) {
         return new Job.BossBarConfig(
            "<yellow><level> <gray>level <job> <gradient:#6D6666:#938B8B:#777676>+<gained_exp><symbol_exp> <exp>/<next_exp>",
            "<gradient:#5DFF00:#E8FF00:{phase}>You have reached <bold><level></bold>. level in <job> skill.",
            Color.BLUE,
            Overlay.NOTCHED_10
         );
      } else {
         String titleTemplate = bossBarSection.getString(
            "title", "<yellow><level> <gray>level <job> <gradient:#6D6666:#938B8B:#777676>+<gained_exp><symbol_exp> <exp>/<next_exp>"
         );
         String levelUpTemplate = bossBarSection.getString(
            "levelup", "<gradient:#5DFF00:#E8FF00:{phase}>You have reached <bold><level></bold>. level in <job> skill."
         );

         Color color;
         try {
            color = Color.valueOf(bossBarSection.getString("color", "BLUE").toUpperCase());
         } catch (IllegalArgumentException var8) {
            color = Color.BLUE;
         }

         Overlay overlay;
         try {
            overlay = Overlay.valueOf(bossBarSection.getString("overlay", "NOTCHED_10").toUpperCase());
         } catch (IllegalArgumentException var7) {
            overlay = Overlay.NOTCHED_10;
         }

         return new Job.BossBarConfig(titleTemplate, levelUpTemplate, color, overlay);
      }
   }

   private void loadJobActions(Job job, ConfigurationSection actionsSection) {
      for (String actionName : actionsSection.getKeys(false)) {
         Action action = BuiltInActions.get(actionName);
         if (action == null) {
            return;
         }

         ConfigurationSection actionSection = actionsSection.getConfigurationSection(actionName);
         if (actionSection != null) {
            for (String value : actionSection.getKeys(false)) {
               ConfigurationSection rewardSection = actionSection.getConfigurationSection(value);
               if (rewardSection != null) {
                  double exp = rewardSection.getDouble("exp", 0.0);
                  double money = rewardSection.getDouble("money", 0.0);
                  Job.ActionReward actionReward = new Job.ActionReward(exp, money);
                  job.addAction(action, value, actionReward);
                  JobInfoLine infoLine = new JobInfoLine(action, value, actionReward);
                  job.getInfoLines().add(infoLine);
                  this.actionJobMap.computeIfAbsent(action, k -> new HashSet<>()).add(job.getId());
               }
            }
         }
      }
   }

   public Collection<Job> getJobsWithAction(Action action) {
      return this.actionJobMap.getOrDefault(action, Collections.emptySet()).stream().map(this.jobs::get).filter(Objects::nonNull).toList();
   }

   public void processAction(Player player, Action action, String value, Job job, int amount) {
      if (player != null && action != null && job != null && value != null && !value.isBlank() && amount > 0) {
         PlayerJobData playerJobData = this.plugin.getStorage().getCached(player.getUniqueId());
         if (playerJobData != null) {
            Job.ActionReward reward = job.getActionReward(action, value.toLowerCase(Locale.ROOT));
            if (reward != null) {
               JobExpUtils.processJobExp(player, job, reward, amount);
            }
         }
      }
   }

   public boolean shouldIgnore(Player player) {
      if (!this.plugin.getConfig().isSet("ignored_gamemodes")) {
         return false;
      } else {
         List<String> ignoredGamemodes = this.plugin.getConfig().getStringList("ignored_gamemodes");
         return ignoredGamemodes.contains(player.getGameMode().toString());
      }
   }

   @Generated
   public UJobsPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<String, Job> getJobs() {
      return this.jobs;
   }

   @Generated
   public Map<Action, Set<String>> getActionJobMap() {
      return this.actionJobMap;
   }
}
