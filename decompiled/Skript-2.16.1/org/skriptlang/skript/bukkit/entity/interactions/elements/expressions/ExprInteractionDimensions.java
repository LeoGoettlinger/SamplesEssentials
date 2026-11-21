package org.skriptlang.skript.bukkit.entity.interactions.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Interaction Height/Width")
@Description({"Returns the height or width of an interaction entity's hitbox. Both default to 1.\nThe width of the hitbox determines the x/z widths\n"})
@Example.Examples({@Example("set interaction height of last spawned interaction to 5.3"), @Example("set interaction width of last spawned interaction to 2")})
@Since({"2.14"})
public class ExprInteractionDimensions extends SimplePropertyExpression<Entity, Number> {
   private boolean width;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(ExprInteractionDimensions.class, Number.class, "interaction (height|:width)[s]", "entities", true)
               .supplier(ExprInteractionDimensions::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.width = parseResult.hasTag("width");
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Number convert(Entity entity) {
      return entity instanceof Interaction interaction ? this.width ? interaction.getInteractionWidth() : interaction.getInteractionHeight() : null;
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, RESET -> new Class[]{Number.class};
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Entity[] entities = this.getExpr().getArray(event);
      if (entities.length != 0) {
         float deltaValue = delta == null ? 1.0F : ((Number)delta[0]).floatValue();

         for (Entity entity : entities) {
            if (entity instanceof Interaction interaction) {
               switch (mode) {
                  case REMOVE:
                     deltaValue = -deltaValue;
                  case ADD:
                     if (this.width) {
                        interaction.setInteractionWidth(Math.max(interaction.getInteractionWidth() + deltaValue, 0.0F));
                     } else {
                        interaction.setInteractionHeight(Math.max(interaction.getInteractionHeight() + deltaValue, 0.0F));
                     }
                     break;
                  case SET:
                  case RESET:
                     deltaValue = Math.max(deltaValue, 0.0F);
                     if (this.width) {
                        interaction.setInteractionWidth(deltaValue);
                     } else {
                        interaction.setInteractionHeight(deltaValue);
                     }
               }
            }
         }
      }
   }

   @Override
   public Class<? extends Number> getReturnType() {
      return Number.class;
   }

   @Override
   protected String getPropertyName() {
      return "interaction " + (this.width ? "width" : "height");
   }
}
