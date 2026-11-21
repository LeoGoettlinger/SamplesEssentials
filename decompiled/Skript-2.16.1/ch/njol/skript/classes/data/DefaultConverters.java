package ch.njol.skript.classes.data;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.bukkitutil.EntityUtils;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.Node;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.entity.EntityType;
import ch.njol.skript.entity.XpOrbData;
import ch.njol.skript.lang.util.common.AnyAmount;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.util.BlockInventoryHolder;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Experience;
import ch.njol.skript.util.slot.Slot;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.script.Script;

public class DefaultConverters {
   static {
      Converters.registerConverter(Number.class, Byte.class, Number::byteValue);
      Converters.registerConverter(Number.class, Double.class, Number::doubleValue);
      Converters.registerConverter(Number.class, Float.class, Number::floatValue);
      Converters.registerConverter(Number.class, Integer.class, Number::intValue);
      Converters.registerConverter(Number.class, Long.class, Number::longValue);
      Converters.registerConverter(Number.class, Short.class, Number::shortValue);
      Converters.registerConverter(OfflinePlayer.class, PlayerInventory.class, p -> {
         if (!p.isOnline()) {
            return null;
         } else {
            Player online = p.getPlayer();

            assert online != null;

            return online.getInventory();
         }
      }, 8);
      Converters.registerConverter(OfflinePlayer.class, Player.class, OfflinePlayer::getPlayer, 8);
      Converters.registerConverter(CommandSender.class, Player.class, s -> s instanceof Player ? (Player)s : null);
      Converters.registerConverter(BlockCommandSender.class, Block.class, BlockCommandSender::getBlock);
      Converters.registerConverter(Experience.class, Number.class, Experience::getXP);
      Converters.registerConverter(Entity.class, Player.class, e -> e instanceof Player ? (Player)e : null);
      Converters.registerConverter(Entity.class, LivingEntity.class, e -> e instanceof LivingEntity ? (LivingEntity)e : null);
      Converters.registerConverter(
         Block.class, Inventory.class, b -> b.getState() instanceof InventoryHolder ? ((InventoryHolder)b.getState()).getInventory() : null, 8
      );
      Converters.registerConverter(Entity.class, Inventory.class, e -> e instanceof InventoryHolder ? ((InventoryHolder)e).getInventory() : null, 8);
      Converters.registerConverter(Block.class, ItemType.class, ItemType::new, 9);
      Converters.registerConverter(Entity.class, EntityData.class, EntityData::fromEntity, 10);
      Converters.registerConverter(EntityData.class, EntityType.class, data -> new EntityType(data, -1));
      Converters.registerConverter(ItemType.class, ItemStack.class, ItemType::getRandom);
      Converters.registerConverter(ItemStack.class, ItemType.class, ItemType::new);
      Converters.registerConverter(Experience.class, XpOrbData.class, e -> new XpOrbData(e.getXP()));
      Converters.registerConverter(XpOrbData.class, Experience.class, e -> new Experience(e.getExperience()));
      Converters.registerConverter(Slot.class, ItemType.class, s -> {
         ItemStack i = s.getItem();
         return new ItemType(i != null ? i : new ItemStack(Material.AIR, 1));
      });
      Converters.registerConverter(Block.class, InventoryHolder.class, b -> {
         BlockState s = b.getState();
         return s instanceof InventoryHolder ? (InventoryHolder)s : null;
      }, 10);
      Converters.registerConverter(InventoryHolder.class, Block.class, holder -> {
         if (holder instanceof BlockState) {
            return new BlockInventoryHolder((BlockState)holder);
         } else {
            return holder instanceof DoubleChest ? holder.getInventory().getLocation().getBlock() : null;
         }
      }, 3);
      Converters.registerConverter(InventoryHolder.class, Entity.class, holder -> holder instanceof Entity entity ? entity : null, 3);
      Converters.registerConverter(Entity.class, InventoryHolder.class, entity -> entity instanceof InventoryHolder inventoryHolder ? inventoryHolder : null, 2);
      if (!SkriptConfig.useTypeProperties.value()) {
         Converters.registerConverter(OfflinePlayer.class, AnyNamed.class, player -> player::getName, 2);
         if (Skript.classExists("org.bukkit.generator.WorldInfo")) {
            Converters.registerConverter(World.class, AnyNamed.class, world -> world::getName, 2);
         } else {
            Converters.registerConverter(World.class, AnyNamed.class, world -> () -> world.getName(), 2);
         }

         Converters.registerConverter(GameRule.class, AnyNamed.class, rule -> rule::getName, 2);
         Converters.registerConverter(Server.class, AnyNamed.class, server -> server::getName, 2);
         Converters.registerConverter(Plugin.class, AnyNamed.class, plugin -> plugin::getName, 2);
         Converters.registerConverter(WorldType.class, AnyNamed.class, type -> type::getName, 2);
         Converters.registerConverter(Team.class, AnyNamed.class, team -> team::getName, 2);
         Converters.registerConverter(Objective.class, AnyNamed.class, objective -> objective::getName, 2);
         Converters.registerConverter(Nameable.class, AnyNamed.class, nameable -> new AnyNamed() {
            @Nullable
            @Override
            public String name() {
               return nameable.getCustomName();
            }

            @Nullable
            @Override
            public Component nameComponent() {
               return nameable.customName();
            }

            @Override
            public boolean supportsNameChange() {
               return true;
            }

            @Override
            public void setName(String name) {
               nameable.setCustomName(name);
            }

            @Override
            public void setName(Component name) {
               nameable.customName(name);
            }
         }, 2);
         Converters.registerConverter(Block.class, AnyNamed.class, block -> new AnyNamed() {
            @Nullable
            @Override
            public String name() {
               return block.getState() instanceof Nameable nameable ? nameable.getCustomName() : null;
            }

            @Nullable
            @Override
            public Component nameComponent() {
               return block.getState() instanceof Nameable nameable ? nameable.customName() : null;
            }

            @Override
            public boolean supportsNameChange() {
               return true;
            }

            @Override
            public void setName(String name) {
               BlockState state = block.getState();
               if (state instanceof Nameable nameable) {
                  nameable.setCustomName(name);
                  state.update(true, false);
               }
            }

            @Override
            public void setName(Component name) {
               BlockState state = block.getState();
               if (state instanceof Nameable nameable) {
                  nameable.customName(name);
                  state.update(true, false);
               }
            }
         }, 2);
         Converters.registerConverter(CommandSender.class, AnyNamed.class, thing -> thing::getName, 2);
         Converters.registerConverter(ItemStack.class, AnyAmount.class, item -> new AnyAmount() {
            @NotNull
            @Override
            public Number amount() {
               return item.getAmount();
            }

            @Override
            public boolean supportsAmountChange() {
               return true;
            }

            @Override
            public void setAmount(Number amount) {
               item.setAmount(amount != null ? amount.intValue() : 0);
            }
         }, 2);
      }

      Converters.registerConverter(Vector.class, Direction.class, Direction::new);
      Converters.registerConverter(String.class, World.class, Bukkit::getWorld);
      if (Skript.classExists("org.bukkit.entity.EntitySnapshot")) {
         Converters.registerConverter(EntitySnapshot.class, EntityData.class, snapshot -> EntityUtils.toSkriptEntityData(snapshot.getEntityType()));
      }

      Converters.registerConverter(Script.class, Config.class, Script::getConfig);
      Converters.registerConverter(Config.class, Node.class, Config::getMainNode);
      Converters.registerConverter(UUID.class, String.class, UUID::toString);
   }
}
