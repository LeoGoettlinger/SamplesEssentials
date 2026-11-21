package de.oliver.fancynpcs.commands.arguments;

import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.actions.ActionTrigger;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import java.util.List;
import org.bukkit.command.CommandSender;

public class ActionTriggerArgument {
   public static final ActionTriggerArgument INSTANCE = new ActionTriggerArgument();
   private final Translator translator = FancyNpcs.getInstance().getTranslator();

   private ActionTriggerArgument() {
   }

   @Parser(
      name = "",
      suggestions = "action_trigger"
   )
   public ActionTrigger parse(final CommandContext<CommandSender> context, final CommandInput input) {
      String value = input.readString();
      ActionTrigger trigger = ActionTrigger.getByName(value);
      if (trigger == null) {
         throw ReplyingParseException.replying(
            () -> this.translator.translate("command_invalid_action_trigger").withPrefix().replaceStripped("input", value).send(context.sender())
         );
      } else {
         return trigger;
      }
   }

   @Suggestions("action_trigger")
   public List<String> suggestions(final CommandContext<CommandSender> context, final CommandInput input) {
      return List.of(
         ActionTrigger.ANY_CLICK.name().toLowerCase(), ActionTrigger.LEFT_CLICK.name().toLowerCase(), ActionTrigger.RIGHT_CLICK.name().toLowerCase()
      );
   }
}
