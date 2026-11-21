package de.tr7zw.nbtapi;

import de.tr7zw.nbtapi.utils.nmsmappings.ClassWrapper;
import de.tr7zw.nbtapi.utils.nmsmappings.ObjectCreator;
import de.tr7zw.nbtapi.utils.nmsmappings.ReflectionMethod;
import java.io.InputStream;

public class NBTContainer extends NBTCompound {
   private Object nbt;
   private boolean closed;
   private boolean readOnly;

   @Deprecated
   public NBTContainer() {
      super(null, null);
      this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
   }

   @Deprecated
   public NBTContainer(Object nbt) {
      super(null, null);
      if (nbt == null) {
         nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
      }

      if (!ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isAssignableFrom(nbt.getClass())) {
         throw new NbtApiException("The object '" + nbt.getClass() + "' is not a valid NBT-Object!");
      } else {
         this.nbt = nbt;
      }
   }

   @Deprecated
   public NBTContainer(InputStream inputsteam) {
      super(null, null);
      this.nbt = NBTReflectionUtil.readNBT(inputsteam);
   }

   @Deprecated
   public NBTContainer(String nbtString) {
      super(null, null);
      if (nbtString == null) {
         throw new NullPointerException("The String can't be null!");
      } else {
         try {
            this.nbt = ReflectionMethod.PARSE_NBT.run(null, nbtString);
         } catch (Exception var3) {
            throw new NbtApiException("Unable to parse Malformed Json!", var3);
         }
      }
   }

   @Override
   public Object getCompound() {
      return this.nbt;
   }

   @Override
   public void setCompound(Object tag) {
      this.nbt = tag;
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
      return this.readOnly;
   }

   protected NBTContainer setReadOnly(boolean readOnly) {
      this.readOnly = true;
      return this;
   }
}
