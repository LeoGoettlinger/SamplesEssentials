package net.thenextlvl.protect.adapters;

import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LocationAdapter implements TagAdapter<Location> {
   public Location deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      CompoundTag compound = tag.getAsCompound();
      World world = context.deserialize(compound.get("world"), World.class);
      double x = compound.<Tag>get("x").getAsDouble();
      double y = compound.<Tag>get("y").getAsDouble();
      double z = compound.<Tag>get("z").getAsDouble();
      float yaw = compound.<Tag>get("yaw").getAsFloat();
      float pitch = compound.<Tag>get("pitch").getAsFloat();
      return new Location(world, x, y, z, yaw, pitch);
   }

   public Tag serialize(Location location, TagSerializationContext context) throws ParserException {
      return CompoundTag.builder()
         .put("world", context.serialize(location.getWorld()))
         .put("x", location.getX())
         .put("y", location.getY())
         .put("z", location.getZ())
         .put("yaw", location.getYaw())
         .put("pitch", location.getPitch())
         .build();
   }
}
