package org.skriptlang.skript.bukkit.entity.displays.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Display;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Display Brightness")
@Description({"Returns or changes the brightness override of <a href='#display'>displays</a>.", "Unmodified displays will not have a brightness override value set. Resetting or deleting this value will remove the override.", "Use the 'block' or 'sky' options to get/change specific values or get both values as a list by using neither option.", "NOTE: setting only one of the sky/block light overrides of a display without an existing override will set both sky and block light to the given value. Make sure to set both block and sky levels to your desired values for the best results. Likewise, you can only clear the brightness override, you cannot clear/reset the sky/block values individually."})
@Example.Examples({@Example("set sky light override of the last spawned text display to 7"), @Example("subtract 3 from the block light level override of the last spawned text display"), @Example("if sky light level override of {_display} is 5:\n\tclear brightness override of {_display}\n")})
@Since({"2.10"})
public class ExprDisplayBrightness extends SimpleExpression<Integer> {
   @UnknownNullability
   private Expression<Display> displays;
   private boolean blockLight;
   private boolean skyLight;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)PropertyExpression.infoBuilder(
                  ExprDisplayBrightness.class, Integer.class, "[:block|:sky] (light [level]|brightness) override[s]", "displays", false
               )
               .supplier(ExprDisplayBrightness::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.blockLight = parseResult.hasTag("block");
      this.skyLight = parseResult.hasTag("sky");
      this.displays = (Expression<Display>)expressions[0];
      return true;
   }

   @Nullable
   protected Integer[] get(Event event) {
      List<Integer> values = new ArrayList<>();
      if (this.skyLight) {
         for (Display display : this.displays.getArray(event)) {
            Brightness brightness = display.getBrightness();
            if (brightness != null) {
               values.add(brightness.getSkyLight());
            }
         }
      } else if (this.blockLight) {
         for (Display displayx : this.displays.getArray(event)) {
            Brightness brightness = displayx.getBrightness();
            if (brightness != null) {
               values.add(brightness.getBlockLight());
            }
         }
      } else {
         for (Display displayxx : this.displays.getArray(event)) {
            Brightness brightness = displayxx.getBrightness();
            if (brightness != null) {
               values.add(brightness.getBlockLight());
               values.add(brightness.getSkyLight());
            }
         }
      }

      return values.toArray(new Integer[0]);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (!this.skyLight && !this.blockLight) {
         return switch (mode) {
            case SET, RESET, DELETE -> (Class[])CollectionUtils.array(Number.class);
            default -> null;
         };
      } else {
         return switch (mode) {
            case ADD, REMOVE, SET -> (Class[])CollectionUtils.array(Number.class);
            default -> null;
         };
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (!this.skyLight && !this.blockLight) {
         Brightness change = null;
         if (delta != null) {
            int value = Math2.fit(0, ((Number)delta[0]).intValue(), 15);
            change = new Brightness(value, value);
         }

         for (Display display : this.displays.getArray(event)) {
            display.setBrightness(change);
         }
      } else {
         int level = delta == null ? 0 : ((Number)delta[0]).intValue();
         switch (mode) {
            case REMOVE:
               level = -level;
            case ADD:
               for (Display displayx : this.displays.getArray(event)) {
                  Brightness brightness = displayx.getBrightness();
                  if (brightness == null) {
                     int clamped = Math2.fit(0, level, 15);
                     displayx.setBrightness(new Brightness(clamped, clamped));
                  } else if (this.skyLight) {
                     int clamped = Math2.fit(0, level + brightness.getSkyLight(), 15);
                     displayx.setBrightness(new Brightness(brightness.getBlockLight(), clamped));
                  } else {
                     int clamped = Math2.fit(0, level + brightness.getBlockLight(), 15);
                     displayx.setBrightness(new Brightness(clamped, brightness.getSkyLight()));
                  }
               }
               break;
            case SET:
               for (Display display : this.displays.getArray(event)) {
                  Brightness brightness = display.getBrightness();
                  int clamped = Math2.fit(0, level, 15);
                  if (brightness == null) {
                     display.setBrightness(new Brightness(clamped, clamped));
                  } else if (this.skyLight) {
                     display.setBrightness(new Brightness(brightness.getBlockLight(), clamped));
                  } else {
                     display.setBrightness(new Brightness(clamped, brightness.getSkyLight()));
                  }
               }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return (this.skyLight || this.blockLight) && this.displays.isSingle();
   }

   @Override
   public Class<? extends Integer> getReturnType() {
      return Integer.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      if (this.skyLight) {
         return "sky light override of " + this.displays.toString(event, debug);
      } else {
         return this.blockLight
            ? "block light override of " + this.displays.toString(event, debug)
            : "brightness override of " + this.displays.toString(event, debug);
      }
   }
}
