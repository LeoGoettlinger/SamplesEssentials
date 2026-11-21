package com.magmaguy.elitemobs.magmacore.scripting.tables;

import com.magmaguy.elitemobs.magmacore.util.EntityMovementPath;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaTable;
import com.magmaguy.elitemobs.shaded.luaj.vm2.LuaValue;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class LuaEntityTable {
   private static final List<BiConsumer<LuaTable, Entity>> enrichers = new CopyOnWriteArrayList<>();
   private static final String ARROW_ATTACHMENT = "nightbreak_owned_arrow_attachment";

   public static void recordArrowAttachment(Plugin owner, AbstractArrow arrow, Block block) {
      Location attachment = block.getLocation().add(0.5, 0.5, 0.5);
      arrow.setMetadata("nightbreak_owned_arrow_attachment", new FixedMetadataValue(owner, List.of(arrow.getLocation(), attachment)));
   }

   public static void clearArrowAttachment(Plugin owner, Entity arrow) {
      arrow.removeMetadata("nightbreak_owned_arrow_attachment", owner);
   }

   private static LuaValue arrowAttachment(AbstractArrow arrow) {
      for (MetadataValue value : arrow.getMetadata("nightbreak_owned_arrow_attachment")) {
         if (value.value() instanceof List<?> locations
            && locations.size() == 2
            && locations.get(0) instanceof Location impact
            && locations.get(1) instanceof Location attachment
            && arrow.isValid()
            && arrow.isInBlock()
            && value.getOwningPlugin().isEnabled()
            && arrow.getWorld().equals(impact.getWorld())
            && arrow.getLocation().distanceSquared(impact) <= 0.04) {
            return LuaTableSupport.locationToTable(attachment);
         }

         arrow.removeMetadata("nightbreak_owned_arrow_attachment", value.getOwningPlugin());
      }

      return LuaValue.NIL;
   }

   public static void registerEnricher(BiConsumer<LuaTable, Entity> enricher) {
      if (enricher != null) {
         enrichers.add(enricher);
      }
   }

   public static boolean isHostileEffectTarget(Player actor, Entity target) {
      return actor != null
         && actor.isOnline()
         && actor.isValid()
         && !actor.isDead()
         && target instanceof LivingEntity
         && !(target instanceof Player)
         && !(target instanceof ArmorStand)
         && target.isValid()
         && !target.isDead()
         && !target.isInvulnerable()
         && !target.hasMetadata("NPC")
         && !(target instanceof Tameable tameable && tameable.isTamed())
         && actor.getWorld().equals(target.getWorld());
   }

   public static LuaTable build(Entity entity) {
      LuaTable table = new LuaTable();
      if (entity == null) {
         return table;
      } else {
         table.set("uuid", entity.getUniqueId().toString());
         table.set("entity_type", entity.getType().name().toLowerCase());
         LuaTableSupport.lazyField(table, "is_on_ground", () -> LuaValue.valueOf(entity.isOnGround()));
         LuaTableSupport.lazyField(table, "is_flying", () -> LuaValue.valueOf(entity instanceof Player player && player.isFlying()));
         table.set("set_velocity", LuaTableSupport.tableMethod(table, args -> {
            double x = args.checkdouble(1);
            double y = args.checkdouble(2);
            double z = args.checkdouble(3);
            if (Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)) {
               entity.setVelocity(new Vector(x, y, z));
               return LuaValue.NIL;
            } else {
               throw new IllegalArgumentException("Velocity must be finite");
            }
         }));
         table.set("set_fall_distance", LuaTableSupport.tableMethod(table, args -> {
            float distance = (float)args.checkdouble(1);
            if (Float.isFinite(distance) && !(distance < 0.0F)) {
               entity.setFallDistance(distance);
               return LuaValue.NIL;
            } else {
               throw new IllegalArgumentException("Invalid fall distance");
            }
         }));
         if (entity instanceof Projectile projectile) {
            table.set("set_shooter", LuaTableSupport.tableMethod(table, args -> {
               if (Bukkit.getEntity(UUID.fromString(args.checkjstring(1))) instanceof ProjectileSource source) {
                  projectile.setShooter(source);
                  return LuaValue.TRUE;
               } else {
                  return LuaValue.FALSE;
               }
            }));
         }

         if (entity instanceof Fireball fireball) {
            table.set("set_direction", LuaTableSupport.tableMethod(table, args -> {
               Vector direction = new Vector(args.checkdouble(1), args.checkdouble(2), args.checkdouble(3));
               if (Double.isFinite(direction.lengthSquared()) && direction.lengthSquared() != 0.0) {
                  fireball.setDirection(direction);
                  return LuaValue.NIL;
               } else {
                  throw new IllegalArgumentException("Invalid fireball direction");
               }
            }));
         }

         if (entity instanceof AbstractArrow arrow) {
            LuaTableSupport.lazyField(table, "in_block", () -> LuaValue.valueOf(arrow.isInBlock()));
            LuaTableSupport.lazyField(
               table,
               "attachment_location",
               () -> (LuaValue)(!arrow.isInBlock()
                  ? LuaValue.NIL
                  : LuaTableSupport.locationToTable(arrow.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5)))
            );
            LuaTableSupport.lazyField(table, "attachment_block", () -> arrowAttachment(arrow));
            table.set("set_pickup", LuaTableSupport.tableMethod(table, args -> {
               arrow.setPickupStatus(PickupStatus.valueOf(args.checkjstring(1).toUpperCase(Locale.ROOT)));
               return LuaValue.NIL;
            }));
         }

         table.set(
            "can_receive_hostile_effect",
            LuaTableSupport.tableMethod(
               table,
               args -> LuaValue.valueOf(
                  Bukkit.getEntity(UUID.fromString(args.checkjstring(1))) instanceof Player player && isHostileEffectTarget(player, entity)
               )
            )
         );
         table.set(
            "has_clear_movement_path",
            LuaTableSupport.tableMethod(
               table, args -> LuaValue.valueOf(EntityMovementPath.isClear(entity, LuaTableSupport.tableToLocation(args.checktable(1), entity.getWorld())))
            )
         );
         table.set(
            "movement_path_status",
            LuaTableSupport.tableMethod(
               table,
               args -> LuaValue.valueOf(
                  EntityMovementPath.inspect(entity, LuaTableSupport.tableToLocation(args.checktable(1), entity.getWorld())).name().toLowerCase(Locale.ROOT)
               )
            )
         );
         LuaTableSupport.lazyField(table, "is_valid", () -> LuaValue.valueOf(entity.isValid()));
         LuaTableSupport.lazyField(table, "is_dead", () -> LuaValue.valueOf(entity.isDead()));
         LuaTableSupport.lazyField(table, "current_location", () -> {
            Location loc = entity.getLocation();
            return (LuaValue)(loc != null ? LuaTableSupport.locationToTable(loc) : LuaValue.NIL);
         });
         LuaTableSupport.lazyField(table, "world", () -> {
            World w = entity.getWorld();
            return (LuaValue)(w != null ? LuaValue.valueOf(w.getName()) : LuaValue.NIL);
         });
         table.set("teleport", LuaTableSupport.tableMethod(table, args -> {
            LuaTable loc = args.arg1().checktable();
            entity.teleport(LuaTableSupport.tableToLocation(loc, entity.getWorld()));
            return LuaValue.NIL;
         }));
         table.set("remove", LuaTableSupport.tableMethod(table, args -> {
            if (entity.isValid()) {
               entity.remove();
            }

            return LuaValue.NIL;
         }));
         table.set("is_player", LuaValue.valueOf(entity instanceof Player));
         table.set("is_hostile", LuaValue.valueOf(entity instanceof Monster));
         table.set("is_passive", LuaValue.valueOf(entity instanceof Animals));
         table.set("set_silent", LuaTableSupport.tableMethod(table, args -> {
            entity.setSilent(args.checkboolean(1));
            return LuaValue.NIL;
         }));
         table.set("set_invulnerable", LuaTableSupport.tableMethod(table, args -> {
            entity.setInvulnerable(args.checkboolean(1));
            return LuaValue.NIL;
         }));
         table.set("set_gravity", LuaTableSupport.tableMethod(table, args -> {
            entity.setGravity(args.checkboolean(1));
            return LuaValue.NIL;
         }));
         table.set("set_glowing", LuaTableSupport.tableMethod(table, args -> {
            entity.setGlowing(args.checkboolean(1));
            return LuaValue.NIL;
         }));

         for (BiConsumer<LuaTable, Entity> enricher : enrichers) {
            try {
               enricher.accept(table, entity);
            } catch (Throwable var5) {
            }
         }

         return table;
      }
   }
}
