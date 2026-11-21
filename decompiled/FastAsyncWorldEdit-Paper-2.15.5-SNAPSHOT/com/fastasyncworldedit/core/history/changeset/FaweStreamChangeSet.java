package com.fastasyncworldedit.core.history.changeset;

import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.history.change.ChangePopulator;
import com.fastasyncworldedit.core.history.change.MutableBiomeChange;
import com.fastasyncworldedit.core.history.change.MutableBlockChange;
import com.fastasyncworldedit.core.history.change.MutableEntityChange;
import com.fastasyncworldedit.core.history.change.MutableFullBlockChange;
import com.fastasyncworldedit.core.history.change.MutableTileChange;
import com.fastasyncworldedit.core.internal.exception.FaweSmallEditUnsupportedException;
import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.Exchanger;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FaweStreamChangeSet extends AbstractChangeSet {
   public static final int HEADER_SIZE = 9;
   private static final int VERSION = 2;
   private static final byte[] MAGIC_NEW_RELATIVE = new byte[]{0, -128, 0, -128, 0, -128};
   private int mode;
   private final int compression;
   private final int minY;
   protected long blockSize;
   private int originX;
   private int originZ;
   private int version;
   protected FaweStreamChangeSet.FaweStreamIdDelegate idDel;
   protected FaweStreamChangeSet.FaweStreamPositionDelegate posDel;

   public FaweStreamChangeSet(World world) {
      this(world, Settings.settings().HISTORY.COMPRESSION_LEVEL, Settings.settings().HISTORY.STORE_REDO, Settings.settings().HISTORY.SMALL_EDITS);
   }

   public FaweStreamChangeSet(World world, int compression, boolean storeRedo, boolean smallLoc) {
      super(world);
      this.compression = compression;
      this.minY = world.getMinY();
      this.init(storeRedo, smallLoc);
   }

   private void init(boolean storeRedo, boolean smallLoc) {
      if (storeRedo) {
         if (smallLoc) {
            this.mode = 4;
         } else {
            this.mode = 3;
         }
      } else if (smallLoc) {
         this.mode = 1;
      } else {
         this.mode = 2;
      }
   }

   protected void setupStreamDelegates(int mode) {
      this.mode = mode;
      if (mode != 3 && mode != 4) {
         this.idDel = new FaweStreamChangeSet.FaweStreamIdDelegate() {
            @Override
            public void writeChange(FaweOutputStream stream, int combinedFrom, int to) throws IOException {
               stream.writeVarInt(combinedFrom);
            }

            @Override
            public void readCombined(FaweInputStream in, MutableBlockChange change, boolean dir) throws IOException {
               change.ordinal = in.readVarInt();
            }

            @Override
            public void readCombined(FaweInputStream is, MutableFullBlockChange change) throws IOException {
               change.from = is.readVarInt();
               change.to = BlockTypes.AIR.getInternalId();
            }
         };
      } else {
         this.idDel = new FaweStreamChangeSet.FaweStreamIdDelegate() {
            @Override
            public void writeChange(FaweOutputStream stream, int combinedFrom, int combinedTo) throws IOException {
               stream.writeVarInt(combinedFrom);
               stream.writeVarInt(combinedTo);
            }

            @Override
            public void readCombined(FaweInputStream is, MutableBlockChange change, boolean dir) throws IOException {
               if (dir) {
                  is.readVarInt();
                  change.ordinal = is.readVarInt();
               } else {
                  change.ordinal = is.readVarInt();
                  is.readVarInt();
               }
            }

            @Override
            public void readCombined(FaweInputStream is, MutableFullBlockChange change) throws IOException {
               change.from = is.readVarInt();
               change.to = is.readVarInt();
            }
         };
      }

      if (mode != 1 && mode != 4) {
         this.posDel = new FaweStreamChangeSet.FaweStreamPositionDelegate() {
            final byte[] buffer = new byte[6];
            int lx;
            int ly;
            int lz;

            @Override
            public void write(OutputStream stream, int x, int y, int z) throws IOException {
               int rx = -this.lx + (this.lx = x);
               int ry = -this.ly + (this.ly = y);
               int rz = -this.lz + (this.lz = z);
               if (rx >= 32767 || rz >= 32767 || rx <= -32768 || rz <= -32768) {
                  stream.write(FaweStreamChangeSet.MAGIC_NEW_RELATIVE);
                  stream.write((byte)(x >> 24));
                  stream.write((byte)(x >> 16));
                  stream.write((byte)(x >> 8));
                  stream.write((byte)x);
                  stream.write((byte)(z >> 24));
                  stream.write((byte)(z >> 16));
                  stream.write((byte)(z >> 8));
                  stream.write((byte)z);
                  rx = 0;
                  rz = 0;
               }

               stream.write(rx & 0xFF);
               stream.write(rx >> 8 & 0xFF);
               stream.write(rz & 0xFF);
               stream.write(rz >> 8 & 0xFF);
               stream.write(ry & 0xFF);
               stream.write(ry >> 8 & 0xFF);
            }

            @Override
            public int readX(FaweInputStream is) throws IOException {
               is.readFully(this.buffer);
               if (FaweStreamChangeSet.this.version == 2 && Arrays.equals(this.buffer, FaweStreamChangeSet.MAGIC_NEW_RELATIVE)) {
                  this.lx = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
                  this.lz = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
                  is.readFully(this.buffer);
               }

               return this.lx = this.lx + (this.buffer[0] & 0xFF | this.buffer[1] << 8);
            }

            @Override
            public int readY(FaweInputStream is) throws IOException {
               return this.ly = this.ly + (this.buffer[4] & 0xFF | this.buffer[5] << 8);
            }

            @Override
            public int readZ(FaweInputStream is) throws IOException {
               return this.lz = this.lz + (this.buffer[2] & 0xFF | this.buffer[3] << 8);
            }
         };
      } else {
         this.posDel = new FaweStreamChangeSet.FaweStreamPositionDelegate() {
            int lx;
            int ly;
            int lz;
            final byte[] buffer = new byte[4];

            @Override
            public void write(OutputStream out, int x, int y, int z) throws IOException {
               if (y >= 0 && y <= 255) {
                  int rx = -this.lx + (this.lx = x);
                  int ry = -this.ly + (this.ly = y);
                  int rz = -this.lz + (this.lz = z);
                  byte b1 = (byte)ry;
                  byte b2 = (byte)rx;
                  byte b3 = (byte)rz;
                  int x16 = rx >> 8 & 15;
                  int z16 = rz >> 8 & 15;
                  byte b4 = MathMan.pair16(x16, z16);
                  out.write(b1);
                  out.write(b2);
                  out.write(b3);
                  out.write(b4);
               } else {
                  throw new FaweSmallEditUnsupportedException();
               }
            }

            @Override
            public int readX(FaweInputStream in) throws IOException {
               in.readFully(this.buffer);
               return this.lx = this.lx + ((this.buffer[1] & 0xFF | MathMan.unpair16x(this.buffer[3]) << 8) << 20 >> 20);
            }

            @Override
            public int readY(FaweInputStream in) {
               return (this.ly = this.ly + this.buffer[0]) & 0xFF;
            }

            @Override
            public int readZ(FaweInputStream in) throws IOException {
               return this.lz = this.lz + ((this.buffer[2] & 0xFF | MathMan.unpair16y(this.buffer[3]) << 8) << 20 >> 20);
            }
         };
      }
   }

   public void writeHeader(OutputStream os, int x, int y, int z) throws IOException {
      os.write(this.mode);
      os.write(2);
      this.setOrigin(x, z);
      os.write((byte)(x >> 24));
      os.write((byte)(x >> 16));
      os.write((byte)(x >> 8));
      os.write((byte)x);
      os.write((byte)(z >> 24));
      os.write((byte)(z >> 16));
      os.write((byte)(z >> 8));
      os.write((byte)z);
      this.setupStreamDelegates(this.mode);
   }

   public void readHeader(InputStream is) throws IOException {
      int mode = is.read();
      this.version = is.read();
      if (this.version != 1 && this.version != 2) {
         throw new UnsupportedOperationException(String.format("Version %s history not supported!", this.version));
      } else {
         int x = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
         int z = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read();
         this.setOrigin(x, z);
         this.setupStreamDelegates(mode);
      }
   }

   public FaweOutputStream getCompressedOS(OutputStream os) throws IOException {
      return MainUtil.getCompressedOS(os, this.compression);
   }

   @Override
   public boolean isEmpty() {
      if (this.blockSize > 0L) {
         return false;
      } else if (!super.isEmpty()) {
         return false;
      } else {
         this.flush();
         return this.blockSize == 0L;
      }
   }

   @Override
   public long longSize() {
      this.flush();
      return this.blockSize;
   }

   @Override
   public int size() {
      return (int)this.longSize();
   }

   public abstract int getCompressedSize();

   public abstract long getSizeInMemory();

   public long getSizeOnDisk() {
      return 0L;
   }

   public abstract FaweOutputStream getBlockOS(int x, int y, int z) throws IOException;

   public abstract FaweOutputStream getBiomeOS() throws IOException;

   public abstract NBTOutputStream getEntityCreateOS() throws IOException;

   public abstract NBTOutputStream getEntityRemoveOS() throws IOException;

   public abstract NBTOutputStream getTileCreateOS() throws IOException;

   public abstract NBTOutputStream getTileRemoveOS() throws IOException;

   public abstract FaweInputStream getBlockIS() throws IOException;

   public abstract FaweInputStream getBiomeIS() throws IOException;

   public abstract NBTInputStream getEntityCreateIS() throws IOException;

   public abstract NBTInputStream getEntityRemoveIS() throws IOException;

   public abstract NBTInputStream getTileCreateIS() throws IOException;

   public abstract NBTInputStream getTileRemoveIS() throws IOException;

   public void setOrigin(int x, int z) {
      this.originX = x;
      this.originZ = z;
   }

   public int getOriginX() {
      return this.originX;
   }

   public int getOriginZ() {
      return this.originZ;
   }

   @Override
   public void add(int x, int y, int z, int combinedFrom, int combinedTo) {
      this.blockSize++;

      try {
         FaweOutputStream stream = this.getBlockOS(x, y, z);
         this.posDel.write(stream, x - this.originX, y, z - this.originZ);
         this.idDel.writeChange(stream, combinedFrom, combinedTo);
      } catch (IOException var7) {
         var7.printStackTrace();
      }
   }

   @Override
   public void addBiomeChange(int bx, int by, int bz, BiomeType from, BiomeType to) {
      this.blockSize++;

      try {
         int x = bx >> 2;
         int y = by >> 2;
         int z = bz >> 2;
         FaweOutputStream os = this.getBiomeOS();
         os.write((byte)(x >> 24));
         os.write((byte)(x >> 16));
         os.write((byte)(x >> 8));
         os.write((byte)x);
         os.write((byte)(z >> 24));
         os.write((byte)(z >> 16));
         os.write((byte)(z >> 8));
         os.write((byte)z);
         os.write((byte)(y + 128));
         os.writeVarInt(from.getInternalId());
         os.writeVarInt(to.getInternalId());
      } catch (IOException var10) {
         var10.printStackTrace();
      }
   }

   @Override
   public void addTileCreate(final FaweCompoundTag tag) {
      this.blockSize++;

      try {
         NBTOutputStream nbtos = this.getTileCreateOS();
         nbtos.writeTag(new CompoundTag(tag.linTag()));
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void addTileRemove(final FaweCompoundTag tag) {
      this.blockSize++;

      try {
         NBTOutputStream nbtos = this.getTileRemoveOS();
         nbtos.writeTag(new CompoundTag(tag.linTag()));
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void addEntityRemove(final FaweCompoundTag tag) {
      this.blockSize++;

      try {
         NBTOutputStream nbtos = this.getEntityRemoveOS();
         nbtos.writeTag(new CompoundTag(tag.linTag()));
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   @Override
   public void addEntityCreate(final FaweCompoundTag tag) {
      this.blockSize++;

      try {
         NBTOutputStream nbtos = this.getEntityCreateOS();
         nbtos.writeTag(new CompoundTag(tag.linTag()));
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   public Iterator<MutableBlockChange> getBlockIterator(final boolean dir) throws IOException {
      final FaweInputStream is = this.getBlockIS();
      if (is == null) {
         return Collections.emptyIterator();
      } else {
         final MutableBlockChange change = new MutableBlockChange(0, 0, 0, BlockTypes.AIR.getInternalId());
         return new Iterator<MutableBlockChange>() {
            private MutableBlockChange last = this.read();

            public MutableBlockChange read() {
               try {
                  change.x = FaweStreamChangeSet.this.posDel.readX(is) + FaweStreamChangeSet.this.originX;
                  change.y = FaweStreamChangeSet.this.posDel.readY(is);
                  change.z = FaweStreamChangeSet.this.posDel.readZ(is) + FaweStreamChangeSet.this.originZ;
                  FaweStreamChangeSet.this.idDel.readCombined(is, change, dir);
                  return change;
               } catch (EOFException var3) {
               } catch (Exception var4) {
                  var4.printStackTrace();
               }

               try {
                  is.close();
               } catch (IOException var2) {
                  var2.printStackTrace();
               }

               return null;
            }

            @Override
            public boolean hasNext() {
               return this.last != null || (this.last = this.read()) != null;
            }

            public MutableBlockChange next() {
               MutableBlockChange tmp = this.last;
               if (tmp == null) {
                  tmp = this.read();
               }

               this.last = null;
               return tmp;
            }

            @Override
            public void remove() {
               throw new IllegalArgumentException("CANNOT REMOVE");
            }
         };
      }
   }

   public Iterator<MutableBiomeChange> getBiomeIterator(final boolean dir) throws IOException {
      final FaweInputStream is = this.getBiomeIS();
      if (is == null) {
         return Collections.emptyIterator();
      } else {
         final MutableBiomeChange change = new MutableBiomeChange();
         return new Iterator<MutableBiomeChange>() {
            private MutableBiomeChange last = new MutableBiomeChange();

            public MutableBiomeChange read() {
               try {
                  int int1 = is.read();
                  if (int1 != -1) {
                     int x = (int1 << 24) + (is.read() << 16) + (is.read() << 8) + is.read() << 2;
                     int z = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read() << 2;
                     int y = is.read() - 128 << 2;
                     int from = is.readVarInt();
                     int to = is.readVarInt();
                     change.setBiome(x, y, z, from, to);
                     return change;
                  }
               } catch (EOFException var8) {
               } catch (Exception var9) {
                  var9.printStackTrace();
               }

               try {
                  is.close();
               } catch (IOException var7) {
                  var7.printStackTrace();
               }

               return null;
            }

            @Override
            public boolean hasNext() {
               return this.last != null || (this.last = this.read()) != null;
            }

            public MutableBiomeChange next() {
               MutableBiomeChange tmp = this.last;
               if (tmp == null) {
                  tmp = this.read();
               }

               this.last = null;
               return tmp;
            }

            @Override
            public void remove() {
               throw new IllegalArgumentException("CANNOT REMOVE");
            }
         };
      }
   }

   @Override
   public Iterator<Change> getIterator(BlockBag blockBag, int mode, boolean redo) {
      if (blockBag != null && mode > 0) {
         try {
            return this.getFullBlockIterator(blockBag, mode, redo);
         } catch (IOException var5) {
            var5.printStackTrace();
         }
      }

      return this.getIterator(redo);
   }

   public Iterator<MutableFullBlockChange> getFullBlockIterator(BlockBag blockBag, int inventory, final boolean dir) throws IOException {
      final FaweInputStream is = this.getBlockIS();
      if (is == null) {
         return Collections.emptyIterator();
      } else {
         final MutableFullBlockChange change = new MutableFullBlockChange(blockBag, inventory, dir);
         return new Iterator<MutableFullBlockChange>() {
            private MutableFullBlockChange last = this.read();

            public MutableFullBlockChange read() {
               try {
                  change.x = FaweStreamChangeSet.this.posDel.readX(is) + FaweStreamChangeSet.this.originX;
                  change.y = FaweStreamChangeSet.this.posDel.readY(is);
                  change.z = FaweStreamChangeSet.this.posDel.readZ(is) + FaweStreamChangeSet.this.originZ;
                  FaweStreamChangeSet.this.idDel.readCombined(is, change);
                  return change;
               } catch (EOFException var3) {
               } catch (Exception var4) {
                  var4.printStackTrace();
               }

               try {
                  is.close();
               } catch (IOException var2) {
                  var2.printStackTrace();
               }

               return null;
            }

            @Override
            public boolean hasNext() {
               return this.last != null || (this.last = this.read()) != null;
            }

            public MutableFullBlockChange next() {
               MutableFullBlockChange tmp = this.last;
               if (tmp == null) {
                  tmp = this.read();
               }

               this.last = null;
               return tmp;
            }

            @Override
            public void remove() {
               throw new IllegalArgumentException("CANNOT REMOVE");
            }
         };
      }
   }

   public Iterator<MutableEntityChange> getEntityIterator(final NBTInputStream is, final boolean create) {
      if (is == null) {
         return Collections.emptyIterator();
      } else {
         final MutableEntityChange change = new MutableEntityChange(null, create);

         try {
            return new Iterator<MutableEntityChange>() {
               private MutableEntityChange last = this.read();

               public MutableEntityChange read() {
                  try {
                     change.tag = (CompoundTag)is.readTag();
                     return change;
                  } catch (Exception var3) {
                     try {
                        is.close();
                     } catch (IOException var2) {
                        var2.printStackTrace();
                     }

                     return null;
                  }
               }

               @Override
               public boolean hasNext() {
                  return this.last != null || (this.last = this.read()) != null;
               }

               public MutableEntityChange next() {
                  MutableEntityChange tmp = this.last;
                  if (tmp == null) {
                     tmp = this.read();
                  }

                  this.last = null;
                  return tmp;
               }

               @Override
               public void remove() {
                  throw new IllegalArgumentException("CANNOT REMOVE");
               }
            };
         } catch (Exception var5) {
            var5.printStackTrace();
            return null;
         }
      }
   }

   public Iterator<MutableTileChange> getTileIterator(final NBTInputStream is, final boolean create) {
      if (is == null) {
         return Collections.emptyIterator();
      } else {
         final MutableTileChange change = new MutableTileChange(null, create);

         try {
            return new Iterator<MutableTileChange>() {
               private MutableTileChange last = this.read();

               public MutableTileChange read() {
                  try {
                     change.tag = (CompoundTag)is.readTag();
                     return change;
                  } catch (Exception var3) {
                     try {
                        is.close();
                     } catch (IOException var2) {
                        var2.printStackTrace();
                     }

                     return null;
                  }
               }

               @Override
               public boolean hasNext() {
                  return this.last != null || (this.last = this.read()) != null;
               }

               public MutableTileChange next() {
                  MutableTileChange tmp = this.last;
                  if (tmp == null) {
                     tmp = this.read();
                  }

                  this.last = null;
                  return tmp;
               }

               @Override
               public void remove() {
                  throw new IllegalArgumentException("CANNOT REMOVE");
               }
            };
         } catch (Exception var5) {
            var5.printStackTrace();
            return null;
         }
      }
   }

   @Override
   public ChangeExchangeCoordinator getCoordinatedChanges(BlockBag blockBag, int mode, boolean dir) {
      try {
         return this.coordinatedChanges(blockBag, mode, dir);
      } catch (IOException var5) {
         throw new RuntimeException(var5);
      }
   }

   private ChangeExchangeCoordinator coordinatedChanges(final BlockBag blockBag, final int mode, boolean dir) throws IOException {
      this.close();
      ChangePopulator<MutableTileChange> tileCreate = this.tileChangePopulator(this.getTileCreateIS(), true);
      ChangePopulator<MutableTileChange> tileRemove = this.tileChangePopulator(this.getTileRemoveIS(), false);
      ChangePopulator<MutableEntityChange> entityCreate = this.entityChangePopulator(this.getEntityCreateIS(), true);
      ChangePopulator<MutableEntityChange> entityRemove = this.entityChangePopulator(this.getEntityRemoveIS(), false);
      ChangePopulator<? extends Change> blockChange = blockBag != null && mode > 0
         ? this.fullBlockChangePopulator(blockBag, mode, dir)
         : this.blockChangePopulator(dir);
      ChangePopulator<MutableBiomeChange> biomeChange = this.biomeChangePopulator(dir);
      Queue<ChangePopulator<?>> populators = new ArrayDeque<>(List.of(tileCreate, tileRemove, entityCreate, entityRemove, blockChange, biomeChange));
      BiConsumer<Exchanger<Change[]>, Change[]> task = (exchanger, array) -> {
         while (this.fillArray(array, populators)) {
            try {
               array = exchanger.exchange(array);
            } catch (InterruptedException var5x) {
               return;
            }
         }
      };
      return new ChangeExchangeCoordinator(task);
   }

   private boolean fillArray(Change[] changes, Queue<ChangePopulator<?>> populators) {
      ChangePopulator<?> populator = populators.peek();
      if (populator == null) {
         return false;
      } else {
         label25:
         for (int i = 0; i < changes.length; i++) {
            Change change = changes[i];

            do {
               change = populator.updateOrCreate(change);
               if (change != null) {
                  changes[i] = change;
                  continue label25;
               }

               populators.remove();
               populator = populators.peek();
            } while (populator != null);

            changes[i] = null;
            return true;
         }

         return true;
      }
   }

   private ChangePopulator<MutableTileChange> tileChangePopulator(NBTInputStream is, boolean create) {
      class Populator extends FaweStreamChangeSet.CompoundTagPopulator<MutableTileChange> {
         private Populator() {
            super(is);
         }

         @NotNull
         public MutableTileChange create() {
            return new MutableTileChange(null, create);
         }

         protected void write(final MutableTileChange change, final CompoundTag tag) {
            change.tag = tag;
            change.create = create;
         }

         @Override
         public boolean accepts(final Change change) {
            return change instanceof MutableTileChange;
         }
      }

      return (ChangePopulator<MutableTileChange>)(is == null ? ChangePopulator.empty() : new Populator());
   }

   private ChangePopulator<MutableEntityChange> entityChangePopulator(NBTInputStream is, boolean create) {
      class Populator extends FaweStreamChangeSet.CompoundTagPopulator<MutableEntityChange> {
         private Populator() {
            super(is);
         }

         @NotNull
         public MutableEntityChange create() {
            return new MutableEntityChange(null, create);
         }

         protected void write(final MutableEntityChange change, final CompoundTag tag) {
            change.tag = tag;
            change.create = create;
         }

         @Override
         public boolean accepts(final Change change) {
            return change instanceof MutableEntityChange;
         }
      }

      return (ChangePopulator<MutableEntityChange>)(is == null ? ChangePopulator.empty() : new Populator());
   }

   private ChangePopulator<MutableFullBlockChange> fullBlockChangePopulator(BlockBag blockBag, int mode, boolean dir) throws IOException {
      final FaweInputStream is = this.getBlockIS();

      class Populator implements ChangePopulator<MutableFullBlockChange> {
         @NotNull
         public MutableFullBlockChange create() {
            return new MutableFullBlockChange(blockBag, mode, dir);
         }

         @Nullable
         public MutableFullBlockChange populate(@NotNull final MutableFullBlockChange change) {
            try {
               change.x = FaweStreamChangeSet.this.posDel.readX(is) + FaweStreamChangeSet.this.originX;
               change.y = FaweStreamChangeSet.this.posDel.readY(is);
               change.z = FaweStreamChangeSet.this.posDel.readZ(is) + FaweStreamChangeSet.this.originZ;
               FaweStreamChangeSet.this.idDel.readCombined(is, change);
               return change;
            } catch (EOFException var4) {
            } catch (Exception var5) {
               var5.printStackTrace();
            }

            try {
               is.close();
            } catch (IOException var3) {
               var3.printStackTrace();
            }

            return null;
         }

         @Override
         public boolean accepts(final Change change) {
            return change instanceof MutableFullBlockChange;
         }
      }

      return (ChangePopulator<MutableFullBlockChange>)(is == null ? ChangePopulator.empty() : new Populator());
   }

   private ChangePopulator<MutableBlockChange> blockChangePopulator(boolean dir) throws IOException {
      final FaweInputStream is = this.getBlockIS();

      class Populator implements ChangePopulator<MutableBlockChange> {
         @NotNull
         public MutableBlockChange create() {
            return new MutableBlockChange(0, 0, 0, BlockTypes.AIR.getInternalId());
         }

         @Nullable
         public MutableBlockChange populate(@NotNull final MutableBlockChange change) {
            try {
               change.x = FaweStreamChangeSet.this.posDel.readX(is) + FaweStreamChangeSet.this.originX;
               change.y = FaweStreamChangeSet.this.posDel.readY(is);
               change.z = FaweStreamChangeSet.this.posDel.readZ(is) + FaweStreamChangeSet.this.originZ;
               FaweStreamChangeSet.this.idDel.readCombined(is, change, dir);
               return change;
            } catch (EOFException var4) {
            } catch (Exception var5) {
               var5.printStackTrace();
            }

            try {
               is.close();
            } catch (IOException var3) {
               var3.printStackTrace();
            }

            return null;
         }

         @Override
         public boolean accepts(final Change change) {
            return change instanceof MutableBlockChange;
         }
      }

      return (ChangePopulator<MutableBlockChange>)(is == null ? ChangePopulator.empty() : new Populator());
   }

   private ChangePopulator<MutableBiomeChange> biomeChangePopulator(boolean dir) throws IOException {
      final FaweInputStream is = this.getBiomeIS();

      class Populator implements ChangePopulator<MutableBiomeChange> {
         @NotNull
         public MutableBiomeChange create() {
            return new MutableBiomeChange();
         }

         @Nullable
         public MutableBiomeChange populate(@NotNull final MutableBiomeChange change) {
            try {
               int int1 = is.read();
               if (int1 != -1) {
                  int x = (int1 << 24) + (is.read() << 16) + (is.read() << 8) + is.read() << 2;
                  int z = (is.read() << 24) + (is.read() << 16) + (is.read() << 8) + is.read() << 2;
                  int y = is.read() - 128 << 2;
                  int from = is.readVarInt();
                  int to = is.readVarInt();
                  change.setBiome(x, y, z, from, to);
                  return change;
               }
            } catch (EOFException var9) {
            } catch (Exception var10) {
               var10.printStackTrace();
            }

            try {
               is.close();
            } catch (IOException var8) {
               var8.printStackTrace();
            }

            return null;
         }

         @Override
         public boolean accepts(final Change change) {
            return change instanceof MutableBiomeChange;
         }
      }

      return (ChangePopulator<MutableBiomeChange>)(is == null ? ChangePopulator.empty() : new Populator());
   }

   @Override
   public Iterator<Change> getIterator(final boolean dir) {
      try {
         this.close();
         final Iterator<MutableTileChange> tileCreate = this.getTileIterator(this.getTileCreateIS(), true);
         final Iterator<MutableTileChange> tileRemove = this.getTileIterator(this.getTileRemoveIS(), false);
         final Iterator<MutableEntityChange> entityCreate = this.getEntityIterator(this.getEntityCreateIS(), true);
         final Iterator<MutableEntityChange> entityRemove = this.getEntityIterator(this.getEntityRemoveIS(), false);
         final Iterator<MutableBlockChange> blockChange = this.getBlockIterator(dir);
         final Iterator<MutableBiomeChange> biomeChange = this.getBiomeIterator(dir);
         return new Iterator<Change>() {
            final Iterator<Change>[] iterators = new Iterator[]{tileCreate, tileRemove, entityCreate, entityRemove, blockChange, biomeChange};
            int i = 0;
            Iterator<Change> current = FaweStreamChangeSet.super.iterators[0];

            @Override
            public boolean hasNext() {
               if (this.current.hasNext()) {
                  return true;
               } else if (this.i >= this.iterators.length - 1) {
                  return false;
               } else {
                  this.current = this.iterators[++this.i];
                  return this.hasNext();
               }
            }

            @Override
            public void remove() {
               this.current.remove();
            }

            public Change next() {
               try {
                  return this.current.next();
               } catch (Throwable var2) {
                  if (this.i >= this.iterators.length - 1) {
                     throw new NoSuchElementException("End of iterator");
                  } else {
                     this.current = this.iterators[++this.i];
                     return this.next();
                  }
               }
            }
         };
      } catch (Exception var8) {
         var8.printStackTrace();
         return Collections.emptyIterator();
      }
   }

   @Override
   public Iterator<Change> backwardIterator() {
      return this.getIterator(false);
   }

   @Override
   public Iterator<Change> forwardIterator() {
      return this.getIterator(true);
   }

   protected SimpleChangeSetSummary summarizeShallow() {
      return new SimpleChangeSetSummary(this.getOriginX(), this.getOriginZ());
   }

   public SimpleChangeSetSummary summarize(Region region, boolean shallow) {
      int ox = this.getOriginX();
      int oz = this.getOriginZ();
      SimpleChangeSetSummary summary = this.summarizeShallow();
      if (region != null && !region.contains(ox, oz)) {
         return summary;
      } else {
         try {
            SimpleChangeSetSummary var17;
            try (FaweInputStream fis = this.getBlockIS()) {
               if (fis != null) {
                  if (!shallow) {
                     int amount = (Settings.settings().HISTORY.BUFFER_SIZE - 9) / 9;
                     MutableFullBlockChange change = new MutableFullBlockChange(null, 0, false);

                     for (int i = 0; i < amount; i++) {
                        int x = this.posDel.readX(fis) + ox;
                        int y = this.posDel.readY(fis);
                        int z = this.posDel.readZ(fis) + oz;
                        this.idDel.readCombined(fis, change);
                        summary.add(x, z, change.to);
                     }
                  }

                  return summary;
               }

               var17 = summary;
            }

            return var17;
         } catch (EOFException var15) {
         } catch (IOException var16) {
            var16.printStackTrace();
         }

         return summary;
      }
   }

   private abstract static class CompoundTagPopulator<C extends Change> implements ChangePopulator<C> {
      private final NBTInputStream inputStream;

      private CompoundTagPopulator(final NBTInputStream stream) {
         this.inputStream = stream;
      }

      @Nullable
      @Override
      public C populate(@NotNull final C change) {
         try {
            this.write(change, (CompoundTag)this.inputStream.readTag());
            return change;
         } catch (Exception var4) {
            try {
               this.inputStream.close();
            } catch (IOException var3) {
               var3.printStackTrace();
            }

            return null;
         }
      }

      protected abstract void write(C change, CompoundTag tag);
   }

   public interface FaweStreamIdDelegate {
      void writeChange(FaweOutputStream out, int from, int to) throws IOException;

      void readCombined(FaweInputStream in, MutableBlockChange change, boolean dir) throws IOException;

      void readCombined(FaweInputStream in, MutableFullBlockChange change) throws IOException;
   }

   public interface FaweStreamPositionDelegate {
      void write(OutputStream out, int x, int y, int z) throws IOException;

      int readX(FaweInputStream in) throws IOException;

      int readY(FaweInputStream in) throws IOException;

      int readZ(FaweInputStream in) throws IOException;
   }
}
