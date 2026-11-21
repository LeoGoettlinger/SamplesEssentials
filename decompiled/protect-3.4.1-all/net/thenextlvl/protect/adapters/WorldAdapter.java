package net.thenextlvl.protect.adapters;

import net.kyori.adventure.key.Key;
import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.Tag;
import org.bukkit.Server;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class WorldAdapter implements TagAdapter<World> {
   private final Server server;

   public WorldAdapter(Server server) {
      this.server = server;
   }

   public World deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      Key namespace = context.deserialize(tag, Key.class);
      World world = this.server.getWorld(namespace);
      if (world != null) {
         return world;
      } else {
         throw new ParserException("World not found: " + namespace);
      }
   }

   public Tag serialize(World object, TagSerializationContext context) throws ParserException {
      return context.serialize(object.getKey());
   }
}
