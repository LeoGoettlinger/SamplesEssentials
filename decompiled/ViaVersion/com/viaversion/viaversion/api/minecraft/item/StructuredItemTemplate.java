package com.viaversion.viaversion.api.minecraft.item;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.minecraft.data.StructuredDataContainer;

public class StructuredItemTemplate extends StructuredItem {
   public StructuredItemTemplate(int identifier, int amount) {
      super(identifier, amount, new StructuredDataContainer());
   }

   public StructuredItemTemplate(int identifier, int amount, StructuredDataContainer data) {
      super(identifier, amount, data);
   }

   @Override
   public void setIdentifier(int identifier) {
      Preconditions.checkArgument(identifier > 0, "Template item cannot be empty");
      super.setIdentifier(identifier);
   }

   @Override
   public void setAmount(int amount) {
      Preconditions.checkArgument(amount > 0, "Template item cannot be empty");
      super.setAmount(amount);
   }

   @Override
   public boolean isTemplate() {
      return true;
   }

   public StructuredItemTemplate copy() {
      return new StructuredItemTemplate(this.identifier(), this.amount(), this.dataContainer().copy());
   }
}
