package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.GameruleValue;
import ch.njol.skript.util.Utils;
import ch.njol.util.Kleenean;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Gamerule Value")
@Description({"The gamerule value of a world."})
@Example("set the gamerule commandBlockOutput of world \"world\" to false")
@Since({"2.5"})
public class ExprGameRule extends SimpleExpression<GameruleValue> {
   private Expression<GameRule<?>> gamerule;
   private Expression<World> worlds;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.gamerule = (Expression<GameRule<?>>)exprs[0];
      this.worlds = (Expression<World>)exprs[1];
      return true;
   }

   @Nullable
   protected GameruleValue<?>[] get(Event event) {
      GameRule<?> gamerule = this.gamerule.getSingle(event);
      if (gamerule == null) {
         return null;
      } else {
         World[] worlds = this.worlds.getArray(event);
         GameruleValue<?>[] gameruleValues = new GameruleValue[worlds.length];

         for (int i = 0; i < worlds.length; i++) {
            Object gameruleValue = worlds[i].getGameRuleValue(gamerule);

            assert gameruleValue != null;

            gameruleValues[i] = new GameruleValue<>(gameruleValue);
         }

         return gameruleValues;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return mode == Changer.ChangeMode.SET ? new Class[]{Boolean.class, Integer.class} : null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (mode == Changer.ChangeMode.SET) {
         GameRule gamerule = this.gamerule.getSingle(event);
         if (gamerule != null) {
            assert delta != null;

            Object value = delta[0];
            if (!gamerule.getType().isAssignableFrom(value.getClass())) {
               String currentClassName = Classes.toString(Classes.getSuperClassInfo(value.getClass()));
               currentClassName = Utils.a(currentClassName);
               String targetClassName = Classes.toString(Classes.getSuperClassInfo(gamerule.getType()));
               targetClassName = Utils.a(targetClassName);
               this.error("The " + gamerule.getName() + " gamerule can only be set to " + targetClassName + ", not " + currentClassName + ".");
            } else {
               for (World gameruleWorld : this.worlds.getArray(event)) {
                  gameruleWorld.setGameRule(gamerule, value);
               }
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.worlds.isSingle();
   }

   @Override
   public Class<? extends GameruleValue> getReturnType() {
      return GameruleValue.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the gamerule " + this.gamerule.toString(event, debug) + " of " + this.worlds.toString(event, debug);
   }

   static {
      Skript.registerExpression(ExprGameRule.class, GameruleValue.class, ExpressionType.COMBINED, "[the] gamerule %gamerule% of %worlds%");
   }
}
