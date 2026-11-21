package org.skriptlang.skript.bukkit.text.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class AudienceClassInfo extends ClassInfo<Audience> {
   public AudienceClassInfo() {
      super(Audience.class, "audience");
      this.user(new String[]{"audiences?"})
         .name("Audience")
         .description(
            "An audience is a receiver of media, such as individual players, the console, or groups of players (such as those on a team or in a world)."
         )
         .examples("send \"Hello world!\" to the player", "send action bar \"ALERT! A horde of zombies has overrun the central village.\" to the world")
         .since("2.15")
         .defaultExpression(new EventValueExpression<>(CommandSender.class))
         .parser(new AudienceClassInfo.AudienceParser());
   }

   private static final class AudienceParser extends Parser<Audience> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(Audience audience, int flags) {
         return "audience";
      }

      public String toVariableNameString(Audience audience) {
         return "audience:" + audience.hashCode();
      }
   }
}
