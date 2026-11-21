package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Location;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

@Name("Allay Target Jukebox")
@Description({"The location of the jukebox an allay is set to."})
@Example("set {_loc} to the target jukebox of last spawned allay")
@Since({"2.11"})
public class ExprAllayJukebox extends SimplePropertyExpression<LivingEntity, Location> {
   @Nullable
   public Location convert(LivingEntity entity) {
      return entity instanceof Allay allay ? allay.getJukebox() : null;
   }

   @Override
   public Class<Location> getReturnType() {
      return Location.class;
   }

   @Override
   protected String getPropertyName() {
      return "target jukebox";
   }

   static {
      registerDefault(ExprAllayJukebox.class, Location.class, "target jukebox", "livingentities");
   }
}
