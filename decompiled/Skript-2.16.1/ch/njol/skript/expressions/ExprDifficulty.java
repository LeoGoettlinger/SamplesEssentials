package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Difficulty")
@Description({"The difficulty of a world."})
@Example("set the difficulty of \"world\" to hard")
@Since({"2.3"})
public class ExprDifficulty extends SimplePropertyExpression<World, Difficulty> {
   @Nullable
   public Difficulty convert(World world) {
      return world.getDifficulty();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Difficulty.class) : null;
   }

   @Override
   public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (delta != null) {
         Difficulty difficulty = (Difficulty)delta[0];

         for (World world : this.getExpr().getArray(e)) {
            world.setDifficulty(difficulty);
            if (difficulty != Difficulty.PEACEFUL) {
               world.setSpawnFlags(true, world.getAllowAnimals());
            }
         }
      }
   }

   @Override
   protected String getPropertyName() {
      return "difficulty";
   }

   @Override
   public Class<Difficulty> getReturnType() {
      return Difficulty.class;
   }

   static {
      register(ExprDifficulty.class, Difficulty.class, "difficult(y|ies)", "worlds");
   }
}
