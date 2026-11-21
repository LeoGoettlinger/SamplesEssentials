package net.thenextlvl.protect.adapters.vector;

import com.sk89q.worldedit.math.Vector3;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Vector3Adapter implements TagAdapter<Vector3> {
   public Vector3 deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      double x = compound.<Tag>get("x").getAsDouble();
      double y = compound.<Tag>get("y").getAsDouble();
      double z = compound.<Tag>get("z").getAsDouble();
      return Vector3.at(x, y, z);
   }

   public Tag serialize(Vector3 vector, TagSerializationContext context) {
      return CompoundTag.builder().put("x", vector.x()).put("y", vector.y()).put("z", vector.z()).build();
   }
}
