package com.fastasyncworldedit.core.extent.clipboard;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.internal.exception.FaweClipboardVersionMismatchException;
import com.fastasyncworldedit.core.internal.io.ByteBufferInputStream;
import com.fastasyncworldedit.core.jnbt.streamer.IntValueReader;
import com.fastasyncworldedit.core.math.IntTriple;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.NbtUtils;
import com.fastasyncworldedit.core.util.ReflectionUtils;
import com.google.common.collect.Collections2;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

public class DiskOptimizedClipboard extends LinearClipboard {
   public static final int VERSION = 2;
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final int HEADER_SIZE = 27;
   private static final int VERSION_1_HEADER_SIZE = 22;
   private static final int VERSION_2_HEADER_SIZE = 27;
   private static final Map<String, DiskOptimizedClipboard.LockHolder> LOCK_HOLDER_CACHE = new ConcurrentHashMap<>();
   private final HashMap<IntTriple, FaweCompoundTag> nbtMap;
   private final File file;
   private final int headerSize;
   private RandomAccessFile braf;
   private MappedByteBuffer byteBuffer = null;
   private FileChannel fileChannel = null;
   private boolean hasBiomes = false;
   private boolean canHaveBiomes = true;
   private int nbtBytesRemaining;

   public DiskOptimizedClipboard(Region region, UUID uuid) {
      this(
         region.getDimensions(),
         MainUtil.getFile(
            Fawe.instance() != null ? Fawe.<IFawe>platform().getDirectory() : new File("."),
            Settings.settings().PATHS.CLIPBOARD + File.separator + uuid + ".bd"
         )
      );
      this.setOffset(region.getMinimumPoint());
      this.setOrigin(region.getMinimumPoint());
   }

   @Deprecated(
      forRemoval = true,
      since = "2.3.0"
   )
   public DiskOptimizedClipboard(BlockVector3 dimensions) {
      this(
         dimensions,
         MainUtil.getFile(
            Fawe.platform() != null ? Fawe.<IFawe>platform().getDirectory() : new File("."),
            Settings.settings().PATHS.CLIPBOARD + File.separator + UUID.randomUUID() + ".bd"
         )
      );
   }

   public DiskOptimizedClipboard(BlockVector3 dimensions, File file) {
      super(dimensions, BlockVector3.ZERO);
      this.headerSize = 27;
      if (this.headerSize + ((long)this.getVolume() << 1) >= 2147483647L) {
         throw new IllegalArgumentException("Dimensions too large for this clipboard format. Use //lazycopy for large selections.");
      } else {
         if (this.headerSize
               + ((long)this.getVolume() << 1)
               + (long)((this.getHeight() >> 2) + 1) * ((this.getLength() >> 2) + 1) * ((this.getWidth() >> 2) + 1)
            >= 2147483647L) {
            LOGGER.error("Dimensions are too large for biomes to be stored in a DiskOptimizedClipboard");
            this.canHaveBiomes = false;
         }

         this.nbtMap = new HashMap<>();

         try {
            this.file = file;

            try {
               if (!file.exists()) {
                  File parent = file.getParentFile();
                  if (parent != null) {
                     file.getParentFile().mkdirs();
                  }

                  file.createNewFile();
               }
            } catch (Exception var5) {
               var5.printStackTrace();
            }

            this.braf = new RandomAccessFile(file, "rw");
            long fileLength = (long)(this.getVolume() << 1) + this.headerSize;
            this.braf.setLength(0L);
            this.braf.setLength(fileLength);
            this.nbtBytesRemaining = Integer.MAX_VALUE - (int)fileLength;
            this.init();
            this.byteBuffer.putChar(2, '\u0002');
            this.byteBuffer.putChar(4, (char)this.getWidth());
            this.byteBuffer.putChar(6, (char)this.getHeight());
            this.byteBuffer.putChar(8, (char)this.getLength());
         } catch (IOException var6) {
            this.close();
            throw new RuntimeException(var6);
         } catch (Throwable var7) {
            this.close();
            throw var7;
         }
      }
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public DiskOptimizedClipboard(File file) {
      this(file, 2);
   }

   @Deprecated(
      forRemoval = true,
      since = "2.6.2"
   )
   public DiskOptimizedClipboard(File file, int versionOverride) {
      super(readSize(file, versionOverride), BlockVector3.ZERO);
      this.headerSize = this.getHeaderSizeOverrideFromVersion(versionOverride);
      this.nbtMap = new HashMap<>();

      try {
         this.file = file;
         this.braf = new RandomAccessFile(file, "rw");
         this.braf.setLength(file.length());
         this.nbtBytesRemaining = Integer.MAX_VALUE - (int)file.length();
         this.init();
         int biomeLength = ((this.getHeight() >> 2) + 1) * ((this.getLength() >> 2) + 1) * ((this.getWidth() >> 2) + 1);
         this.canHaveBiomes = (long)this.headerSize + biomeLength < 2147483647L;
         if (this.headerSize >= 27) {
            this.readBiomeStatusFromHeader();
            int nbtCount = this.readNBTSavedCountFromHeader();
            int entitiesCount = this.readEntitiesSavedCountFromHeader();
            if (Settings.settings().CLIPBOARD.SAVE_CLIPBOARD_NBT_TO_DISK && nbtCount + entitiesCount > 0) {
               this.loadNBTFromFileFooter(nbtCount, entitiesCount, biomeLength);
            }
         } else if (this.canHaveBiomes && this.braf.length() - this.headerSize == ((long)this.getVolume() << 1) + biomeLength) {
            this.hasBiomes = true;
         }

         this.getAndSetOffsetAndOrigin();
      } catch (IOException var6) {
         this.close();
         throw new RuntimeException(var6);
      } catch (Throwable var7) {
         this.close();
         throw var7;
      }
   }

   public static DiskOptimizedClipboard loadFromFile(final File file) {
      DiskOptimizedClipboard doc;
      try {
         doc = new DiskOptimizedClipboard(file);
      } catch (FaweClipboardVersionMismatchException var4) {
         int version = var4.getClipboardVersion();
         doc = new DiskOptimizedClipboard(file, version);
      }

      return doc;
   }

   private static BlockVector3 readSize(File file, int expectedVersion) {
      try {
         BlockVector3 var4;
         try (DataInputStream is = new DataInputStream(new FileInputStream(file))) {
            is.skipBytes(2);
            int version = is.readChar();
            if (version != expectedVersion) {
               throw new FaweClipboardVersionMismatchException(expectedVersion, version);
            }

            var4 = BlockVector3.at(is.readChar(), is.readChar(), is.readChar());
         }

         return var4;
      } catch (IOException var7) {
         var7.printStackTrace();
         throw new RuntimeException(var7);
      }
   }

   private void loadNBTFromFileFooter(int nbtCount, int entitiesCount, long biomeLength) throws IOException {
      int biomeBlocksLength = this.headerSize + (this.getVolume() << 1) + (this.hasBiomes ? (int)biomeLength : 0);
      MappedByteBuffer tmp = this.fileChannel.map(MapMode.READ_ONLY, biomeBlocksLength, this.braf.length());

      try (NBTInputStream nbtIS = new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new ByteBufferInputStream(tmp)))) {
         Iterator<CompoundTag> iter = nbtIS.toIterator();

         while (nbtCount > 0 && iter.hasNext()) {
            LinCompoundTag tag = iter.next().toLinTag();
            int x = tag.getTag("x", LinTagType.intTag()).valueAsInt();
            int y = tag.getTag("y", LinTagType.intTag()).valueAsInt();
            int z = tag.getTag("z", LinTagType.intTag()).valueAsInt();
            IntTriple pos = new IntTriple(x, y, z);
            this.nbtMap.put(pos, FaweCompoundTag.of(tag));
            nbtCount--;
         }

         while (entitiesCount > 0 && iter.hasNext()) {
            CompoundTag tag = iter.next();
            Tag posTag = tag.getValue().get("Pos");
            if (posTag == null) {
               LOGGER.warn("Missing pos tag: {}", tag);
               return;
            }

            List<DoubleTag> pos = (List<DoubleTag>)posTag.getValue();
            double x = pos.get(0).getValue();
            double y = pos.get(1).getValue();
            double z = pos.get(2).getValue();
            BaseEntity entity = new BaseEntity(tag);
            BlockArrayClipboard.ClipboardEntity clipboardEntity = new BlockArrayClipboard.ClipboardEntity(this, x, y, z, 0.0F, 0.0F, entity);
            this.entities.add(clipboardEntity);
            entitiesCount--;
         }
      } catch (Exception var22) {
         var22.printStackTrace();
      }
   }

   private int getHeaderSizeOverrideFromVersion(int versionOverride) {
      return switch (versionOverride) {
         case 1 -> 22;
         case 2 -> 27;
         default -> 27;
      };
   }

   @Override
   public URI getURI() {
      return this.file.toURI();
   }

   public File getFile() {
      return this.file;
   }

   private void init() throws IOException {
      if (this.fileChannel == null) {
         this.fileChannel = this.braf.getChannel();
         if (Settings.settings().CLIPBOARD.LOCK_CLIPBOARD_FILE) {
            try {
               FileLock lock = this.fileChannel.lock();
               LOCK_HOLDER_CACHE.put(this.file.getName(), new DiskOptimizedClipboard.LockHolder(lock));
            } catch (OverlappingFileLockException var5) {
               DiskOptimizedClipboard.LockHolder existing = LOCK_HOLDER_CACHE.get(this.file.getName());
               if (existing != null) {
                  long ms = System.currentTimeMillis() - existing.lockHeldSince;
                  LOGGER.error("Cannot lock clipboard file {} acquired by thread {}, {}ms ago", this.file.getName(), existing.thread, ms);
               }

               throw var5;
            }
         }

         this.byteBuffer = this.fileChannel.map(MapMode.READ_WRITE, 0L, this.braf.length());
      }
   }

   private boolean initBiome() {
      if (!this.canHaveBiomes) {
         return false;
      } else {
         if (!this.hasBiomes) {
            try {
               this.hasBiomes = true;
               this.close();
               this.braf = new RandomAccessFile(this.file, "rw");
               long length = this.headerSize
                  + ((long)this.getVolume() << 1)
                  + (long)((this.getHeight() >> 2) + 1) * ((this.getLength() >> 2) + 1) * ((this.getWidth() >> 2) + 1);
               this.braf.setLength(length);
               this.nbtBytesRemaining = Integer.MAX_VALUE - (int)length;
               this.init();
            } catch (IOException var3) {
               var3.printStackTrace();
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public boolean hasBiomes() {
      return this.hasBiomes;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      this.setBiome(this.getBiomeIndex(x, y, z), biome);
      return true;
   }

   @Override
   public void setBiome(int index, BiomeType biome) {
      if (this.initBiome()) {
         try {
            this.byteBuffer.put(this.headerSize + (this.getVolume() << 1) + index, (byte)biome.getInternalId());
         } catch (IndexOutOfBoundsException var4) {
            LOGGER.info((long)(this.getHeight() >> 2) * (this.getLength() >> 2) * (this.getWidth() >> 2));
            LOGGER.info(index);
            var4.printStackTrace();
         }
      }
   }

   @Override
   public BiomeType getBiome(int index) {
      if (!this.hasBiomes()) {
         return null;
      } else {
         int biomeId = this.byteBuffer.get(this.headerSize + (this.getVolume() << 1) + index) & 255;
         return BiomeTypes.get(biomeId);
      }
   }

   @Override
   public void streamBiomes(IntValueReader task) {
      if (this.hasBiomes()) {
         int mbbIndex = this.headerSize + (this.getVolume() << 1);

         try {
            for (int y = 0; y < this.getHeight(); y++) {
               for (int z = 0; z < this.getLength(); z++) {
                  for (int x = 0; x < this.getWidth(); x++) {
                     int biome = this.byteBuffer.get(mbbIndex + this.getBiomeIndex(x, y, z)) & 255;
                     task.applyInt(this.getIndex(x, y, z), biome);
                  }
               }
            }
         } catch (IOException var7) {
            throw new RuntimeException(var7);
         }
      }
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.getBiome(this.getBiomeIndex(x, y, z));
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.getBiome(this.getBiomeIndex(position.x(), position.y(), position.z()));
   }

   public BlockArrayClipboard toClipboard() {
      try {
         Region region = this.getRegion();
         region.shift(this.offset);
         BlockArrayClipboard clipboard = new BlockArrayClipboard(region, this);
         clipboard.setOrigin(this.getOrigin().add(this.offset));
         return clipboard;
      } catch (Throwable var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public void setOrigin(BlockVector3 origin) {
      super.setOrigin(origin);
      origin = origin.subtract(this.offset);

      try {
         this.byteBuffer.putShort(10, (short)origin.x());
         this.byteBuffer.putShort(12, (short)origin.y());
         this.byteBuffer.putShort(14, (short)origin.z());
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   @Override
   protected void setOffset(BlockVector3 offset) {
      super.setOffset(offset);

      try {
         this.byteBuffer.putShort(16, (short)offset.x());
         this.byteBuffer.putShort(18, (short)offset.y());
         this.byteBuffer.putShort(20, (short)offset.z());
      } catch (Throwable var3) {
         var3.printStackTrace();
      }
   }

   private void getAndSetOffsetAndOrigin() {
      int x = this.byteBuffer.getShort(16);
      int y = this.byteBuffer.getShort(18);
      int z = this.byteBuffer.getShort(20);
      super.setOffset(BlockVector3.at(x, y, z));
      int ox = this.byteBuffer.getShort(10);
      int oy = this.byteBuffer.getShort(12);
      int oz = this.byteBuffer.getShort(14);
      super.setOrigin(BlockVector3.at(ox, oy, oz));
   }

   @Override
   public void flush() {
      this.byteBuffer.force();
   }

   private void closeDirectBuffer(ByteBuffer cb) {
      if (cb != null && cb.isDirect()) {
         ReflectionUtils.getUnsafe().invokeCleaner(cb);
      }
   }

   private void writeBiomeStatusToHeader() {
      this.byteBuffer.put(22, (byte)(this.hasBiomes ? 1 : 0));
   }

   private void writeNBTSavedCountToHeader(int count) {
      this.byteBuffer.putChar(23, (char)count);
   }

   private void writeEntitiesSavedCountToHeader(int count) {
      this.byteBuffer.putChar(25, (char)count);
   }

   private boolean readBiomeStatusFromHeader() {
      return this.hasBiomes = this.byteBuffer.get(22) == 1;
   }

   private int readNBTSavedCountFromHeader() {
      return this.byteBuffer.getChar(23);
   }

   private int readEntitiesSavedCountFromHeader() {
      return this.byteBuffer.getChar(25);
   }

   @Override
   public void close() {
      try {
         if (this.byteBuffer != null) {
            if (this.headerSize >= 27) {
               if (Settings.settings().CLIPBOARD.SAVE_CLIPBOARD_NBT_TO_DISK) {
                  try {
                     this.writeNBTToDisk();
                  } catch (Exception var4) {
                     LOGGER.error("Unable to save NBT data to disk.", var4);
                  }
               }

               this.writeBiomeStatusToHeader();
            }

            this.byteBuffer.force();
            this.fileChannel.close();
            this.braf.close();
            this.file.setWritable(true);
            MappedByteBuffer tmpBuffer = this.byteBuffer;
            this.byteBuffer = null;
            this.closeDirectBuffer(tmpBuffer);
            this.fileChannel = null;
            this.braf = null;
         } else if (this.fileChannel != null) {
            this.fileChannel.close();
            this.fileChannel = null;
         }
      } catch (IOException var5) {
         var5.printStackTrace();
         if (this.fileChannel != null) {
            try {
               this.fileChannel.close();
               this.fileChannel = null;
            } catch (IOException var3) {
               LOGGER.error(
                  "Could not close file channel on clipboard {}. If this belongs to a player, the server may need to be restarted for clipboard use to work.",
                  this.getFile().getName(),
                  var3
               );
            }
         }
      }
   }

   private void writeNBTToDisk() throws IOException {
      NBTOutputStream nbtOS;
      label95: {
         if (this.nbtMap.isEmpty() && this.entities.isEmpty()) {
            this.writeNBTSavedCountToHeader(0);
            this.writeEntitiesSavedCountToHeader(0);
         } else {
            byte[] output = null;
            boolean entitiesFit = false;
            ByteArrayOutputStream baOS = new ByteArrayOutputStream();
            nbtOS = new NBTOutputStream((DataOutputStream)MainUtil.getCompressedOS(baOS, Settings.settings().CLIPBOARD.COMPRESSION_LEVEL));

            try {
               if (!this.nbtMap.isEmpty()) {
                  try {
                     for (FaweCompoundTag tag : this.nbtMap.values()) {
                        nbtOS.writeTag(new CompoundTag(tag.linTag()));
                     }
                  } catch (IOException var11) {
                     var11.printStackTrace();
                  }

                  nbtOS.flush();
                  if (baOS.size() > this.nbtBytesRemaining) {
                     LOGGER.warn("Clipboard file {} does not have enough remaining space to store NBT data on disk.", this.file.getName());
                     this.writeNBTSavedCountToHeader(0);
                     this.writeEntitiesSavedCountToHeader(0);
                     break label95;
                  }

                  this.writeNBTSavedCountToHeader(this.nbtMap.size());
                  this.nbtBytesRemaining = this.nbtBytesRemaining - baOS.size();
                  output = baOS.toByteArray();
               }

               if (!this.entities.isEmpty()) {
                  try {
                     for (BlockArrayClipboard.ClipboardEntity entity : this.entities) {
                        if (entity.getState() != null && entity.getState().getNbtData() != null) {
                           CompoundTag data = entity.getState().getNbtData();
                           HashMap<String, Tag<?, ?>> value = new HashMap<>(data.getValue());
                           List<DoubleTag> pos = new ArrayList<>(3);
                           pos.add(new DoubleTag(entity.getLocation().x()));
                           pos.add(new DoubleTag(entity.getLocation().x()));
                           pos.add(new DoubleTag(entity.getLocation().x()));
                           value.put("Pos", new ListTag(DoubleTag.class, pos));
                           nbtOS.writeTag(new CompoundTag(value));
                        }
                     }
                  } catch (IOException var12) {
                     var12.printStackTrace();
                  }

                  nbtOS.flush();
                  if (baOS.size() > this.nbtBytesRemaining) {
                     LOGGER.warn("Clipboard file {} does not have enough remaining space to store entity data on disk.", this.file.getName());
                     this.writeEntitiesSavedCountToHeader(0);
                  } else {
                     entitiesFit = true;
                     this.writeEntitiesSavedCountToHeader(this.entities.size());
                  }
               }
            } catch (Throwable var13) {
               try {
                  nbtOS.close();
               } catch (Throwable var10) {
                  var13.addSuppressed(var10);
               }

               throw var13;
            }

            nbtOS.close();
            if (entitiesFit) {
               output = baOS.toByteArray();
            }

            if (output == null) {
               return;
            }

            long currentLength = this.braf.length();
            this.braf.setLength(currentLength + baOS.size());
            MappedByteBuffer tempBuffer = this.fileChannel.map(MapMode.READ_WRITE, currentLength, baOS.size());
            tempBuffer.put(output);
            tempBuffer.force();
            this.closeDirectBuffer(tempBuffer);
         }

         return;
      }

      nbtOS.close();
   }

   @Override
   public Collection<CompoundTag> getTileEntities() {
      return Collections2.transform(this.nbtMap.values(), fct -> new CompoundTag(fct.linTag()));
   }

   public int getIndex(int x, int y, int z) {
      return x + y * this.getArea() + z * this.getWidth();
   }

   public int getBiomeIndex(int x, int y, int z) {
      return (x >> 2) + (y >> 2) * (this.getWidth() >> 2) * (this.getLength() >> 2) + (z >> 2) * (this.getWidth() >> 2);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.toBaseBlock(this.getBlock(x, y, z), x, y, z);
   }

   private BaseBlock toBaseBlock(BlockState state, int i) {
      if (state.getMaterial().hasContainer() && !this.nbtMap.isEmpty()) {
         FaweCompoundTag nbt;
         if (this.nbtMap.size() < 4) {
            nbt = null;

            for (Entry<IntTriple, FaweCompoundTag> entry : this.nbtMap.entrySet()) {
               IntTriple key = entry.getKey();
               int index = this.getIndex(key.x(), key.y(), key.z());
               if (index == i) {
                  nbt = entry.getValue();
                  break;
               }
            }
         } else {
            int y = i / this.getArea();
            int newI = i - y * this.getArea();
            int z = newI / this.getWidth();
            int x = newI - z * this.getWidth();
            nbt = this.nbtMap.get(new IntTriple(x, y, z));
         }

         return state.toBaseBlock(nbt == null ? null : nbt.linTag());
      } else {
         return state.toBaseBlock();
      }
   }

   private BaseBlock toBaseBlock(BlockState state, int x, int y, int z) {
      if (state.getMaterial().hasContainer() && !this.nbtMap.isEmpty()) {
         FaweCompoundTag nbt = this.nbtMap.get(new IntTriple(x, y, z));
         return state.toBaseBlock(nbt == null ? null : nbt.linTag());
      } else {
         return state.toBaseBlock();
      }
   }

   @Override
   public BaseBlock getFullBlock(int i) {
      return this.toBaseBlock(this.getBlock(i), i);
   }

   @Override
   public BlockState getBlock(int index) {
      try {
         int diskIndex = this.headerSize + (index << 1);
         char ordinal = this.byteBuffer.getChar(diskIndex);
         return BlockState.getFromOrdinal(ordinal);
      } catch (IndexOutOfBoundsException var4) {
         return BlockTypes.AIR.getDefaultState();
      }
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getBlock(this.getIndex(x, y, z));
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      this.nbtMap.put(new IntTriple(x, y, z), NbtUtils.withPosition(tile, x, y, z));
      return true;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) {
      try {
         int index = this.headerSize + (this.getIndex(x, y, z) << 1);
         char ordinal = block.getOrdinalChar();
         if (ordinal == 0) {
            ordinal = 1;
         }

         this.byteBuffer.putChar(index, ordinal);
         boolean hasNbt = block instanceof BaseBlock && block.hasNbtData();
         if (hasNbt) {
            this.setTile(x, y, z, block.getNbtData());
         }

         return true;
      } catch (Exception var8) {
         var8.printStackTrace();
         return false;
      }
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int i, B block) {
      try {
         char ordinal = block.getOrdinalChar();
         int index = this.headerSize + (i << 1);
         this.byteBuffer.putChar(index, ordinal);
         boolean hasNbt = block instanceof BaseBlock && block.hasNbtData();
         if (hasNbt) {
            int y = i / this.getArea();
            int newI = i - y * this.getArea();
            int z = newI / this.getWidth();
            int x = newI - z * this.getWidth();
            this.setTile(x, y, z, block.getNbtData());
         }

         return true;
      } catch (Exception var10) {
         var10.printStackTrace();
         return false;
      }
   }

   private static class LockHolder {
      final FileLock lock;
      final long lockHeldSince;
      final String thread;

      LockHolder(FileLock lock) {
         this.lock = lock;
         this.lockHeldSince = System.currentTimeMillis();
         this.thread = Thread.currentThread().getName();
      }
   }
}
