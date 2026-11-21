package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentUtils;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Player List Header and Footer")
@Description({"The message above and below the player list in the tab menu."})
@Example.Examples({@Example("set all players' tab list header to \"Welcome to the Server!\""), @Example("send \"%the player's tab list header%\" to player"), @Example("reset all players' tab list header")})
@Since({"2.4"})
@Keywords({"tablist", "tab list"})
public class ExprPlayerListHeaderFooter extends SimplePropertyExpression<Player, Component> {
   private boolean isFooter;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprPlayerListHeaderFooter.class, Component.class, "(player|tab)[ ]list (header|:footer) [text|message]", "players", false
               )
               .supplier(ExprPlayerListHeaderFooter::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.isFooter = parseResult.hasTag("footer");
      return super.init(exprs, matchedPattern, isDelayed, parseResult);
   }

   @Nullable
   public Component convert(Player player) {
      return this.isFooter ? player.playerListFooter() : player.playerListHeader();
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Component[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Component text = Component.empty();
      if (delta != null) {
         text = TextComponentUtils.joinByNewLine(Arrays.copyOf(delta, delta.length, Component[].class));
      }

      for (Player player : this.getExpr().getArray(event)) {
         if (this.isFooter) {
            player.sendPlayerListFooter(text);
         } else {
            player.sendPlayerListHeader(text);
         }
      }
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   protected String getPropertyName() {
      return "player list " + (this.isFooter ? "footer" : "header");
   }
}
