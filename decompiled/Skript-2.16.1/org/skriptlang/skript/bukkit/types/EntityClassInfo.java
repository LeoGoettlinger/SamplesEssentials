package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import java.util.Locale;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

@Internal
public class EntityClassInfo extends ClassInfo<Entity> {
   public EntityClassInfo() {
      super(Entity.class, "entity");
      this.user(new String[]{"entit(y|ies)"})
         .name("Entity")
         .description(
            "An entity is something in a <a href='#world'>world</a> that's not a <a href='#block'>block</a>, e.g. a <a href='#player'>player</a>, a skeleton, or a zombie, but also <a href='#projectile'>projectiles</a> like arrows, fireballs or thrown potions, or special entities like dropped items, falling blocks or paintings."
         )
         .usage("player, op, wolf, tamed ocelot, powered creeper, zombie, unsaddled pig, fireball, arrow, dropped item, item frame, etc.")
         .examples("entity is a zombie or creeper", "player is an op", "projectile is an arrow", "shoot a fireball from the player")
         .since("1.0")
         .defaultExpression(new EventValueExpression<>(Entity.class))
         .parser(new EntityClassInfo.EntityParser())
         .changer(new EntityClassInfo.EntityChanger())
         .property(
            Property.NAME,
            "The entity's name, if it has one, as text.Note that the regular name cannot be changed, meaning the entity's custom (display) name will be changed instead.",
            Skript.instance(),
            EntityClassInfo.EntityNameHandler.name()
         )
         .property(
            Property.DISPLAY_NAME,
            "The entity's custom name, if it has one, as text. Can be set or reset.",
            Skript.instance(),
            EntityClassInfo.EntityNameHandler.displayName()
         );
   }

   public static class EntityChanger implements Changer<Entity> {
      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case ADD -> (Class[])CollectionUtils.array(ItemType[].class, Inventory.class, Experience[].class);
            case DELETE -> (Class[])CollectionUtils.array();
            case REMOVE -> (Class[])CollectionUtils.array(PotionEffectType[].class, ItemType[].class, Inventory.class);
            case REMOVE_ALL -> (Class[])CollectionUtils.array(PotionEffectType[].class, ItemType[].class);
            case SET, RESET -> null;
         };
      }

      public void change(Entity[] entities, @Nullable Object[] delta, Changer.ChangeMode mode) {
         if (delta == null) {
            for (Entity entity : entities) {
               if (!(entity instanceof Player)) {
                  entity.remove();
               }
            }
         } else {
            boolean hasItem = false;

            for (Entity entityx : entities) {
               for (Object deltaObj : delta) {
                  if (deltaObj instanceof PotionEffectType potionEffectType) {
                     assert mode == Changer.ChangeMode.REMOVE || mode == Changer.ChangeMode.REMOVE_ALL;

                     if (entityx instanceof LivingEntity livingEntity) {
                        livingEntity.removePotionEffect(potionEffectType);
                     }
                  } else if (entityx instanceof Player player) {
                     if (deltaObj instanceof Experience experience) {
                        player.giveExp(experience.getXP());
                     } else if (deltaObj instanceof Inventory itemStacks) {
                        PlayerInventory inventory = player.getInventory();

                        for (ItemStack itemStack : itemStacks) {
                           if (itemStack != null) {
                              if (mode == Changer.ChangeMode.ADD) {
                                 inventory.addItem(new ItemStack[]{itemStack});
                              } else {
                                 inventory.remove(itemStack);
                              }
                           }
                        }
                     } else if (deltaObj instanceof ItemType itemType) {
                        hasItem = true;
                        PlayerInventory invi = player.getInventory();
                        if (mode == Changer.ChangeMode.ADD) {
                           itemType.addTo(invi);
                        } else if (mode == Changer.ChangeMode.REMOVE) {
                           itemType.removeFrom(invi);
                        } else {
                           itemType.removeAll(invi);
                        }
                     }
                  }
               }

               if (entityx instanceof Player playerx && hasItem) {
                  PlayerUtils.updateInventory(playerx);
               }
            }
         }
      }
   }

   private static class EntityNameHandler implements ExpressionPropertyHandler<Entity, Component> {
      private final boolean isDisplayName;

      public static EntityClassInfo.EntityNameHandler name() {
         return new EntityClassInfo.EntityNameHandler(false);
      }

      public static EntityClassInfo.EntityNameHandler displayName() {
         return new EntityClassInfo.EntityNameHandler(true);
      }

      private EntityNameHandler(boolean isDisplayName) {
         this.isDisplayName = isDisplayName;
      }

      public Component convert(Entity entity) {
         return this.isDisplayName ? entity.customName() : entity.name();
      }

      @Nullable
      @Override
      public Class<?>[] acceptChange(Changer.ChangeMode mode) {
         return switch (mode) {
            case DELETE, SET, RESET -> new Class[]{Component.class};
            default -> null;
         };
      }

      public void change(Entity entity, @Nullable Object[] delta, Changer.ChangeMode mode) {
         Component name = delta == null ? null : (Component)delta[0];
         entity.customName(name);
         if (this.isDisplayName || mode == Changer.ChangeMode.RESET) {
            entity.setCustomNameVisible(name != null);
         }

         if (entity instanceof LivingEntity living) {
            living.setRemoveWhenFarAway(name == null);
         }
      }

      @NotNull
      @Override
      public Class<Component> returnType() {
         return Component.class;
      }
   }

   private static class EntityParser extends Parser<Entity> {
      @Nullable
      public Entity parse(String s, ParseContext context) {
         return Utils.isValidUUID(s) ? Bukkit.getEntity(UUID.fromString(s)) : null;
      }

      @Override
      public boolean canParse(ParseContext context) {
         return context == ParseContext.COMMAND || context == ParseContext.PARSE;
      }

      public String toVariableNameString(Entity entity) {
         return "entity:" + entity.getUniqueId().toString().toLowerCase(Locale.ENGLISH);
      }

      public String toString(Entity e, int flags) {
         return EntityData.toString(e, flags);
      }
   }
}
