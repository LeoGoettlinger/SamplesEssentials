package org.skriptlang.skript.bukkit.text.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import java.time.Duration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.title.Title.Times;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Title - Send")
@Description({"Sends a title and/or subtitle to an audience with an optional fade in, stay, and/or fade out time.", "If sending only the subtitle, it will only be shown if the audience currently has a title displayed. Otherwise, it will be shown when the audience is next shown a title.", "Additionally, if no input is given for the times, the previous times of the last sent title will be used (or default values). Use the <a href='#EffResetTitle'>reset title</a> effect to restore the default values for the times."})
@Example.Examples({@Example("send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds"), @Example("send title \"Hi %player%\" to player"), @Example("send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players"), @Example("send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player for 5 seconds with fadein 1 second and fade out 1 second"), @Example("send subtitle \"Party!\" to all players")})
@Since({"2.3", "2.15 (support for showing anything)"})
public class EffSendTitle extends Effect {
   @Nullable
   private Expression<? extends Component> title;
   @Nullable
   private Expression<? extends Component> subtitle;
   private Expression<Audience> audiences;
   @Nullable
   private Expression<Timespan> fadeIn;
   @Nullable
   private Expression<Timespan> stay;
   @Nullable
   private Expression<Timespan> fadeOut;

   public static void register(SyntaxRegistry syntaxRegistry) {
      String suffix = "[to %audiences%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]";
      syntaxRegistry.register(
         SyntaxRegistry.EFFECT,
         SyntaxInfo.builder(EffSendTitle.class)
            .supplier(EffSendTitle::new)
            .addPatterns("send title %object% [with subtitle %-object%] " + suffix, "send subtitle %object% " + suffix)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (matchedPattern == 0) {
         this.title = TextComponentUtils.asComponentExpression(exprs[0]);
         if (this.title == null) {
            return false;
         }
      }

      Expression<?> subtitle = exprs[1 - matchedPattern];
      if (subtitle != null) {
         this.subtitle = TextComponentUtils.asComponentExpression(subtitle);
         if (this.subtitle == null) {
            return false;
         }
      }

      this.audiences = (Expression<Audience>)exprs[2 - matchedPattern];
      this.stay = (Expression<Timespan>)exprs[3 - matchedPattern];
      this.fadeIn = (Expression<Timespan>)exprs[4 - matchedPattern];
      this.fadeOut = (Expression<Timespan>)exprs[5 - matchedPattern];
      return true;
   }

   @Override
   protected void execute(Event event) {
      Component title = null;
      if (this.title != null) {
         title = this.title.getSingle(event);
         if (title == null) {
            return;
         }
      }

      Component subtitle = null;
      if (this.subtitle != null) {
         subtitle = this.subtitle.getSingle(event);
         if (subtitle == null) {
            return;
         }
      }

      boolean specifiesTimes = false;
      Duration stay;
      if (this.stay == null) {
         stay = Title.DEFAULT_TIMES.stay();
      } else {
         Timespan stayTimespan = this.stay.getSingle(event);
         if (stayTimespan == null) {
            return;
         }

         stay = stayTimespan.getDuration();
         specifiesTimes = true;
      }

      Duration fadeIn;
      if (this.fadeIn == null) {
         fadeIn = Title.DEFAULT_TIMES.fadeIn();
      } else {
         Timespan fadeInTimespan = this.fadeIn.getSingle(event);
         if (fadeInTimespan == null) {
            return;
         }

         fadeIn = fadeInTimespan.getDuration();
         specifiesTimes = true;
      }

      Duration fadeOut;
      if (this.fadeOut == null) {
         fadeOut = Title.DEFAULT_TIMES.fadeOut();
      } else {
         Timespan fadeOutTimespan = this.fadeOut.getSingle(event);
         if (fadeOutTimespan == null) {
            return;
         }

         fadeOut = fadeOutTimespan.getDuration();
         specifiesTimes = true;
      }

      Audience audience = Audience.audience(this.audiences.getArray(event));
      if (specifiesTimes) {
         audience.sendTitlePart(TitlePart.TIMES, Times.times(fadeIn, stay, fadeOut));
      }

      if (subtitle != null) {
         audience.sendTitlePart(TitlePart.SUBTITLE, subtitle);
      }

      if (title != null) {
         audience.sendTitlePart(TitlePart.TITLE, title);
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      builder.append("send");
      if (this.title != null) {
         builder.append("title", this.title);
      }

      if (this.subtitle != null) {
         if (this.title != null) {
            builder.append("with");
         }

         builder.append("subtitle", this.subtitle);
      }

      builder.append("to", this.audiences);
      if (this.stay != null) {
         builder.append("for", this.stay);
      }

      if (this.fadeIn != null) {
         builder.append("with fade in", this.fadeIn);
      }

      if (this.fadeOut != null) {
         builder.append("with fade out", this.fadeOut);
      }

      return builder.toString();
   }
}
