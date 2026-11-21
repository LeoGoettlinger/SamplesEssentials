package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.function.DynamicFunctionReference;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.Script;

@Name("Name / Display Name / Tab List Name")
@Description({"Represents the Minecraft account, display or tab list name of a player, or the custom name of an item, entity, block, inventory, gamerule, world, script or function.", "", "<strong>Players:</strong>", "\t<strong>Name:</strong> The Minecraft account name of the player. Can't be changed, but 'display name' can be changed.", "\t<strong>Display Name:</strong> The name of the player that is displayed in messages. This name can be changed freely and can include color codes, and is shared among all plugins (e.g. chat plugins will use the display name).", "", "<strong>Entities:</strong>", "\t<strong>Name:</strong> The custom name of the entity. Can be changed. But for living entities, the players will have to target the entity to see its name tag. For non-living entities, the name will not be visible at all. To prevent this, use 'display name'.", "\t<strong>Display Name:</strong> The custom name of the entity. Can be changed, which will also enable <em>custom name visibility</em> of the entity so name tag of the entity will be visible always.", "", "<strong>Items:</strong>", "\t<strong>Name and Display Name:</strong> The <em>custom</em> name of the item (not the Minecraft locale name). Can be changed.", "", "<strong>Inventories:</strong>", "\t<strong>Name and Display Name:</strong> The name/title of the inventory. Changing name of an inventory means opening the same inventory with the same contents but with a different name to its current viewers.", "", "<strong>Gamerules:</strong>", "\t<strong>Name:</strong> The name of the gamerule. Cannot be changed.", "", "<strong>Worlds:</strong>", "\t<strong>Name:</strong> The name of the world. Cannot be changed.", "", "<strong>Scripts:</strong>", "\t<strong>Name:</strong> The name of a script, excluding its file extension."})
@Example.Examples({@Example("on join:\n\tplayer has permission \"name.red\"\n\tset the player's display name to \"<red>[admin] <gold>%name of player%\"\n\tset the player's tab list name to \"<green>%player's name%\"\n"), @Example("set the name of the player's tool to \"Legendary Sword of Awesomeness\"")})
@Since({"before 2.1", "2.2-dev20 (inventory name)", "2.4 (non-living entity support, changeable inventory name)", "2.7 (worlds)"})
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public class ExprName extends SimplePropertyExpression<Object, Object> {
   private int mark;
   private boolean scriptResolvedName;
   private boolean isComponent = true;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.mark = matchedPattern / 2 + 1;
      this.setExpr((Expression<? extends Object>)exprs[0]);
      this.scriptResolvedName = this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION);
      return true;
   }

   @Nullable
   @Override
   public Object convert(Object object) {
      if (object instanceof OfflinePlayer offlinePlayer) {
         if (!offlinePlayer.isOnline()) {
            if (this.mark != 1) {
               return null;
            }

            String name = offlinePlayer.getName();
            if (name == null) {
               return null;
            }

            return this.isComponent ? Component.text(name) : name;
         }

         object = offlinePlayer.getPlayer();
      }

      if (!this.scriptResolvedName && object instanceof Script script) {
         String nameAndPath = script.nameAndPath();
         if (nameAndPath == null) {
            return null;
         } else {
            return this.isComponent ? Component.text(nameAndPath) : nameAndPath;
         }
      } else if (object instanceof Player player) {
         return switch (this.mark) {
            case 1 -> this.isComponent ? player.name() : player.getName();
            case 2 -> this.isComponent ? player.displayName() : player.getDisplayName();
            case 3 -> this.isComponent ? player.playerListName() : player.getPlayerListName();
            default -> throw new IllegalStateException("Unexpected value: " + this.mark);
         };
      } else if (object instanceof Nameable nameable) {
         if (this.mark == 1 && nameable instanceof CommandSender sender) {
            return this.isComponent ? sender.name() : sender.getName();
         } else {
            return this.isComponent ? nameable.customName() : nameable.getCustomName();
         }
      } else if (object instanceof Inventory inventory) {
         if (inventory.getViewers().isEmpty()) {
            return null;
         } else {
            InventoryView view = ((HumanEntity)inventory.getViewers().getFirst()).getOpenInventory();
            return this.isComponent ? view.title() : view.getTitle();
         }
      } else if (object instanceof AnyNamed named) {
         return this.isComponent ? named.nameComponent() : named.name();
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      if (mode != Changer.ChangeMode.SET && mode != Changer.ChangeMode.RESET) {
         return null;
      } else {
         if (this.mark == 1) {
            if (Player.class.isAssignableFrom(this.getExpr().getReturnType())) {
               Skript.error("Can't change the Minecraft name of a player. Change the 'display name' or 'tab list name' instead.");
               return null;
            }

            if (World.class.isAssignableFrom(this.getExpr().getReturnType())) {
               return null;
            }

            if (Script.class.isAssignableFrom(this.getExpr().getReturnType())) {
               return null;
            }

            if (DynamicFunctionReference.class.isAssignableFrom(this.getExpr().getReturnType())) {
               return null;
            }
         }

         return CollectionUtils.array(Component.class);
      }
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      Component name = delta != null ? (Component)delta[0] : null;

      for (Object object : this.getExpr().getArray(event)) {
         if (object instanceof Player player) {
            switch (this.mark) {
               case 2:
                  player.displayName(name);
                  break;
               case 3:
                  player.playerListName(name);
            }
         } else if (object instanceof Entity entity) {
            entity.customName(name);
            if (this.mark == 2 || mode == Changer.ChangeMode.RESET) {
               entity.setCustomNameVisible(name != null);
            }

            if (object instanceof LivingEntity living) {
               living.setRemoveWhenFarAway(name == null);
            }
         } else if (object instanceof AnyNamed named) {
            if (named.supportsNameChange()) {
               named.setName(name);
            }
         } else if (object instanceof Inventory inventory) {
            if (inventory.getViewers().isEmpty()) {
               return;
            }

            List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
            InventoryType type = inventory.getType();
            if (!type.isCreatable()) {
               return;
            }

            if (name == null) {
               name = type.defaultTitle();
            }

            Inventory copy;
            if (type == InventoryType.CHEST) {
               copy = Bukkit.createInventory(inventory.getHolder(), inventory.getSize(), name);
            } else {
               copy = Bukkit.createInventory(inventory.getHolder(), type, name);
            }

            copy.setContents(inventory.getContents());
            viewers.forEach(viewer -> viewer.openInventory(copy));
         }
      }
   }

   @Override
   public Class<?> getReturnType() {
      return this.isComponent ? Component.class : String.class;
   }

   @Override
   protected String getPropertyName() {
      return switch (this.mark) {
         case 2 -> "display name";
         case 3 -> "tablist name";
         default -> "name";
      };
   }

   @SafeVarargs
   @Nullable
   @Override
   public final <R> Expression<? extends R> getConvertedExpression(Class<R>... to) {
      for (Class<R> clazz : to) {
         if (String.class.isAssignableFrom(clazz)) {
            ExprName converted = new ExprName();
            converted.setExpr(this.getExpr());
            converted.rawExpr = this.rawExpr;
            converted.mark = this.mark;
            converted.scriptResolvedName = this.scriptResolvedName;
            converted.isComponent = false;
            return converted;
         }
      }

      return super.getConvertedExpression(to);
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         List<String> patterns = new ArrayList<>();
         patterns.addAll(Arrays.asList(getPatterns("name[s]", "offlineplayers/entities/nameds/inventories")));
         patterns.addAll(Arrays.asList(getPatterns("(display|nick|chat|custom)[ ]name[s]", "offlineplayers/entities/nameds/inventories")));
         Skript.registerExpression(ExprName.class, Object.class, ExpressionType.COMBINED, patterns.toArray(new String[0]));
      }
   }
}
