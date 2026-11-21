package de.oliver.fancynpcs.commands.arguments;

import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.actions.NpcAction;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import org.bukkit.command.CommandSender;

public class ActionTypeArgument {
   public static final ActionTypeArgument INSTANCE = new ActionTypeArgument();
   private static final FancyNpcs PLUGIN = FancyNpcs.getInstance();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   private ActionTypeArgument() {
   }

   @Parser(
      name = "",
      suggestions = "action_type"
   )
   public NpcAction parse(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString();
      NpcAction action = PLUGIN.getActionManager().getActionByName(value);
      if (action == null) {
         throw ReplyingParseException.replying(
            () -> this.translator.translate("command_invalid_action_type").withPrefix().replaceStripped("input", value).send(context.sender())
         );
      } else {
         return action;
      }
   }

   @Suggestions("action_type")
   public Iterable<String> suggestions(final CommandContext<CommandSender> context, final CommandInput input) {
      return PLUGIN.getActionManager().getAllActions().stream().map(NpcAction::getName).filter(name -> !name.equalsIgnoreCase("player_command_as_op")).toList();
   }
}
