package vrx.economy.vRXClans;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ClanCache {
   private final Map<String, Clan> clans = new ConcurrentHashMap<>();
   private final Map<UUID, Clan> playerClans = new ConcurrentHashMap<>();
   private final Map<ClanCache.ChunkKey, Set<Clan>> baseChunkIndex = new ConcurrentHashMap<>();

   public void replaceAll(Map<String, Clan> loadedClans, Map<UUID, Clan> loadedPlayerClans) {
      this.clans.clear();
      this.playerClans.clear();
      this.clans.putAll(loadedClans);
      this.playerClans.putAll(loadedPlayerClans);
      this.rebuildRegionIndex();
   }

   public Map<String, Clan> clans() {
      return this.clans;
   }

   public Map<UUID, Clan> playerClans() {
      return this.playerClans;
   }

   public Clan getClan(Player player) {
      return this.playerClans.get(player.getUniqueId());
   }

   public Clan getClan(UUID uuid) {
      return this.playerClans.get(uuid);
   }

   public Clan getClanByName(String clanName) {
      if (clanName == null) {
         return null;
      } else {
         Clan direct = this.clans.get(clanName);
         if (direct != null) {
            return direct;
         } else {
            for (Entry<String, Clan> entry : this.clans.entrySet()) {
               if (entry.getKey().equalsIgnoreCase(clanName)) {
                  return entry.getValue();
               }
            }

            return null;
         }
      }
   }

   public void addClan(Clan clan) {
      this.clans.put(clan.getName(), clan);
      this.indexClanBase(clan);
   }

   public void removeClan(Clan clan) {
      this.clans.remove(clan.getName());
      clan.getMembers().keySet().forEach(this.playerClans::remove);
      this.rebuildRegionIndex();
   }

   public void addMember(UUID uuid, Clan clan) {
      this.playerClans.put(uuid, clan);
   }

   public void removeMember(UUID uuid) {
      this.playerClans.remove(uuid);
   }

   public void renameClan(Clan clan, String oldName, String newName) {
      this.clans.remove(oldName);
      clan.setName(newName);
      this.clans.put(newName, clan);
      this.rebuildRegionIndex();
   }

   public void rebuildRegionIndex() {
      this.baseChunkIndex.clear();
      this.clans.values().forEach(this::indexClanBase);
   }

   public void updateClanBase(Clan clan) {
      this.rebuildRegionIndex();
   }

   public Collection<Clan> getCandidateBaseClans(Location location) {
      if (location != null && location.getWorld() != null) {
         Chunk chunk = location.getChunk();
         return this.baseChunkIndex.getOrDefault(new ClanCache.ChunkKey(location.getWorld().getUID(), chunk.getX(), chunk.getZ()), Set.of());
      } else {
         return List.of();
      }
   }

   public Clan getClanByVaultInventory(Inventory inventory) {
      if (inventory == null) {
         return null;
      } else {
         for (Clan clan : this.clans.values()) {
            if (clan.isVaultInventory(inventory)) {
               return clan;
            }
         }

         return null;
      }
   }

   private void indexClanBase(Clan clan) {
      Location base = clan.getBaseLocation();
      if (base != null && base.getWorld() != null) {
         int radius = Math.max(0, clan.getDynamicRadius(VRXClans.getInstance()));
         int minChunkX = Math.floorDiv(base.getBlockX() - radius, 16);
         int maxChunkX = Math.floorDiv(base.getBlockX() + radius, 16);
         int minChunkZ = Math.floorDiv(base.getBlockZ() - radius, 16);
         int maxChunkZ = Math.floorDiv(base.getBlockZ() + radius, 16);
         UUID worldId = base.getWorld().getUID();

         for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
               this.baseChunkIndex.computeIfAbsent(new ClanCache.ChunkKey(worldId, x, z), ignored -> ConcurrentHashMap.newKeySet()).add(clan);
            }
         }
      }
   }

   private record ChunkKey(UUID worldId, int x, int z) {
   }
}
