package me.usainsrht.ujobs.storage;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.models.PlayerJobData;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public class PDCStorage implements Storage {
   private final UJobsPlugin plugin;
   private final Map<UUID, PlayerJobData> cache;
   public static final NamespacedKey TAG_JOBS_DATA = new NamespacedKey("ujobs", "jobs_data");
   public static final NamespacedKey TAG_LEVEL = new NamespacedKey("ujobs", "level");
   public static final NamespacedKey TAG_EXP = new NamespacedKey("ujobs", "exp");
   public static final NamespacedKey TAG_TOTAL_MONEY = new NamespacedKey("ujobs", "total_money");

   public PDCStorage(UJobsPlugin plugin) {
      this.plugin = plugin;
      this.cache = new ConcurrentHashMap<>();
   }

   public PersistentDataContainer serialize(PersistentDataAdapterContext context, PlayerJobData playerJobData) {
      PersistentDataContainer container = context.newPersistentDataContainer();

      for (Entry<String, PlayerJobData.JobStats> entry : playerJobData.getJobStats().entrySet()) {
         PlayerJobData.JobStats jobStats = entry.getValue();
         if (jobStats.getExp() != 0.0 || jobStats.getLevel() != 0 || jobStats.getTotalMoney() != 0.0) {
            NamespacedKey jobKey = NamespacedKey.fromString(entry.getKey(), this.plugin);
            if (jobKey != null) {
               PersistentDataContainer jobContainer = this.serialize(context, jobStats);
               container.set(jobKey, PersistentDataType.TAG_CONTAINER, jobContainer);
            }
         }
      }

      return container;
   }

   public PersistentDataContainer serialize(PersistentDataAdapterContext context, PlayerJobData.JobStats jobStats) {
      PersistentDataContainer container = context.newPersistentDataContainer();
      container.set(TAG_LEVEL, PersistentDataType.INTEGER, jobStats.getLevel());
      container.set(TAG_EXP, PersistentDataType.DOUBLE, jobStats.getExp());
      container.set(TAG_TOTAL_MONEY, PersistentDataType.DOUBLE, jobStats.getTotalMoney());
      return container;
   }

   public PlayerJobData deserialize(UUID uuid, PersistentDataContainer pdc) {
      PlayerJobData playerJobData = new PlayerJobData(uuid);

      for (NamespacedKey jobKey : pdc.getKeys()) {
         if (pdc.has(jobKey, PersistentDataType.TAG_CONTAINER)) {
            PersistentDataContainer jobContainer = (PersistentDataContainer)pdc.get(jobKey, PersistentDataType.TAG_CONTAINER);
            PlayerJobData.JobStats jobStats = this.deserializeJobStats(jobContainer);
            playerJobData.setJobStats(jobKey.getKey(), jobStats);
         }
      }

      return playerJobData;
   }

   public PlayerJobData.JobStats deserializeJobStats(PersistentDataContainer jobContainer) {
      Integer level = (Integer)jobContainer.get(TAG_LEVEL, PersistentDataType.INTEGER);
      Double exp = (Double)jobContainer.get(TAG_EXP, PersistentDataType.DOUBLE);
      Double totalMoney = (Double)jobContainer.get(TAG_TOTAL_MONEY, PersistentDataType.DOUBLE);
      return new PlayerJobData.JobStats(level == null ? 0 : level, exp == null ? 0.0 : exp, totalMoney == null ? 0.0 : totalMoney);
   }

   @Override
   public void save() {
      for (UUID uuid : this.cache.keySet()) {
         if (this.plugin.isEnabled()) {
            this.save(uuid);
         } else {
            this.saveNow(uuid);
         }
      }
   }

   @Override
   public void save(UUID uuid) {
      if (uuid != null) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            PlayerJobData playerJobData = this.getCached(uuid);
            if (playerJobData == null) {
               return;
            }

            if (!this.plugin.isEnabled()) {
               this.set(player, playerJobData);
               return;
            }

            this.plugin
               .getMorePaperLib()
               .scheduling()
               .entitySpecificScheduler(player)
               .run(() -> this.set(player, playerJobData), () -> this.plugin.getLogger().warning("Can't save data of " + uuid + "! storage: PDC"));
         }
      }
   }

   public void saveNow(Player player) {
      if (player != null) {
         UUID uuid = player.getUniqueId();
         PlayerJobData playerJobData = this.getCached(uuid);
         if (playerJobData != null) {
            this.saveNow(player, playerJobData);
         }
      }
   }

   public void saveNow(Player player, PlayerJobData playerJobData) {
      if (player != null && playerJobData != null) {
         this.cache.put(player.getUniqueId(), playerJobData);
         this.set(player, playerJobData);
      }
   }

   public void saveNow(UUID uuid) {
      if (uuid != null) {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            this.saveNow(player);
         }
      }
   }

   public void saveNowAll() {
      for (UUID uuid : this.cache.keySet()) {
         this.saveNow(uuid);
      }
   }

   @Override
   public CompletableFuture<PlayerJobData> load(UUID uuid) {
      CompletableFuture<PlayerJobData> future = new CompletableFuture<>();
      if (uuid == null) {
         future.complete(new PlayerJobData(new UUID(0L, 0L)));
         return future;
      } else if (!this.plugin.isEnabled()) {
         PlayerJobData playerJobData = this.cache.computeIfAbsent(uuid, PlayerJobData::new);
         future.complete(playerJobData);
         return future;
      } else {
         Player player = Bukkit.getPlayer(uuid);
         if (player != null && player.isOnline()) {
            this.plugin.getMorePaperLib().scheduling().entitySpecificScheduler(player).run(() -> {
               PersistentDataContainer pdc = player.getPersistentDataContainer();
               PlayerJobData playerJobDatax;
               if (pdc.has(TAG_JOBS_DATA, PersistentDataType.TAG_CONTAINER)) {
                  playerJobDatax = this.deserialize(uuid, (PersistentDataContainer)pdc.get(TAG_JOBS_DATA, PersistentDataType.TAG_CONTAINER));
               } else {
                  playerJobDatax = new PlayerJobData(uuid);
               }

               this.cache.put(uuid, playerJobDatax);
               future.complete(playerJobDatax);
            }, () -> {
               this.plugin.getLogger().warning("Can't load data of " + uuid + "! storage: PDC");
               future.complete(new PlayerJobData(uuid));
            });
         } else {
            PlayerJobData playerJobData = this.cache.computeIfAbsent(uuid, PlayerJobData::new);
            future.complete(playerJobData);
         }

         return future;
      }
   }

   @Override
   public boolean isCached(UUID uuid) {
      return uuid != null && this.cache.containsKey(uuid);
   }

   @Nullable
   @Override
   public PlayerJobData getCached(UUID uuid) {
      return uuid == null ? null : this.cache.get(uuid);
   }

   @Override
   public void set(UUID uuid, PlayerJobData playerJobData) {
      if (uuid != null && playerJobData != null) {
         if (!this.plugin.isEnabled()) {
            this.cache.put(uuid, playerJobData);
         } else {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
               this.cache.put(uuid, playerJobData);
               this.plugin
                  .getMorePaperLib()
                  .scheduling()
                  .entitySpecificScheduler(player)
                  .run(() -> this.set(player, playerJobData), () -> this.plugin.getLogger().warning("Can't set data of " + uuid + "! storage: PDC"));
            }
         }
      }
   }

   private void set(Player player, PlayerJobData playerJobData) {
      PersistentDataContainer pdc = player.getPersistentDataContainer();
      pdc.set(TAG_JOBS_DATA, PersistentDataType.TAG_CONTAINER, this.serialize(pdc.getAdapterContext(), playerJobData));
   }

   @Override
   public void remove(UUID uuid) {
      if (uuid != null) {
         this.removeFromCache(uuid);
         if (this.plugin.isEnabled()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
               this.plugin
                  .getMorePaperLib()
                  .scheduling()
                  .entitySpecificScheduler(player)
                  .run(
                     () -> player.getPersistentDataContainer().remove(TAG_JOBS_DATA),
                     () -> this.plugin.getLogger().warning("Can't remove data of " + uuid + "! storage: PDC")
                  );
            }
         }
      }
   }

   @Override
   public void removeFromCache(UUID uuid) {
      if (uuid != null) {
         this.cache.remove(uuid);
      }
   }

   @Override
   public Collection<PlayerJobData> getAllCachedData() {
      return this.cache.values();
   }

   @Generated
   public UJobsPlugin getPlugin() {
      return this.plugin;
   }

   @Generated
   public Map<UUID, PlayerJobData> getCache() {
      return this.cache;
   }
}
