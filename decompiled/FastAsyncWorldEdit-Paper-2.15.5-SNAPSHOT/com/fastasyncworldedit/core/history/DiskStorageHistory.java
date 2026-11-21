package com.fastasyncworldedit.core.history;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.database.DBHandler;
import com.fastasyncworldedit.core.database.RollbackDatabase;
import com.fastasyncworldedit.core.history.changeset.FaweStreamChangeSet;
import com.fastasyncworldedit.core.history.changeset.SimpleChangeSetSummary;
import com.fastasyncworldedit.core.internal.io.FaweInputStream;
import com.fastasyncworldedit.core.internal.io.FaweOutputStream;
import com.fastasyncworldedit.core.math.IntPair;
import com.fastasyncworldedit.core.util.MainUtil;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;

public class DiskStorageHistory extends FaweStreamChangeSet {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final Map<String, Map<UUID, Integer>> NEXT_INDEX = new ConcurrentHashMap<>();
   private UUID uuid;
   private File bdFile;
   private File bioFile;
   private File nbtfFile;
   private File nbttFile;
   private File entfFile;
   private File enttFile;
   private FaweOutputStream osBD;
   private FaweOutputStream osBIO;
   private NBTOutputStream osNBTF;
   private NBTOutputStream osNBTT;
   private NBTOutputStream osENTCF;
   private NBTOutputStream osENTCT;
   private int index;

   public DiskStorageHistory(World world, UUID uuid) {
      super(world);
      this.init(uuid, world.getName());
   }

   private void init(UUID uuid, String worldName) {
      File folder = MainUtil.getFile(
         Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HISTORY + File.separator + worldName + File.separator + uuid
      );
      int max = NEXT_INDEX.computeIfAbsent(worldName, _worldName -> new ConcurrentHashMap<>())
            .compute(uuid, (_uuid, id) -> (id == null ? MainUtil.getMaxFileId(folder) : id) + 1)
         - 1;
      this.init(uuid, max);
   }

   public DiskStorageHistory(World world, UUID uuid, int index) {
      super(world);
      this.init(uuid, index);
   }

   public DiskStorageHistory(File folder, World world, UUID uuid, int i) {
      super(world);
      this.uuid = uuid;
      this.index = i;
      this.initFiles(folder);
   }

   private void initFiles(File folder) {
      this.nbtfFile = new File(folder, this.index + ".nbtf");
      this.nbttFile = new File(folder, this.index + ".nbtt");
      this.entfFile = new File(folder, this.index + ".entf");
      this.enttFile = new File(folder, this.index + ".entt");
      this.bdFile = new File(folder, this.index + ".bd");
      this.bioFile = new File(folder, this.index + ".bio");
   }

   private void init(UUID uuid, int i) {
      this.uuid = uuid;
      this.index = i;
      File folder = MainUtil.getFile(
         Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HISTORY + File.separator + this.getWorld().getName() + File.separator + uuid
      );
      this.initFiles(folder);
   }

   @Override
   public void delete() {
      this.deleteFiles();
      if (Settings.settings().HISTORY.USE_DATABASE) {
         RollbackDatabase db = DBHandler.dbHandler().getDatabase(this.getWorld());
         db.delete(this.uuid, this.index);
      }
   }

   public void deleteFiles() {
      this.bdFile.delete();
      this.nbtfFile.delete();
      this.nbttFile.delete();
      this.entfFile.delete();
      this.enttFile.delete();
      this.bioFile.delete();
   }

   public void undo(Actor actor, Region[] regions) {
      try {
         this.close();
      } catch (IOException var7) {
         var7.printStackTrace();
         return;
      }

      try (EditSession session = this.toEditSession(actor, regions)) {
         session.setBlocks(this, ChangeSetExecutor.Type.UNDO);
      }
   }

   public void undo(Actor actor) {
      this.undo(actor, null);
   }

   public void redo(Actor actor, Region[] regions) {
      try {
         this.close();
      } catch (IOException var4) {
         var4.printStackTrace();
         return;
      }

      EditSession session = this.toEditSession(actor, regions);
      session.setBlocks(this, ChangeSetExecutor.Type.REDO);
   }

   public void redo(Actor actor) {
      this.redo(actor, null);
   }

   public UUID getUUID() {
      return this.uuid;
   }

   public File getBDFile() {
      return this.bdFile;
   }

   public File getNbtfFile() {
      return this.nbtfFile;
   }

   public File getNbttFile() {
      return this.nbttFile;
   }

   public File getEntfFile() {
      return this.entfFile;
   }

   public File getEnttFile() {
      return this.enttFile;
   }

   public File getBioFile() {
      return this.bioFile;
   }

   public int getIndex() {
      return this.index;
   }

   @Override
   public void flush() {
      super.flush();
      synchronized (this) {
         try {
            if (this.osBD != null) {
               this.osBD.flush();
            }

            if (this.osBIO != null) {
               this.osBIO.flush();
            }

            if (this.osNBTF != null) {
               this.osNBTF.flush();
            }

            if (this.osNBTT != null) {
               this.osNBTT.flush();
            }

            if (this.osENTCF != null) {
               this.osENTCF.flush();
            }

            if (this.osENTCT != null) {
               this.osENTCT.flush();
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   @Override
   public void close() throws IOException {
      super.close();
      synchronized (this) {
         try {
            if (this.osBD != null) {
               this.osBD.close();
               this.osBD = null;
            }

            if (this.osBIO != null) {
               this.osBIO.close();
               this.osBIO = null;
            }

            if (this.osNBTF != null) {
               this.osNBTF.close();
               this.osNBTF = null;
            }

            if (this.osNBTT != null) {
               this.osNBTT.close();
               this.osNBTT = null;
            }

            if (this.osENTCF != null) {
               this.osENTCF.close();
               this.osENTCF = null;
            }

            if (this.osENTCT != null) {
               this.osENTCT.close();
               this.osENTCT = null;
            }
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }
   }

   @Override
   public int getCompressedSize() {
      return this.bdFile.exists() ? (int)this.bdFile.length() : 0;
   }

   @Override
   public long getSizeInMemory() {
      return 80L;
   }

   @Override
   public long getSizeOnDisk() {
      int total = 0;
      if (this.bdFile.exists()) {
         total = (int)(total + this.bdFile.length());
      }

      if (this.bioFile.exists()) {
         total = (int)(total + this.bioFile.length());
      }

      if (this.nbtfFile.exists()) {
         total = (int)(total + this.nbtfFile.length());
      }

      if (this.nbttFile.exists()) {
         total = (int)(total + this.nbttFile.length());
      }

      if (this.entfFile.exists()) {
         total = (int)(total + this.entfFile.length());
      }

      if (this.enttFile.exists()) {
         total = (int)(total + this.enttFile.length());
      }

      return total;
   }

   @Override
   public FaweOutputStream getBlockOS(int x, int y, int z) throws IOException {
      if (this.osBD != null) {
         return this.osBD;
      } else {
         synchronized (this) {
            this.bdFile.getParentFile().mkdirs();
            this.bdFile.createNewFile();
            this.osBD = this.getCompressedOS(new FileOutputStream(this.bdFile));
            this.writeHeader(this.osBD, x, y, z);
            return this.osBD;
         }
      }
   }

   @Override
   public FaweOutputStream getBiomeOS() throws IOException {
      if (this.osBIO != null) {
         return this.osBIO;
      } else {
         synchronized (this) {
            this.bioFile.getParentFile().mkdirs();
            this.bioFile.createNewFile();
            this.osBIO = this.getCompressedOS(new FileOutputStream(this.bioFile));
            return this.osBIO;
         }
      }
   }

   @Override
   public NBTOutputStream getEntityCreateOS() throws IOException {
      if (this.osENTCT != null) {
         return this.osENTCT;
      } else {
         this.enttFile.getParentFile().mkdirs();
         this.enttFile.createNewFile();
         this.osENTCT = new NBTOutputStream((DataOutputStream)this.getCompressedOS(new FileOutputStream(this.enttFile)));
         return this.osENTCT;
      }
   }

   @Override
   public NBTOutputStream getEntityRemoveOS() throws IOException {
      if (this.osENTCF != null) {
         return this.osENTCF;
      } else {
         this.entfFile.getParentFile().mkdirs();
         this.entfFile.createNewFile();
         this.osENTCF = new NBTOutputStream((DataOutputStream)this.getCompressedOS(new FileOutputStream(this.entfFile)));
         return this.osENTCF;
      }
   }

   @Override
   public NBTOutputStream getTileCreateOS() throws IOException {
      if (this.osNBTT != null) {
         return this.osNBTT;
      } else {
         this.nbttFile.getParentFile().mkdirs();
         this.nbttFile.createNewFile();
         this.osNBTT = new NBTOutputStream((DataOutputStream)this.getCompressedOS(new FileOutputStream(this.nbttFile)));
         return this.osNBTT;
      }
   }

   @Override
   public NBTOutputStream getTileRemoveOS() throws IOException {
      if (this.osNBTF != null) {
         return this.osNBTF;
      } else {
         this.nbtfFile.getParentFile().mkdirs();
         this.nbtfFile.createNewFile();
         this.osNBTF = new NBTOutputStream((DataOutputStream)this.getCompressedOS(new FileOutputStream(this.nbtfFile)));
         return this.osNBTF;
      }
   }

   @Override
   public FaweInputStream getBlockIS() throws IOException {
      if (!this.bdFile.exists()) {
         return null;
      } else {
         try {
            FaweInputStream is = MainUtil.getCompressedIS(new FileInputStream(this.bdFile));
            this.readHeader(is);
            return is;
         } catch (IOException var2) {
            LOGGER.error("Could not load block history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public FaweInputStream getBiomeIS() throws IOException {
      if (!this.bioFile.exists()) {
         return null;
      } else {
         try {
            return MainUtil.getCompressedIS(new FileInputStream(this.bioFile));
         } catch (IOException var2) {
            LOGGER.error("Could not load biome history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public NBTInputStream getEntityCreateIS() throws IOException {
      if (!this.enttFile.exists()) {
         return null;
      } else {
         try {
            return new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FileInputStream(this.enttFile)));
         } catch (IOException var2) {
            LOGGER.error("Could not load entity create history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public NBTInputStream getEntityRemoveIS() throws IOException {
      if (!this.entfFile.exists()) {
         return null;
      } else {
         try {
            return new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FileInputStream(this.entfFile)));
         } catch (IOException var2) {
            LOGGER.error("Could not load entity remove history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public NBTInputStream getTileCreateIS() throws IOException {
      if (!this.nbttFile.exists()) {
         return null;
      } else {
         try {
            return new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FileInputStream(this.nbttFile)));
         } catch (IOException var2) {
            LOGGER.error("Could not load tile create history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public NBTInputStream getTileRemoveIS() throws IOException {
      if (!this.nbtfFile.exists()) {
         return null;
      } else {
         try {
            return new NBTInputStream((DataInputStream)MainUtil.getCompressedIS(new FileInputStream(this.nbtfFile)));
         } catch (IOException var2) {
            LOGGER.error("Could not load tile remove history file {}", this.bdFile);
            throw var2;
         }
      }
   }

   @Override
   public SimpleChangeSetSummary summarize(Region region, boolean shallow) {
      return this.bdFile.exists() ? super.summarize(region, shallow) : null;
   }

   public IntPair readHeader() {
      int ox = this.getOriginX();
      int oz = this.getOriginZ();
      if (ox == 0 && oz == 0 && this.bdFile.exists()) {
         try (
            FileInputStream fis = new FileInputStream(this.bdFile);
            FaweInputStream gis = MainUtil.getCompressedIS(fis);
         ) {
            gis.skipFully(1);
            gis.skipFully(1);
            ox = (gis.read() << 24) + (gis.read() << 16) + (gis.read() << 8) + gis.read();
            oz = (gis.read() << 24) + (gis.read() << 16) + (gis.read() << 8) + gis.read();
            this.setOrigin(ox, oz);
         } catch (IOException var11) {
            var11.printStackTrace();
         }
      }

      return new IntPair(ox, oz);
   }

   @Override
   public boolean isRecordingChanges() {
      return false;
   }

   @Override
   public void setRecordChanges(boolean recordChanges) {
   }
}
