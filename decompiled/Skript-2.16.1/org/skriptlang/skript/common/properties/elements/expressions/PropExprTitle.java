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

@Name("Title")
@Description({"Represents the title of something.\n"})
@Example.Examples({@Example("set title of event-bossbar to \"<red>hello!\"\n"), @Example("on inventory click:\n\tif type of event-item is written book:\n\t\tset title of event-item to formatted \"<obf>%random uuid%\"\n\t\tsend \"Your book's title is now forever forgotten..\" to player\n")})
@Since({"2.16"})
@RelatedProperty("title")
public class PropExprTitle extends PropertyBaseExpression<ExpressionPropertyHandler<?, ?>> {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(PropExprTitle.class, Object.class, "title[s]", "objects", false)
               .supplier(PropExprTitle::new))
            .build()
      );
   }

   @Override
   public Property<ExpressionPropertyHandler<?, ?>> getProperty() {
      return Property.TITLE;
   }
}
