package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Can See")
@Description({"Checks whether the given players can see the provided entities."})
@Example.Examples({@Example("if sender can't see the player-argument:\n\tmessage \"who dat?\"\n"), @Example("if the player can see the last spawned entity:\n\tmessage \"hello there!\"\n")})
@Since({"2.3, 2.10 (entities)"})
@RequiredPlugins({"Minecraft 1.19+ (entities)"})
public class CondCanSee extends Condition {
   private Expression<Player> viewers;
   private Expression<Entity> entities;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult result) {
      if (matchedPattern != 1 && matchedPattern != 3) {
         this.entities = (Expression<Entity>)exprs[0];
         this.viewers = (Expression<Player>)exprs[1];
      } else {
         this.viewers = (Expression<Player>)exprs[0];
         this.entities = (Expression<Entity>)exprs[1];
      }

      this.setNegated(matchedPattern > 1 ^ result.hasTag("invisible"));
      return true;
   }

   @Override
   public boolean check(Event event) {
      return this.viewers.check(event, player -> this.entities.check(event, player::canSee), this.isNegated());
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return PropertyCondition.toString(this, PropertyCondition.PropertyType.CAN, event, debug, this.viewers, "see " + this.entities.toString(event, debug));
   }

   static {
      Skript.registerCondition(
         CondCanSee.class,
         "%entities% (is|are) [visible|:invisible] for %players%",
         "%players% can see %entities%",
         "%entities% (is|are)(n't| not) [visible|:invisible] for %players%",
         "%players% can('t| not) see %entities%"
      );
   }
}
