package org.skriptlang.skript.bukkit.text.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Message")
@Description({"Sends a message to an audience, such as a player or the console.", "Only styles written in given string or in <a href=#ExprColored>formatted expressions</a> will be parsed."})
@Example.Examples({@Example("message \"A wild %player% appeared!\""), @Example("message \"This message is a distraction. Mwahaha!\""), @Example("send \"Your kill streak is %{kill streak::%uuid of player%}%.\" to player"), @Example("if the targeted entity exists:\n\tmessage \"You're currently looking at a %type of the targeted entity%!\"\n")})
@Since({"1.0", "2.2-dev26 (advanced features)", "2.6 (support for sending anything)"})
public class EffMessage extends Effect {
   private Expression<? extends Component> messages;
   private Expression<Audience> recipients;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffMessage.class).supplier(EffMessage::new).addPattern("(message|send [message[s]]) %objects% [to %audiences%]").build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.messages = TextComponentUtils.asComponentExpression(expressions[0]);
      if (this.messages == null) {
         return false;
      } else {
         this.recipients = (Expression<Audience>)expressions[1];
         return true;
      }
   }

   @Override
   protected void execute(Event event) {
      Audience audience = Audience.audience(this.recipients.getArray(event));

      for (Component component : this.messages.getArray(event)) {
         audience.sendMessage(component);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("message", this.messages);
      if (this.recipients != null) {
         builder.append("to", this.recipients);
      }

      return builder.toString();
   }
}
