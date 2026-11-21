package me.usainsrht.ujobs.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.models.Job;
import me.usainsrht.ujobs.models.PlayerJobData;
import me.usainsrht.ujobs.models.PlayerLeaderboardData;
import me.usainsrht.ujobs.utils.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.StyleBuilderApplicable;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class LeaderboardManager {
   private final UJobsPlugin plugin;
   private final Map<UUID, PlayerLeaderboardData> leaderboardPlayerCache;
   private final Map<Job, UUID[]> leaderboardJobCache;
   private final Object leaderboardLock = new Object();

   public LeaderboardManager(UJobsPlugin plugin) {
      this.plugin = plugin;
      this.leaderboardPlayerCache = new ConcurrentHashMap<>();
      this.leaderboardJobCache = new ConcurrentHashMap<>();
   }

   public void load(ConfigurationSection yml) {
      synchronized (this.leaderboardLock) {
         this.leaderboardPlayerCache.clear();
         this.leaderboardJobCache.clear();
         int top = this.plugin.getConfig().getInt("leaderboard.calculate_top", 100);

         for (Job job : this.plugin.getJobManager().getJobs().values()) {
            this.leaderboardJobCache.put(job, new UUID[top]);
         }

         ConfigurationSection leaderboardSection = yml.getConfigurationSection("leaderboard");
         if (leaderboardSection != null) {
            leaderboardSection.getKeys(false).forEach(jobId -> {
               Job jobx = this.plugin.getJobManager().getJobs().get(jobId);
               if (jobx != null) {
                  ConfigurationSection jobSection = yml.getConfigurationSection("leaderboard." + jobId);
                  if (jobSection != null) {
                     List<PlayerLeaderboardData.LeaderboardStats> entries = new ArrayList<>();
                     Map<PlayerLeaderboardData.LeaderboardStats, UUID> statsToUuid = new HashMap<>();
                     jobSection.getKeys(false).forEach(uuidString -> {
                        try {
                           UUID uuidx = UUID.fromString(uuidString);
                           int position = jobSection.getInt(uuidString + ".position", -1);
                           int level = jobSection.getInt(uuidString + ".level", -1);
                           if (position >= 0 && level >= 0) {
                              PlayerLeaderboardData playerData = this.leaderboardPlayerCache.computeIfAbsent(uuidx, PlayerLeaderboardData::new);
                              PlayerLeaderboardData.LeaderboardStats statsx = new PlayerLeaderboardData.LeaderboardStats(position, level);
                              playerData.getLeaderboardStats().put(jobx, statsx);
                              entries.add(statsx);
                              statsToUuid.put(statsx, uuidx);
                           }
                        } catch (IllegalArgumentException var11) {
                        }
                     });
                     entries.sort((s1, s2) -> {
                        int c = Integer.compare(s2.getLevel(), s1.getLevel());
                        return c != 0 ? c : Integer.compare(s1.getPosition(), s2.getPosition());
                     });
                     UUID[] leaderboard = this.leaderboardJobCache.get(jobx);

                     for (int i = 0; i < entries.size() && i < leaderboard.length; i++) {
                        PlayerLeaderboardData.LeaderboardStats stats = entries.get(i);
                        UUID uuid = statsToUuid.get(stats);
                        leaderboard[i] = uuid;
                        stats.setPosition(i);
                     }
                  }
               }
            });
         }
      }
   }

   public void save() {
      synchronized (this.leaderboardLock) {
         YamlConfiguration leaderboardConfig = this.plugin.getConfigManager().getLeaderboardConfig();
         leaderboardConfig.set("leaderboard", null);

         for (Job job : this.plugin.getJobManager().getJobs().values()) {
            if (!this.leaderboardJobCache.containsKey(job) || this.leaderboardJobCache.get(job) == null || this.leaderboardJobCache.get(job)[0] == null) {
               this.leaderboardJobCache.put(job, this.createLeaderboard(job));
            }
         }

         this.leaderboardPlayerCache.forEach((uuid, playerLeaderboardData) -> playerLeaderboardData.getLeaderboardStats().forEach((jobx, stats) -> {
            String path = "leaderboard." + jobx.getId() + "." + uuid.toString();
            leaderboardConfig.set(path + ".position", stats.getPosition());
            leaderboardConfig.set(path + ".level", stats.getLevel());
         }));
         this.plugin.getConfigManager().saveLeaderboard();
      }
   }

   public UUID[] createLeaderboard(Job job) {
      synchronized (this.leaderboardLock) {
         int top = this.plugin.getConfig().getInt("leaderboard.calculate_top", 100);
         UUID[] leaderboard = new UUID[top];
         List<PlayerJobData> players = new ArrayList<>(this.plugin.getStorage().getAllCachedData());
         players.removeIf(px -> px.getJobStats(job.getId()).getLevel() <= 0);
         players.sort((p1, p2) -> Integer.compare(p2.getJobStats(job.getId()).getLevel(), p1.getJobStats(job.getId()).getLevel()));

         for (int i = 0; i < players.size() && i < top; i++) {
            PlayerJobData p = players.get(i);
            leaderboard[i] = p.getUuid();
            this.leaderboardPlayerCache
               .computeIfAbsent(p.getUuid(), PlayerLeaderboardData::new)
               .getLeaderboardStats()
               .put(job, new PlayerLeaderboardData.LeaderboardStats(i, p.getJobStats(job.getId()).getLevel()));
         }

         return leaderboard;
      }
   }

   public int getPosition(UUID uuid, Job job) {
      synchronized (this.leaderboardLock) {
         PlayerLeaderboardData data = this.leaderboardPlayerCache.get(uuid);
         if (data == null) {
            return -1;
         } else {
            PlayerLeaderboardData.LeaderboardStats stats = data.getLeaderboardStats().get(job);
            return stats == null ? -1 : stats.getPosition();
         }
      }
   }

   public int getLevel(UUID uuid, Job job) {
      synchronized (this.leaderboardLock) {
         PlayerLeaderboardData data = this.leaderboardPlayerCache.get(uuid);
         if (data == null) {
            return -1;
         } else {
            PlayerLeaderboardData.LeaderboardStats stats = data.getLeaderboardStats().get(job);
            return stats == null ? -1 : stats.getLevel();
         }
      }
   }

   public PlayerLeaderboardData.LeaderboardStats getStats(UUID uuid, Job job) {
      synchronized (this.leaderboardLock) {
         PlayerLeaderboardData data = this.leaderboardPlayerCache.get(uuid);
         return data == null ? null : data.getLeaderboardStats().get(job);
      }
   }

   public UUID getPlayerByPosition(int position, Job job) {
      synchronized (this.leaderboardLock) {
         if (position < 0) {
            return null;
         } else {
            UUID[] leaderboard = this.leaderboardJobCache.get(job);
            return leaderboard != null && position < leaderboard.length ? leaderboard[position] : null;
         }
      }
   }

   public UUID[] getTopPlayersSnapshot(Job job) {
      synchronized (this.leaderboardLock) {
         UUID[] leaderboard = this.leaderboardJobCache.get(job);
         return leaderboard == null ? new UUID[0] : Arrays.copyOf(leaderboard, leaderboard.length);
      }
   }

   public PlayerLeaderboardData getPlayerData(UUID uuid) {
      synchronized (this.leaderboardLock) {
         return uuid == null ? null : this.leaderboardPlayerCache.get(uuid);
      }
   }

   public void checkLeaderboardChange(UUID uuid, Job job, int level) {
      synchronized (this.leaderboardLock) {
         UUID[] leaderboard = this.leaderboardJobCache.get(job);
         if (leaderboard != null) {
            int currentPos = -1;

            for (int i = 0; i < leaderboard.length; i++) {
               if (uuid.equals(leaderboard[i])) {
                  currentPos = i;
                  break;
               }
            }

            if (currentPos != -1) {
               for (int ix = currentPos; ix < leaderboard.length - 1; ix++) {
                  leaderboard[ix] = leaderboard[ix + 1];
                  if (leaderboard[ix] != null) {
                     this.updatePosition(leaderboard[ix], job, ix);
                  }
               }

               leaderboard[leaderboard.length - 1] = null;
            }

            PlayerLeaderboardData pData = this.leaderboardPlayerCache.computeIfAbsent(uuid, PlayerLeaderboardData::new);
            PlayerLeaderboardData.LeaderboardStats stats = pData.getLeaderboardStats()
               .computeIfAbsent(job, k -> new PlayerLeaderboardData.LeaderboardStats(-1, 0));
            stats.setLevel(level);

            int newPos;
            for (newPos = 0; newPos < leaderboard.length; newPos++) {
               UUID other = leaderboard[newPos];
               if (other == null || level > this.getLevel(other, job)) {
                  break;
               }
            }

            if (newPos >= leaderboard.length) {
               if (currentPos != -1) {
                  pData.getLeaderboardStats().remove(job);
                  if (pData.getLeaderboardStats().isEmpty()) {
                     this.leaderboardPlayerCache.remove(uuid);
                  }
               }

               this.save();
            } else {
               UUID fallen = leaderboard[leaderboard.length - 1];
               if (fallen != null) {
                  PlayerLeaderboardData fData = this.leaderboardPlayerCache.get(fallen);
                  if (fData != null) {
                     fData.getLeaderboardStats().remove(job);
                     if (fData.getLeaderboardStats().isEmpty()) {
                        this.leaderboardPlayerCache.remove(fallen);
                     }
                  }
               }

               for (int ixx = leaderboard.length - 1; ixx > newPos; ixx--) {
                  leaderboard[ixx] = leaderboard[ixx - 1];
                  if (leaderboard[ixx] != null) {
                     this.updatePosition(leaderboard[ixx], job, ixx);
                  }
               }

               leaderboard[newPos] = uuid;
               stats.setPosition(newPos);
               if (currentPos == -1 || newPos < currentPos) {
                  OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                  Set<TagResolver> placeholderSet = new HashSet<>();
                  placeholderSet.add(
                     Placeholder.component(
                        "displayname", (ComponentLike)(player.isOnline() ? player.getPlayer().displayName() : Component.text(player.getName()))
                     )
                  );
                  placeholderSet.add(Placeholder.unparsed("name", player.getName()));
                  placeholderSet.add(Formatter.number("level", level));
                  placeholderSet.add(Formatter.number("position", newPos + 1));
                  placeholderSet.add(Placeholder.component("job", job.getName()));

                  try {
                     placeholderSet.add(Placeholder.styling("primary", new StyleBuilderApplicable[]{((Component)job.getName().children().getFirst()).color()}));
                     placeholderSet.add(Placeholder.styling("secondary", new StyleBuilderApplicable[]{((Component)job.getName().children().getLast()).color()}));
                  } catch (Exception var18) {
                  }

                  UUID opponent = null;
                  if (newPos + 1 < leaderboard.length) {
                     opponent = leaderboard[newPos + 1];
                  }

                  if (opponent != null) {
                     OfflinePlayer opponentPlayer = Bukkit.getOfflinePlayer(opponent);

                     try {
                        placeholderSet.add(
                           Placeholder.component(
                              "opponent_displayname",
                              (ComponentLike)(opponentPlayer.isOnline() ? opponentPlayer.getPlayer().displayName() : Component.text(opponentPlayer.getName()))
                           )
                        );
                        placeholderSet.add(Placeholder.unparsed("opponent_name", opponentPlayer.getName()));
                     } catch (Exception var17) {
                     }

                     TagResolver[] placeholders = placeholderSet.toArray(new TagResolver[0]);
                     if (player.isOnline()) {
                        MessageUtil.send(player.getPlayer(), this.plugin.getConfigManager().getMessage("leaderboard_take_someones_position"), placeholders);
                     }

                     if (opponentPlayer.isOnline()) {
                        MessageUtil.send(opponentPlayer.getPlayer(), this.plugin.getConfigManager().getMessage("leaderboard_your_position_taken"), placeholders);
                     }
                  }

                  TagResolver[] placeholdersx = placeholderSet.toArray(new TagResolver[0]);
                  if (newPos == 0 && opponent != null) {
                     MessageUtil.send(this.plugin.getServer(), this.plugin.getConfigManager().getMessage("leaderboard_take_lead"), placeholdersx);
                  } else if (newPos == 9) {
                     MessageUtil.send(this.plugin.getServer(), this.plugin.getConfigManager().getMessage("leaderboard_get_in_top_10"), placeholdersx);
                  }
               }

               this.save();
            }
         }
      }
   }

   private void updatePosition(UUID uuid, Job job, int position) {
      PlayerLeaderboardData data = this.leaderboardPlayerCache.get(uuid);
      if (data != null) {
         PlayerLeaderboardData.LeaderboardStats stats = data.getLeaderboardStats().get(job);
         if (stats != null) {
            stats.setPosition(position);
         }
      }
   }

   @Generated
   public UJobsPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<UUID, PlayerLeaderboardData> getLeaderboardPlayerCache() {
      return this.leaderboardPlayerCache;
   }

   @Generated
   public Map<Job, UUID[]> getLeaderboardJobCache() {
      return this.leaderboardJobCache;
   }

   @Generated
   public Object getLeaderboardLock() {
      return this.leaderboardLock;
   }
}
