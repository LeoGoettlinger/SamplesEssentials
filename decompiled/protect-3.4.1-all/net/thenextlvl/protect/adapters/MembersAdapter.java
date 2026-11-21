package net.thenextlvl.protect.adapters;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.ListTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class MembersAdapter implements TagAdapter<Set<UUID>> {
   public Set<UUID> deserialize(Tag tag, TagDeserializationContext context) {
      return tag.getAsList().stream().map(compound -> context.deserialize(compound, UUID.class)).collect(Collectors.toSet());
   }

   public Tag serialize(Set<UUID> object, TagSerializationContext context) {
      return ListTag.of((byte)10, object.stream().map(context::serialize).toList());
   }
}
