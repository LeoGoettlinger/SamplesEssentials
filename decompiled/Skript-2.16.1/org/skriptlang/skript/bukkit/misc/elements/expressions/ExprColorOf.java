package org.skriptlang.skript.bukkit.misc.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Colorable;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.bossbar.BossBarUtils;
import org.skriptlang.skript.bukkit.entity.displays.DisplayData;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Color of")
@Description({"The <a href='#color'>color</a> of an item, entity, block, firework effect, or text display.", "This can also be used to color chat messages with \"&lt;%color of ...%&gt;this text is colored!\".", "Do note that firework effects support setting, adding, removing, resetting, and deleting; text displays support setting and resetting; and items, entities, and blocks only support setting, and only for very few items/blocks."})
@Example("on click on wool:\n\tif event-block is tagged with minecraft tag \"wool\":\n\t\tmessage \"This wool block is <%color of block%>%color of block%<reset>!\"\n\t\tset the color of the block to black\n")
@Since({"1.2, 2.10 (displays), 2.16 (boss bars)"})
public class ExprColorOf extends PropertyExpression<Object, Color> {
   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)infoBuilder(
                  ExprColorOf.class, Color.class, "colo[u]r[s]", "blocks/itemtypes/entities/fireworkeffects/potioneffecttypes/displays/bossbars", false
               )
               .supplier(ExprColorOf::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends Object>)exprs[0]);
      return true;
   }

   protected Color[] get(Event event, Object[] source) {
      if (!(source instanceof FireworkEffect[])) {
         return this.get(source, object -> {
            if (object instanceof Display) {
               if (object instanceof TextDisplay display) {
                  if (display.isDefaultBackground()) {
                     return ColorRGB.fromBukkitColor(DisplayData.DEFAULT_BACKGROUND_COLOR);
                  } else {
                     org.bukkit.Color bukkitColor = display.getBackgroundColor();
                     return bukkitColor == null ? null : ColorRGB.fromBukkitColor(bukkitColor);
                  }
               } else {
                  return null;
               }
            } else {
               return object instanceof BossBar bar ? BossBarUtils.rgbFromBarColor(bar.getColor()) : this.getColor(object);
            }
         });
      } else {
         List<Color> colors = new ArrayList<>();

         for (FireworkEffect effect : (FireworkEffect[])source) {
            effect.getColors().stream().map(ColorRGB::fromBukkitColor).forEach(colors::add);
         }

         return colors.toArray(new Color[0]);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      Expression<?> expression = this.getExpr();
      if (expression.canReturn(FireworkEffect.class)) {
         return CollectionUtils.array(Color[].class);
      } else if ((mode != Changer.ChangeMode.RESET && mode != Changer.ChangeMode.SET || !expression.canReturn(Display.class))
         && !expression.canReturn(BossBar.class)) {
         return mode != Changer.ChangeMode.SET
               || !expression.canReturn(Entity.class) && !expression.canReturn(Block.class) && !expression.canReturn(ItemType.class)
            ? null
            : CollectionUtils.array(Color.class);
      } else {
         return CollectionUtils.array(Color.class);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Color[] colors = delta != null ? (Color[])delta : null;
      Consumer<TextDisplay> displayChanger = this.getDisplayChanger(mode, colors);
      Consumer<FireworkEffect> fireworkChanger = this.getFireworkChanger(mode, colors);

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof BossBar bar) {
            if (mode == Changer.ChangeMode.SET) {
               BarColor barColor = BossBarUtils.nearest(colors[0]);
               if (barColor == null) {
                  this.error("Could not round to a color that is similar to " + Classes.toString(colors[0]));
               } else {
                  bar.setColor(barColor);
               }
            } else if (mode == Changer.ChangeMode.RESET) {
               bar.setColor(BarColor.WHITE);
            }
         } else if (object instanceof TextDisplay display) {
            displayChanger.accept(display);
         } else if (object instanceof FireworkEffect effect) {
            fireworkChanger.accept(effect);
         } else if (mode != Changer.ChangeMode.SET || !(object instanceof Block) && !(object instanceof Colorable)) {
            if (mode == Changer.ChangeMode.SET && (object instanceof Item || object instanceof ItemType)) {
               assert colors[0] != null;

               ItemStack stack = object instanceof Item ? ((Item)object).getItemStack() : ((ItemType)object).getRandom();
               if (stack != null) {
                  MaterialData data = stack.getData();
                  if (data instanceof Colorable colorable) {
                     colorable.setColor(colors[0].asDyeColor());
                     stack.setData(data);
                     if (object instanceof Item item) {
                        item.setItemStack(stack);
                     }
                  }
               }
            }
         } else {
            assert colors[0] != null;

            Colorable colorablex = this.getColorable(object);
            if (colorablex != null) {
               try {
                  colorablex.setColor(colors[0].asDyeColor());
               } catch (UnsupportedOperationException var18) {
                  Skript.error(
                     "Tried setting the color of a bed, but this isn't possible in your Minecraft version, since different colored beds are different materials. Instead, set the block to right material, such as a blue bed."
                  );
               }
            } else if (object instanceof Block block && block.getState() instanceof Banner banner) {
               banner.setBaseColor(colors[0].asDyeColor());
            }
         }
      }
   }

   @Override
   public Class<? extends Color> getReturnType() {
      return Color.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "color of " + this.getExpr().toString(event, debug);
   }

   private Consumer<TextDisplay> getDisplayChanger(Changer.ChangeMode mode, @Nullable Color[] colors) {
      Color color = colors != null && colors.length == 1 ? colors[0] : null;

      return switch (mode) {
         case RESET -> display -> display.setDefaultBackground(true);
         case SET -> display -> {
            if (color != null) {
               if (display.isDefaultBackground()) {
                  display.setDefaultBackground(false);
               }

               display.setBackgroundColor(color.asBukkitColor());
            }
         };
         default -> display -> {};
      };
   }

   private Consumer<FireworkEffect> getFireworkChanger(Changer.ChangeMode mode, @Nullable Color[] colors) {
      return switch (mode) {
         case RESET, DELETE -> effect -> effect.getColors().clear();
         case SET -> effect -> {
            effect.getColors().clear();

            for (Color color : colors) {
               effect.getColors().add(color.asBukkitColor());
            }
         };
         case ADD -> effect -> {
            for (Color color : colors) {
               effect.getColors().add(color.asBukkitColor());
            }
         };
         case REMOVE, REMOVE_ALL -> effect -> {
            for (Color color : colors) {
               effect.getColors().remove(color.asBukkitColor());
            }
         };
         default -> effect -> {};
      };
   }

   @Nullable
   private Colorable getColorable(Object colorable) {
      if (!(colorable instanceof Item) && !(colorable instanceof ItemType)) {
         if (colorable instanceof Block) {
            BlockState state = ((Block)colorable).getState();
            if (state instanceof Colorable) {
               return (Colorable)state;
            }
         } else if (colorable instanceof Colorable) {
            return (Colorable)colorable;
         }
      } else {
         ItemStack item = colorable instanceof Item ? ((Item)colorable).getItemStack() : ((ItemType)colorable).getRandom();
         if (item == null) {
            return null;
         }

         MaterialData data = item.getData();
         if (data instanceof Colorable) {
            return (Colorable)data;
         }
      }

      return null;
   }

   @Nullable
   private Color getColor(Object object) {
      Colorable colorable = this.getColorable(object);
      if (colorable != null) {
         DyeColor dyeColor = colorable.getColor();
         return dyeColor == null ? null : SkriptColor.fromDyeColor(dyeColor);
      } else if (object instanceof Block block && block.getState() instanceof Banner banner) {
         return SkriptColor.fromDyeColor(banner.getBaseColor());
      } else {
         return object instanceof PotionEffectType potionEffectType ? ColorRGB.fromBukkitColor(potionEffectType.getColor()) : null;
      }
   }
}
