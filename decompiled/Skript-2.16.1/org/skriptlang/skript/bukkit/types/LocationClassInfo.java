package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.BlockUtils;
import ch.njol.yggdrasil.Fields;
import java.io.StreamCorruptedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

@Internal
public class LocationClassInfo extends ClassInfo<Location> {
   public LocationClassInfo() {
      super(Location.class, "location");
      this.user(new String[]{"locations?"})
         .name("Location")
         .description(
            "A location in a <a href='#world'>world</a>. Locations are world-specific and even store a <a href='#direction'>direction</a>, e.g. if you save a location and later teleport to it you will face the exact same direction you did when you saved the location."
         )
         .usage("")
         .examples("teleport player to location at 0, 69, 0", "set {home::%uuid of player%} to location of the player")
         .since("1.0")
         .defaultExpression(new EventValueExpression<>(Location.class))
         .parser(new LocationClassInfo.LocationParser())
         .serializer(new LocationClassInfo.LocationSerializer())
         .cloner(Location::clone)
         .property(Property.WXYZ, "The X, Y, or Z coordinate of the location.", Skript.instance(), new LocationClassInfo.LocationWXYZHandler());
      Converters.registerConverter(OfflinePlayer.class, Location.class, OfflinePlayer::getLocation);
      Converters.registerConverter(Block.class, Location.class, BlockUtils::getLocation, 8);
      Converters.registerConverter(Entity.class, Location.class, Entity::getLocation, 8);
      Converters.registerConverter(InventoryHolder.class, Location.class, holder -> {
         if (holder instanceof Entity entity) {
            return entity.getLocation();
         } else if (holder instanceof Block block) {
            return block.getLocation();
         } else if (holder instanceof BlockState state) {
            return BlockUtils.getLocation(state.getBlock());
         } else {
            if (holder instanceof DoubleChest doubleChest) {
               if (doubleChest.getLeftSide() != null) {
                  return BlockUtils.getLocation(((BlockState)doubleChest.getLeftSide()).getBlock());
               }

               if (doubleChest.getRightSide() != null) {
                  return BlockUtils.getLocation(((BlockState)doubleChest.getRightSide()).getBlock());
               }
            }

            return null;
         }
      });
   }

   private static class LocationParser extends Parser<Location> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Location loc, int flags) {
         String worldPart = loc.getWorld() == null ? "" : " in '" + loc.getWorld().getName() + "'";
         return "x: "
            + Skript.toString(loc.getX())
            + ", y: "
            + Skript.toString(loc.getY())
            + ", z: "
            + Skript.toString(loc.getZ())
            + ", yaw: "
            + Skript.toString(loc.getYaw())
            + ", pitch: "
            + Skript.toString(loc.getPitch())
            + worldPart;
      }

      public String toVariableNameString(Location loc) {
         String worldPart = loc.getWorld() == null ? "" : loc.getWorld().getName() + ":";
         return worldPart + loc.getX() + "," + loc.getY() + "," + loc.getZ();
      }

      public String getDebugMessage(Location loc) {
         return "("
            + loc.getWorld().getName()
            + ":"
            + loc.getX()
            + ","
            + loc.getY()
            + ","
            + loc.getZ()
            + "|yaw="
            + loc.getYaw()
            + "/pitch="
            + loc.getPitch()
            + ")";
      }
   }

   private static class LocationSerializer extends Serializer<Location> {
      public Fields serialize(Location location) {
         Fields fields = new Fields();
         World world = null;

         try {
            world = location.getWorld();
         } catch (IllegalArgumentException var5) {
            Skript.warning("A location failed to serialize with its defined world, as the world was unloaded.");
         }

         fields.putObject("world", world);
         fields.putPrimitive("x", location.getX());
         fields.putPrimitive("y", location.getY());
         fields.putPrimitive("z", location.getZ());
         fields.putPrimitive("yaw", location.getYaw());
         fields.putPrimitive("pitch", location.getPitch());
         return fields;
      }

      public void deserialize(Location o, Fields f) {
         assert false;
      }

      public Location deserialize(Fields f) throws StreamCorruptedException {
         return new Location(
            f.getObject("world", World.class),
            f.getPrimitive("x", double.class),
            f.getPrimitive("y", double.class),
            f.getPrimitive("z", double.class),
            f.getPrimitive("yaw", float.class),
            f.getPrimitive("pitch", float.class)
         );
      }

      @Override
      public boolean canBeInstantiated() {
         return false;
      }

      @Override
      public boolean mustSyncDeserialization() {
         return true;
      }

      @Nullable
      public Location deserialize(String input) {
         String[] split = input.split("[:,|/]");
         if (split.length != 6) {
            return null;
         } else {
            World w = Bukkit.getWorld(split[0]);
            if (w == null) {
               return null;
            } else {
               try {
                  double[] l = new double[5];

                  for (int i = 0; i < 5; i++) {
                     l[i] = Double.parseDouble(split[i + 1]);
                  }

                  return new Location(w, l[0], l[1], l[2], (float)l[3], (float)l[4]);
               } catch (NumberFormatException var6) {
                  return null;
               }
            }
         }
      }
   }

   private static class LocationWXYZHandler extends WXYZHandler<Location, Double> {
      @Override
      public PropertyHandler<Location> newInstance() {
         LocationClassInfo.LocationWXYZHandler instance = new LocationClassInfo.LocationWXYZHandler();
         instance.axis(this.axis);
         return instance;
      }

      @Nullable
      public Double convert(Location propertyHolder) {
         return switch (this.axis) {
            case X -> propertyHolder.getX();
            case Y -> propertyHolder.getY();
            case Z -> propertyHolder.getZ();
            default -> null;
         };
      }

      @Override
      public boolean supportsAxis(WXYZHandler.Axis axis) {
         return axis != WXYZHandler.Axis.W;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case ADD, SET, REMOVE -> new Class[]{Float.class};
            default -> null;
         };
      }

      public void change(Location location, @Nullable Object[] delta, Changer.ChangeMode mode) {
         assert delta != null;

         float value = (Float)delta[0];
         if (this.axis != WXYZHandler.Axis.W) {
            switch (mode) {
               case SET:
                  switch (this.axis) {
                     case X:
                        location.setX(value);
                        return;
                     case Y:
                        location.setY(value);
                        return;
                     case Z:
                        location.setZ(value);
                        return;
                     default:
                        return;
                  }
               case REMOVE:
                  value = -value;
               case ADD:
                  switch (this.axis) {
                     case X:
                        location.setX(location.getX() + value);
                        return;
                     case Y:
                        location.setY(location.getY() + value);
                        return;
                     case Z:
                        location.setZ(location.getZ() + value);
                        return;
                     default:
                        return;
                  }
               default:
                  assert false;
            }
         }
      }

      @NotNull
      @Override
      public Class<Double> returnType() {
         return Double.class;
      }

      @Override
      public boolean requiresSourceExprChange() {
         return true;
      }
   }
}
