package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.util.coll.CollectionUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.KeyedBossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

public class BossBarClassInfo extends ClassInfo<BossBar> {
   public BossBarClassInfo(SkriptAddon addon) {
      super(BossBar.class, "bossbar");
      this.user(new String[]{"boss ?bars?"})
         .name("BossBar")
         .description(
            "Represents a boss bar.\nA boss bar is displayed at the top of a players screen and can have its title, progress, style, color, and viewers changed.\n"
         )
         .since("2.16")
         .parser(new BossBarClassInfo.BossBarParser())
         .changer(new BossBarClassInfo.BossBarChangeHandler())
         .defaultExpression(new EventValueExpression<>(BossBar.class))
         .property(
            Property.TITLE,
            "The title of a boss bar.\nNote that due to Bukkit limitations formatting like shadow, sprites or fonts will not work.\n",
            addon,
            new BossBarClassInfo.BossBarTitleHandler()
         )
         .property(
            Property.NAME,
            "The name of a boss bar.\nNote that due to Bukkit limitations formatting like shadow, sprites or fonts will not work.\n",
            addon,
            new BossBarClassInfo.BossBarTitleHandler()
         )
         .property(Property.PROGRESS, "The progress of a boss bar. Ranges from 0 to 1.", addon, new BossBarClassInfo.BossBarProgressHandler())
         .property(Property.STYLE, "The style of a boss bar.", addon, new BossBarClassInfo.BossBarStyleHandler())
         .property(
            Property.VIEWERS,
            "The viewers of a boss bar.\nIf you remove a player from viewers of a boss bar they will no longer see the bossbar or be affected by its flags.\n",
            addon,
            new BossBarClassInfo.BossBarViewersHandler()
         );
   }

   private static class BossBarChangeHandler implements Changer<BossBar> {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.DELETE ? CollectionUtils.array() : null;
      }

      public void change(BossBar[] bars, @Nullable Object[] delta, Changer.ChangeMode mode) {
         for (BossBar bar : bars) {
            bar.removeAll();
            if (bar instanceof KeyedBossBar keyed) {
               Bukkit.removeBossBar(keyed.getKey());
            }
         }
      }
   }

   private static class BossBarParser extends Parser<BossBar> {
      @Override
      public boolean canParse(ParseContext context) {
         return false;
      }

      public String toString(BossBar bar, int flags) {
         boolean emptyTitle = bar.getTitle().isEmpty();
         return emptyTitle ? "boss bar" : "boss bar titled '" + bar.getTitle() + "'";
      }

      public String toVariableNameString(BossBar bar) {
         return this.toString(bar, 0);
      }
   }

   private static class BossBarProgressHandler implements ExpressionPropertyHandler<BossBar, Double> {
      @Nullable
      public Double convert(BossBar bar) {
         return bar.getProgress();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, RESET, ADD, REMOVE -> (Class[])CollectionUtils.array(Double.class);
            default -> null;
         };
      }

      public void change(BossBar bar, @Nullable Object[] delta, Changer.ChangeMode mode) {
         double progress = delta == null ? 0.0 : (Double)delta[0];
         if (mode == Changer.ChangeMode.ADD) {
            progress += bar.getProgress();
         } else if (mode == Changer.ChangeMode.REMOVE) {
            progress -= bar.getProgress();
         }

         bar.setProgress(Math.clamp(progress, 0.0, 1.0));
      }

      @NotNull
      @Override
      public Class<Double> returnType() {
         return Double.class;
      }
   }

   private static class BossBarStyleHandler implements ExpressionPropertyHandler<BossBar, BarStyle> {
      @Nullable
      public BarStyle convert(BossBar bar) {
         return bar.getStyle();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, RESET -> (Class[])CollectionUtils.array(BarStyle.class);
            default -> null;
         };
      }

      public void change(BossBar bar, @Nullable Object[] delta, Changer.ChangeMode mode) {
         BarStyle style = delta == null ? BarStyle.SOLID : (BarStyle)delta[0];
         bar.setStyle(style);
      }

      @NotNull
      @Override
      public Class<BarStyle> returnType() {
         return BarStyle.class;
      }
   }

   private static class BossBarTitleHandler implements ExpressionPropertyHandler<BossBar, Component> {
      @Nullable
      public Component convert(BossBar bar) {
         return LegacyComponentSerializer.legacySection().deserialize(bar.getTitle());
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Component.class);
            default -> null;
         };
      }

      public void change(BossBar bar, @Nullable Object[] delta, Changer.ChangeMode mode) {
         String title = delta == null ? null : LegacyComponentSerializer.legacySection().serialize((Component)delta[0]);
         bar.setTitle(title);
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class BossBarViewersHandler implements ExpressionPropertyHandler<BossBar, Object> {
      public Player[] convert(BossBar bar) {
         return bar.getPlayers().toArray(Player[]::new);
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> (Class[])CollectionUtils.array(Player[].class);
            default -> null;
         };
      }

      public void change(BossBar bar, @Nullable Object[] delta, Changer.ChangeMode mode) {
         switch (mode) {
            case SET:
               bar.removeAll();

               assert delta != null;

               for (Object player : delta) {
                  bar.addPlayer((Player)player);
               }
               break;
            case DELETE:
            case RESET:
               bar.removeAll();
               break;
            case ADD:
               assert delta != null;

               for (Object player : delta) {
                  bar.addPlayer((Player)player);
               }
               break;
            case REMOVE:
               assert delta != null;

               for (Object player : delta) {
                  bar.removePlayer((Player)player);
               }
         }
      }

      @NotNull
      @Override
      public Class<Object> returnType() {
         return Player.class;
      }
   }
}
