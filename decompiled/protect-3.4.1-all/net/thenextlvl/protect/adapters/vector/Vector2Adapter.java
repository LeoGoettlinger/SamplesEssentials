package net.thenextlvl.protect.adapters.vector;

import com.sk89q.worldedit.math.Vector2;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Vector2Adapter implements TagAdapter<Vector2> {
   public Vector2 deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      int x = compound.<Tag>get("x").getAsInt();
      int z = compound.<Tag>get("z").getAsInt();
      return Vector2.at(x, z);
   }

   public Tag serialize(Vector2 vector, TagSerializationContext context) {
      return CompoundTag.builder().put("x", vector.x()).put("z", vector.z()).build();
   }
}
