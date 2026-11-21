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

@Name("Viewers")
@Description({"Represents something that is viewing something.\n"})
@Example.Examples({@Example("add {_player} to viewers of event-bossbar\n"), @Example("on inventory open:\n\tset {_mylist::*} to viewers of event-inventory\n\tsend \"%size of {_list::*}% other players are viewing this inventory!\"\n")})
@Since({"2.16"})
@RelatedProperty("viewers")
public class PropExprViewers extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(PropExprViewers.class, Object.class, "viewer[s]", "objects", false)
               .supplier(PropExprViewers::new))
            .build()
      );
   }

   @Override
   public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
      return Property.VIEWERS;
   }
}
