package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import java.util.Locale;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;

@Name("Has Custom Model Data")
@Description({"Check if an item has a custom model data tag"})
@Example.Examples({@Example("player's tool has custom model data"), @Example("if player's tool has custom model data flags:\n\tloop custom model data flags of player's tool:\n\t\tsend \"Flag %loop-index%: %loop-value%\"\n"), @Example("set {_coloured} to whether player's tool has model data colours")})
@Since({"2.5, 2.12 (expanded data types)"})
@RequiredPlugins({"Minecraft 1.21.4+ (floats/flags/strings/colours)"})
public class CondHasCustomModelData extends PropertyCondition<ItemType> {
   private static final boolean HAS_HAS_COMPONENT = Skript.methodExists(ItemMeta.class, "hasCustomModelDataComponent");
   private CondHasCustomModelData.CMDType dataType;

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.dataType = CondHasCustomModelData.CMDType.values()[parseResult.mark];
      return super.init(expressions, matchedPattern, isDelayed, parseResult);
   }

   public boolean check(ItemType item) {
      ItemMeta meta = item.getItemMeta();
      if (this.dataType == CondHasCustomModelData.CMDType.ANY) {
         return HAS_HAS_COMPONENT ? meta.hasCustomModelDataComponent() : meta.hasCustomModelData();
      } else {
         CustomModelDataComponent component = meta.getCustomModelDataComponent();

         return switch (this.dataType) {
            case ANY -> throw new IllegalStateException("Wrong path for CMDType.ANY.");
            case FLOATS -> !component.getFloats().isEmpty();
            case FLAGS -> !component.getFlags().isEmpty();
            case STRINGS -> !component.getStrings().isEmpty();
            case COLORS -> !component.getColors().isEmpty();
         };
      }
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return PropertyCondition.PropertyType.HAVE;
   }

   @Override
   protected String getPropertyName() {
      return "custom model data" + (this.dataType != CondHasCustomModelData.CMDType.ANY ? " " + this.dataType.name().toLowerCase(Locale.ENGLISH) : "");
   }

   static {
      if (Skript.methodExists(ItemMeta.class, "getCustomModelDataComponent")) {
         register(
            CondHasCustomModelData.class, PropertyCondition.PropertyType.HAVE, "[custom] model data [1:floats|2:flags|3:strings|4:colo[u]rs]", "itemtypes"
         );
      } else {
         register(CondHasCustomModelData.class, PropertyCondition.PropertyType.HAVE, "[custom] model data", "itemtypes");
      }
   }

   private static enum CMDType {
      ANY,
      FLOATS,
      FLAGS,
      STRINGS,
      COLORS;
   }
}
