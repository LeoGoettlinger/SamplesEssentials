package net.thenextlvl.nbt.serialization.adapters;

import java.nio.file.Path;
import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.StringTag;
import net.thenextlvl.nbt.tag.Tag;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class PathAdapter implements TagAdapter<Path> {
   public static final PathAdapter INSTANCE = new PathAdapter();

   private PathAdapter() {
   }

   public Path deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      return Path.of(tag.getAsString());
   }

   public Tag serialize(Path path, TagSerializationContext context) throws ParserException {
      return StringTag.of(path.toString());
   }
}
