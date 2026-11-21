package net.thenextlvl.protect.adapters.region;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.CylinderRegion;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CylinderRegionAdapter implements TagAdapter<CylinderRegion> {
   public CylinderRegion deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      BlockVector3 center = context.deserialize(compound.get("center"), BlockVector3.class);
      Vector2 radius = context.deserialize(compound.get("radius"), Vector2.class);
      int max = compound.<Tag>get("max").getAsInt();
      int min = compound.<Tag>get("min").getAsInt();
      return new CylinderRegion(center, radius, min, max);
   }

   public Tag serialize(CylinderRegion vector, TagSerializationContext context) {
      return CompoundTag.builder()
         .put("center", context.serialize(vector.getCenter().toBlockPoint()))
         .put("radius", context.serialize(vector.getRadius()))
         .put("max", vector.getMaximumY())
         .put("min", vector.getMinimumY())
         .build();
   }
}
