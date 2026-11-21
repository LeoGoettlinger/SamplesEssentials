package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotation.specifier.Greedy;
import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.api.actions.NpcAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ActionCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   @Command("npc action <npc> <trigger> add <actionType> [value]")
   @Permission({"fancynpcs.command.npc.action.add"})
   public void onActionAdd(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @NotNull final NpcAction actionType,
      @Greedy @Nullable final String value
   ) {
      if (this.checkAddPermissions(sender, actionType)) {
         if (value != null && DisplayNameCMD.INSTANCE.hasBlockedCommands(value)) {
            this.translator.translate("command_input_contains_blocked_command").withPrefix().send(sender);
         } else if (!actionType.requiresValue() || value != null && !value.isEmpty()) {
            if (actionType.getName().equalsIgnoreCase("player_command_as_op")) {
               this.translator.translate("npc_action_add_op_warning").withPrefix().send(sender);
            } else {
               List<NpcAction.NpcActionData> currentActions = npc.getData().getActions().getOrDefault(trigger, new ArrayList<>());
               npc.getData().addAction(trigger, currentActions.size() + 1, actionType, value);
               this.translator
                  .translate("npc_action_add_success")
                  .withPrefix()
                  .replaceStripped("total", String.valueOf(npc.getData().getActions(trigger).size()))
                  .send(sender);
            }
         } else {
            this.translator.translate("npc_action_requires_value").withPrefix().send(sender);
         }
      }
   }

   @Command("npc action <npc> <trigger> add_before <index> <actionType> [value]")
   @Permission({"fancynpcs.command.npc.action.addBefore"})
   public void onActionAddBefore(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") final int index,
      @NotNull final NpcAction actionType,
      @Greedy @Nullable final String value
   ) {
      if (this.checkAddPermissions(sender, actionType)) {
         if (value != null && DisplayNameCMD.INSTANCE.hasBlockedCommands(value)) {
            this.translator.translate("command_input_contains_blocked_command").withPrefix().send(sender);
         } else if (!actionType.requiresValue() || value != null && !value.isEmpty()) {
            if (actionType.getName().equalsIgnoreCase("player_command_as_op")) {
               this.translator.translate("npc_action_add_op_warning").withPrefix().send(sender);
            } else {
               List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
               currentActions.add(Math.clamp((long)(index - 1), 0, currentActions.size()), new NpcAction.NpcActionData(index, actionType, value));
               npc.getData().setActions(trigger, this.reorderActions(currentActions));
               this.translator
                  .translate("npc_action_add_before_success")
                  .withPrefix()
                  .replaceStripped("number", String.valueOf(index))
                  .replaceStripped("total", String.valueOf(npc.getData().getActions(trigger).size()))
                  .send(sender);
            }
         } else {
            this.translator.translate("npc_action_requires_value").withPrefix().send(sender);
         }
      }
   }

   @Command("npc action <npc> <trigger> add_after <index> <actionType> [value]")
   @Permission({"fancynpcs.command.npc.action.addAfter"})
   public void onActionAddAfter(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") final int index,
      @NotNull final NpcAction actionType,
      @Greedy @Nullable final String value
   ) {
      if (this.checkAddPermissions(sender, actionType)) {
         if (value != null && DisplayNameCMD.INSTANCE.hasBlockedCommands(value)) {
            this.translator.translate("command_input_contains_blocked_command").withPrefix().send(sender);
         } else if (!actionType.requiresValue() || value != null && !value.isEmpty()) {
            if (actionType.getName().equalsIgnoreCase("player_command_as_op")) {
               this.translator.translate("npc_action_add_op_warning").withPrefix().send(sender);
            } else {
               List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
               currentActions.add(Math.clamp((long)index, 0, currentActions.size() + 1), new NpcAction.NpcActionData(index, actionType, value));
               npc.getData().setActions(trigger, this.reorderActions(currentActions));
               this.translator
                  .translate("npc_action_add_after_success")
                  .withPrefix()
                  .replaceStripped("number", String.valueOf(index))
                  .replaceStripped("total", String.valueOf(npc.getData().getActions(trigger).size()))
                  .send(sender);
            }
         } else {
            this.translator.translate("npc_action_requires_value").withPrefix().send(sender);
         }
      }
   }

   @Command("npc action <npc> <trigger> set <number> <actionType> [value]")
   @Permission({"fancynpcs.command.npc.action.set"})
   public void onActionSet(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") @NotNull final Integer number,
      @NotNull final NpcAction actionType,
      @Greedy @Nullable final String value
   ) {
      if (this.checkAddPermissions(sender, actionType)) {
         if (value != null && DisplayNameCMD.INSTANCE.hasBlockedCommands(value)) {
            this.translator.translate("command_input_contains_blocked_command").withPrefix().send(sender);
         } else if (!actionType.requiresValue() || value != null && !value.isEmpty()) {
            if (actionType.getName().equalsIgnoreCase("player_command_as_op")) {
               this.translator.translate("npc_action_add_op_warning").withPrefix().send(sender);
            } else {
               List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
               if (number >= 1 && number <= currentActions.size()) {
                  currentActions.set(number - 1, new NpcAction.NpcActionData(number, actionType, value));
                  npc.getData().setActions(trigger, currentActions);
                  this.translator
                     .translate("npc_action_set_success")
                     .withPrefix()
                     .replaceStripped("number", String.valueOf(number))
                     .replaceStripped("total", String.valueOf(npc.getData().getActions(trigger).size()))
                     .send(sender);
               } else {
                  this.translator.translate("npc_action_set_failure").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
               }
            }
         } else {
            this.translator.translate("npc_action_requires_value").withPrefix().send(sender);
         }
      }
   }

   @Command("npc action <npc> <trigger> remove <number>")
   @Permission({"fancynpcs.command.npc.action.remove"})
   public void onActionRemove(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") final int number
   ) {
      List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
      if (number >= 1 && number <= currentActions.size()) {
         currentActions.remove(number - 1);
         npc.getData().setActions(trigger, this.reorderActions(currentActions));
         this.translator
            .translate("npc_action_remove_success")
            .withPrefix()
            .replaceStripped("number", String.valueOf(number))
            .replaceStripped("total", String.valueOf(npc.getData().getActions(trigger).size()))
            .send(sender);
      } else {
         this.translator.translate("npc_action_remove_failure").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
      }
   }

   @Command("npc action <npc> <trigger> move_up <number>")
   @Permission({"fancynpcs.command.npc.action.moveUp"})
   public void onActionMoveUp(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") final int number
   ) {
      List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
      if (number <= 1) {
         this.translator.translate("npc_action_move_up_failure").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
      } else {
         NpcAction.NpcActionData action = currentActions.get(number - 1);
         currentActions.remove(number - 1);
         currentActions.add(number - 2, action);
         npc.getData().setActions(trigger, this.reorderActions(currentActions));
         this.translator.translate("npc_action_move_up_success").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
      }
   }

   @Command("npc action <npc> <trigger> move_down <number>")
   @Permission({"fancynpcs.command.npc.action.moveDown"})
   public void onActionMoveDown(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final ActionTrigger trigger,
      @Argument(suggestions = "ActionCMD/number_range") final int number
   ) {
      List<NpcAction.NpcActionData> currentActions = npc.getData().getActions(trigger);
      if (number >= currentActions.size()) {
         this.translator.translate("npc_action_move_down_failure").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
      } else {
         NpcAction.NpcActionData action = currentActions.get(number - 1);
         currentActions.remove(number - 1);
         currentActions.add(number, action);
         npc.getData().setActions(trigger, this.reorderActions(currentActions));
         this.translator.translate("npc_action_move_down_success").withPrefix().replaceStripped("number", String.valueOf(number)).send(sender);
      }
   }

   @Command("npc action <npc> <trigger> clear")
   @Permission({"fancynpcs.command.npc.action.clear"})
   public void onActionClear(@NotNull final CommandSender sender, @NotNull final Npc npc, @NotNull final ActionTrigger trigger) {
      npc.getData().setActions(trigger, new ArrayList<>());
      this.translator.translate("npc_action_clear_success").withPrefix().send(sender);
   }

   @Command("npc action <npc> <trigger> list")
   @Permission({"fancynpcs.command.npc.action.list"})
   public void onActionList(@NotNull final CommandSender sender, @NotNull final Npc npc, @NotNull final ActionTrigger trigger) {
      List<NpcAction.NpcActionData> actions = npc.getData().getActions(trigger);
      if (actions.isEmpty()) {
         this.translator.translate("npc_action_list_failure_empty").withPrefix().send(sender);
      } else {
         this.translator.translate("npc_action_list_header").replaceStripped("trigger", trigger.name()).send(sender);

         for (int i = 0; i < actions.size(); i++) {
            NpcAction.NpcActionData action = actions.get(i);
            this.translator
               .translate("npc_action_list_entry")
               .replaceStripped("number", String.valueOf(action.order()))
               .replaceStripped("action", action.action().getName())
               .replaceStripped("value", action.value() != null ? action.value() : "")
               .send(sender);
         }

         this.translator.translate("npc_action_list_footer").replaceStripped("total", String.valueOf(actions.size())).send(sender);
      }
   }

   private List<NpcAction.NpcActionData> reorderActions(List<NpcAction.NpcActionData> actions) {
      List<NpcAction.NpcActionData> newActions = new ArrayList<>();

      for (int i = 0; i < actions.size(); i++) {
         NpcAction.NpcActionData a = actions.get(i);
         newActions.add(new NpcAction.NpcActionData(i + 1, a.action(), a.value()));
      }

      return newActions;
   }

   private boolean checkAddPermissions(final CommandSender sender, final NpcAction action) {
      boolean hasGeneralPerms = sender.hasPermission("fancynpcs.command.npc.action.add.*");
      boolean hasSpecificPerms = sender.hasPermission("fancynpcs.command.npc.action.add." + action.getName());
      if (!hasGeneralPerms && !hasSpecificPerms) {
         this.translator.translate("action_missing_permissions").withPrefix().send(sender);
         return false;
      } else {
         return true;
      }
   }

   @Suggestions("ActionCMD/number_range")
   public List<String> suggestNumber(final CommandContext<CommandSender> context, final CommandInput input) {
      Npc npc = context.getOrDefault("npc", null);
      ActionTrigger trigger = context.getOrDefault("trigger", null);
      if (npc != null && trigger != null) {
         List<String> suggestions = new ArrayList<>();

         for (int i = 0; i < npc.getData().getActions(trigger).size(); i++) {
            suggestions.add(String.valueOf(i + 1));
         }

         return suggestions;
      } else {
         return Collections.emptyList();
      }
   }
}
