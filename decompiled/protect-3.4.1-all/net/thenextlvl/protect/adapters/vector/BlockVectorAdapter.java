package net.thenextlvl.protect.adapters.vector;

import com.sk89q.worldedit.math.BlockVector3;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class BlockVectorAdapter implements TagAdapter<BlockVector3> {
   public BlockVector3 deserialize(Tag tag, TagDeserializationContext context) {
      CompoundTag compound = tag.getAsCompound();
      int x = compound.<Tag>get("x").getAsInt();
      int y = compound.<Tag>get("y").getAsInt();
      int z = compound.<Tag>get("z").getAsInt();
      return BlockVector3.at(x, y, z);
   }

   public Tag serialize(BlockVector3 vector, TagSerializationContext context) {
      return CompoundTag.builder().put("x", vector.x()).put("y", vector.y()).put("z", vector.z()).build();
   }
}
