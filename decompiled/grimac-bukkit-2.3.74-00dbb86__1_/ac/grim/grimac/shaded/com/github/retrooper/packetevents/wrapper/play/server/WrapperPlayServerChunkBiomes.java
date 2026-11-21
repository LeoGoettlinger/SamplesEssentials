package ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.play.server;

import ac.grim.grimac.shaded.com.github.retrooper.packetevents.event.PacketSendEvent;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.packettype.PacketType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.biome.Biome;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.palette.DataPalette;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.palette.SingletonPalette;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.chunk.storage.BitStorage;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector2i;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WrapperPlayServerChunkBiomes extends PacketWrapper<WrapperPlayServerChunkBiomes> {
   private Map<Vector2i, WrapperPlayServerChunkBiomes.ChunkBiomeData> chunks;

   public WrapperPlayServerChunkBiomes(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerChunkBiomes(Map<Vector2i, WrapperPlayServerChunkBiomes.ChunkBiomeData> chunks) {
      super(PacketType.Play.Server.CHUNK_BIOMES);
      this.chunks = chunks;
   }

   @Override
   public void read() {
      this.chunks = this.readMap(Vector2i::read, WrapperPlayServerChunkBiomes.ChunkBiomeData::read);
   }

   @Override
   public void write() {
      this.writeMap(this.chunks, Vector2i::write, WrapperPlayServerChunkBiomes.ChunkBiomeData::write);
   }

   public void copy(WrapperPlayServerChunkBiomes wrapper) {
      this.chunks = wrapper.chunks;
   }

   public Map<Vector2i, WrapperPlayServerChunkBiomes.ChunkBiomeData> getChunks() {
      return this.chunks;
   }

   public WrapperPlayServerChunkBiomes.ChunkBiomeData getChunk(Vector2i chunkPos) {
      return this.chunks.get(chunkPos);
   }

   public static class ChunkBiomeData {
      private final List<DataPalette> sections;

      private ChunkBiomeData(List<DataPalette> sections) {
         this.sections = sections;
      }

      public static WrapperPlayServerChunkBiomes.ChunkBiomeData createWithRepeatingPalette(DataPalette palette, int sectionCount) {
         WrapperPlayServerChunkBiomes.ChunkBiomeData data = new WrapperPlayServerChunkBiomes.ChunkBiomeData(new ArrayList<>());

         for (int i = 0; i < sectionCount; i++) {
            data.sections.add(palette);
         }

         return data;
      }

      public static WrapperPlayServerChunkBiomes.ChunkBiomeData createWithSingleBiome(Biome biome, ClientVersion version, int sectionCount) {
         return createWithSingleBiome(biome.getId(version), sectionCount);
      }

      public static WrapperPlayServerChunkBiomes.ChunkBiomeData createWithSingleBiome(int biomeId, int sectionCount) {
         DataPalette palette = new DataPalette(new SingletonPalette(biomeId), new BitStorage(0, PaletteType.BIOME.getStorageSize()), PaletteType.BIOME);
         return createWithRepeatingPalette(palette, sectionCount);
      }

      public static WrapperPlayServerChunkBiomes.ChunkBiomeData read(PacketWrapper<?> wrapper) {
         int endIndex = wrapper.readVarInt() + ByteBufHelper.readerIndex(wrapper.buffer);
         List<DataPalette> sections = new ArrayList<>();

         while (ByteBufHelper.readerIndex(wrapper.buffer) < endIndex) {
            sections.add(PaletteType.BIOME.read(wrapper));
         }

         if (ByteBufHelper.readerIndex(wrapper.buffer) != endIndex) {
            throw new IllegalStateException("Unexpected buffer position, expected " + endIndex + ", received " + wrapper.buffer);
         } else {
            return new WrapperPlayServerChunkBiomes.ChunkBiomeData(sections);
         }
      }

      public static void write(PacketWrapper<?> wrapper, WrapperPlayServerChunkBiomes.ChunkBiomeData biomeData) {
         int startIndex = ByteBufHelper.writerIndex(wrapper.buffer);

         for (DataPalette biomePalette : biomeData.sections) {
            PaletteType.write(wrapper, biomePalette);
         }

         int dataLength = ByteBufHelper.writerIndex(wrapper.buffer) - startIndex;
         ByteBufHelper.writerIndex(wrapper.buffer, startIndex);
         wrapper.writeVarInt(dataLength);

         for (DataPalette biomePalette : biomeData.sections) {
            PaletteType.write(wrapper, biomePalette);
         }
      }
   }
}
