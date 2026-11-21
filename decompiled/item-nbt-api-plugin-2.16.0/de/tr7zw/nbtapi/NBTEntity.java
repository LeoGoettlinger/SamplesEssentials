package de.tr7zw.nbtapi;

import de.tr7zw.nbtapi.utils.CheckUtil;
import de.tr7zw.nbtapi.utils.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class NBTEntity extends NBTCompound {
   private final Entity ent;
   private final boolean readonly;
   private final Object compound;
   private boolean closed = false;

   protected NBTEntity(Entity entity, boolean readonly) {
      super(null, null);
      if (entity == null) {
         throw new NullPointerException("Entity can't be null!");
      } else {
         this.readonly = readonly;
         this.ent = entity;
         if (readonly) {
            this.compound = this.getCompound();
         } else {
            this.compound = null;
         }
      }
   }

   @Deprecated
   public NBTEntity(Entity entity) {
      super(null, null);
      if (entity == null) {
         throw new NullPointerException("Entity can't be null!");
      } else {
         this.readonly = false;
         this.compound = null;
         this.ent = entity;
      }
   }

   @Override
   protected void setClosed() {
      this.closed = true;
   }

   @Override
   protected boolean isClosed() {
      return this.closed;
   }

   @Override
   protected boolean isReadOnly() {
      return this.readonly;
   }

   @Override
   public Object getCompound() {
      if (this.readonly && this.compound != null) {
         return this.compound;
      } else if (!Bukkit.isPrimaryThread()) {
         throw new NbtApiException("Entity NBT needs to be accessed sync!");
      } else {
         return NBTReflectionUtil.getEntityNBTTagCompound(NBTReflectionUtil.getNMSEntity(this.ent));
      }
   }

   @Override
   protected void setCompound(Object compound) {
      if (this.readonly) {
         throw new NbtApiException("Tried setting data in read only mode!");
      } else if (!Bukkit.isPrimaryThread()) {
         throw new NbtApiException("Entity NBT needs to be accessed sync!");
      } else {
         NBTReflectionUtil.setEntityNBTTag(compound, NBTReflectionUtil.getNMSEntity(this.ent));
      }
   }

   public NBTCompound getPersistentDataContainer() {
      CheckUtil.assertAvailable(MinecraftVersion.MC1_14_R1);
      return new NBTPersistentDataContainer(this.ent.getPersistentDataContainer());
   }
}
