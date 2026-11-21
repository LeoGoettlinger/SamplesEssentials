package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.command.factory.ItemUseFactory;
import com.sk89q.worldedit.command.factory.ReplaceFactory;
import com.sk89q.worldedit.command.factory.TreeGeneratorFactory;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.factory.ApplyRegion;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Direction;
import com.sk89q.worldedit.internal.command.CommandRegistrationHandler;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.generation.TreeType;
import java.util.Objects;
import java.util.stream.Collectors;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.CommandManagerService;
import org.enginehub.piston.CommandParameters;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.part.CommandArgument;
import org.enginehub.piston.part.CommandParts;
import org.enginehub.piston.part.SubCommandPart;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class ApplyBrushCommands {
   private static final CommandArgument REGION_FACTORY = CommandParts.arg(Caption.of("shape"), Caption.of("worldedit.brush.apply.shape"))
      .defaultsTo(ImmutableList.of())
      .ofTypes(ImmutableList.of(Key.of(RegionFactory.class)))
      .build();
   private static final CommandArgument RADIUS = CommandParts.arg(Caption.of("radius"), Caption.of("worldedit.brush.apply.radius"))
      .defaultsTo(ImmutableList.of("5"))
      .ofTypes(ImmutableList.of(Key.of(double.class)))
      .build();

   public static void register(CommandManagerService service, CommandManager commandManager, CommandRegistrationHandler registration) {
      commandManager.register(
         "apply",
         builder -> {
            builder.description(Caption.of("worldedit.brush.apply.description"));
            builder.action(Command.Action.NULL_ACTION);
            CommandManager manager = service.newCommandManager();
            registration.register(manager, ApplyBrushCommandsRegistration.builder(), new ApplyBrushCommands());
            builder.condition(new PermissionCondition(ImmutableSet.of("worldedit.brush.apply")));
            builder.addParts(REGION_FACTORY, RADIUS);
            builder.addPart(
               SubCommandPart.builder(Caption.of("type"), Caption.of("worldedit.brush.apply.type"))
                  .withCommands(manager.getAllCommands().collect(Collectors.toList()))
                  .required()
                  .build()
            );
         }
      );
   }

   private void setApplyBrush(CommandParameters parameters, Player player, LocalSession localSession, Contextual<? extends RegionFunction> generatorFactory) throws WorldEditException {
      double radius = Objects.requireNonNull(RADIUS.value(parameters).asSingle(double.class));
      RegionFactory regionFactory = REGION_FACTORY.value(parameters).asSingle(RegionFactory.class);
      BrushCommands.setOperationBasedBrush(
         player, localSession, Expression.compile(Double.toString(radius)), new ApplyRegion(generatorFactory), regionFactory, "worldedit.brush.apply"
      );
   }

   @org.enginehub.piston.annotation.Command(
      name = "forest",
      desc = "Plant trees"
   )
   public void forest(CommandParameters parameters, Player player, LocalSession localSession, @Arg(desc = "The type of tree to plant") TreeType type) throws WorldEditException {
      this.setApplyBrush(parameters, player, localSession, new TreeGeneratorFactory(type));
   }

   @org.enginehub.piston.annotation.Command(
      name = "item",
      desc = "Use an item"
   )
   @CommandPermissions({"worldedit.brush.item"})
   public void item(
      CommandParameters parameters,
      Player player,
      LocalSession localSession,
      @Arg(desc = "The type of item to use") BaseItem item,
      @Direction(includeDiagonals = true) @Arg(desc = "The direction in which the item will be applied",def = {"up"}) com.sk89q.worldedit.util.Direction direction
   ) throws WorldEditException {
      player.print(TextComponent.builder().append("WARNING: ").append(Caption.of("worldedit.brush.apply.item.warning")).build());
      this.setApplyBrush(parameters, player, localSession, new ItemUseFactory(item, direction));
   }

   @org.enginehub.piston.annotation.Command(
      name = "set",
      desc = "Place a block"
   )
   public void set(CommandParameters parameters, Player player, LocalSession localSession, @Arg(desc = "The pattern of blocks to use") Pattern pattern) throws WorldEditException {
      this.setApplyBrush(parameters, player, localSession, new ReplaceFactory(pattern));
   }
}
