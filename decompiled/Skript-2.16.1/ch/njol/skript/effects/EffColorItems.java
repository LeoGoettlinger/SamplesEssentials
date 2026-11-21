package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Color;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

@Name("Color Items")
@Description({"Colors items in a given <a href='#color'>color</a>. You can also use RGB codes if you feel limited with the 16 default colors. RGB codes are three numbers from 0 to 255 in the order <code>(red, green, blue)</code>, where <code>(0,0,0)</code> is black and <code>(255,255,255)</code> is white. Armor is colorable for all Minecraft versions. With Minecraft 1.11 or newer you can also color potions and maps. Note that the colors might not look exactly how you'd expect."})
@Example.Examples({@Example("dye player's helmet blue"), @Example("color the player's tool red")})
@Since({"2.0, 2.2-dev26 (maps and potions)"})
public class EffColorItems extends Effect {
   private static final boolean MAPS_AND_POTIONS_COLORS = Skript.methodExists(PotionMeta.class, "setColor", Color.class);
   private Expression<ItemType> items;
   private Expression<ch.njol.skript.util.Color> color;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parser) {
      this.items = (Expression<ItemType>)exprs[0];
      if (matchedPattern == 0) {
         this.color = (Expression<ch.njol.skript.util.Color>)exprs[1];
      } else {
         this.color = new SimpleExpression<ch.njol.skript.util.Color>() {
            private Expression<Number> red;
            private Expression<Number> green;
            private Expression<Number> blue;

            @Override
            public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
               this.red = (Expression<Number>)exprs[0];
               this.green = (Expression<Number>)exprs[1];
               this.blue = (Expression<Number>)exprs[2];
               return true;
            }

            @Nullable
            protected ch.njol.skript.util.Color[] get(Event e) {
               Number r = this.red.getSingle(e);
               Number g = this.green.getSingle(e);
               Number b = this.blue.getSingle(e);
               return r != null && g != null && b != null ? CollectionUtils.array(ColorRGB.fromRGB(r.intValue(), g.intValue(), b.intValue())) : null;
            }

            @Override
            public boolean isSingle() {
               return true;
            }

            @Override
            public Class<? extends ch.njol.skript.util.Color> getReturnType() {
               return ColorRGB.class;
            }

            @Override
            public String toString(@Nullable Event e, boolean debug) {
               return "RED: " + this.red.toString(e, debug) + ", GREEN: " + this.green.toString(e, debug) + "BLUE: " + this.blue.toString(e, debug);
            }
         };
         this.color.init(CollectionUtils.array(exprs[1], exprs[2], exprs[3]), 0, isDelayed, parser);
      }

      return true;
   }

   @Override
   protected void execute(Event e) {
      ch.njol.skript.util.Color color = this.color.getSingle(e);
      ItemType[] items = this.items.getArray(e);
      if (color != null) {
         Color c = color.asBukkitColor();

         for (ItemType item : items) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof LeatherArmorMeta m) {
               m.setColor(c);
               item.setItemMeta(m);
            } else if (MAPS_AND_POTIONS_COLORS) {
               if (meta instanceof MapMeta m) {
                  m.setColor(c);
                  item.setItemMeta(m);
               } else if (meta instanceof PotionMeta m) {
                  m.setColor(c);
                  item.setItemMeta(m);
               }
            }
         }
      }
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return "dye " + this.items.toString(e, debug) + " " + this.color.toString(e, debug);
   }

   static {
      Skript.registerEffect(
         EffColorItems.class, "(dye|colo[u]r|paint) %itemtypes% %color%", "(dye|colo[u]r|paint) %itemtypes% \\(%number%, %number%, %number%\\)"
      );
   }
}
