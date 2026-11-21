package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.ItemUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.jetbrains.annotations.Nullable;

@Name("Spawn Egg Entity")
@Description({"Gets or sets the entity snapshot that the provided spawn eggs will spawn when used."})
@Example.Examples({@Example("set {_item} to a zombie spawn egg"), @Example("broadcast the spawn egg entity of {_item}"), @Example("spawn a pig at location(0,0,0):\n\tset the max health of entity to 20\n\tset the health of entity to 20\n\tset {_snapshot} to the entity snapshot of entity\n\tclear entity\nset the spawn egg entity of {_item} to {_snapshot}\n"), @Example("if the spawn egg entity of {_item} is {_snapshot}: # Minecraft 1.20.5+\n\tset the spawn egg entity of {_item} to (random element out of all entities)\n"), @Example("set the spawn egg entity of {_item} to a zombie")})
@RequiredPlugins({"Minecraft 1.20.2+, Minecraft 1.20.5+ (comparisons)"})
@Since({"2.10"})
public class ExprSpawnEggEntity extends SimplePropertyExpression<Object, EntitySnapshot> {
   @Nullable
   public EntitySnapshot convert(Object object) {
      ItemStack itemStack = ItemUtils.asItemStack(object);
      return itemStack != null && itemStack.getItemMeta() instanceof SpawnEggMeta eggMeta ? eggMeta.getSpawnedEntity() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(EntitySnapshot.class, Entity.class, EntityData.class) : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         EntitySnapshot snapshot = null;
         if (delta[0] instanceof EntitySnapshot entitySnapshot) {
            snapshot = entitySnapshot;
         } else if (delta[0] instanceof Entity entity) {
            snapshot = entity.createSnapshot();
         } else if (delta[0] instanceof EntityData<?> entityData) {
            Entity entity = entityData.create();
            snapshot = entity.createSnapshot();
            entity.remove();
         }

         if (snapshot != null) {
            for (Object object : this.getExpr().getArray(event)) {
               ItemStack item = ItemUtils.asItemStack(object);
               if (item != null && item.getItemMeta() instanceof SpawnEggMeta eggMeta) {
                  eggMeta.setSpawnedEntity(snapshot);
                  if (object instanceof Slot slot) {
                     item.setItemMeta(eggMeta);
                     slot.setItem(item);
                  } else if (object instanceof ItemType itemType) {
                     itemType.setItemMeta(eggMeta);
                  } else if (object instanceof ItemStack itemStack) {
                     itemStack.setItemMeta(eggMeta);
                  }
               }
            }
         }
      }
   }

   @Override
   public Class<EntitySnapshot> getReturnType() {
      return EntitySnapshot.class;
   }

   @Override
   protected String getPropertyName() {
      return "spawn egg entity";
   }

   static {
      if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
         register(ExprSpawnEggEntity.class, EntitySnapshot.class, "spawn egg entity", "itemstacks/itemtypes/slots");
      }
   }
}
