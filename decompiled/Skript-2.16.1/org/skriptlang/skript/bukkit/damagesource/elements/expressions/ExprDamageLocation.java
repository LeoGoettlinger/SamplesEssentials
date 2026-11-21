package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Source - Damage Location")
@Description({"The location where the damage was originated from.", "The 'damage location' on vanilla damage sources will be set if an entity did not cause the damage.", "Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."})
@Example.Examples({@Example("damage all players by 5 using a custom damage source:\n\tset the damage type to magic\n\tset the causing entity to {_player}\n\tset the direct entity to {_arrow}\n\tset the damage location to location(0, 0, 10)\n"), @Example("on death:\n\tset {_location} to the damage location of event-damage source\n")})
@Since({"2.12"})
public class ExprDamageLocation extends SimplePropertyExpression<DamageSource, Location> {
   private boolean isEvent;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprDamageLocation.class, Location.class, "damage location", "damagesources", true)
               .supplier(ExprDamageLocation::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isEvent = this.getParser().isCurrentEvent(ExprSecDamageSource.DamageSourceSectionEvent.class);
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Location convert(DamageSource damageSource) {
      return damageSource.getDamageLocation();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.isEvent) {
         Skript.error("You cannot change the attributes of a damage source outside a 'custom damage source' section.");
      } else if (this.getExpr().isSingle() && this.getExpr().isDefault()) {
         if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) {
            return CollectionUtils.array(Location.class);
         }
      } else {
         Skript.error("You can only change the attributes of the damage source being created in this section.");
      }

      return null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof ExprSecDamageSource.DamageSourceSectionEvent sectionEvent) {
         sectionEvent.damageLocation = delta == null ? null : (Location)delta[0];
      }
   }

   @Override
   public Class<Location> getReturnType() {
      return Location.class;
   }

   @Override
   protected String getPropertyName() {
      return "damage location";
   }
}
