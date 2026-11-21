package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

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
import org.bukkit.entity.Display;
import org.bukkit.event.Event;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Transformation Rotation")
@Description({"Returns or changes the transformation rotation of <a href='#display'>displays</a>.", "The left rotation is applied first, with the right rotation then being applied based on the rotated axis."})
@Example("set left transformation rotation of last spawned block display to quaternion(1, 0, 0, 0) # reset block display")
@Since({"2.10"})
public class ExprDisplayTransformationRotation extends SimplePropertyExpression<Display, Quaternionf> {
   private boolean left;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprDisplayTransformationRotation.class, Quaternionf.class, "(:left|right) [transformation] rotation", "displays", true
               )
               .supplier(ExprDisplayTransformationRotation::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.left = parseResult.hasTag("left");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Quaternionf convert(Display display) {
      Transformation transformation = display.getTransformation();
      return this.left ? transformation.getLeftRotation() : transformation.getRightRotation();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, RESET -> (Class[])CollectionUtils.array(Quaternionf.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Quaternionf quaternion = null;
      if (mode == Changer.ChangeMode.RESET) {
         quaternion = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
      }

      if (delta != null) {
         quaternion = (Quaternionf)delta[0];
      }

      if (quaternion != null && quaternion.isFinite()) {
         for (Display display : this.getExpr().getArray(event)) {
            Transformation transformation = display.getTransformation();
            Transformation change;
            if (this.left) {
               change = new Transformation(transformation.getTranslation(), quaternion, transformation.getScale(), transformation.getRightRotation());
            } else {
               change = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), quaternion);
            }

            display.setTransformation(change);
         }
      }
   }

   @Override
   public Class<? extends Quaternionf> getReturnType() {
      return Quaternionf.class;
   }

   @Override
   protected String getPropertyName() {
      return (this.left ? "left" : "right") + " transformation rotation";
   }
}
