package org.skriptlang.skript.bukkit.damagesource.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Location;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.damage.DamageSource.Builder;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Damage Source")
@Description({"Create a custom damage source and change the attributes.", "When setting a 'causing entity' you must also set a 'direct entity'.", "Attributes of a damage source cannot be changed once created, only while within the 'custom damage source' section."})
@Example.Examples({@Example("set {_source} to a custom damage source:\n\tset the damage type to magic\n\tset the causing entity to {_player}\n\tset the direct entity to {_arrow}\n\tset the damage location to location(0, 0, 10)\ndamage all players by 5 using {_source}\n"), @Example("on damage:\n\tif the damage type of event-damage source is magic:\n\t\tset the damage to damage * 2\n")})
@Since({"2.12"})
public class ExprSecDamageSource extends SectionExpression<DamageSource> {
   @Nullable
   private Expression<DamageType> damageType;
   private Trigger trigger = null;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprSecDamageSource.class, DamageSource.class
                  )
                  .addPatterns(new String[]{"[a] custom damage source [(with|using) [the|a] [damage type [of]] %-damagetype%]"}))
               .supplier(ExprSecDamageSource::new))
            .build()
      );
      EventValues.registerEventValue(
         ExprSecDamageSource.DamageSourceSectionEvent.class, DamageSource.class, ExprSecDamageSource.DamageSourceSectionEvent::buildDamageSource
      );
   }

   @Override
   public boolean init(
      Expression<?>[] exprs,
      int pattern,
      Kleenean delayed,
      SkriptParser.ParseResult result,
      @Nullable SectionNode node,
      @Nullable List<TriggerItem> triggerItems
   ) {
      if (exprs[0] == null) {
         if (node == null) {
            Skript.error("You must contain a section for this expression.");
            return false;
         }

         if (node.isEmpty()) {
            Skript.error("You must contain code inside this section.");
            return false;
         }
      } else {
         this.damageType = (Expression<DamageType>)exprs[0];
      }

      if (node != null) {
         new AtomicBoolean(false);
         this.trigger = SectionUtils.loadLinkedCode(
            "custom damage source",
            (beforeLoading, afterLoading) -> this.loadCode(
               node, "custom damage source", beforeLoading, afterLoading, new Class[]{ExprSecDamageSource.DamageSourceSectionEvent.class}
            )
         );
         return this.trigger != null;
      } else {
         return true;
      }
   }

   @Nullable
   protected DamageSource[] get(Event event) {
      ExprSecDamageSource.DamageSourceSectionEvent sectionEvent = new ExprSecDamageSource.DamageSourceSectionEvent();
      if (this.damageType != null) {
         DamageType damageType = this.damageType.getSingle(event);
         if (damageType != null) {
            sectionEvent.damageType = damageType;
         }
      }

      if (this.trigger != null) {
         Variables.withLocalVariables(event, sectionEvent, () -> TriggerItem.walk(this.trigger, sectionEvent));
         if (sectionEvent.causingEntity != null && sectionEvent.directEntity == null) {
            this.error("You must set a 'direct entity' when setting a 'causing entity'.");
            return null;
         }
      }

      return new DamageSource[]{sectionEvent.buildDamageSource()};
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<DamageSource> getReturnType() {
      return DamageSource.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "a custom damage source";
   }

   static class DamageSourceSectionEvent extends Event {
      public DamageType damageType = DamageType.GENERIC;
      @Nullable
      public Entity causingEntity = null;
      @Nullable
      public Entity directEntity = null;
      @Nullable
      public Location damageLocation = null;

      public DamageSource buildDamageSource() {
         Builder builder = DamageSource.builder(this.damageType);
         if (this.damageLocation != null) {
            builder = builder.withDamageLocation(this.damageLocation.clone());
         }

         if (this.causingEntity != null) {
            builder = builder.withCausingEntity(this.causingEntity);
         }

         if (this.directEntity != null) {
            builder = builder.withDirectEntity(this.directEntity);
         }

         return builder.build();
      }

      public DamageSourceSectionEvent() {
      }

      @NotNull
      public HandlerList getHandlers() {
         throw new IllegalStateException();
      }
   }
}
