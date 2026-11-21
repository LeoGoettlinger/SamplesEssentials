package me.byteful.plugin.leveltools.libs.nbtapi;

public class NBTListCompound extends NBTCompound {
   private NBTList<?> owner;
   private Object compound;

   protected NBTListCompound(NBTList<?> parent, Object obj) {
      super(null, null);
      this.owner = parent;
      this.compound = obj;
   }

   public NBTList<?> getListParent() {
      return this.owner;
   }

   @Override
   protected boolean isClosed() {
      return this.owner.getParent().isClosed();
   }

   @Override
   protected boolean isReadOnly() {
      return this.owner.getParent().isReadOnly();
   }

   @Override
   public Object getCompound() {
      if (this.isClosed()) {
         throw new NbtApiException("Tried using closed NBT data!");
      } else {
         return this.compound;
      }
   }

   @Override
   protected void setCompound(Object compound) {
      if (this.isClosed()) {
         throw new NbtApiException("Tried using closed NBT data!");
      } else if (this.isReadOnly()) {
         throw new NbtApiException("Tried setting data in read only mode!");
      } else {
         this.compound = compound;
      }
   }

   @Override
   protected void saveCompound() {
      this.owner.save();
   }
}
