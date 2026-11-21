package ac.grim.grimac.shaded.incendo.cloud.help;

import ac.grim.grimac.shaded.geantyref.GenericTypeReflector;
import ac.grim.grimac.shaded.incendo.cloud.Command;
import ac.grim.grimac.shaded.incendo.cloud.CommandManager;
import ac.grim.grimac.shaded.incendo.cloud.component.CommandComponent;
import ac.grim.grimac.shaded.incendo.cloud.help.result.CommandEntry;
import ac.grim.grimac.shaded.incendo.cloud.help.result.HelpQueryResult;
import ac.grim.grimac.shaded.incendo.cloud.help.result.IndexCommandResult;
import ac.grim.grimac.shaded.incendo.cloud.help.result.MultipleCommandResult;
import ac.grim.grimac.shaded.incendo.cloud.help.result.VerboseCommandResult;
import ac.grim.grimac.shaded.incendo.cloud.internal.CommandInputTokenizer;
import ac.grim.grimac.shaded.incendo.cloud.internal.CommandNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = Status.STABLE
)
public class StandardHelpHandler<C> implements HelpHandler<C> {
   private final CommandManager<C> commandManager;
   private final CommandPredicate<C> commandFilter;

   public StandardHelpHandler(@NonNull final CommandManager<C> commandManager, @NonNull final CommandPredicate<C> commandPredicate) {
      this.commandManager = commandManager;
      this.commandFilter = commandPredicate;
   }

   @NonNull
   @Override
   public HelpQueryResult<C> query(@NonNull final HelpQuery<C> query) {
      List<CommandEntry<C>> commands = this.commands(query.sender());
      if (query.query().replace(" ", "").isEmpty()) {
         return IndexCommandResult.of(query, commands);
      } else {
         List<String> queryFragments = new CommandInputTokenizer(query.query()).tokenize();
         String rootFragment = queryFragments.get(0);
         List<Command<C>> availableCommands = new LinkedList<>();
         Set<String> availableCommandLabels = new HashSet<>();
         boolean exactMatch = false;

         for (CommandEntry<C> entry : commands) {
            Command<C> command = entry.command();
            CommandComponent<C> component = command.rootComponent();

            for (String alias : component.aliases()) {
               if (alias.toLowerCase(Locale.ENGLISH).startsWith(rootFragment.toLowerCase(Locale.ENGLISH))) {
                  availableCommands.add(command);
                  availableCommandLabels.add(component.name());
                  break;
               }
            }

            for (String aliasx : component.aliases()) {
               if (aliasx.equalsIgnoreCase(rootFragment)) {
                  exactMatch = true;
                  break;
               }
            }

            if (rootFragment.equalsIgnoreCase(component.name())) {
               availableCommandLabels.clear();
               availableCommands.clear();
               availableCommandLabels.add(component.name());
               availableCommands.add(command);
               break;
            }
         }

         if (availableCommands.isEmpty()) {
            return IndexCommandResult.of(query, Collections.emptyList());
         } else if (exactMatch && availableCommandLabels.size() <= 1) {
            CommandNode<C> node = this.commandManager.commandTree().getNamedNode(availableCommandLabels.iterator().next());
            List<CommandComponent<C>> traversedNodes = new LinkedList<>();
            CommandNode<C> head = node;
            int index = 0;

            label112:
            while (head != null && this.isNodeVisible(head)) {
               index++;
               traversedNodes.add(head.component());
               if (head.component() != null
                  && head.command() != null
                  && (head.isLeaf() || index == queryFragments.size())
                  && this.isAllowed(query.sender(), head.command())) {
                  return VerboseCommandResult.of(
                     query,
                     CommandEntry.of(head.command(), this.commandManager.commandSyntaxFormatter().apply(query.sender(), head.command().components(), null))
                  );
               }

               if (head.children().size() == 1) {
                  head = head.children().get(0);
               } else {
                  if (index < queryFragments.size()) {
                     CommandNode<C> potentialVariable = null;

                     for (CommandNode<C> child : head.children()) {
                        if (child.component() != null && child.component().type() == CommandComponent.ComponentType.LITERAL) {
                           for (String childAlias : child.component().aliases()) {
                              if (childAlias.equalsIgnoreCase(queryFragments.get(index))) {
                                 head = child;
                                 continue label112;
                              }
                           }
                        } else if (child.component() != null) {
                           potentialVariable = child;
                        }
                     }

                     if (potentialVariable != null) {
                        head = potentialVariable;
                        continue;
                     }
                  }

                  String currentDescription = this.commandManager.commandSyntaxFormatter().apply(query.sender(), traversedNodes, null);
                  List<String> childSuggestions = new LinkedList<>();

                  for (CommandNode<C> childx : head.children()) {
                     if (this.isNodeVisible(childx)) {
                        List<CommandComponent<C>> traversedNodesSub = new LinkedList<>(traversedNodes);
                        if (childx.component() == null || childx.command() == null || this.isAllowed(query.sender(), childx.command())) {
                           traversedNodesSub.add(childx.component());
                           childSuggestions.add(this.commandManager.commandSyntaxFormatter().apply(query.sender(), traversedNodesSub, childx));
                        }
                     }
                  }

                  return MultipleCommandResult.of(query, currentDescription, childSuggestions);
               }
            }

            return IndexCommandResult.of(query, Collections.emptyList());
         } else {
            return IndexCommandResult.of(
               query,
               availableCommands.stream()
                  .map(
                     command -> CommandEntry.of(
                        (Command<C>)command, this.commandManager.commandSyntaxFormatter().apply(query.sender(), command.components(), null)
                     )
                  )
                  .sorted()
                  .filter(entry -> this.isAllowed(query.sender(), entry.command()))
                  .collect(Collectors.toList())
            );
         }
      }
   }

   @NonNull
   protected List<CommandEntry<C>> commands(@NonNull final C sender) {
      return this.commandManager
         .commands()
         .stream()
         .filter(this.commandFilter)
         .filter(command -> this.isAllowed(sender, (Command<C>)command))
         .map(command -> CommandEntry.of((Command<C>)command, this.commandManager.commandSyntaxFormatter().apply(sender, command.components(), null)))
         .sorted()
         .collect(Collectors.toList());
   }

   private boolean isAllowed(final C sender, final Command<C> command) {
      return command.senderType().isPresent() && !GenericTypeReflector.isSuperType(command.senderType().get().getType(), sender.getClass())
         ? false
         : this.commandManager.testPermission(sender, command.commandPermission()).allowed();
   }

   protected boolean isNodeVisible(@NonNull final CommandNode<C> node) {
      CommandComponent<C> component = node.component();
      if (component != null) {
         Command<C> owningCommand = node.command();
         if (owningCommand != null && this.commandFilter.test(owningCommand)) {
            return true;
         }
      }

      for (CommandNode<C> childNode : node.children()) {
         if (this.isNodeVisible(childNode)) {
            return true;
         }
      }

      return false;
   }
}
