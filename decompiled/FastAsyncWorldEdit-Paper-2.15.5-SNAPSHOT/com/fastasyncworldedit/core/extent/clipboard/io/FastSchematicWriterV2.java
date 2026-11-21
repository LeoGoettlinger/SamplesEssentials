package com.fastasyncworldedit.core.extent.clipboard.io;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.jnbt.streamer.IntValueReader;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.util.IOUtil;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

public class FastSchematicWriterV2 implements ClipboardWriter {
   public static final int CURRENT_VERSION = 2;
   private static final int MAX_SIZE = 65535;
   private final NBTOutputStream outputStream;
   private boolean brokenEntities = false;

   public FastSchematicWriterV2(NBTOutputStream outputStream) {
      Preconditions.checkNotNull(outputStream);
      this.outputStream = outputStream;
   }

   public void setBrokenEntities(boolean brokenEntities) {
      this.brokenEntities = brokenEntities;
   }

   @Override
   public void write(Clipboard clipboard) throws IOException {
      clipboard.flush();
      this.write2(clipboard);
   }

   private void write2(Clipboard clipboard) throws IOException {
      Region region = clipboard.getRegion();
      BlockVector3 origin = clipboard.getOrigin();
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 offset = min.subtract(origin);
      int width = region.getWidth();
      int height = region.getHeight();
      int length = region.getLength();
      if (width > 65535) {
         throw new IllegalArgumentException("Width of region too large for a .schematic");
      } else if (height > 65535) {
         throw new IllegalArgumentException("Height of region too large for a .schematic");
      } else if (length > 65535) {
         throw new IllegalArgumentException("Length of region too large for a .schematic");
      } else {
         DataOutput rawStream = this.outputStream.getOutputStream();
         this.outputStream.writeLazyCompoundTag("Schematic", out -> {
            out.writeNamedTag("Version", 2);
            out.writeNamedTag("DataVersion", WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataVersion());
            out.writeNamedTag("Width", (short)width);
            out.writeNamedTag("Height", (short)height);
            out.writeNamedTag("Length", (short)length);
            out.writeNamedTag("Offset", new int[]{min.x(), min.y(), min.z()});
            out.writeLazyCompoundTag("Metadata", out1 -> {
               out1.writeNamedTag("WEOffsetX", offset.x());
               out1.writeNamedTag("WEOffsetY", offset.y());
               out1.writeNamedTag("WEOffsetZ", offset.z());
               out1.writeNamedTag("FAWEVersion", Fawe.instance().getVersion().build);
            });
            ByteArrayOutputStream blocksCompressed = new ByteArrayOutputStream();
            FaweOutputStream blocksOut = new FaweOutputStream(new DataOutputStream(new LZ4BlockOutputStream(blocksCompressed)));
            ByteArrayOutputStream tilesCompressed = new ByteArrayOutputStream();
            NBTOutputStream tilesOut = new NBTOutputStream(new LZ4BlockOutputStream(tilesCompressed));
            List<Integer> paletteList = new ArrayList<>();
            char[] palette = new char[BlockTypesCache.states.length];
            Arrays.fill(palette, '\uffff');
            int paletteMax = 0;
            int numTiles = 0;
            Clipboard finalClipboard;
            if (clipboard instanceof BlockArrayClipboard) {
               finalClipboard = ((BlockArrayClipboard)clipboard).getParent();
            } else {
               finalClipboard = clipboard;
            }

            for (BlockVector3 pos : finalClipboard) {
               BaseBlock block = pos.getFullBlock(finalClipboard);
               CompoundTag nbt = block.getNbtData();
               if (nbt != null) {
                  Map<String, Tag<?, ?>> values = new HashMap<>(nbt.getValue());
                  values.remove("x");
                  values.remove("y");
                  values.remove("z");
                  values.put("Id", new StringTag(block.getNbtId()));
                  values.remove("id");
                  values.put("Pos", new IntArrayTag(new int[]{pos.x(), pos.y(), pos.z()}));
                  numTiles++;
                  tilesOut.writeTagPayload(new CompoundTag(values));
               }

               int ordinal = block.getOrdinal();
               if (ordinal == 0) {
                  ordinal = 1;
               }

               char value = palette[ordinal];
               if (value == '\uffff') {
                  int size = paletteMax++;
                  palette[ordinal] = value = (char)size;
                  paletteList.add(ordinal);
               }

               blocksOut.writeVarInt(value);
            }

            tilesOut.close();
            blocksOut.close();
            out.writeNamedTag("PaletteMax", paletteMax);
            out.writeLazyCompoundTag("Palette", out12 -> {
               for (int i = 0; i < paletteList.size(); i++) {
                  int stateOrdinal = paletteList.get(i);
                  BlockState statex = BlockTypesCache.states[stateOrdinal];
                  out12.writeNamedTag(statex.getAsString(), i);
               }
            });
            out.writeNamedTagName("BlockData", 7);
            rawStream.writeInt(blocksOut.size());
            LZ4BlockInputStream in = new LZ4BlockInputStream(new ByteArrayInputStream(blocksCompressed.toByteArray()));

            try {
               IOUtil.copy(in, rawStream);
            } catch (Throwable var30) {
               try {
                  in.close();
               } catch (Throwable var28) {
                  var30.addSuppressed(var28);
               }

               throw var30;
            }

            in.close();
            if (numTiles != 0) {
               out.writeNamedTagName("BlockEntities", 9);
               rawStream.write(10);
               rawStream.writeInt(numTiles);

               try (LZ4BlockInputStream inx = new LZ4BlockInputStream(new ByteArrayInputStream(tilesCompressed.toByteArray()))) {
                  IOUtil.copy(inx, rawStream);
               }
            } else {
               out.writeNamedEmptyList("BlockEntities");
            }

            if (finalClipboard.hasBiomes()) {
               this.writeBiomes(finalClipboard, out);
            }

            List<Tag> entities = new ArrayList<>();

            for (Entity entity : finalClipboard.getEntities()) {
               BaseEntity state = entity.getState();
               if (state != null) {
                  Map<String, Tag<?, ?>> values = new HashMap<>();
                  CompoundTag rawTag = state.getNbtData();
                  if (rawTag != null) {
                     values.putAll(rawTag.getValue());
                  }

                  values.remove("id");
                  Location loc = entity.getLocation();
                  if (!this.brokenEntities) {
                     loc = loc.setPosition(loc.add(min.toVector3()));
                  }

                  values.put("Id", new StringTag(state.getType().id()));
                  values.put("Pos", this.writeVector(loc));
                  values.put("Rotation", this.writeRotation(entity.getLocation()));
                  CompoundTag entityTag = new CompoundTag(values);
                  entities.add(entityTag);
               }
            }

            if (entities.isEmpty()) {
               out.writeNamedEmptyList("Entities");
            } else {
               out.writeNamedTag("Entities", new ListTag(CompoundTag.class, entities));
            }
         });
      }
   }

   private void writeBiomes(Clipboard clipboard, NBTOutputStream out) throws IOException {
      ByteArrayOutputStream biomesCompressed = new ByteArrayOutputStream();
      final DataOutputStream biomesOut = new DataOutputStream(new LZ4BlockOutputStream(biomesCompressed));
      final List<Integer> paletteList = new ArrayList<>();
      final int[] palette = new int[BiomeTypes.getMaxId() + 1];
      Arrays.fill(palette, Integer.MAX_VALUE);
      final int[] paletteMax = new int[]{0};
      IntValueReader task = new IntValueReader() {
         @Override
         public void applyInt(int index, int ordinal) {
            try {
               int value = palette[ordinal];
               if (value == Integer.MAX_VALUE) {
                  int size = paletteMax[0]++;
                  value = size;
                  palette[ordinal] = size;
                  paletteList.add(ordinal);
               }

               IOUtil.writeVarInt(biomesOut, value);
            } catch (IOException var5) {
               var5.printStackTrace();
            }
         }
      };
      BlockVector3 min = clipboard.getMinimumPoint();
      int width = clipboard.getRegion().getWidth();
      int length = clipboard.getRegion().getLength();
      MutableBlockVector3 mutable = new MutableBlockVector3();
      int z = 0;

      for (int i = 0; z < length; z++) {
         int z0 = min.z() + z;

         for (int x = 0; x < width; i++) {
            int x0 = min.x() + x;
            BiomeType biome = clipboard.getBiome(mutable.setComponents(x0, min.y(), z0));
            task.applyInt(i, biome.getInternalId());
            x++;
         }
      }

      biomesOut.close();
      out.writeNamedTag("BiomePaletteMax", paletteMax[0]);
      out.writeLazyCompoundTag("BiomePalette", out12 -> {
         for (int ix = 0; ix < paletteList.size(); ix++) {
            int ordinal = paletteList.get(ix);
            BiomeType state = BiomeTypes.get(ordinal);
            out12.writeNamedTag(state.id(), ix);
         }
      });
      out.writeNamedTagName("BiomeData", 7);
      out.writeInt(biomesOut.size());

      try (LZ4BlockInputStream in = new LZ4BlockInputStream(new ByteArrayInputStream(biomesCompressed.toByteArray()))) {
         IOUtil.copy(in, (DataOutput)out);
      }
   }

   @Override
   public void close() throws IOException {
      this.outputStream.close();
   }
}
