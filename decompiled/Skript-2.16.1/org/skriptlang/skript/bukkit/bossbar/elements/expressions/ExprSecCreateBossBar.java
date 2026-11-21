package org.skriptlang.skript.bukkit.bossbar.elements.expressions;

import ch.njol.skript.bukkitutil.NamespacedUtils;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.util.Color;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.bossbar.BossBarUtils;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValue;
import org.skriptlang.skript.bukkit.lang.eventvalue.EventValueRegistry;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Create Boss Bar")
@Description({"Creates a new boss bar.\nBoss bars can be pink/blue/red/green/yellow/purple/white, and will pick the closest valid color to the one you provide.\nBoss bars can have viewers removed or added to them.\nMaking the boss bar 'keyed' will add it to the persistent storage of the server and will be editable by commands and restored after restart.\n"})
@Example.Examples({@Example("on join:\n\tset {_bar} to a boss bar:\n\t\tset color of event-bossbar to white\n\t\tset title of event-bossbar to \"<green>Welcome %player%!\"\n\t\tset progress of event-bossbar to 50%\n\t\tset style of event-bossbar to 6 notches\n\t\tmake event-bossbar darken the sky\n\tadd player to viewers of {_bar}\n\twait 5 seconds\n\tremove player from viewers of {_bar}\n"), @Example("command /createkeyedbar:\n    set {_bar} to a red keyed bossbar with title \"<red>My persistent red bar\":\n        set progress of event-bossbar to 14%\n        set style of event-bossbar to 20 notches\n    loop all operators:\n        add loop-value to viewers of {_bar}\n")})
@Since({"2.16"})
public class ExprSecCreateBossBar extends SectionExpression<BossBar> {
   private Trigger trigger = null;
   private Expression<String> key;
   private Expression<Color> color;
   private Expression<Component> title;
   private boolean isKeyed;

   public static void register(SyntaxRegistry syntaxRegistry, EventValueRegistry eventValueRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprSecCreateBossBar.class, BossBar.class
                  )
                  .addPatterns(
                     new String[]{
                        "[a] [new] [%-color%] boss[ ]bar [(with title|titled) %-textcomponent%]",
                        "[a] [new] keyed [%-color%] boss[ ]bar with (id|key) %string% [(with title|titled) %-textcomponent%]"
                     }
                  ))
               .supplier(ExprSecCreateBossBar::new))
            .build()
      );
      eventValueRegistry.register(
         EventValue.builder(ExprSecCreateBossBar.CreateBossBarEvent.class, BossBar.class).getter(ExprSecCreateBossBar.CreateBossBarEvent::getBossBar).build()
      );
   }

   @Override
   public boolean init(
      Expression<?>[] expressions,
      int matchedPattern,
      Kleenean delayed,
      SkriptParser.ParseResult result,
      @Nullable SectionNode node,
      @Nullable List<TriggerItem> triggerItems
   ) {
      this.color = (Expression<Color>)expressions[0];
      if (matchedPattern == 1) {
         this.isKeyed = true;
         this.key = (Expression<String>)expressions[1];
      } else {
         this.isKeyed = false;
      }

      this.title = (Expression<Component>)expressions[expressions.length - 1];
      if (node != null) {
         this.trigger = SectionUtils.loadLinkedCode(
            "create bossbar",
            (beforeLoading, afterLoading) -> this.loadCode(
               node, "create bossbar", beforeLoading, afterLoading, new Class[]{ExprSecCreateBossBar.CreateBossBarEvent.class}
            )
         );
         return this.trigger != null;
      } else {
         return true;
      }
   }

   @Nullable
   protected BossBar[] get(Event event) {
      Color color = null;
      if (this.color != null) {
         color = this.color.getSingle(event);
      }

      BarColor barColor = color != null && BossBarUtils.nearest(color) != null ? BossBarUtils.nearest(color) : BarColor.WHITE;
      if (barColor == null) {
         return new BossBar[0];
      } else {
         String legacyTitle = null;
         if (this.title != null) {
            Component title = this.title.getSingle(event);
            if (title == null) {
               return new BossBar[0];
            }

            legacyTitle = LegacyComponentSerializer.legacySection().serialize(title);
         }

         BossBar bar;
         if (this.isKeyed) {
            String stringKey = this.key.getSingle(event);
            if (stringKey == null) {
               return new BossBar[0];
            }

            NamespacedKey key = NamespacedUtils.checkValidationAndSend(stringKey, this);
            if (key == null) {
               return new BossBar[0];
            }

            Bukkit.createBossBar(key, legacyTitle, barColor, BarStyle.SOLID, new BarFlag[0]);
            bar = Bukkit.getBossBar(key);
         } else {
            bar = Bukkit.createBossBar(legacyTitle, barColor, BarStyle.SOLID, new BarFlag[0]);
         }

         if (this.trigger == null) {
            return new BossBar[]{bar};
         } else {
            ExprSecCreateBossBar.CreateBossBarEvent bossbarEvent = new ExprSecCreateBossBar.CreateBossBarEvent(bar);
            Variables.withLocalVariables(event, bossbarEvent, () -> TriggerItem.walk(this.trigger, bossbarEvent));
            return new BossBar[]{bossbarEvent.getBossBar()};
         }
      }
   }

   @Override
   public boolean isSingle() {
      return true;
   }

   @Override
   public Class<? extends BossBar> getReturnType() {
      return this.key != null ? KeyedBossBar.class : BossBar.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return new SyntaxStringBuilder(event, debug)
         .append("a")
         .appendIf(this.key != null, "keyed")
         .appendIf(this.color != null, this.color)
         .append("boss bar")
         .appendIf(this.key != null, "with key", this.key)
         .appendIf(this.title != null, "with title", this.title)
         .toString();
   }

   private static class CreateBossBarEvent extends Event {
      private final BossBar bar;

      public CreateBossBarEvent(BossBar bar) {
         this.bar = bar;
      }

      public BossBar getBossBar() {
         return this.bar;
      }

      @NotNull
      public HandlerList getHandlers() {
         throw new IllegalStateException();
      }
   }
}
