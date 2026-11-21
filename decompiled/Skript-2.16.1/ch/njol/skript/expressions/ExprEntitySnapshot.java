package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

@Name("Entity Snapshot")
@Description({"Returns the entity snapshot of a provided entity, which includes all the data associated with it (name, health, attributes, etc.) at the time this expression is used.", "Individual attributes of a snapshot cannot be modified or retrieved."})
@Example("spawn a pig at location(0, 0, 0):\n\tset the max health of entity to 20\n\tset the health of entity to 20\n\tset {_snapshot} to the entity snapshot of entity\n\tclear entity\nspawn {_snapshot} at location(0, 0, 0)\n")
@RequiredPlugins({"Minecraft 1.20.2+"})
@Since({"2.10"})
public class ExprEntitySnapshot extends SimplePropertyExpression<Object, EntitySnapshot> {
   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!Player.class.isAssignableFrom(exprs[0].getReturnType()) && !FishHook.class.isAssignableFrom(exprs[0].getReturnType())) {
         return super.init(exprs, matchedPattern, isDelayed, parseResult);
      } else {
         Skript.error("One or more listed entities can not return an entity snapshot.");
         return false;
      }
   }

   @Nullable
   public EntitySnapshot convert(Object object) {
      if (object instanceof Entity entity) {
         return entity.createSnapshot();
      } else if (object instanceof EntityData<?> entityData) {
         Entity entity = entityData.create();
         EntitySnapshot snapshot = entity.createSnapshot();
         entity.remove();
         return snapshot;
      } else {
         return null;
      }
   }

   @Override
   protected String getPropertyName() {
      return "entity snapshot";
   }

   @Override
   public Class<EntitySnapshot> getReturnType() {
      return EntitySnapshot.class;
   }

   static {
      if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
         register(ExprEntitySnapshot.class, EntitySnapshot.class, "entity snapshot", "entities/entitydatas");
      }
   }
}
