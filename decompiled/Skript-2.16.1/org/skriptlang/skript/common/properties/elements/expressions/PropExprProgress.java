package org.skriptlang.skript.common.properties.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseExpression;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Progress")
@Description({"Represents the progress of something.\n"})
@Example("set progress of event-bossbar to 75%\n")
@Since({"2.16"})
@RelatedProperty("progress")
public class PropExprProgress extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(PropExprProgress.class, Object.class, "progress", "objects", false)
               .supplier(PropExprProgress::new))
            .build()
      );
   }

   @Override
   public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
      return Property.PROGRESS;
   }
}
