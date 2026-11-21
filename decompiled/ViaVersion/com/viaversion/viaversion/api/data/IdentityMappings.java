package com.viaversion.viaversion.api.data;

public record IdentityMappings(int size, int mappedSize) implements Mappings {
   @Override
   public int getNewId(int id) {
      return id >= 0 && id < this.size ? id : -1;
   }

   @Override
   public void setNewId(int id, int mappedId) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Mappings inverse() {
      return new IdentityMappings(this.mappedSize, this.size);
   }

   @Override
   public boolean isIdentity() {
      return true;
   }
}
