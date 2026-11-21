package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.EventValues;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCollisionEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.jetbrains.annotations.Nullable;

public class EvtVehicleCollision extends SkriptEvent {
   private Literal<?> expr;
   private boolean blockCollision;
   private boolean entityCollision;
   private final List<ItemType> itemTypes = new ArrayList<>();
   private final List<BlockData> blockDatas = new ArrayList<>();
   private final List<EntityData<?>> entityDatas = new ArrayList<>();

   @Override
   public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
      if (args[0] != null) {
         this.expr = args[0];

         for (Object object : this.expr.getAll()) {
            if (object instanceof ItemType itemType) {
               this.itemTypes.add(itemType);
            } else if (object instanceof BlockData blockData) {
               this.blockDatas.add(blockData);
            } else if (object instanceof EntityData<?> entityData) {
               this.entityDatas.add(entityData);
            }
         }
      }

      this.blockCollision = matchedPattern == 1;
      this.entityCollision = matchedPattern == 2;
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (event instanceof VehicleCollisionEvent collisionEvent) {
         if (this.expr == null) {
            return this.blockCollision && !(event instanceof VehicleBlockCollisionEvent)
               ? false
               : !this.entityCollision || event instanceof VehicleEntityCollisionEvent;
         } else {
            if (collisionEvent instanceof VehicleBlockCollisionEvent blockCollisionEvent && (!this.itemTypes.isEmpty() || !this.blockDatas.isEmpty())) {
               Block eventBlock = blockCollisionEvent.getBlock();
               ItemType eventItemType = new ItemType(eventBlock.getType());
               BlockData eventBlockData = eventBlock.getBlockData();

               for (ItemType itemType : this.itemTypes) {
                  if (itemType.isSupertypeOf(eventItemType)) {
                     return true;
                  }
               }

               Iterator var13 = this.blockDatas.iterator();

               BlockData blockData;
               do {
                  if (!var13.hasNext()) {
                     return false;
                  }

                  blockData = (BlockData)var13.next();
               } while (!blockData.matches(eventBlockData));

               return true;
            } else if (collisionEvent instanceof VehicleEntityCollisionEvent entityCollisionEvent && !this.entityDatas.isEmpty()) {
               EntityData<?> eventEntityData = EntityData.fromEntity(entityCollisionEvent.getEntity());

               for (EntityData<?> entityData : this.entityDatas) {
                  if (entityData.isSupertypeOf(eventEntityData)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("vehicle");
      if (this.blockCollision) {
         builder.append("block");
      } else if (this.entityCollision) {
         builder.append("entity");
      }

      builder.append("collision");
      if (this.expr != null) {
         builder.append("of", this.expr);
      }

      return builder.toString();
   }

   static {
      Skript.registerEvent(
            "Vehicle Collision",
            EvtVehicleCollision.class,
            new Class[]{VehicleBlockCollisionEvent.class, VehicleEntityCollisionEvent.class},
            "vehicle collision [(with|of) [a[n]] %-itemtypes/blockdatas/entitydatas%]",
            "vehicle block collision [(with|of) [a[n]] %-itemtypes/blockdatas%]",
            "vehicle entity collision [(with|of) [a[n]] %-entitydatas%]"
         )
         .description("Called when a vehicle collides with a block or entity.")
         .examples("on vehicle collision:", "on vehicle collision with obsidian:", "on vehicle collision with a zombie:")
         .since("2.10");
      EventValues.registerEventValue(VehicleBlockCollisionEvent.class, Block.class, VehicleBlockCollisionEvent::getBlock);
      EventValues.registerEventValue(VehicleEntityCollisionEvent.class, Entity.class, VehicleEntityCollisionEvent::getEntity);
   }
}
