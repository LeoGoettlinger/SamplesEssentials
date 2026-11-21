package de.oliver.fancynpcs.commands.npc;

import de.oliver.cloud.annotations.Argument;
import de.oliver.cloud.annotations.Command;
import de.oliver.cloud.annotations.Permission;
import de.oliver.cloud.annotations.parser.Parser;
import de.oliver.cloud.annotations.suggestion.Suggestions;
import de.oliver.cloud.context.CommandContext;
import de.oliver.cloud.context.CommandInput;
import de.oliver.fancylib.translations.Translator;
import de.oliver.fancynpcs.FancyNpcs;
import de.oliver.fancynpcs.api.AttributeManager;
import de.oliver.fancynpcs.api.Npc;
import de.oliver.fancynpcs.api.NpcAttribute;
import de.oliver.fancynpcs.api.events.NpcModifyEvent;
import de.oliver.fancynpcs.commands.exceptions.ReplyingParseException;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public enum AttributeCMD {
   INSTANCE;

   private final Translator translator = FancyNpcs.getInstance().getTranslator();
   private final AttributeManager attributeManager = FancyNpcs.getInstance().getAttributeManager();

   @Command("npc attribute <npc> set <attribute> <attributeValue>")
   @Permission({"fancynpcs.command.npc.attribute.set"})
   public void onAttributeSet(
      @NotNull final CommandSender sender,
      @NotNull final Npc npc,
      @NotNull final NpcAttribute attribute,
      @Argument(parserName = "AttributeCMD/attribute_value") @NotNull final String attributeValue
   ) {
      if (new NpcModifyEvent(npc, NpcModifyEvent.NpcModification.ATTRIBUTE, new Object[]{attribute, attributeValue}, sender).callEvent()) {
         npc.getData().addAttribute(attribute, attributeValue);
         npc.updateForAll();
         this.translator
            .translate("npc_attribute_set")
            .withPrefix()
            .replace("attribute", attribute.getName())
            .replaceStripped("value", attributeValue.toLowerCase())
            .send(sender);
      } else {
         this.translator.translate("command_npc_modification_cancelled").withPrefix().send(sender);
      }
   }

   @Command("npc attribute <npc> list")
   @Permission({"fancynpcs.command.npc.attribute.list"})
   public void onAttributeList(@NotNull final CommandSender sender, @NotNull final Npc npc) {
      if (npc.getData().getAttributes().isEmpty()) {
         this.translator.translate("npc_attribute_list_failure_empty").withPrefix().send(sender);
      } else {
         this.translator.translate("npc_attribute_list_header").send(sender);
         npc.getData()
            .getAttributes()
            .forEach(
               (attribute, value) -> this.translator
                  .translate("npc_attribute_list_entry")
                  .replace("attribute", attribute.getName())
                  .replace("value", value)
                  .send(sender)
            );
         this.translator.translate("npc_attribute_list_footer").send(sender);
      }
   }

   @Parser(
      name = "",
      suggestions = "AttributeCMD/attribute"
   )
   public NpcAttribute parseAttribute(final CommandContext<CommandSender> context, final CommandInput input) {
      Npc npc = context.get("npc");
      String value = input.readString();
      NpcAttribute attribute = this.attributeManager.getAttributeByName(npc.getData().getType(), value);
      if (attribute == null) {
         throw ReplyingParseException.replying(
            () -> this.translator.translate("command_invalid_attribute").withPrefix().replaceStripped("input", value).send(context.sender())
         );
      } else {
         return attribute;
      }
   }

   @Parser(
      name = "AttributeCMD/attribute_value",
      suggestions = "AttributeCMD/attribute_value"
   )
   public String parseAttributeValue(final CommandContext<CommandSender> context, final CommandInput input) {
      NpcAttribute attribute = context.get("attribute");
      String value = input.read(input.remainingLength());
      if (!attribute.isValidValue(value)) {
         throw ReplyingParseException.replying(
            () -> this.translator.translate("command_invalid_attribute_value").withPrefix().replaceStripped("input", value).send(context.sender())
         );
      } else {
         return value;
      }
   }

   @Suggestions("AttributeCMD/attribute")
   public List<String> suggestAttribute(final CommandContext<CommandSender> context, final CommandInput input) {
      Npc npc = context.getOrDefault("npc", null);
      return this.attributeManager.getAllAttributesForEntityType(npc.getData().getType()).stream().map(NpcAttribute::getName).toList();
   }

   @Suggestions("AttributeCMD/attribute_value")
   public List<String> suggestAttributeValue(final CommandContext<CommandSender> context, final CommandInput input) {
      Npc npc = context.get("npc");
      NpcAttribute attribute = context.get("attribute");
      return this.attributeManager.getAttributeByName(npc.getData().getType(), attribute.getName()).getPossibleValues();
   }
}
