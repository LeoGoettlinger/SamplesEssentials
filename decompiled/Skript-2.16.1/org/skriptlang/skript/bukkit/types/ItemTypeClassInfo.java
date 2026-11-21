package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemData;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.YggdrasilSerializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.coll.CollectionUtils;
import java.util.Arrays;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class ItemTypeClassInfo extends ClassInfo<ItemType> {
   public ItemTypeClassInfo() {
      super(ItemType.class, "itemtype");
      this.user(new String[]{"item ?types?", "materials?"})
         .name("Item Type")
         .description(
            "An item type is an alias that can result in different items when added to an inventory, and unlike <a href='#itemstack'>items</a> they are well suited for checking whether an inventory contains a certain item or whether a certain item is of a certain type.",
            "An item type can also have one or more <a href='#enchantmenttype'>enchantments</a> with or without a specific level defined, and can optionally start with 'all' or 'every' to make this item type represent <i>all</i> types that the alias represents, including data ranges."
         )
         .usage("[<number> [of]] [all/every] <alias> [of <enchantment> [<level>] [,/and <more enchantments...>]]")
         .examples(
            "give 4 torches to the player",
            "add oak slab to the inventory of the block",
            "player's tool is a diamond sword of sharpness",
            "block is dirt or farmland"
         )
         .since("1.0")
         .before("itemstack", "entitydata", "entitytype")
         .after("number", "integer", "long", "time")
         .supplier(() -> Arrays.stream(Material.values()).map(ItemType::new).iterator())
         .parser(new ItemTypeClassInfo.ItemTypeParser())
         .cloner(ItemType::clone)
         .serializer(new YggdrasilSerializer<>())
         .property(
            Property.TITLE, "An item's title. This currently only applies to signed books.", Skript.instance(), new ItemTypeClassInfo.ItemTypeTitleHandler()
         )
         .property(Property.NAME, "An item type's custom name, if set. Can be set or reset.", Skript.instance(), new ItemTypeClassInfo.ItemTypeNameHandler())
         .property(
            Property.DISPLAY_NAME, "An item type's custom name, if set. Can be set or reset.", Skript.instance(), new ItemTypeClassInfo.ItemTypeNameHandler()
         )
         .property(
            Property.AMOUNT,
            "The amount of items in the stack this type represents. E.g. 5 for '5 stone swords'. Can be set.",
            Skript.instance(),
            new ItemTypeClassInfo.ItemTypeAmountHandler()
         );
   }

   private static class ItemTypeAmountHandler implements ExpressionPropertyHandler<ItemType, Number> {
      public Number convert(ItemType itemType) {
         return itemType.getAmount();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return mode == Changer.ChangeMode.SET ? new Class[]{Integer.class} : null;
      }

      public void change(ItemType itemType, @Nullable Object[] delta, Changer.ChangeMode mode) {
         if (mode == Changer.ChangeMode.SET) {
            assert delta != null;

            itemType.setAmount((Integer)delta[0]);
         }
      }

      @NotNull
      @Override
      public Class<Number> returnType() {
         return Number.class;
      }
   }

   private static class ItemTypeNameHandler implements ExpressionPropertyHandler<ItemType, Component> {
      public Component convert(ItemType itemType) {
         ItemMeta meta = itemType.getItemMeta();
         return meta.hasDisplayName() ? meta.displayName() : null;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, DELETE, RESET -> new Class[]{Component.class};
            default -> null;
         };
      }

      public void change(ItemType itemType, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component name = delta == null ? null : (Component)delta[0];
         ItemMeta meta = itemType.getItemMeta();
         meta.displayName(name);
         itemType.setItemMeta(meta);
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class ItemTypeParser extends Parser<ItemType> {
      @Nullable
      public ItemType parse(String s, ParseContext context) {
         return Aliases.parseItemType(s);
      }

      public String toString(ItemType t, int flags) {
         return t.toString(flags);
      }

      public String getDebugMessage(ItemType t) {
         return t.getDebugMessage();
      }

      public String toVariableNameString(ItemType itemType) {
         StringBuilder result = new StringBuilder("itemtype:");
         result.append(itemType.getInternalAmount());
         result.append(",").append(itemType.isAll());

         for (ItemData itemData : itemType.getTypes()) {
            result.append(",").append(itemData.getType());
         }

         EnchantmentType[] enchantmentTypes = itemType.getEnchantmentTypes();
         if (enchantmentTypes != null) {
            result.append("|");

            for (EnchantmentType enchantmentType : enchantmentTypes) {
               Enchantment enchantment = enchantmentType.getType();
               if (enchantment != null) {
                  result.append("#").append(enchantment.getKey());
                  result.append(":").append(enchantmentType.getLevel());
               }
            }
         }

         return result.toString();
      }
   }

   private static class ItemTypeTitleHandler implements ExpressionPropertyHandler<ItemType, Component> {
      @Nullable
      public Component convert(ItemType item) {
         return item.getItemMeta() instanceof BookMeta bookMeta && bookMeta.hasTitle() ? bookMeta.title() : null;
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case SET, DELETE, RESET -> (Class[])CollectionUtils.array(Component.class);
            default -> null;
         };
      }

      public void change(ItemType item, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component title = delta == null ? null : (Component)delta[0];
         if (item.getItemMeta() instanceof BookMeta bookMeta) {
            bookMeta.title(title);
            item.setItemMeta(bookMeta);
         }
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }
}
