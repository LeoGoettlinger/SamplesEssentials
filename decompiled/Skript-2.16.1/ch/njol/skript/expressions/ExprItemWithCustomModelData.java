package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.Nullable;

@Name("Item with Custom Model Data")
@Description({"Get an item with custom model data."})
@Example.Examples({@Example("give player a diamond sword with custom model data 2"), @Example("set slot 1 of inventory of player to wooden hoe with custom model data 357"), @Example("give player a diamond hoe with custom model data 2, true, true, \"scythe\", and rgb(0,0,100)")})
@RequiredPlugins({"Minecraft 1.21.4+ (boolean/string/color support)"})
@Since({"2.5", "2.12 (boolean/string/color support)"})
public class ExprItemWithCustomModelData extends PropertyExpression<ItemType, ItemType> {
   private static final boolean USE_NEW_CMD = Skript.classExists("org.bukkit.inventory.meta.components.CustomModelDataComponent");
   private Expression<?> data;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
      this.setExpr((Expression<? extends ItemType>)exprs[0]);
      this.data = exprs[1];
      return true;
   }

   protected ItemType[] get(Event event, ItemType[] source) {
      Object[] data = this.data.getArray(event);
      if (data.length == 0) {
         return source;
      } else if (!USE_NEW_CMD) {
         return this.get(source, item -> {
            ItemType clone = item.clone();
            ItemMeta meta = clone.getItemMeta();
            meta.setCustomModelData(((Number)data[0]).intValue());
            clone.setItemMeta(meta);
            return clone;
         });
      } else {
         List<Float> floats = new ArrayList<>();
         List<Boolean> flags = new ArrayList<>();
         List<String> strings = new ArrayList<>();
         List<Color> colors = new ArrayList<>();

         for (Object dataValue : data) {
            if (dataValue instanceof Number number) {
               floats.addLast(number.floatValue());
            } else if (dataValue instanceof Boolean aBoolean) {
               flags.addLast(aBoolean);
            } else if (dataValue instanceof String string) {
               strings.addLast(string);
            } else if (dataValue instanceof Color color) {
               colors.addLast(color);
            }
         }

         return this.get(source, item -> {
            ItemType clone = item.clone();
            ItemMeta meta = clone.getItemMeta();
            CustomModelDataComponent component = meta.getCustomModelDataComponent();
            component.setFloats(floats);
            component.setFlags(flags);
            component.setStrings(strings);
            component.setColors(colors.stream().map(Color::asBukkitColor).toList());
            meta.setCustomModelDataComponent(component);
            clone.setItemMeta(meta);
            return clone;
         });
      }
   }

   @Override
   public Class<? extends ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.getExpr().toString(event, debug) + " with custom model data " + this.data.toString(event, debug);
   }

   static {
      if (USE_NEW_CMD) {
         Skript.registerExpression(
            ExprItemWithCustomModelData.class, ItemType.class, ExpressionType.PROPERTY, "%itemtype% with [custom] model data %numbers/booleans/strings/colors%"
         );
      } else {
         Skript.registerExpression(ExprItemWithCustomModelData.class, ItemType.class, ExpressionType.PROPERTY, "%itemtype% with [custom] model data %number%");
      }
   }
}
