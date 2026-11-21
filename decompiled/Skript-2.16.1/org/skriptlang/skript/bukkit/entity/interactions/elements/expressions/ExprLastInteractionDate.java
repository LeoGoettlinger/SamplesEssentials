package org.skriptlang.skript.bukkit.entity.interactions.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Date;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Interaction.PreviousInteraction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionType;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Last Interaction Date")
@Description({"Returns the date of the last attack (left click), or interaction (right click) on an interaction entity\nUsing 'clicked on' will return the latest attack or interaction, whichever was more recent.\n"})
@Examples({"if the last time {_interaction} was clicked < 5 seconds ago"})
@Since({"2.14"})
public class ExprLastInteractionDate extends SimplePropertyExpression<Entity, Date> {
   private InteractionType interactionType;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprLastInteractionDate.class, Date.class
                  )
                  .addPatterns(new String[]{"[the] last (date|time)[s] [that|when] %entities% (were|was) (attacked|1:interacted with|2:clicked [on])"}))
               .supplier(ExprLastInteractionDate::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.interactionType = InteractionType.values()[parseResult.mark];
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Date convert(Entity entity) {
      if (entity instanceof Interaction interaction) {
         PreviousInteraction lastInteraction = switch (this.interactionType) {
            case ATTACK -> interaction.getLastAttack();
            case INTERACT -> interaction.getLastInteraction();
            case BOTH -> InteractionType.getLatest(interaction);
         };
         return lastInteraction == null ? null : new Date(lastInteraction.getTimestamp());
      } else {
         return null;
      }
   }

   @Override
   public Class<? extends Date> getReturnType() {
      return Date.class;
   }

   @Override
   protected String getPropertyName() {
      return "UNUSED";
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .append("the last date that")
         .append(this.getExpr())
         .append(this.getExpr().isSingle() ? "was" : "were")
         .append(switch (this.interactionType) {
            case ATTACK -> "attacked";
            case INTERACT -> "interacted with";
            case BOTH -> "clicked on";
         })
         .toString();
   }
}
