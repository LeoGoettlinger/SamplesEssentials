package su.nightexpress.nightcore.core;

import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.language.entry.LangEnum;
import su.nightexpress.nightcore.language.entry.LangItem;
import su.nightexpress.nightcore.language.entry.LangString;
import su.nightexpress.nightcore.language.entry.LangText;
import su.nightexpress.nightcore.language.entry.LangUIButton;
import su.nightexpress.nightcore.ui.menu.click.ClickKey;
import su.nightexpress.nightcore.util.Placeholders;
import su.nightexpress.nightcore.util.bridge.wrapper.ClickEventType;
import su.nightexpress.nightcore.util.bridge.wrapper.HoverEventType;
import su.nightexpress.nightcore.util.text.tag.Tags;

@Deprecated
public class CoreLang {
   @Deprecated
   public static final LangEnum<ClickKey> CLICK_KEY = LangEnum.of("ClickKey", ClickKey.class, map -> {
      map.put(ClickKey.LEFT, "L-Click");
      map.put(ClickKey.RIGHT, "R-Click");
      map.put(ClickKey.SHIFT_LEFT, "Shift + L-Click");
      map.put(ClickKey.SHIFT_RIGHT, "Shift + R-Click");
      map.put(ClickKey.DROP_KEY, "Drop [Q]");
      map.put(ClickKey.SWAP_KEY, "Swap [F]");
      map.put(ClickKey.DRAG_N_DROP, "Drag & Drop");
   });
   public static final LangString COMMAND_ARGUMENT_FORMAT_REQUIRED = LangString.of("Command.Argument.Type.Required", Tags.LIGHT_RED.wrap("<%name%>"));
   public static final LangString COMMAND_ARGUMENT_FORMAT_OPTIONAL = LangString.of("Command.Argument.Type.Optional", Tags.LIGHT_YELLOW.wrap("[%name%]"));
   public static final LangString COMMAND_FLAG_FORMAT = LangString.of("Command.Flag.Format", Tags.LIGHT_GRAY.wrap("[%name%]"));
   public static final LangString COMMAND_ARGUMENT_NAME_GENERIC = LangString.of("Command.Argument.Name.Generic", "value");
   public static final LangString COMMAND_ARGUMENT_NAME_TYPE = LangString.of("Command.Argument.Name.Type", "type");
   public static final LangString COMMAND_ARGUMENT_NAME_NAME = LangString.of("Command.Argument.Name.Name", "name");
   public static final LangString COMMAND_ARGUMENT_NAME_PLAYER = LangString.of("Command.Argument.Name.Player", "player");
   public static final LangString COMMAND_ARGUMENT_NAME_WORLD = LangString.of("Command.Argument.Name.World", "world");
   public static final LangString COMMAND_ARGUMENT_NAME_AMOUNT = LangString.of("Command.Argument.Name.Amount", "amount");
   public static final LangString COMMAND_ARGUMENT_NAME_MATERIAL = LangString.of("Command.Argument.Name.Material", "material");
   public static final LangString COMMAND_ARGUMENT_NAME_ITEM_MATERIAL = LangString.of("Command.Argument.Name.ItemMaterial", "item type");
   public static final LangString COMMAND_ARGUMENT_NAME_BLOCK_MATERIAL = LangString.of("Command.Argument.Name.BlockMaterial", "block type");
   public static final LangString COMMAND_ARGUMENT_NAME_ENCHANTMENT = LangString.of("Command.Argument.Name.Enchantment", "enchantment");
   public static final LangText COMMAND_HELP_LIST = LangText.of(
      "Command.Help.List",
      Placeholders.TAG_NO_PREFIX,
      " ",
      "  " + Tags.YELLOW.wrap(Tags.BOLD.wrap("%name%")) + Tags.GRAY.wrap(" - ") + Tags.YELLOW.wrap(Tags.BOLD.wrap("Commands:")),
      " ",
      Tags.GRAY.wrap("  " + Tags.RED.wrap(Tags.BOLD.wrap("<>")) + " - Required, " + Tags.GREEN.wrap(Tags.BOLD.wrap("[]")) + " - Optional."),
      " ",
      "%entry%",
      " "
   );
   public static final LangString COMMAND_HELP_ENTRY = LangString.of(
      "Command.Help.Entry",
      "  " + Tags.YELLOW.wrap("/%command_label%") + " " + Tags.ORANGE.wrap("%command_usage%") + Tags.GRAY.wrap(" - %command_description%")
   );
   public static final LangString COMMAND_HELP_DESC = LangString.of("Command.Help.Desc", "Show help page.");
   public static final LangString COMMAND_CHECKPERM_DESC = LangString.of("Command.CheckPerm.Desc", "Print player permission info.");
   public static final LangString COMMAND_DUMPITEM_DESC = LangString.of("Command.DumpItem.Desc", "Print item components info.");
   public static final LangString COMMAND_RELOAD_DESC = LangString.of("Command.Reload.Desc", "Reload the plugin.");
   public static final LangText COMMAND_RELOAD_DONE = LangText.of(
      "Command.Reload.Done", Tags.LIGHT_GRAY.wrap("Plugin " + Tags.LIGHT_GREEN.wrap("reloaded") + "!")
   );
   public static final LangString TIME_DAY = LangString.of("Time.Day", "%amount%d.");
   public static final LangString TIME_HOUR = LangString.of("Time.Hour", "%amount%h.");
   public static final LangString TIME_MINUTE = LangString.of("Time.Min", "%amount%min.");
   public static final LangString TIME_SECOND = LangString.of("Time.Sec", "%amount%sec.");
   public static final LangString TIME_DELIMITER = LangString.of("Time.Delimiter", " ");
   public static final LangString OTHER_YES = LangString.of("Other.Yes", Tags.GREEN.wrap("Yes"));
   public static final LangString OTHER_NO = LangString.of("Other.No", Tags.RED.wrap("No"));
   public static final LangString OTHER_ENABLED = LangString.of("Other.Enabled", Tags.GREEN.wrap("Enabled"));
   public static final LangString OTHER_DISABLED = LangString.of("Other.Disabled", Tags.RED.wrap("Disabled"));
   public static final LangString OTHER_ANY = LangString.of("Other.Any", "Any");
   public static final LangString OTHER_NONE = LangString.of("Other.None", "None");
   public static final LangString OTHER_NEVER = LangString.of("Other.Never", "Never");
   public static final LangString OTHER_ONE_TIMED = LangString.of("Other.OneTimed", "One-Timed");
   public static final LangString OTHER_UNLIMITED = LangString.of("Other.Unlimited", "Unlimited");
   public static final LangString OTHER_INFINITY = LangString.of("Other.Infinity", "∞");
   public static final LangString ENTRY_GOOD = LangString.of("Entry.Good", Tags.GREEN.wrap("✔") + " " + Tags.GRAY.wrap("%entry%"));
   public static final LangString ENTRY_BAD = LangString.of("Entry.Bad", Tags.RED.wrap("✘") + " " + Tags.GRAY.wrap("%entry%"));
   public static final LangString ENTRY_WARN = LangString.of("Entry.Warn", Tags.ORANGE.wrap("[❗]") + " " + Tags.GRAY.wrap("%entry%"));
   public static final LangText ERROR_INVALID_PLAYER = LangText.of("Error.Invalid_Player", Tags.RED.wrap("Invalid player!"));
   public static final LangText ERROR_INVALID_WORLD = LangText.of("Error.Invalid_World", Tags.RED.wrap("Invalid world!"));
   public static final LangText ERROR_INVALID_NUMBER = LangText.of("Error.Invalid_Number", Tags.RED.wrap("Invalid number!"));
   @Deprecated
   public static final LangText ERROR_INVALID_MATERIAL = LangText.of("Error.InvalidMaterial", Tags.RED.wrap("Invalid material!"));
   @Deprecated
   public static final LangText ERROR_INVALID_ENCHANTMENT = LangText.of("Error.InvalidEnchantment", Tags.RED.wrap("Invalid enchantment!"));
   public static final LangText ERROR_NO_PERMISSION = LangText.of("Error.NoPermission", Tags.RED.wrap("You don't have permissions to do that!"));
   public static final LangText ERROR_COMMAND_PARSE_FLAG = LangText.of(
      "Error.Command.ParseFlag", Tags.LIGHT_GRAY.wrap("Invalid value " + Tags.LIGHT_RED.wrap("%value%") + " for " + Tags.LIGHT_RED.wrap("%name%") + " flag.")
   );
   public static final LangText ERROR_COMMAND_PARSE_ARGUMENT = LangText.of(
      "Error.Command.ParseArgument",
      Tags.LIGHT_GRAY.wrap("Invalid value " + Tags.LIGHT_RED.wrap("%value%") + " for " + Tags.LIGHT_RED.wrap("%name%") + " argument.")
   );
   public static final LangText ERROR_COMMAND_INVALID_PLAYER_ARGUMENT = LangText.of(
      "Error.Command.Argument.InvalidPlayer", Tags.LIGHT_GRAY.wrap(Tags.LIGHT_RED.wrap("Can not find player %value%") + "!")
   );
   public static final LangText ERROR_COMMAND_INVALID_WORLD_ARGUMENT = LangText.of(
      "Error.Command.Argument.InvalidWorld", Tags.LIGHT_GRAY.wrap(Tags.LIGHT_RED.wrap("Can not find world %value%") + "!")
   );
   public static final LangText ERROR_COMMAND_INVALID_NUMBER_ARGUMENT = LangText.of(
      "Error.Command.Argument.InvalidNumber", Tags.LIGHT_GRAY.wrap(Tags.LIGHT_RED.wrap("%value%") + " is not a valid number!")
   );
   public static final LangText ERROR_COMMAND_INVALID_MATERIAL_ARGUMENT = LangText.of(
      "Error.Command.Argument.InvalidMaterial", Tags.LIGHT_GRAY.wrap(Tags.LIGHT_RED.wrap("%value%") + " is not a valid material!")
   );
   public static final LangText ERROR_COMMAND_INVALID_ENCHANTMENT_ARGUMENT = LangText.of(
      "Error.Command.Argument.InvalidEnchantment", Tags.LIGHT_GRAY.wrap(Tags.LIGHT_RED.wrap("%value%") + " is not a valid enchantment!")
   );
   public static final LangText ERROR_COMMAND_NOT_YOURSELF = LangText.of(
      "Error.Command.NotYourself", Tags.RED.wrap("This command can not be used on yourself.")
   );
   public static final LangText ERROR_COMMAND_PLAYER_ONLY = LangText.of("Error.Command.PlayerOnly", Tags.RED.wrap("This command is for players only."));
   public static final LangText ERROR_COMMAND_USAGE = LangText.of(
      "Error.Command.Usage",
      Placeholders.TAG_NO_PREFIX,
      " ",
      Tags.RED.wrap("Error: ") + Tags.GRAY.wrap("Wrong arguments!"),
      Tags.RED.wrap("Usage: ") + Tags.YELLOW.wrap("/%command_label%") + " " + Tags.ORANGE.wrap("%command_usage%"),
      " "
   );
   @Deprecated
   public static final LangText EDITOR_ACTION_EXIT = LangText.of(
      "Editor.Action.Exit",
      Placeholders.TAG_NO_PREFIX,
      "",
      Tags.GRAY
         .wrap(
            "Click "
               + Tags.CLICK
                  .wrap(
                     Tags.HOVER.wrap(Tags.GREEN.wrap("[Here]"), HoverEventType.SHOW_TEXT, Tags.GRAY.wrap("Click to cancel")),
                     ClickEventType.RUN_COMMAND,
                     "/#exit"
                  )
               + " to leave input mode."
         ),
      ""
   );
   @Deprecated
   public static final LangString DIALOG_HEADER = LangString.of("Dialog.Header", Tags.LIGHT_YELLOW.wrap("%time%"));
   @Deprecated
   public static final LangString DIALOG_DEFAULT_PROMPT = LangString.of(
      "Dialog.DefaultPrompt", Tags.LIGHT_GRAY.wrap("Enter " + Tags.LIGHT_GREEN.wrap("[Value]"))
   );
   @Deprecated
   public static final LangText DIALOG_INFO_EXIT = LangText.of(
      "Dialog.Info.Exit",
      Placeholders.TAG_NO_PREFIX,
      "",
      Tags.GRAY
         .wrap(
            "Click "
               + Tags.CLICK.wrapRunCommand(Tags.HOVER.wrapShowText(Tags.GREEN.wrap("[Here]"), Tags.GRAY.wrap("Click to cancel.")), "/#exit")
               + " to leave input mode."
         ),
      ""
   );
   @Deprecated
   public static final LangString EDITOR_INPUT_HEADER_MAIN = LangString.of("Editor.Input.Header.Main", Tags.GREEN.wrap(Tags.BOLD.wrap("Input Mode")));
   @Deprecated
   public static final LangString EDITOR_INPUT_HEADER_ERROR = LangString.of("Editor.Input.Header.Error", Tags.RED.wrap(Tags.BOLD.wrap("ERROR")));
   @Deprecated
   public static final LangString EDITOR_INPUT_ERROR_NOT_INTEGER = LangString.of(
      "Editor.Input.Error.NotInteger", Tags.GRAY.wrap("Expecting " + Tags.RED.wrap("whole") + " number!")
   );
   @Deprecated
   public static final LangString EDITOR_INPUT_ERROR_GENERIC = LangString.of("Editor.Input.Error.Generic", Tags.GRAY.wrap("Invalid value!"));
   @Deprecated
   public static final LangString EDITOR_BUTTON_NAME = LangString.of("Editor.Button.Name", Tags.LIGHT_YELLOW.wrap(Tags.BOLD.wrap("%name%")));
   @Deprecated
   public static final LangString EDITOR_BUTTON_DESCRIPTION = LangString.of("Editor.Button.Description", Tags.GRAY.wrap("%entry%"));
   @Deprecated
   public static final LangString EDITOR_BUTTON_CURRENT_DEFAULT_NAME = LangString.of("Editor.Button.Current.DefaultName", "Current");
   @Deprecated
   public static final LangString EDITOR_BUTTON_CURRENT_INFO = LangString.of(
      "Editor.Button.Current.Info", Tags.LIGHT_YELLOW.wrap("➥ " + Tags.GRAY.wrap("%name%: ") + "%value%")
   );
   @Deprecated
   public static final LangString EDITOR_BUTTON_CLICK_KEY = LangString.of(
      "Editor.Button.ClickKey", Tags.LIGHT_YELLOW.wrap("→ " + Tags.UNDERLINED.wrap("%name% to %value%"))
   );
   @Deprecated
   public static final LangItem EDITOR_ITEM_CLOSE = LangItem.of("Editor.Generic.Close", Tags.LIGHT_RED.wrap(Tags.BOLD.wrap("Exit")));
   @Deprecated
   public static final LangItem EDITOR_ITEM_RETURN = LangItem.of("Editor.Generic.Return", Tags.LIGHT_GRAY.wrap(Tags.BOLD.wrap("Return")));
   @Deprecated
   public static final LangItem EDITOR_ITEM_NEXT_PAGE = LangItem.of("Editor.Generic.NextPage", Tags.LIGHT_GRAY.wrap("Next Page →"));
   @Deprecated
   public static final LangItem EDITOR_ITEM_PREVIOUS_PAGE = LangItem.of("Editor.Generic.PreviousPage", Tags.LIGHT_GRAY.wrap("← Previous Page"));
   public static final LangUIButton EDITOR_ITEM_EXIT = LangUIButton.builder("Editor.Item.Exit", Tags.LIGHT_RED.wrap(Tags.UNDERLINED.wrap("Exit")))
      .formatted(false)
      .build();
   public static final LangUIButton EDITOR_ITEM_BACK = LangUIButton.builder("Editor.Item.Return", Tags.WHITE.wrap(Tags.UNDERLINED.wrap("Return")))
      .formatted(false)
      .build();
   public static final LangUIButton EDITOR_ITEM_NEXT = LangUIButton.builder(
         "Editor.Item.NextPage", Tags.LIGHT_YELLOW.wrap(Tags.UNDERLINED.wrap("Next Page") + " →")
      )
      .formatted(false)
      .build();
   public static final LangUIButton EDITOR_ITEM_PREVIOUS = LangUIButton.builder(
         "Editor.Item.PreviousPage", Tags.LIGHT_YELLOW.wrap("← " + Tags.UNDERLINED.wrap("Previous Page"))
      )
      .formatted(false)
      .build();

   @NonNull
   public static String getYesOrNo(boolean value) {
      return (value ? OTHER_YES : OTHER_NO).getString();
   }

   @NonNull
   public static String getEnabledOrDisabled(boolean value) {
      return (value ? OTHER_ENABLED : OTHER_DISABLED).getString();
   }

   @NonNull
   public static String goodEntry(@NonNull String str) {
      return ENTRY_GOOD.getString().replace("%entry%", str);
   }

   @NonNull
   public static String badEntry(@NonNull String str) {
      return ENTRY_BAD.getString().replace("%entry%", str);
   }

   @NonNull
   public static String warnEntry(@NonNull String str) {
      return ENTRY_WARN.getString().replace("%entry%", str);
   }
}
