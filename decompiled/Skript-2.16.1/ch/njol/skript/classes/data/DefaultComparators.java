package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.entity.BoatChestData;
import ch.njol.skript.entity.BoatData;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.RabbitData;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.GameruleValue;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.WeatherType;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;
import ch.njol.util.StringUtils;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.skriptlang.skript.lang.comparator.Comparator;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;

public class DefaultComparators {
   public static final Comparator<EntityData, ItemType> entityItemComparator = new Comparator<EntityData, ItemType>() {
      public Relation compare(EntityData e, ItemType i) {
         if (e instanceof Item) {
            return Relation.get(i.isOfType(((Item)e).getItemStack()));
         } else if (e instanceof BoatData) {
            return Relation.get(((BoatData)e).isOfItemType(i));
         } else if (e instanceof BoatChestData) {
            return Relation.get(((BoatChestData)e).isOfItemType(i));
         } else if (e instanceof RabbitData) {
            return Relation.get(i.isOfType(Material.RABBIT));
         } else {
            for (ItemData data : i.getTypes()) {
               assert data != null;

               EntityData<?> entity = Aliases.getRelatedEntity(data);
               if (entity != null && entity.getType().isAssignableFrom(e.getType())) {
                  return Relation.EQUAL;
               }
            }

            return Relation.NOT_EQUAL;
         }
      }

      @Override
      public boolean supportsOrdering() {
         return false;
      }
   };

   static {
      Comparators.registerComparator(Number.class, Number.class, new Comparator<Number, Number>() {
         public Relation compare(Number n1, Number n2) {
            if (n1 instanceof Long && n2 instanceof Long) {
               return Relation.get((double)(n1.longValue() - n2.longValue()));
            } else {
               double epsilon = 1.0E-10;
               Double d1;
               Double d2;
               if (!(n1 instanceof Float) && !(n2 instanceof Float)) {
                  d1 = n1.doubleValue();
                  d2 = n2.doubleValue();
               } else {
                  d1 = (double)n1.floatValue();
                  d2 = (double)n2.floatValue();
                  epsilon = Math.min(d1, d2) * 1.0E-6;
               }

               if (d1.isNaN() || d2.isNaN()) {
                  return Relation.SMALLER;
               } else if (!d1.isInfinite() && !d2.isInfinite()) {
                  double diff = d1 - d2;
                  return Math.abs(diff) < epsilon ? Relation.EQUAL : Relation.get(diff);
               } else {
                  return d1 > d2 ? Relation.GREATER : (d1 < d2 ? Relation.SMALLER : Relation.EQUAL);
               }
            }
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(Slot.class, Slot.class, new Comparator<Slot, Slot>() {
         public Relation compare(Slot slot1, Slot slot2) {
            return slot1 instanceof EquipmentSlot != (slot2 instanceof EquipmentSlot) ? Relation.NOT_EQUAL : Relation.get(slot1.isSameSlot(slot2));
         }
      });
      Comparators.registerComparator(Slot.class, Number.class, new Comparator<Slot, Number>() {
         public Relation compare(Slot o1, Number o2) {
            return o1 instanceof SlotWithIndex ? Relation.get(((SlotWithIndex)o1).getIndex() - o2.intValue()) : Relation.NOT_EQUAL;
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(
         Slot.class,
         ItemType.class,
         new Comparator<Slot, ItemType>() {
            public Relation compare(Slot slot, ItemType item) {
               ItemStack stack = slot.getItem();
               return stack != null && stack.getAmount() != 0
                  ? Comparators.compare(new ItemType(stack), item)
                  : Comparators.compare(new ItemType(Material.AIR), item);
            }

            @Override
            public boolean supportsInversion() {
               return false;
            }
         }
      );
      Comparators.registerComparator(
         ItemType.class,
         Slot.class,
         new Comparator<ItemType, Slot>() {
            public Relation compare(ItemType item, Slot slot) {
               ItemStack stack = slot.getItem();
               return stack != null && stack.getAmount() != 0
                  ? Comparators.compare(item, new ItemType(stack))
                  : Comparators.compare(item, new ItemType(Material.AIR));
            }

            @Override
            public boolean supportsInversion() {
               return false;
            }
         }
      );
      Comparators.registerComparator(ItemStack.class, ItemType.class, new Comparator<ItemStack, ItemType>() {
         public Relation compare(ItemStack is, ItemType it) {
            return Comparators.compare(new ItemType(is), it);
         }

         @Override
         public boolean supportsInversion() {
            return false;
         }
      });
      Comparators.registerComparator(ItemType.class, ItemStack.class, new Comparator<ItemType, ItemStack>() {
         public Relation compare(ItemType it, ItemStack is) {
            return Comparators.compare(it, new ItemType(is));
         }

         @Override
         public boolean supportsInversion() {
            return false;
         }
      });
      Comparators.registerComparator(Block.class, ItemType.class, new Comparator<Block, ItemType>() {
         public Relation compare(Block b, ItemType it) {
            return Comparators.compare(new ItemType(b), it);
         }

         @Override
         public boolean supportsInversion() {
            return false;
         }
      });
      Comparators.registerComparator(Block.class, BlockData.class, new Comparator<Block, BlockData>() {
         public Relation compare(Block block, BlockData data) {
            return Relation.get(block.getBlockData().matches(data));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(BlockData.class, BlockData.class, new Comparator<BlockData, BlockData>() {
         public Relation compare(BlockData data1, BlockData data2) {
            return Relation.get(data1.matches(data2));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(ItemType.class, ItemType.class, new Comparator<ItemType, ItemType>() {
         public Relation compare(ItemType i1, ItemType i2) {
            int otherAmount = i2.getAmount();
            if (i1.getAmount() != otherAmount) {
               if (otherAmount != 1) {
                  return Relation.NOT_EQUAL;
               }

               for (ItemData itemData : i2.getTypes()) {
                  if (!itemData.isAlias()) {
                     return Relation.NOT_EQUAL;
                  }
               }
            }

            return Relation.get(i1.isSimilar(i2));
         }

         @Override
         public boolean supportsInversion() {
            return false;
         }
      });
      Comparators.registerComparator(Block.class, Block.class, new Comparator<Block, Block>() {
         public Relation compare(Block b1, Block b2) {
            return Relation.get(BlockUtils.extractBlock(b1).equals(BlockUtils.extractBlock(b2)));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(Entity.class, EntityData.class, new Comparator<Entity, EntityData>() {
         public Relation compare(Entity e, EntityData t) {
            return Relation.get(t.isInstance(e));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(EntityData.class, EntityData.class, new Comparator<EntityData, EntityData>() {
         public Relation compare(EntityData t1, EntityData t2) {
            return Relation.get(t2.isSupertypeOf(t1));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(EntityData.class, ItemType.class, entityItemComparator);
      Comparators.registerComparator(Entity.class, ItemType.class, new Comparator<Entity, ItemType>() {
         public Relation compare(Entity entity, ItemType item) {
            return DefaultComparators.entityItemComparator.compare(EntityData.fromEntity(entity), item);
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(CommandSender.class, CommandSender.class, new Comparator<CommandSender, CommandSender>() {
         public Relation compare(CommandSender s1, CommandSender s2) {
            return Relation.get(s1.equals(s2));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(OfflinePlayer.class, OfflinePlayer.class, new Comparator<OfflinePlayer, OfflinePlayer>() {
         public Relation compare(OfflinePlayer p1, OfflinePlayer p2) {
            return Relation.get(Objects.equals(p1.getUniqueId(), p2.getUniqueId()));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(OfflinePlayer.class, String.class, new Comparator<OfflinePlayer, String>() {
         public Relation compare(OfflinePlayer player, String name) {
            if (Utils.isValidUUID(name)) {
               UUID uuid = UUID.fromString(name);
               return Relation.get(player.getUniqueId().equals(uuid));
            } else {
               String playerName = player.getName();
               return playerName == null ? Relation.NOT_EQUAL : Relation.get(playerName.equalsIgnoreCase(name));
            }
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(OfflinePlayer.class, UUID.class, (player, uuid) -> Relation.get(player.getUniqueId().equals(uuid)));
      Comparators.registerComparator(World.class, String.class, new Comparator<World, String>() {
         public Relation compare(World w, String name) {
            return Relation.get(w.getName().equalsIgnoreCase(name));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(String.class, String.class, new Comparator<String, String>() {
         public Relation compare(String s1, String s2) {
            return Relation.get(StringUtils.equals(s1, s2, SkriptConfig.caseSensitive.value()));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(Date.class, Date.class, new Comparator<Date, Date>() {
         public Relation compare(Date d1, Date d2) {
            return Relation.get(d1.compareTo(d2));
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(Time.class, Time.class, new Comparator<Time, Time>() {
         public Relation compare(Time t1, Time t2) {
            return Relation.get(t1.getTime() - t2.getTime());
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(Timespan.class, Timespan.class, new Comparator<Timespan, Timespan>() {
         public Relation compare(Timespan t1, Timespan t2) {
            return Relation.get((double)(t1.getAs(Timespan.TimePeriod.MILLISECOND) - t2.getAs(Timespan.TimePeriod.MILLISECOND)));
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(Time.class, Timeperiod.class, new Comparator<Time, Timeperiod>() {
         public Relation compare(Time t, Timeperiod p) {
            return Relation.get(p.contains(t));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(Object.class, ClassInfo.class, new Comparator<Object, ClassInfo>() {
         public Relation compare(Object o, ClassInfo c) {
            return Relation.get(c.getC().isInstance(o) || o instanceof ClassInfo && c.getC().isAssignableFrom(((ClassInfo)o).getC()));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(DamageCause.class, ItemType.class, new Comparator<DamageCause, ItemType>() {
         public Relation compare(DamageCause dc, ItemType t) {
            switch (dc) {
               case FIRE:
                  return Relation.get(t.isOfType(Material.FIRE));
               case LAVA:
                  return Relation.get(t.getMaterial() == Material.LAVA);
               case MAGIC:
                  return Relation.get(t.isOfType(Material.POTION));
               case HOT_FLOOR:
                  return Relation.get(t.isOfType(Material.MAGMA_BLOCK));
               default:
                  return Relation.NOT_EQUAL;
            }
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(DamageCause.class, EntityData.class, new Comparator<DamageCause, EntityData>() {
         public Relation compare(DamageCause dc, EntityData e) {
            switch (dc) {
               case ENTITY_ATTACK:
                  return Relation.get(EntityData.fromClass(Entity.class).isSupertypeOf(e));
               case PROJECTILE:
                  return Relation.get(EntityData.fromClass(Projectile.class).isSupertypeOf(e));
               case WITHER:
                  return Relation.get(EntityData.fromClass(Wither.class).isSupertypeOf(e));
               case FALLING_BLOCK:
                  return Relation.get(EntityData.fromClass(FallingBlock.class).isSupertypeOf(e));
               default:
                  return Relation.NOT_EQUAL;
            }
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(GameruleValue.class, GameruleValue.class, new Comparator<GameruleValue, GameruleValue>() {
         public Relation compare(GameruleValue o1, GameruleValue o2) {
            return Relation.get(o1.equals(o2));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(GameruleValue.class, Number.class, new Comparator<GameruleValue, Number>() {
         public Relation compare(GameruleValue o1, Number o2) {
            if (!(o1.getGameruleValue() instanceof Number)) {
               return Relation.NOT_EQUAL;
            } else {
               Number gameruleValue = (Number)o1.getGameruleValue();
               return Comparators.compare(gameruleValue, o2);
            }
         }

         @Override
         public boolean supportsOrdering() {
            return true;
         }
      });
      Comparators.registerComparator(GameruleValue.class, Boolean.class, new Comparator<GameruleValue, Boolean>() {
         public Relation compare(GameruleValue o1, Boolean o2) {
            return !(o1.getGameruleValue() instanceof Boolean) ? Relation.NOT_EQUAL : Relation.get(o2.equals(o1.getGameruleValue()));
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(Inventory.class, InventoryType.class, new Comparator<Inventory, InventoryType>() {
         public Relation compare(Inventory inventory, InventoryType inventoryType) {
            return Relation.get(inventory.getType() == inventoryType);
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(World.class, WeatherType.class, new Comparator<World, WeatherType>() {
         public Relation compare(World world, WeatherType weatherType) {
            return Relation.get(WeatherType.fromWorld(world) == weatherType);
         }

         @Override
         public boolean supportsOrdering() {
            return false;
         }
      });
      Comparators.registerComparator(
         Location.class,
         Location.class,
         new Comparator<Location, Location>() {
            public Relation compare(Location first, Location second) {
               return Relation.get(
                  Objects.equals(first.getWorld(), second.getWorld())
                     && first.toVector().equals(second.toVector())
                     && Location.normalizeYaw(first.getYaw()) == Location.normalizeYaw(second.getYaw())
                     && Location.normalizePitch(first.getPitch()) == Location.normalizePitch(second.getPitch())
               );
            }

            @Override
            public boolean supportsOrdering() {
               return false;
            }
         }
      );
      Comparators.registerComparator(Color.class, Color.class, (one, two) -> Relation.get(one.asBukkitColor().equals(two.asBukkitColor())));
      Comparators.registerComparator(Color.class, org.bukkit.Color.class, (one, two) -> Relation.get(one.asBukkitColor().equals(two)));
      Comparators.registerComparator(org.bukkit.Color.class, org.bukkit.Color.class, (one, two) -> Relation.get(one.equals(two)));
      if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
         final boolean SNAPSHOT_AS_STRING_EXISTS = Skript.methodExists(EntitySnapshot.class, "getAsString");
         Comparators.registerComparator(EntitySnapshot.class, EntitySnapshot.class, new Comparator<EntitySnapshot, EntitySnapshot>() {
            public Relation compare(EntitySnapshot snap1, EntitySnapshot snap2) {
               if (!snap1.getEntityType().equals(snap1.getEntityType())) {
                  return Relation.NOT_EQUAL;
               } else {
                  boolean isEqual;
                  if (!SNAPSHOT_AS_STRING_EXISTS) {
                     isEqual = snap1.equals(snap2) || snap1.hashCode() == snap2.hashCode();
                  } else {
                     isEqual = snap1.getAsString().equalsIgnoreCase(snap2.getAsString());
                  }

                  return Relation.get(isEqual);
               }
            }
         });
      }

      Comparators.registerComparator(UUID.class, UUID.class, (one, two) -> Relation.get(one.equals(two)));
      Comparators.registerComparator(UUID.class, String.class, (one, two) -> Relation.get(one.toString().equals(two)));
   }
}
