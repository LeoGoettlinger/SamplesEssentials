package ac.grim.grimac.predictionengine.blockeffects;

import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.PacketEvents;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.enums.PotentSulfurState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.fastutil.longs.Long2ObjectMap;
import ac.grim.grimac.shaded.fastutil.longs.Long2ObjectOpenHashMap;
import ac.grim.grimac.utils.chunks.Column;
import ac.grim.grimac.utils.math.GrimMath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CompensatedGeysers {
   private static final ClientVersion SERVER_VERSION = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();
   private final Long2ObjectMap<Long2ObjectMap<CompensatedGeysers.GeyserBlockEntity>> tickersBySection = new Long2ObjectOpenHashMap<>();
   private long tickOrder = 0L;

   public void addChunkToCache(Column chunk, TileEntity[] tileEntities, int minHeight) {
      if (!SERVER_VERSION.isOlderThan(ClientVersion.V_26_2)) {
         this.removeChunk(chunk.x(), chunk.z(), chunk.chunks().length);
         if (tileEntities != null) {
            this.addPotentSulfurTickersFromTileEntities(chunk.x(), chunk.z(), tileEntities, minHeight, chunk.chunks());
         }
      }
   }

   public void updateBlock(int x, int y, int z, WrappedBlockState previousState, WrappedBlockState newState, int minHeight) {
      if (!SERVER_VERSION.isOlderThan(ClientVersion.V_26_2)) {
         boolean hadTicker = this.hasPotentSulfurTicker(previousState);
         boolean hasTicker = this.hasPotentSulfurTicker(newState);
         if (hadTicker && !hasTicker) {
            this.removeTicker(x, y, z, minHeight);
         } else if (!hadTicker && hasTicker) {
            this.addTicker(x, y, z, minHeight);
         }
      }
   }

   public List<CompensatedGeysers.GeyserBlockEntity> getTickersInOrder(GrimPlayer player, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      if (SERVER_VERSION.isOlderThan(ClientVersion.V_26_2)) {
         return null;
      } else {
         List<CompensatedGeysers.GeyserBlockEntity> candidates = null;
         int minChunkX = minX >> 4;
         int maxChunkX = maxX >> 4;
         int minChunkZ = minZ >> 4;
         int maxChunkZ = maxZ >> 4;
         int minHeight = player.compensatedWorld.getMinHeight();

         for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
               Column column = player.compensatedWorld.getChunk(chunkX, chunkZ);
               if (column != null) {
                  int minSection = Math.max(0, minY - minHeight >> 4);
                  int maxSection = Math.min(column.chunks().length - 1, maxY - minHeight >> 4);

                  for (int sectionIndex = minSection; sectionIndex <= maxSection; sectionIndex++) {
                     Long2ObjectMap<CompensatedGeysers.GeyserBlockEntity> section = this.tickersBySection.get(sectionKey(chunkX, chunkZ, sectionIndex));
                     if (section != null) {
                        for (CompensatedGeysers.GeyserBlockEntity geyser : section.values()) {
                           if (geyser.isInside(minX, minY, minZ, maxX, maxY, maxZ)) {
                              if (candidates == null) {
                                 candidates = new ArrayList<>();
                              }

                              candidates.add(geyser);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (candidates != null) {
            candidates.sort(Comparator.comparingLong(CompensatedGeysers.GeyserBlockEntity::tickOrder));
         }

         return candidates;
      }
   }

   public void removeChunk(int chunkX, int chunkZ, int sectionCount) {
      for (int sectionIndex = 0; sectionIndex < sectionCount; sectionIndex++) {
         this.removeSection(chunkX, chunkZ, sectionIndex);
      }
   }

   public void clear() {
      this.tickersBySection.clear();
      this.tickOrder = 0L;
   }

   private void addPotentSulfurTickersFromTileEntities(int chunkX, int chunkZ, TileEntity[] tileEntities, int minHeight, BaseChunk[] sections) {
      for (TileEntity tileEntity : tileEntities) {
         int x = (chunkX << 4) + tileEntity.getX();
         int y = tileEntity.getY();
         int z = (chunkZ << 4) + tileEntity.getZ();
         int sectionIndex = y - minHeight >> 4;
         if (sectionIndex >= 0 && sectionIndex < sections.length && sections[sectionIndex] != null) {
            WrappedBlockState state = sections[sectionIndex].get(SERVER_VERSION, x & 15, y - minHeight & 15, z & 15);
            if (this.hasPotentSulfurTicker(state)) {
               this.addTicker(x, y, z, minHeight);
            }
         }
      }
   }

   private boolean hasPotentSulfurTicker(WrappedBlockState state) {
      return state.getType() != StateTypes.POTENT_SULFUR ? false : state.getPotentSulfurState() != PotentSulfurState.DRY;
   }

   private void addTicker(int x, int y, int z, int minHeight) {
      Long2ObjectMap<CompensatedGeysers.GeyserBlockEntity> section = this.tickersBySection
         .computeIfAbsent(sectionKey(x >> 4, z >> 4, y - minHeight >> 4), ignored -> new Long2ObjectOpenHashMap<>());
      long pos = GrimMath.asLong(x, y, z);
      if (!section.containsKey(pos)) {
         section.put(pos, new CompensatedGeysers.GeyserBlockEntity(x, y, z, this.tickOrder++));
      }
   }

   private void removeTicker(int x, int y, int z, int minHeight) {
      this.removeFromSection(x, y, z, sectionKey(x >> 4, z >> 4, y - minHeight >> 4));
   }

   private void removeSection(int chunkX, int chunkZ, int sectionIndex) {
      this.tickersBySection.remove(sectionKey(chunkX, chunkZ, sectionIndex));
   }

   private void removeFromSection(int x, int y, int z, long sectionKey) {
      Long2ObjectMap<CompensatedGeysers.GeyserBlockEntity> section = this.tickersBySection.get(sectionKey);
      if (section != null) {
         section.remove(GrimMath.asLong(x, y, z));
         if (section.isEmpty()) {
            this.tickersBySection.remove(sectionKey);
         }
      }
   }

   private static long sectionKey(int chunkX, int chunkZ, int sectionIndex) {
      return (chunkX & 67108863L) << 38 | (chunkZ & 67108863L) << 12 | sectionIndex & 4095L;
   }

   public record GeyserBlockEntity(int x, int y, int z, long tickOrder) {
      private boolean isInside(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
         return this.x >= minX && this.x <= maxX && this.y >= minY && this.y <= maxY && this.z >= minZ && this.z <= maxZ;
      }
   }
}
