package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffChange;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParsingStack;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Iterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptWarning;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Chat Format")
@Description({"Can be used to modify the chat format.\nThe sender of a message is represented by player.\nThe message is represented by message.\n"})
@Example("set the chat format to \"<yellow>%player%<light gray>: <green>%message%\"")
@Since({"2.2-dev31"})
@Events({"chat"})
public class ExprChatFormat extends SimpleExpression<Component> implements EventRestrictedSyntax {
   private boolean suppressDeprecatedWarning;
   private boolean containsPlaceholders;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprChatFormat.class, Component.class
                  )
                  .supplier(ExprChatFormat::new))
               .addPattern("[the] (message|chat) format[ting]"))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      Iterator<ParsingStack.Element> stackIterator = this.getParser().getParsingStack().iterator();
      if (stackIterator.hasNext() && stackIterator.next().getSyntaxElementClass() == EffChange.class) {
         Script script = this.getParser().getCurrentScript();
         if (script != null) {
            this.suppressDeprecatedWarning = script.suppressesWarning(ScriptWarning.DEPRECATED_SYNTAX);
         }

         return true;
      } else {
         Skript.error("'" + this.toString(null, false) + "' can only be changed, not obtained");
         return false;
      }
   }

   protected Component[] get(Event event) {
      this.error("'" + this.toString(null, false) + "' cannot be obtained. Returning <none>.");
      return new Component[0];
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (this.getParser().getHasDelayBefore().isTrue()) {
         Skript.error("'" + this.toString(null, false) + "' can't be changed after the event has passed");
         return null;
      } else {
         return switch (mode) {
            case SET, RESET -> new Class[]{Component.class};
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof AsyncChatEvent asyncChatEvent) {
         if (delta == null) {
            asyncChatEvent.renderer(
               ChatRenderer.viewerUnaware(
                  (source, sourceDisplayName, message) -> Component.translatable("chat.type.text", new ComponentLike[]{sourceDisplayName, message})
               )
            );
         } else {
            asyncChatEvent.renderer(
               ChatRenderer.viewerUnaware(
                  (source, sourceDisplayName, message) -> ((Component)delta[0])
                     .replaceText(
                        (TextReplacementConfig)TextReplacementConfig.builder()
                           .match("(?i)\\[(player|sender|message|msg)]")
                           .replacement((matchResult, builder) -> {
                              this.containsPlaceholders = true;
                              return matchResult.group(1).startsWith("m") ? message : sourceDisplayName;
                           })
                           .build()
                     )
               )
            );
            if (this.containsPlaceholders && !this.suppressDeprecatedWarning) {
               this.containsPlaceholders = false;
               this.warning(
                  "Using [player], [sender], [message] and [msg] is deprecated and scheduled for removal. Please use the equivalent expressions, such as '%player%' and '%message%'."
               );
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the chat format";
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return new Class[]{AsyncChatEvent.class};
   }
}
