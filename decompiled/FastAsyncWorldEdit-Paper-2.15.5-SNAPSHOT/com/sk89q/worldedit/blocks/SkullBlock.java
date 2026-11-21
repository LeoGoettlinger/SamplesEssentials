package com.sk89q.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.HashMap;
import java.util.Map;

@Deprecated(
   forRemoval = true
)
public class SkullBlock extends LegacyBaseBlockWrapper {
   private String owner = "";

   public SkullBlock(BlockState state) {
      super(state);
      this.owner = "";
   }

   public SkullBlock(BlockState blockState, String owner) {
      super(blockState);
      this.setOwner(owner);
   }

   public void setOwner(String owner) {
      if (owner == null) {
         this.owner = "";
      } else if (owner.length() <= 16 && !owner.isEmpty()) {
         this.owner = owner;
      } else {
         this.owner = "";
      }
   }

   public String getOwner() {
      return this.owner;
   }

   @Deprecated
   @Override
   public boolean hasNbtData() {
      return true;
   }

   @Override
   public String getNbtId() {
      return "skull";
   }

   @Deprecated
   @Override
   public CompoundTag getNbtData() {
      Map<String, Tag<?, ?>> values = new HashMap<>();
      Map<String, Tag<?, ?>> inner = new HashMap<>();
      inner.put("Name", new StringTag(this.owner));
      values.put(DeprecationUtil.getHeadOwnerKey(), new CompoundTag(inner));
      return new CompoundTag(values);
   }

   @Deprecated
   @Override
   public void setNbtData(CompoundTag rootTag) {
      if (rootTag != null) {
         Map<String, Tag<?, ?>> values = rootTag.getValue();
         Tag t = values.get("id");
         if (t instanceof StringTag && ((StringTag)t).getValue().equals(this.getNbtId())) {
            t = values.get(DeprecationUtil.getHeadOwnerKey());
            if (t instanceof CompoundTag) {
               this.setOwner(((CompoundTag)t).getValue().get("Name").getValue().toString());
            }
         } else {
            throw new RuntimeException(String.format("'%s' tile entity expected", this.getNbtId()));
         }
      }
   }
}
