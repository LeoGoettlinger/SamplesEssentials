package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.banner.PatternType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Banner Pattern Item")
@Description({"Gets the item from a banner pattern type.", "Note that not all banner pattern types have an item."})
@Example.Examples({@Example("set {_item} to creeper charged banner pattern item"), @Example("set {_item} to snout banner pattern item"), @Example("set {_item} to thing banner pattern item")})
@Since({"2.10"})
public class ExprBannerItem extends SimpleExpression<ItemType> {
   private static final Map<Object, Material> bannerMaterials = new HashMap<>();
   private static boolean PATTERN_TYPE_IS_REGISTRY = false;
   private PatternType[] patternTypes;
   private Literal<PatternType> literalPattern;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.literalPattern = (Literal<PatternType>)exprs[0];
      this.patternTypes = this.literalPattern.getArray();

      for (PatternType type : this.patternTypes) {
         if (!bannerMaterials.containsKey(type)) {
            Skript.error("There is no item for the banner pattern type '" + type + "'.");
            return false;
         }
      }

      return true;
   }

   @Nullable
   protected ItemType[] get(Event event) {
      List<ItemType> itemTypes = new ArrayList<>();

      for (PatternType type : this.patternTypes) {
         Material material = bannerMaterials.get(type);
         ItemType itemType = new ItemType(material);
         itemTypes.add(itemType);
      }

      return itemTypes.toArray(new ItemType[0]);
   }

   @Override
   public boolean isSingle() {
      return this.literalPattern.isSingle();
   }

   @Override
   public Class<ItemType> getReturnType() {
      return ItemType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return this.literalPattern.toString(event, debug) + " items";
   }

   @Nullable
   private static Material getMaterial(Object object) {
      if (object instanceof PatternType patternType) {
         String key = null;

         try {
            if (PATTERN_TYPE_IS_REGISTRY) {
               NamespacedKey namespacedKey = (NamespacedKey)PatternType.class.getMethod("getKey").invoke(patternType);
               if (namespacedKey != null) {
                  key = namespacedKey.getKey().toUpperCase(Locale.ENGLISH);
               }
            } else {
               key = (String)PatternType.class.getMethod("toString").invoke(patternType);
            }
         } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException var4) {
            throw new RuntimeException(var4);
         }

         if (key == null) {
            return null;
         } else {
            Material material = Material.getMaterial(key + "_BANNER_PATTERN");
            return material != null ? material : checkAlias(patternType);
         }
      } else {
         return null;
      }
   }

   @Nullable
   private static Material checkAlias(PatternType patternType) {
      if (patternType == PatternType.BRICKS && Material.getMaterial("FIELD_MASONED_BANNER_PATTERN") != null) {
         return Material.FIELD_MASONED_BANNER_PATTERN;
      } else {
         return patternType == PatternType.BORDER && Material.getMaterial("BORDURE_INDENTED_BANNER_PATTER") != null
            ? Material.BORDURE_INDENTED_BANNER_PATTERN
            : null;
      }
   }

   static {
      Registry<PatternType> patternRegistry = Bukkit.getRegistry(PatternType.class);
      Object[] bannerPatterns;
      if (patternRegistry != null) {
         bannerPatterns = patternRegistry.stream().toArray();
         PATTERN_TYPE_IS_REGISTRY = true;
      } else {
         try {
            Class<?> patternClass = Class.forName("org.bukkit.block.banner.PatternType");
            if (!patternClass.isEnum()) {
               throw new IllegalStateException("PatternType is neither an enum nor a valid registry.");
            }

            bannerPatterns = patternClass.getEnumConstants();
         } catch (ClassNotFoundException var7) {
            throw new RuntimeException(var7);
         }
      }

      if (bannerPatterns != null) {
         for (Object object : bannerPatterns) {
            Material material = getMaterial(object);
            if (material != null) {
               bannerMaterials.put(object, material);
            }
         }

         Skript.registerExpression(ExprBannerItem.class, ItemType.class, ExpressionType.COMBINED, "[a[n]] %*bannerpatterntypes% item[s]");
      }
   }
}
