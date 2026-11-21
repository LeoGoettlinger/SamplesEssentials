package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.Nullable;

@Name("Custom Model Data")
@Description({"Get/set the custom model data of an item. Using just `custom model data` will return an integer. Items without model data will return 0.", "Since 1.21.4, custom model data instead consists of a list of numbers (floats), a list of booleans (flags), a list of strings, and a list of colours. Accessing and modifying these lists can be done type-by-type, or all at once with `complete custom model data`. This is the more accurate and recommended method of using custom model data."})
@Example.Examples({@Example("set custom model data of player's tool to 3\nset {_model} to custom model data of player's tool\n"), @Example("set custom model data colours of {_flag} to red, white, and blue\nadd 10.5 to the model data floats of {_flag}\n"), @Example("set the full custom model data of {_item} to 10, \"sword\", and rgb(100, 200, 30)\n")})
@RequiredPlugins({"Minecraft 1.21.4+ (floats/flags/strings/colours/full model data)"})
@Since({"2.5", "2.12 (floats/flags/strings/colours/full model data)"})
public class ExprCustomModelData extends PropertyExpression<ItemType, Object> {
   private static final boolean USE_NEW_CMD = Skript.classExists("org.bukkit.inventory.meta.components.CustomModelDataComponent");
   private ExprCustomModelData.CMDType dataType;
   private Class<?> returnType;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.dataType = ExprCustomModelData.CMDType.values()[parseResult.mark];
      this.returnType = Classes.getSuperClassInfo(this.dataType.types).getC();
      this.setExpr((Expression<? extends ItemType>)expressions[0]);
      return true;
   }

   protected Object[] get(Event event, ItemType[] source) {
      int var4 = source.length;
      byte var5 = 0;
      if (var5 < var4) {
         ItemType from = source[var5];
         ItemMeta meta = from.getItemMeta();
         if (this.dataType != ExprCustomModelData.CMDType.SINGLE_INT) {
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            IntFunction<Object[]> arrayConstructor = n -> (Object[])Array.newInstance(this.getReturnType(), n);

            return (Object[])(switch (this.dataType) {
               case SINGLE_INT -> throw new IllegalStateException("Unreachable state for SINGLE_INT!");
               case FLOATS -> component.getFloats().toArray(arrayConstructor);
               case FLAGS -> component.getFlags().toArray(arrayConstructor);
               case STRINGS -> component.getStrings().toArray(arrayConstructor);
               case COLORS -> (ColorRGB[])component.getColors().stream().map(ColorRGB::fromBukkitColor).toList().toArray(ColorRGB[]::new);
               case ALL -> {
                  List<Object> data = new ArrayList<>();
                  data.addAll(component.getFloats());
                  data.addAll(component.getFlags());
                  data.addAll(component.getStrings());
                  data.addAll(component.getColors().stream().map(ColorRGB::fromBukkitColor).toList());
                  yield data.toArray(arrayConstructor);
               }
            });
         } else {
            return !meta.hasCustomModelData() || USE_NEW_CMD && meta.getCustomModelDataComponent().getFloats().isEmpty()
               ? new Integer[]{0}
               : new Integer[]{meta.getCustomModelData()};
         }
      } else {
         return (Object[])Array.newInstance(this.getReturnType(), 0);
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, REMOVE, SET, DELETE, RESET -> {
            Class<?>[] arrayClasses = new Class[this.dataType.types.length];

            for (int i = 0; i < this.dataType.types.length; i++) {
               arrayClasses[i] = this.dataType.types[i].arrayType();
            }

            yield arrayClasses;
         }
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      for (ItemType item : this.getExpr().getArray(event)) {
         ItemMeta meta = item.getItemMeta();
         switch (this.dataType) {
            case SINGLE_INT:
               long deltaValue = delta == null ? 0L : ((Number)delta[0]).intValue();
               this.changeOld(meta, mode, deltaValue);
               break;
            case FLOATS:
            case FLAGS:
            case STRINGS:
            case COLORS:
               this.changeSingleType(meta, mode, delta);
               break;
            case ALL:
               this.changeAll(meta, mode, delta);
         }

         item.setItemMeta(meta);
      }
   }

   private void changeOld(ItemMeta meta, Changer.ChangeMode mode, long delta) {
      if (mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.RESET) {
         long oldData = 0L;
         if (meta.hasCustomModelData() && (!USE_NEW_CMD || !meta.getCustomModelDataComponent().getFloats().isEmpty())) {
            oldData = meta.getCustomModelData();
         }

         switch (mode) {
            case REMOVE:
            case REMOVE_ALL:
               delta = -delta;
            case ADD:
               delta = oldData + delta;
               meta.setCustomModelData((int)delta);
               break;
            case SET:
               meta.setCustomModelData((int)delta);
            case DELETE:
            case RESET:
         }
      } else {
         meta.setCustomModelData(null);
      }
   }

   private <T> void changeSingleType(ItemMeta meta, Changer.ChangeMode mode, @Nullable T[] delta) {
      if (delta != null || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
         CustomModelDataComponent component = meta.getCustomModelDataComponent();

         List<T> data = new ArrayList<>(switch (this.dataType) {
            case FLOATS -> component.getFloats();
            case FLAGS -> component.getFlags();
            case STRINGS -> component.getStrings();
            case COLORS -> component.getColors().stream().map(ColorRGB::fromBukkitColor).toList();
            default -> throw new IllegalStateException("Wrong changemode for changeSingleType");
         });
         switch (mode) {
            case ADD:
               data.addAll(Arrays.asList(delta));
               break;
            case REMOVE:
               data.removeAll(Arrays.asList(delta));
               break;
            case SET:
               data = Arrays.asList(delta);
               break;
            case DELETE:
            case RESET:
               data.clear();
         }

         switch (this.dataType) {
            case FLOATS:
               component.setFloats(data);
               break;
            case FLAGS:
               component.setFlags(data);
               break;
            case STRINGS:
               component.setStrings(data);
               break;
            case COLORS:
               component.setColors(data.stream().map(colorRGB -> ((ColorRGB)colorRGB).asBukkitColor()).toList());
         }

         meta.setCustomModelDataComponent(component);
      }
   }

   private void changeAll(ItemMeta meta, Changer.ChangeMode mode, @Nullable Object[] delta) {
      if (mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET) {
         meta.setCustomModelDataComponent(null);
      } else if (delta != null) {
         CustomModelDataComponent component = meta.getCustomModelDataComponent();
         List<Float> floats = new ArrayList<>(component.getFloats());
         List<Boolean> flags = new ArrayList<>(component.getFlags());
         List<String> strings = new ArrayList<>(component.getStrings());
         List<Color> colors = new ArrayList<>(component.getColors().stream().map(ColorRGB::fromBukkitColor).toList());
         switch (mode) {
            case REMOVE:
               for (Object deltaValuex : delta) {
                  if (deltaValuex instanceof Float) {
                     floats.remove(deltaValuex);
                  } else if (deltaValuex instanceof Boolean) {
                     flags.remove(deltaValuex);
                  } else if (deltaValuex instanceof String) {
                     strings.remove(deltaValuex);
                  } else if (deltaValuex instanceof Color) {
                     colors.remove(deltaValuex);
                  }
               }
               break;
            case SET:
               floats.clear();
               flags.clear();
               strings.clear();
               colors.clear();
            case ADD:
               for (Object deltaValue : delta) {
                  if (deltaValue instanceof Float aFloat) {
                     floats.addLast(aFloat);
                  } else if (deltaValue instanceof Boolean aBoolean) {
                     flags.addLast(aBoolean);
                  } else if (deltaValue instanceof String string) {
                     strings.addLast(string);
                  } else if (deltaValue instanceof Color color) {
                     colors.addLast(color);
                  }
               }
         }

         component.setFloats(floats);
         component.setFlags(flags);
         component.setStrings(strings);
         component.setColors(colors.stream().map(Color::asBukkitColor).toList());
         meta.setCustomModelDataComponent(component);
      }
   }

   @Override
   public boolean isSingle() {
      return this.dataType == ExprCustomModelData.CMDType.SINGLE_INT && this.getExpr().isSingle();
   }

   @Override
   public Class<?> getReturnType() {
      return this.returnType;
   }

   @Override
   public Class<?>[] possibleReturnTypes() {
      return this.dataType.types;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return switch (this.dataType) {
         case SINGLE_INT -> "custom model data";
         case FLOATS -> "custom model data floats";
         case FLAGS -> "custom model data flags";
         case STRINGS -> "custom model data strings";
         case COLORS -> "custom model data colors";
         case ALL -> "complete custom model data";
      } + " of " + this.getExpr().toString(event, debug);
   }

   static {
      if (USE_NEW_CMD) {
         List<String> patterns = new ArrayList<>();
         patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data", "itemtypes")));
         patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("[custom] model data (1:floats|2:flags|3:strings|4:colo[u]rs)", "itemtype")));
         patterns.addAll(Arrays.asList(PropertyExpression.getPatterns("(5:(complete|full)) [custom] model data", "itemtype")));
         Skript.registerExpression(ExprCustomModelData.class, Object.class, ExpressionType.PROPERTY, patterns.toArray(String[]::new));
      } else {
         register(ExprCustomModelData.class, Object.class, "[custom] model data", "itemtypes");
      }
   }

   private static enum CMDType {
      SINGLE_INT(Integer.class),
      FLOATS(Float.class),
      FLAGS(Boolean.class),
      STRINGS(String.class),
      COLORS(Color.class),
      ALL(Float.class, Boolean.class, String.class, Color.class);

      private final Class<?>[] types;

      private CMDType(Class<?>... returns) {
         this.types = returns;
      }
   }
}
