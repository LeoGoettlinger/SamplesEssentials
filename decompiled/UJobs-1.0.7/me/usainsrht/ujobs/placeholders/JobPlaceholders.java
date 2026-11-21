package me.usainsrht.ujobs.placeholders;

import java.text.DecimalFormat;
import java.util.UUID;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.models.Job;
import me.usainsrht.ujobs.models.PlayerJobData;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class JobPlaceholders extends PlaceholderExpansion {
   private final UJobsPlugin plugin;

   public JobPlaceholders(UJobsPlugin plugin) {
      this.plugin = plugin;
   }

   @NotNull
   public String getAuthor() {
      return String.join(", ", this.plugin.getPluginMeta().getAuthors());
   }

   @NotNull
   public String getIdentifier() {
      return "ujobs";
   }

   @NotNull
   public String getVersion() {
      return this.plugin.getPluginMeta().getVersion();
   }

   public boolean persist() {
      return true;
   }

   public String onRequest(OfflinePlayer player, @NotNull String params) {
      String[] param = params.split("_");
      if (param.length < 3) {
         return null;
      } else {
         String jobId = param[2];
         Job job = this.plugin.getJobManager().getJobs().get(jobId);
         if (job == null) {
            return null;
         } else {
            if (param[0].equalsIgnoreCase("job")) {
               if (param[1].equalsIgnoreCase("name")) {
                  return PlainTextComponentSerializer.plainText().serialize(job.getName());
               }

               if (param[1].equalsIgnoreCase("displayname")) {
                  return (String)this.plugin.getMiniMessage().serialize(job.getName());
               }

               if (param[1].equalsIgnoreCase("legacydisplayname")) {
                  return LegacyComponentSerializer.legacySection().serialize(job.getName());
               }
            } else if (param[0].equalsIgnoreCase("player")) {
               if (player == null || !player.isOnline()) {
                  return null;
               }

               PlayerJobData playerJobData = this.plugin.getStorage().getCached(player.getUniqueId());
               if (playerJobData == null) {
                  return null;
               }

               PlayerJobData.JobStats jobStats = playerJobData.getJobStats(jobId);
               if (param[1].equalsIgnoreCase("level")) {
                  return jobStats != null ? String.valueOf(jobStats.getLevel()) : "0";
               }

               if (param[1].equalsIgnoreCase("exp")) {
                  return jobStats != null ? new DecimalFormat().format(jobStats.getExp()) : "0";
               }

               if (param[1].equalsIgnoreCase("position")) {
                  int position = this.plugin.getLeaderboardManager().getPosition(player.getUniqueId(), job);
                  return String.valueOf(position + 1);
               }

               if (param[1].equalsIgnoreCase("totalmoney")) {
                  return jobStats != null ? String.valueOf(jobStats.getTotalMoney()) : "0";
               }
            } else if (param[0].equalsIgnoreCase("leaderboard")) {
               if (param.length < 4) {
                  return null;
               }

               int position = Integer.parseInt(param[3]);
               UUID target = this.plugin.getLeaderboardManager().getPlayerByPosition(position - 1, job);
               if (target == null) {
                  return "?";
               }

               if (param[1].equalsIgnoreCase("name")) {
                  OfflinePlayer lbPlayer = this.plugin.getServer().getOfflinePlayer(target);
                  return lbPlayer.getName();
               }

               if (param[1].equalsIgnoreCase("level")) {
                  return String.valueOf(this.plugin.getLeaderboardManager().getLevel(target, job));
               }
            }

            return null;
         }
      }
   }
}
