package net.thenextlvl.protect.adapters.region;

import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Region;
import java.util.HashMap;
import net.thenextlvl.nbt.serialization.ParserException;
import net.thenextlvl.nbt.serialization.TagAdapter;
import net.thenextlvl.nbt.serialization.TagDeserializationContext;
import net.thenextlvl.nbt.serialization.TagSerializationContext;
import net.thenextlvl.nbt.tag.CompoundTag;
import net.thenextlvl.nbt.tag.Tag;
import net.thenextlvl.protect.exception.UnsupportedRegionException;
import net.thenextlvl.protect.region.GroupedRegion;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GroupedRegionAdapter implements TagAdapter<GroupedRegion> {
   public GroupedRegion deserialize(Tag tag, TagDeserializationContext context) throws ParserException {
      CompoundTag compound = tag.getAsCompound();
      Class<? extends Region> type = this.type(compound.<Tag>get("type").getAsString());
      CompoundTag regions = compound.getAsCompound("regions");
      HashMap<String, Region> map = new HashMap<>();
      regions.forEach((name, value) -> map.put(name, context.deserialize(value, type)));
      return new GroupedRegion(map);
   }

   public Tag serialize(GroupedRegion group, TagSerializationContext context) throws ParserException {
      CompoundTag.Builder tag = CompoundTag.builder();
      tag.put("type", this.type(group.getRegions().values().iterator().next().getClass()));
      CompoundTag.Builder regions = CompoundTag.builder();
      group.getRegions().forEach((name, region) -> regions.put(name, context.serialize(region)));
      tag.put("regions", regions.build());
      return tag.build();
   }

   public String type(Class<? extends Region> type) {
      if (type == CuboidRegion.class) {
         return "cuboid";
      } else if (type == CylinderRegion.class) {
         return "cylinder";
      } else if (type == EllipsoidRegion.class) {
         return "ellipsoid";
      } else {
         throw new UnsupportedRegionException(type);
      }
   }

   public Class<? extends Region> type(String type) {
      return switch (type) {
         case "cuboid" -> CuboidRegion.class;
         case "ellipsoid" -> EllipsoidRegion.class;
         case "cylinder" -> CylinderRegion.class;
         default -> throw new UnsupportedRegionException(Region.class, type);
      };
   }
}
