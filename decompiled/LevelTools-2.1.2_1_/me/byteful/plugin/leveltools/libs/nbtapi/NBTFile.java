package me.byteful.plugin.leveltools.libs.nbtapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import me.byteful.plugin.leveltools.libs.nbtapi.iface.NBTFileHandle;
import me.byteful.plugin.leveltools.libs.nbtapi.utils.nmsmappings.ObjectCreator;

public class NBTFile extends NBTCompound implements NBTFileHandle {
   private final File file;
   private Object nbt;

   @Deprecated
   public NBTFile(File file) throws IOException {
      super(null, null);
      if (file == null) {
         throw new NullPointerException("File can't be null!");
      } else {
         this.file = file;
         if (file.exists()) {
            this.nbt = NBTReflectionUtil.readNBT(Files.newInputStream(file.toPath()));
         } else {
            this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
            this.save();
         }
      }
   }

   @Override
   public void save() throws IOException {
      try {
         this.getWriteLock().lock();
         saveTo(this.file, this);
      } finally {
         this.getWriteLock().unlock();
      }
   }

   @Override
   public File getFile() {
      return this.file;
   }

   @Override
   public Object getCompound() {
      return this.nbt;
   }

   @Override
   protected void setCompound(Object compound) {
      this.nbt = compound;
   }

   @Deprecated
   public static NBTCompound readFrom(File file) throws IOException {
      return !file.exists() ? new NBTContainer() : new NBTContainer(NBTReflectionUtil.readNBT(Files.newInputStream(file.toPath())));
   }

   @Deprecated
   public static void saveTo(File file, NBTCompound nbt) throws IOException {
      if (!file.exists()) {
         file.getParentFile().mkdirs();
         if (!file.createNewFile()) {
            throw new IOException("Unable to create file at " + file.getAbsolutePath());
         }
      }

      nbt.writeCompound(Files.newOutputStream(file.toPath()));
   }
}
