package com.artillexstudios.axtrade.libs.lamp.bukkit.brigadier;

import com.artillexstudios.axtrade.libs.lamp.CommandHandler;
import com.artillexstudios.axtrade.libs.lamp.bukkit.BukkitBrigadier;
import com.artillexstudios.axtrade.libs.lamp.bukkit.annotation.LiteralEnum;
import com.artillexstudios.axtrade.libs.lamp.bukkit.core.BukkitHandler;
import com.artillexstudios.axtrade.libs.lamp.command.ArgumentStack;
import com.artillexstudios.axtrade.libs.lamp.command.CommandActor;
import com.artillexstudios.axtrade.libs.lamp.command.CommandCategory;
import com.artillexstudios.axtrade.libs.lamp.command.CommandParameter;
import com.artillexstudios.axtrade.libs.lamp.command.ExecutableCommand;
import com.artillexstudios.axtrade.libs.lamp.command.trait.PermissionHolder;
import com.artillexstudios.axtrade.libs.lamp.core.EitherParameter;
import com.artillexstudios.axtrade.libs.lamp.exception.ArgumentParseException;
import com.artillexstudios.axtrade.libs.lamp.util.Either;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class NodeParser {
   private final BukkitBrigadier brigadier;

   public NodeParser(BukkitBrigadier brigadier) {
      this.brigadier = brigadier;
   }

   private Node createNode(ArgumentBuilder<?, ?> builder) {
      return Node.from(builder);
   }

   public List<Node> parse(CommandHandler handler) {
      List<Node> nodes = new ArrayList<>();

      for (CommandCategory category : handler.getCategories().values()) {
         if (category.getPath().isRoot()) {
            nodes.add(this.create(category));
         }
      }

      for (ExecutableCommand command : handler.getCommands().values()) {
         if (command.getPath().isRoot()) {
            nodes.add(this.create(command));
         }
      }

      return nodes;
   }

   private List<Node> createNodes(CommandParameter parameter) {
      if (parameter.isSwitch()) {
         String switchLiteral = parameter.getCommandHandler().getSwitchPrefix() + parameter.getSwitchName();
         return Collections.singletonList(this.createNode(LiteralArgumentBuilder.literal(switchLiteral)));
      } else if (parameter.getType() == Either.class) {
         EitherParameter[] either = EitherParameter.create(parameter);
         List<Node> first = this.createNodes(either[0]);
         List<Node> second = this.createNodes(either[1]);
         return Stream.concat(first.stream(), second.stream()).collect(Collectors.toList());
      } else if (parameter.getType().isEnum() && (parameter.hasAnnotation(LiteralEnum.class) || this.brigadier.isShowEnumsAsNativeLiterals())) {
         List<Node> nodes = new ArrayList<>();

         for (Object v : parameter.getType().getEnumConstants()) {
            String name = ((Enum)v).name().toLowerCase();
            LiteralArgumentBuilder<Object> literal = LiteralArgumentBuilder.literal(name);
            nodes.add(Node.from(literal));
         }

         return nodes;
      } else {
         ExecutableCommand command = parameter.getDeclaringCommand();
         ArgumentType<?> argumentType = this.brigadier.getArgumentType(parameter);
         boolean isLast = parameter.getCommandIndex() == command.getValueParameters().size() - 1;
         Node node = this.createNode(RequiredArgumentBuilder.argument(parameter.getName(), argumentType));
         node.require(this.generateRequirement(parameter));
         node.suggest(createSuggestionProvider(this.brigadier, parameter));
         if (isLast) {
            node.canBeExecuted(this.brigadier);
         }

         return Collections.singletonList(node);
      }
   }

   public Node create(CommandCategory category) {
      Node node = this.createNode(LiteralArgumentBuilder.literal(category.getName()));
      node.require(this.generateRequirement(category));

      for (CommandCategory subcategory : category.getCategories().values()) {
         Node subNode = this.create(subcategory);
         node.addChild(subNode);
      }

      for (ExecutableCommand subcommands : category.getCommands().values()) {
         Node subNode = this.create(subcommands);
         node.addChild(subNode);
      }

      if (category.getDefaultAction() != null) {
         this.addExecutables(category.getDefaultAction(), node);
      }

      return node;
   }

   private Predicate<Object> generateRequirement(PermissionHolder holder) {
      return sender -> holder.getPermission().canExecute(this.brigadier.wrapSource(sender));
   }

   public Node create(ExecutableCommand command) {
      Node node = this.createNode(LiteralArgumentBuilder.literal(command.getName()));
      node.require(this.generateRequirement(command));
      this.addExecutables(command, node);
      return node;
   }

   public void addExecutables(ExecutableCommand command, Node targetNode) {
      if (command.getValueParameters().isEmpty()) {
         targetNode.canBeExecuted(this.brigadier);
      } else {
         this.addParameterNodes(command, targetNode);
      }
   }

   private void addParameterNodes(ExecutableCommand command, Node targetNode) {
      ArrayList<Node> lastNodes = (ArrayList<Node>)com.artillexstudios.axtrade.libs.lamp.util.Collections.<Node>listOf(targetNode);

      for (CommandParameter parameter : new ArrayList<>(command.getValueParameters().values())) {
         if (parameter.isFlag()) {
            this.addFlagParameter(parameter, lastNodes);
         } else {
            if (parameter.isOptional() || parameter.isSwitch()) {
               lastNodes.forEach(lastNode -> lastNode.canBeExecuted(this.brigadier));
            }

            List<Node> paramNodes = this.createNodes(parameter);
            if (paramNodes != null && !paramNodes.isEmpty()) {
               lastNodes.forEach(lastNode -> lastNode.addChildren(paramNodes));
               lastNodes.clear();
               lastNodes.addAll(paramNodes);
            }
         }
      }
   }

   private void addFlagParameter(CommandParameter parameter, ArrayList<Node> lastNodes) {
      Node flagLiteral = this.createNode(LiteralArgumentBuilder.literal(parameter.getCommandHandler().getFlagPrefix() + parameter.getFlagName()));
      flagLiteral.require(this.generateRequirement(parameter));
      if (parameter.isOptional()) {
         lastNodes.forEach(lastNode -> lastNode.canBeExecuted(this.brigadier));
      }

      List<Node> flagNodes = this.createNodes(parameter);
      flagLiteral.addChildren(flagNodes);
      lastNodes.forEach(lastNode -> lastNode.addChild(flagLiteral));
      lastNodes.clear();
      lastNodes.addAll(flagNodes);
   }

   private static SuggestionProvider<Object> createSuggestionProvider(BukkitBrigadier brigadier, CommandParameter parameter) {
      if (parameter.getSuggestionProvider() == com.artillexstudios.axtrade.libs.lamp.autocomplete.SuggestionProvider.EMPTY) {
         return null;
      } else {
         return brigadier.isNativePlayerCompletionEnabled() && parameter.getSuggestionProvider() == BukkitHandler.playerSuggestionProvider
            ? null
            : (context, builder) -> {
               try {
                  CommandActor actor = brigadier.wrapSource(context.getSource());
                  String tooltipMessage = parameter.getDescription() == null ? parameter.getName() : parameter.getDescription();
                  Message tooltip = new LiteralMessage(tooltipMessage);
                  String input = context.getInput();

                  try {
                     ArgumentStack args = ArgumentStack.parseForAutoCompletion(input.startsWith("/") ? input.substring(1) : input);
                     parameter.getSuggestionProvider()
                        .getSuggestions(args, actor, parameter.getDeclaringCommand())
                        .stream()
                        .filter(c -> c.toLowerCase().startsWith(args.getLast().toLowerCase()))
                        .sorted(String.CASE_INSENSITIVE_ORDER)
                        .distinct()
                        .forEach(c -> builder.suggest(c, tooltip));
                  } catch (ArgumentParseException var9) {
                  }
               } catch (Throwable var10) {
                  var10.printStackTrace();
               }

               return builder.buildFuture();
            };
      }
   }
}
