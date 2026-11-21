package net.thenextlvl.protect.adapters.region;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CuboidRegionAdapter implements TagAdapter<CuboidRegion> {
   public CuboidRegion deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      BlockVector3 pos1 = context.deserialize(compound.get("pos1"), BlockVector3.class);
      BlockVector3 pos2 = context.deserialize(compound.get("pos2"), BlockVector3.class);
      return new CuboidRegion(pos1, pos2);
   }

   public Tag serialize(CuboidRegion region, TagSerializationContext context) {
      return CompoundTag.builder().put("pos1", context.serialize(region.getPos1())).put("pos2", context.serialize(region.getPos2())).build();
   }
}
