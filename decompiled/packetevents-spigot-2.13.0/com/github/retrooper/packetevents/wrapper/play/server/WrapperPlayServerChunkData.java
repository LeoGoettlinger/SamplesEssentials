package com.github.retrooper.packetevents.wrapper.play.server;

import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.netty.buffer.ByteBufHelper;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.chunk.BaseChunk;
import com.github.retrooper.packetevents.protocol.world.chunk.ChunkBitMask;
import com.github.retrooper.packetevents.protocol.world.chunk.Column;
import com.github.retrooper.packetevents.protocol.world.chunk.HeightmapType;
import com.github.retrooper.packetevents.protocol.world.chunk.LightData;
import com.github.retrooper.packetevents.protocol.world.chunk.NetworkChunkData;
import com.github.retrooper.packetevents.protocol.world.chunk.TileEntity;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_16.Chunk_v1_9;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_7.Chunk_v1_7;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v1_8.Chunk_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.impl.v_1_18.Chunk_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.ChunkReader;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_16;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_18;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_7;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_8;
import com.github.retrooper.packetevents.protocol.world.chunk.reader.impl.ChunkReader_v1_9;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import java.util.BitSet;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class WrapperPlayServerChunkData extends PacketWrapper<WrapperPlayServerChunkData> {
   private static ChunkReader_v1_7 chunkReader_v1_7 = new ChunkReader_v1_7();
   private static ChunkReader_v1_8 chunkReader_v1_8 = new ChunkReader_v1_8();
   private static ChunkReader_v1_9 chunkReader_v1_9 = new ChunkReader_v1_9();
   private static ChunkReader_v1_16 chunkReader_v1_16 = new ChunkReader_v1_16();
   private static ChunkReader_v1_18 chunkReader_v1_18 = new ChunkReader_v1_18();
   private Column column;
   private LightData lightData;
   private boolean ignoreOldData;

   public WrapperPlayServerChunkData(PacketSendEvent event) {
      super(event);
   }

   public WrapperPlayServerChunkData(Column column) {
      this(column, null, false);
   }

   public WrapperPlayServerChunkData(Column column, LightData lightData) {
      this(column, lightData, false);
   }

   public WrapperPlayServerChunkData(Column column, LightData lightData, boolean ignoreOldData) {
      super(PacketType.Play.Server.CHUNK_DATA);
      this.column = column;
      this.lightData = lightData;
      this.ignoreOldData = ignoreOldData;
   }

   @Override
   public void read() {
      int chunkX = this.readInt();
      int chunkZ = this.readInt();
      boolean checkFullChunk = this.serverVersion.isOlderThan(ServerVersion.V_1_17);
      boolean fullChunk = !checkFullChunk || this.readBoolean();
      if (this.serverVersion == ServerVersion.V_1_16 || this.serverVersion == ServerVersion.V_1_16_1) {
         this.ignoreOldData = this.readBoolean();
      }

      BitSet chunkMask = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18) ? null : ChunkBitMask.readChunkMask(this);
      boolean hasHeightMaps = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14);
      NBTCompound heightmapsNbt = null;
      Map<HeightmapType, long[]> modernHeightmaps = null;
      if (hasHeightMaps) {
         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            modernHeightmaps = this.readMap(HeightmapType::read, PacketWrapper::readLongArray);
         } else {
            heightmapsNbt = this.readNBT();
         }
      }

      BitSet secondaryChunkMask = null;
      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         secondaryChunkMask = ChunkBitMask.readChunkMask(this);
      }

      int chunkSize = 16;
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17)) {
         chunkSize = this.user.getTotalWorldHeight() >> 4;
      }

      boolean hasBiomeData = fullChunk && this.serverVersion.isOlderThan(ServerVersion.V_1_18);
      boolean bytesInsteadOfInts = this.serverVersion.isOlderThan(ServerVersion.V_1_13);
      int[] biomeDataInts = null;
      byte[] biomeDataBytes = null;
      if (hasBiomeData && this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
         biomeDataInts = this.readVarIntArray();
      } else if (hasBiomeData && this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15)) {
         biomeDataInts = new int[1024];

         for (int i = 0; i < biomeDataInts.length; i++) {
            biomeDataInts[i] = this.readInt();
         }
      }

      boolean hasBlockLight = (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16) || this.serverVersion.isOlderThan(ServerVersion.V_1_14))
         && !this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8);
      boolean hasSkyLight = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)
         || this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_8_8)
         || this.user != null && this.user.getDimensionType().hasSkyLight() && this.serverVersion.isOlderThan(ServerVersion.V_1_14);
      Object originalBuffer = this.buffer;
      int dataLength;
      if (this.serverVersion.isOlderThanOrEquals(ServerVersion.V_1_7_10)) {
         byte[] data = this.inflate(this.readByteArray(), chunkMask, fullChunk);
         this.buffer = UnpooledByteBufAllocationHelper.wrappedBuffer(data);
         dataLength = data.length;
      } else {
         dataLength = this.readVarInt();
      }

      BaseChunk[] chunks;
      try {
         int expectedReaderIndex = ByteBufHelper.readerIndex(this.buffer) + dataLength;
         chunks = this.getChunkReader()
            .read(this.user.getDimensionType(), chunkMask, secondaryChunkMask, fullChunk, hasBlockLight, hasSkyLight, chunkSize, dataLength, this);
         if (hasBiomeData && this.serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
               biomeDataInts = new int[256];

               for (int i = 0; i < biomeDataInts.length; i++) {
                  biomeDataInts[i] = this.readInt();
               }
            } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
               biomeDataBytes = new byte[256];

               for (int i = 0; i < biomeDataBytes.length; i++) {
                  biomeDataBytes[i] = this.readByte();
               }
            } else if (dataLength == 0) {
               biomeDataBytes = new byte[0];
            } else {
               biomeDataBytes = this.readBytes(256);
            }
         }

         int readerIndex = ByteBufHelper.readerIndex(this.buffer);
         if (expectedReaderIndex != readerIndex) {
            if (expectedReaderIndex < readerIndex) {
               throw new RuntimeException(
                  "Error while decoding chunk at " + chunkX + " " + chunkZ + "; expected reader index " + expectedReaderIndex + ", got " + readerIndex
               );
            }

            ByteBufHelper.readerIndex(this.buffer, expectedReaderIndex);
         }
      } finally {
         if (this.buffer != originalBuffer) {
            ByteBufHelper.release(this.buffer);
            this.buffer = originalBuffer;
         }
      }

      int tileEntityCount = this.serverVersion.isOlderThan(ServerVersion.V_1_9) ? 0 : this.readVarInt();
      TileEntity[] tileEntities = new TileEntity[tileEntityCount];
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
         for (int i = 0; i < tileEntities.length; i++) {
            tileEntities[i] = new TileEntity(this.readByte(), this.readShort(), this.readVarInt(), this.readNBT());
         }
      } else {
         for (int i = 0; i < tileEntities.length; i++) {
            tileEntities[i] = new TileEntity(this.readNBT());
         }
      }

      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
         this.lightData = LightData.read(this);
      }

      if (hasBiomeData) {
         if (hasHeightMaps) {
            if (bytesInsteadOfInts) {
               this.column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightmapsNbt, biomeDataBytes);
            } else {
               this.column = new Column(chunkX, chunkZ, true, chunks, tileEntities, heightmapsNbt, biomeDataInts);
            }
         } else if (bytesInsteadOfInts) {
            this.column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataBytes);
         } else {
            this.column = new Column(chunkX, chunkZ, true, chunks, tileEntities, biomeDataInts);
         }
      } else if (hasHeightMaps) {
         if (modernHeightmaps != null) {
            this.column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities, modernHeightmaps);
         } else {
            this.column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities, heightmapsNbt);
         }
      } else {
         this.column = new Column(chunkX, chunkZ, fullChunk, chunks, tileEntities);
      }
   }

   private byte[] inflate(byte[] input, BitSet mask, boolean fullChunk) {
      int chunkCount = 0;

      for (int count = 0; count < 16; count++) {
         chunkCount += mask.get(count) ? 1 : 0;
      }

      int len = 12288 * chunkCount;
      if (fullChunk) {
         len += 256;
      }

      byte[] data = new byte[len];
      Inflater inflater = new Inflater();
      inflater.setInput(input, 0, input.length);

      try {
         inflater.inflate(data);
      } catch (DataFormatException var12) {
         var12.printStackTrace();
      } finally {
         inflater.end();
      }

      return data;
   }

   @Override
   public void write() {
      this.writeInt(this.column.getX());
      this.writeInt(this.column.getZ());
      boolean v1_18 = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18);
      boolean v1_17 = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_17);
      boolean v1_9 = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9);
      boolean v1_8 = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8);
      if (!v1_17) {
         this.writeBoolean(this.column.isFullChunk());
      }

      boolean hasWrittenBiomeData = false;
      if (this.serverVersion == ServerVersion.V_1_16 || this.serverVersion == ServerVersion.V_1_16_1) {
         this.writeBoolean(this.ignoreOldData);
      }

      BitSet chunkMask = new BitSet();
      BaseChunk[] chunks = this.column.getChunks();
      if (!this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         if (v1_8) {
            NetworkChunkData data = ChunkReader_v1_8.chunksToData((Chunk_v1_8[])chunks, this.column.getBiomeDataBytes());
            this.writeShort(data.getMask());
            this.writeByteArray(data.getData());
         } else {
            NetworkChunkData data = ChunkReader_v1_7.chunksToData((Chunk_v1_7[])chunks, this.column.getBiomeDataBytes());
            Deflater deflater = new Deflater(-1);
            byte[] deflated = new byte[data.getData().length];

            int len;
            try {
               deflater.setInput(data.getData(), 0, data.getData().length);
               deflater.finish();
               len = deflater.deflate(deflated);
            } finally {
               deflater.end();
            }

            this.writeShort(data.getMask());
            this.writeShort(data.getExtendedChunkMask());
            this.writeInt(len);
            ByteBufHelper.writeBytes(this.buffer, deflated, 0, len);
         }
      } else {
         Object originalBuffer = this.buffer;
         Object dataBuffer = ByteBufHelper.allocateNewBuffer(this.buffer);
         this.buffer = dataBuffer;

         for (int index = 0; index < chunks.length; index++) {
            BaseChunk chunk = chunks[index];
            if (v1_18) {
               Chunk_v1_18.write(this, (Chunk_v1_18)chunk);
            } else if (v1_9 && chunk != null) {
               chunkMask.set(index);
               Chunk_v1_9.write(this, (Chunk_v1_9)chunk);
            }
         }

         this.buffer = originalBuffer;
         if (this.serverVersion.isOlderThan(ServerVersion.V_1_21_6) && this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
            int zeroBytes = ChunkReader_v1_18.getMojangZeroByteSuffixLength(chunks);
            int newWriterIndex = ByteBufHelper.writerIndex(dataBuffer) + zeroBytes;
            if (newWriterIndex > ByteBufHelper.capacity(dataBuffer)) {
               ByteBufHelper.capacity(dataBuffer, newWriterIndex);
            }

            ByteBufHelper.writerIndex(dataBuffer, newWriterIndex);
         }

         if (this.column.isFullChunk() && this.serverVersion.isOlderThan(ServerVersion.V_1_15)) {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_13)) {
               for (int i : this.column.getBiomeDataInts()) {
                  ByteBufHelper.writeInt(dataBuffer, i);
               }
            } else {
               for (byte i : this.column.getBiomeDataBytes()) {
                  ByteBufHelper.writeByte(dataBuffer, i);
               }
            }

            hasWrittenBiomeData = true;
         }

         if (!v1_18) {
            ChunkBitMask.writeChunkMask(this, chunkMask);
         }

         boolean hasHeightMaps = this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_14);
         if (hasHeightMaps) {
            if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_21_5)) {
               this.writeMap(this.column.getHeightmaps(), HeightmapType::write, PacketWrapper::writeLongArray);
            } else {
               this.writeNBT(this.column.getHeightMaps());
            }
         }

         if (this.column.hasBiomeData() && this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_15) && !v1_18) {
            boolean bytesInsteadOfInts = this.serverVersion.isOlderThan(ServerVersion.V_1_13);
            int[] biomeDataInts = this.column.getBiomeDataInts();
            byte[] biomeDataByes = this.column.getBiomeDataBytes();
            if (bytesInsteadOfInts) {
               for (byte biomeDataBye : biomeDataByes) {
                  this.writeByte(biomeDataBye);
               }
            } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16_2)) {
               this.writeVarIntArray(biomeDataInts);
            } else {
               for (int biomeDataInt : biomeDataInts) {
                  this.writeInt(biomeDataInt);
               }
            }

            hasWrittenBiomeData = true;
         }

         this.writeVarInt(ByteBufHelper.readableBytes(dataBuffer));
         ByteBufHelper.writeBytes(this.buffer, dataBuffer);
         ByteBufHelper.release(dataBuffer);
         if (this.column.hasBiomeData() && !hasWrittenBiomeData) {
            byte[] biomeDataBytes = new byte[256];
            int[] biomeData = this.column.getBiomeDataInts();

            for (int i = 0; i < biomeDataBytes.length; i++) {
               biomeDataBytes[i] = (byte)biomeData[i];
            }

            this.writeByteArray(biomeDataBytes);
         }

         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            this.writeVarInt(this.column.getTileEntities().length);

            for (TileEntity tileEntity : this.column.getTileEntities()) {
               this.writeByte(tileEntity.getPackedByte());
               this.writeShort(tileEntity.getYShort());
               this.writeVarInt(tileEntity.getType());
               this.writeNBT(tileEntity.getNBT());
            }
         } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
            TileEntity[] tileEntities = this.column.getTileEntities();
            this.writeVarInt(tileEntities.length);

            for (TileEntity tileEntity : tileEntities) {
               this.writeNBT(tileEntity.getNBT());
            }
         }

         if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
            LightData.write(this, this.lightData);
         }
      }
   }

   public void copy(WrapperPlayServerChunkData wrapper) {
      this.column = wrapper.column;
      this.lightData = wrapper.lightData != null ? wrapper.lightData.clone() : null;
      this.ignoreOldData = wrapper.ignoreOldData;
   }

   public Column getColumn() {
      return this.column;
   }

   public void setColumn(Column column) {
      this.column = column;
   }

   public LightData getLightData() {
      return this.lightData;
   }

   public void setLightData(LightData lightData) {
      this.lightData = lightData;
   }

   public boolean isIgnoreOldData() {
      return this.ignoreOldData;
   }

   public void setIgnoreOldData(boolean ignoreOldData) {
      this.ignoreOldData = ignoreOldData;
   }

   private ChunkReader getChunkReader() {
      if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_18)) {
         return chunkReader_v1_18;
      } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_16)) {
         return chunkReader_v1_16;
      } else if (this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_9)) {
         return chunkReader_v1_9;
      } else {
         return (ChunkReader)(this.serverVersion.isNewerThanOrEquals(ServerVersion.V_1_8) ? chunkReader_v1_8 : chunkReader_v1_7);
      }
   }
}
