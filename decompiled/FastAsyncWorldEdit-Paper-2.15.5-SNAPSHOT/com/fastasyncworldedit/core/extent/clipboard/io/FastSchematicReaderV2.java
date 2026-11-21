package com.fastasyncworldedit.core.extent.clipboard.io;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.clipboard.LinearClipboard;
import com.fastasyncworldedit.core.extent.clipboard.SimpleClipboard;
import com.fastasyncworldedit.core.internal.io.FastByteArrayOutputStream;
import com.fastasyncworldedit.core.internal.io.FastByteArraysInputStream;
import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.jnbt.streamer.StreamDelegate;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.LinBusConverter;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.NBTSchematicReader;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.DataFixer;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;
import org.apache.logging.log4j.Logger;

public class FastSchematicReaderV2 extends NBTSchematicReader {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final NBTInputStream inputStream;
   private final DataFixer fixer;
   private int dataVersion = -1;
   private int version = -1;
   private int faweWritten = -1;
   private FastByteArrayOutputStream blocksOut;
   private FaweOutputStream blocks;
   private FastByteArrayOutputStream biomesOut;
   private FaweOutputStream biomes;
   private List<Map<String, Object>> tiles;
   private List<Map<String, Object>> entities;
   private int width;
   private int height;
   private int length;
   private int offsetX;
   private int offsetY;
   private int offsetZ;
   private char[] palette;
   private char[] biomePalette;
   private BlockVector3 min = BlockVector3.ZERO;
   private boolean brokenEntities = false;
   private boolean isWorldEdit = false;

   public FastSchematicReaderV2(NBTInputStream inputStream) {
      Preconditions.checkNotNull(inputStream);
      this.inputStream = inputStream;
      this.fixer = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).getDataFixer();
   }

   public void setBrokenEntities(boolean brokenEntities) {
      this.brokenEntities = brokenEntities;
   }

   private String fix(String palettePart) {
      return this.fixer != null && this.dataVersion != -1 ? this.fixer.fixUp(DataFixer.FixTypes.BLOCK_STATE, palettePart, this.dataVersion) : palettePart;
   }

   private CompoundTag fixBlockEntity(CompoundTag tag) {
      return this.fixer != null && this.dataVersion != -1
         ? (CompoundTag)LinBusConverter.fromLinBus(this.fixer.fixUp(DataFixer.FixTypes.BLOCK_ENTITY, tag.toLinTag(), this.dataVersion))
         : tag;
   }

   private CompoundTag fixEntity(CompoundTag tag) {
      return this.fixer != null && this.dataVersion != -1
         ? (CompoundTag)LinBusConverter.fromLinBus(this.fixer.fixUp(DataFixer.FixTypes.ENTITY, tag.toLinTag(), this.dataVersion))
         : tag;
   }

   private String fixBiome(String biomePalettePart) {
      return this.fixer != null && this.dataVersion != -1 ? this.fixer.fixUp(DataFixer.FixTypes.BIOME, biomePalettePart, this.dataVersion) : biomePalettePart;
   }

   public StreamDelegate createVersionDelegate() {
      StreamDelegate root = new StreamDelegate();
      StreamDelegate schematic = root.add("Schematic");
      schematic.add("DataVersion").withInt((i, v) -> this.dataVersion = v);
      schematic.add("Version").withInt((i, v) -> {
         this.version = v;
         if (v == 1 && this.dataVersion == -1) {
            this.dataVersion = 1631;
         }
      });
      return root;
   }

   public StreamDelegate createDelegate() {
      StreamDelegate root = new StreamDelegate();
      StreamDelegate schematic = root.add("Schematic");
      schematic.add("Width").withInt((i, v) -> this.width = v);
      schematic.add("Height").withInt((i, v) -> this.height = v);
      schematic.add("Length").withInt((i, v) -> this.length = v);
      schematic.add("Offset").withValue((index, v) -> this.min = BlockVector3.at(v[0], v[1], v[2]));
      StreamDelegate metadata = schematic.add("Metadata");
      metadata.add("WEOffsetX").withInt((i, v) -> this.offsetX = v);
      metadata.add("WEOffsetY").withInt((i, v) -> this.offsetY = v);
      metadata.add("WEOffsetZ").withInt((i, v) -> this.offsetZ = v);
      metadata.add("FAWEVersion").withInt((i, v) -> this.faweWritten = v);
      StreamDelegate worldEditSection = metadata.add("WorldEdit");
      worldEditSection.withValue((index, v) -> this.isWorldEdit = true);
      StreamDelegate paletteDelegate = schematic.add("Palette");
      paletteDelegate.withValue((ignore, v) -> {
         this.palette = new char[v.size()];

         for (Entry<String, Object> entry : v.entrySet()) {
            String palettePart = this.fix(entry.getKey());

            BlockState state;
            try {
               state = BlockState.get(palettePart);
            } catch (InputParseException var8x) {
               LOGGER.warn("Invalid BlockState in palette: {}. Block will be replaced with air.", palettePart);
               state = BlockTypes.AIR.getDefaultState();
            }

            int index = (Integer)entry.getValue();
            this.palette[index] = (char)state.getOrdinal();
         }
      });
      StreamDelegate blockData = schematic.add("BlockData");
      blockData.withInfo((length, type) -> {
         this.blocksOut = new FastByteArrayOutputStream();
         this.blocks = new FaweOutputStream(new LZ4BlockOutputStream(this.blocksOut));
      });
      blockData.withInt((index, value) -> this.blocks.write(value));
      StreamDelegate tilesDelegate = schematic.add("BlockEntities");
      tilesDelegate.withInfo((length, type) -> this.tiles = new ArrayList<>(length));
      tilesDelegate.withElem((index, tile) -> this.tiles.add(tile));
      StreamDelegate compatTilesDelegate = schematic.add("TileEntities");
      compatTilesDelegate.withInfo((length, type) -> this.tiles = new ArrayList<>(length));
      compatTilesDelegate.withElem((index, tile) -> this.tiles.add(tile));
      StreamDelegate entitiesDelegate = schematic.add("Entities");
      entitiesDelegate.withInfo((length, type) -> this.entities = new ArrayList<>(length));
      entitiesDelegate.withElem((index, entity) -> this.entities.add(entity));
      StreamDelegate biomePaletteDelegate = schematic.add("BiomePalette");
      biomePaletteDelegate.withValue((ignore, v) -> {
         this.biomePalette = new char[v.size()];

         for (Entry<String, Object> entry : v.entrySet()) {
            BiomeType biome = null;

            try {
               String biomePalettePart = this.fixBiome(entry.getKey());
               biome = BiomeTypes.get(biomePalettePart);
            } catch (InputParseException var7x) {
               var7x.printStackTrace();
            }

            int index = (Integer)entry.getValue();
            this.biomePalette[index] = (char)biome.getInternalId();
         }
      });
      StreamDelegate biomeData = schematic.add("BiomeData");
      biomeData.withInfo((length, type) -> {
         this.biomesOut = new FastByteArrayOutputStream();
         this.biomes = new FaweOutputStream(new LZ4BlockOutputStream(this.biomesOut));
      });
      biomeData.withInt((index, value) -> this.biomes.write(value));
      return root;
   }

   private BlockState getBlockState(int id) {
      return BlockTypesCache.states[this.palette[id]];
   }

   private BiomeType getBiomeType(FaweInputStream fis) throws IOException {
      char biomeId = this.biomePalette[fis.readVarInt()];
      return BiomeTypes.get(biomeId);
   }

   @Override
   public Clipboard read(UUID uuid, Function<BlockVector3, Clipboard> createOutput) throws IOException {
      StreamDelegate root = this.createDelegate();
      StreamDelegate versions = this.createVersionDelegate();
      this.inputStream.mark(Integer.MAX_VALUE);
      this.inputStream.readNamedTagLazy(versions);
      this.inputStream.reset();
      this.inputStream.readNamedTagLazy(root);
      if (this.version != 1 && this.version != 2) {
         throw new IOException(
            "This schematic version is not supported; Version: "
               + this.version
               + ", DataVersion: "
               + this.dataVersion
               + ". It's very likely your schematic has an invalid file extension, if the schematic has been created on a version lower than 1.13.2, the extension MUST be `.schematic`, elsewise the schematic can't be read properly. If you are using a litematica schematic, it is not supported!"
         );
      } else {
         if (this.blocks != null) {
            this.blocks.close();
         }

         if (this.biomes != null) {
            this.biomes.close();
         }

         this.blocks = null;
         this.biomes = null;
         BlockVector3 dimensions = BlockVector3.at(this.width, this.height, this.length);
         BlockVector3 origin;
         if (this.offsetX != Integer.MIN_VALUE && this.offsetY != Integer.MIN_VALUE && this.offsetZ != Integer.MIN_VALUE) {
            origin = BlockVector3.at(-this.offsetX, -this.offsetY, -this.offsetZ);
         } else {
            origin = BlockVector3.ZERO;
         }

         Clipboard clipboard = createOutput.apply(dimensions);
         if (this.blocksOut != null && this.blocksOut.getSize() != 0) {
            try (FaweInputStream fis = new FaweInputStream(new LZ4BlockInputStream(new FastByteArraysInputStream(this.blocksOut.toByteArrays())))) {
               if (clipboard instanceof LinearClipboard linear) {
                  int volume = this.width * this.height * this.length;
                  if (this.palette.length < 128) {
                     for (int index = 0; index < volume; index++) {
                        int ordinal = fis.read();
                        linear.setBlock(index, this.getBlockState(ordinal));
                     }
                  } else {
                     for (int index = 0; index < volume; index++) {
                        int ordinal = fis.readVarInt();
                        linear.setBlock(index, this.getBlockState(ordinal));
                     }
                  }
               } else if (this.palette.length < 128) {
                  for (int y = 0; y < this.height; y++) {
                     for (int z = 0; z < this.length; z++) {
                        for (int x = 0; x < this.width; x++) {
                           int ordinal = fis.read();
                           clipboard.setBlock(x, y, z, this.getBlockState(ordinal));
                        }
                     }
                  }
               } else {
                  for (int y = 0; y < this.height; y++) {
                     for (int z = 0; z < this.length; z++) {
                        for (int x = 0; x < this.width; x++) {
                           int ordinal = fis.readVarInt();
                           clipboard.setBlock(x, y, z, this.getBlockState(ordinal));
                        }
                     }
                  }
               }
            }
         }

         if (this.biomesOut != null && this.biomesOut.getSize() != 0 && this.biomePalette != null && this.biomePalette.length > 0) {
            try (FaweInputStream fisx = new FaweInputStream(new LZ4BlockInputStream(new FastByteArraysInputStream(this.biomesOut.toByteArrays())))) {
               for (int z = 0; z < this.length; z++) {
                  for (int x = 0; x < this.width; x++) {
                     BiomeType biome = this.getBiomeType(fisx);

                     for (int y = 0; y < this.height; y++) {
                        clipboard.setBiome(x, y, z, biome);
                     }
                  }
               }
            }
         }

         if (this.tiles != null && !this.tiles.isEmpty()) {
            for (Map<String, Object> tileRaw : this.tiles) {
               CompoundTag tile = FaweCache.INSTANCE.asTag(tileRaw);
               int[] pos = tile.getIntArray("Pos");
               int z;
               int x;
               int y;
               if (pos.length != 3) {
                  if (!tile.containsKey("x") || !tile.containsKey("y") || !tile.containsKey("z")) {
                     return null;
                  }

                  x = tile.getInt("x");
                  y = tile.getInt("y");
                  z = tile.getInt("z");
               } else {
                  x = pos[0];
                  y = pos[1];
                  z = pos[2];
               }

               Map<String, Tag<?, ?>> values = new HashMap<>(tile.getValue());
               Tag id = values.get("Id");
               if (id != null) {
                  values.put("x", new IntTag(x));
                  values.put("y", new IntTag(y));
                  values.put("z", new IntTag(z));
                  values.put("id", id);
               } else if (values.get("id") != null) {
                  values.put("x", new IntTag(x));
                  values.put("y", new IntTag(y));
                  values.put("z", new IntTag(z));
               }

               values.remove("Id");
               values.remove("Pos");
               clipboard.setTile(x, y, z, this.fixBlockEntity(new CompoundTag(values)));
            }
         }

         if (this.entities != null && !this.entities.isEmpty()) {
            Iterator var28 = this.entities.iterator();

            label180:
            while (true) {
               Map<String, Tag<?, ?>> value;
               StringTag id;
               do {
                  if (!var28.hasNext()) {
                     break label180;
                  }

                  Map<String, Object> entRaw = (Map<String, Object>)var28.next();
                  value = new HashMap<>(FaweCache.INSTANCE.asTag(entRaw).getValue());
                  id = (StringTag)value.get("Id");
                  if (id != null) {
                     break;
                  }

                  id = (StringTag)value.get("id");
               } while (id == null);

               value.put("id", id);
               value.remove("Id");
               EntityType type = EntityTypes.parse(id.getValue());
               if (type != null) {
                  CompoundTag ent = this.fixEntity(new CompoundTag(value));
                  BaseEntity state = new BaseEntity(type, ent);
                  Location loc = ent.getEntityLocation(clipboard);
                  if (!this.brokenEntities) {
                     if (!this.isWorldEdit && this.faweWritten == -1) {
                        int locX = loc.getBlockX();
                        int locY = loc.getBlockY();
                        int locZ = loc.getBlockZ();
                        BlockVector3 max = this.min.add(dimensions).subtract(BlockVector3.ONE);
                        if (locX < this.min.x() || locY < this.min.y() || locZ < this.min.z() || locX > max.x() || locY > max.y() || locZ > max.z()) {
                           for (Entity e : clipboard.getEntities()) {
                              clipboard.removeEntity(e);
                           }

                           LOGGER.error(
                              "Detected schematic entity outside clipboard region. FAWE will not load entities. Please try loading the schematic with the format \"legacyentity\""
                           );
                           break;
                        }
                     }

                     clipboard.createEntity(loc.setPosition(loc.subtract(this.min.toVector3())), state);
                  } else {
                     clipboard.createEntity(loc, state);
                  }
               } else {
                  LOGGER.error("Invalid entity: {}", id);
               }
            }
         }

         clipboard.setOrigin(origin);
         if (clipboard instanceof SimpleClipboard && !this.min.equals(BlockVector3.ZERO)) {
            clipboard = new BlockArrayClipboard((SimpleClipboard)clipboard, this.min);
         }

         return clipboard;
      }
   }

   @Override
   public void close() throws IOException {
      this.inputStream.close();
   }
}
