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
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Source - Causing Entity")
@Description({"The causing entity of a damage source.", "The causing entity is the entity that ultimately caused the damage. (e.g. the entity that shot an arrow)", "When setting a 'causing entity' you must also set a 'direct entity'.", "Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."})
@Example.Examples({@Example("set {_source} to a custom damage source:\n\tset the damage type to magic\n\tset the causing entity to {_player}\n\tset the direct entity to {_arrow}\n\tset the damage location to location(0, 0, 10)\n"), @Example("on damage:\n\tset {_causing} to the causing entity of event-damage source\n")})
@Since({"2.12"})
public class ExprCausingEntity extends SimplePropertyExpression<DamageSource, Entity> {
   private boolean isEvent;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprCausingEntity.class, Entity.class, "(causing|responsible) entity", "damagesources", true)
               .supplier(ExprCausingEntity::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isEvent = this.getParser().isCurrentEvent(ExprSecDamageSource.DamageSourceSectionEvent.class);
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Entity convert(DamageSource damageSource) {
      return damageSource.getCausingEntity();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.isEvent) {
         Skript.error("You cannot change the attributes of a damage source outside a 'custom damage source' section.");
      } else if (this.getExpr().isSingle() && this.getExpr().isDefault()) {
         if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) {
            return CollectionUtils.array(Entity.class);
         }
      } else {
         Skript.error("You can only change the attributes of the damage source being created in this section.");
      }

      return null;
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof ExprSecDamageSource.DamageSourceSectionEvent sectionEvent) {
         sectionEvent.causingEntity = delta == null ? null : (Entity)delta[0];
      }
   }

   @Override
   public Class<Entity> getReturnType() {
      return Entity.class;
   }

   @Override
   protected String getPropertyName() {
      return "causing entity";
   }
}
