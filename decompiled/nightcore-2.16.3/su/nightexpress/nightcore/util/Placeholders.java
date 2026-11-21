package su.nightexpress.nightcore.util;

import java.util.function.UnaryOperator;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.language.tag.MessageTags;
import su.nightexpress.nightcore.util.placeholder.CommonPlaceholders;
import su.nightexpress.nightcore.util.placeholder.PlaceholderList;
import su.nightexpress.nightcore.util.text.tag.Tags;

public class Placeholders {
   public static final String GITHUB_URL = "https://github.com/nulli0n/nightcore-spigot";
   public static final String URL_WIKI = "https://nightexpressdev.com/nightcore/";
   public static final String URL_WIKI_ITEMS = "https://nightexpressdev.com/nightcore/configuration/item-formation/";
   public static final String URL_WIKI_TEXT = "https://nightexpressdev.com/nightcore/configuration/text-formation/";
   public static final String URL_WIKI_LANG = "https://nightexpressdev.com/nightcore/configuration/language/";
   public static final String URL_WIKI_NUMBER = "https://nightexpressdev.com/nightcore/configuration/number-formation/";
   public static final String URL_WIKI_CURRENCIES = "https://nightexpressdev.com/nightcore/integrations/currencies/";
   public static final String URL_WIKI_CUSTOM_ITEMS = "https://nightexpressdev.com/nightcore/integrations/items/";
   @Deprecated
   public static final String WIKI_MAIN_URL = "https://nightexpressdev.com/nightcore/";
   @Deprecated
   public static final String WIKI_ITEMS_URL = "https://nightexpressdev.com/nightcore/configuration/item-formation/";
   @Deprecated
   public static final String WIKI_TEXT_URL = "https://nightexpressdev.com/nightcore/configuration/text-formation/";
   @Deprecated
   public static final String WIKI_LANG_URL = "https://nightexpressdev.com/nightcore/configuration/language/";
   public static final String DEFAULT = "default";
   public static final String NONE = "none";
   public static final String WILDCARD = "*";
   public static final String GENERIC_NAME = "%name%";
   public static final String GENERIC_ITEM = "%item%";
   public static final String GENERIC_VALUE = "%value%";
   public static final String GENERIC_AMOUNT = "%amount%";
   public static final String GENERIC_ENTRY = "%entry%";
   public static final String GENERIC_TIME = "%time%";
   public static final String GENERIC_INPUT = "%input%";
   public static final String GENERIC_COMMAND = "%command%";
   public static final String GENERIC_DESCRIPTION = "%description%";
   @Deprecated
   public static final String TAG_NO_PREFIX = MessageTags.NO_PREFIX.getBracketsName();
   @Deprecated
   public static final String TAG_LINE_BREAK = Tags.LINE_BREAK.getBracketsName();
   public static final String PLAYER_NAME = "%player_name%";
   public static final String PLAYER_DISPLAY_NAME = "%player_display_name%";
   public static final String PLAYER_PREFIX = "%player_prefix%";
   public static final String PLAYER_SUFFIX = "%player_suffix%";
   public static final String LOCATION_X = "%location_x%";
   public static final String LOCATION_Y = "%location_y%";
   public static final String LOCATION_Z = "%location_z%";
   public static final String LOCATION_WORLD = "%location_world%";
   @Deprecated
   public static final String SKIN_WRONG_MARK = "27548362a24c0fa8453e4d93e68c5969ddbde57bf6666c0319c1ed1e84d89065";
   @Deprecated
   public static final String SKIN_QUESTION_MARK = "2705fd94a0c431927fb4e639b0fcfb49717e412285a02b439e0112da22b2e2ec";
   @Deprecated
   public static final String SKIN_CHECK_MARK = "a79a5c95ee17abfef45c8dc224189964944d560f19a44f19f8a46aef3fee4756";
   @Deprecated
   public static final String SKIN_EXCLAMATION_MARK = "7b41996fd20ca21d79adfc0e12057b2f2ceadf7b3cf5bb5f8a92fe3460161acd";
   @Deprecated
   public static final String SKIN_ARROW_LEFT = "86971dd881dbaf4fd6bcaa93614493c612f869641ed59d1c9363a3666a5fa6";
   @Deprecated
   public static final String SKIN_ARROW_RIGHT = "f32ca66056b72863e98f7f32bd7d94c7a0d796af691c9ac3a9136331352288f9";
   @Deprecated
   public static final String SKIN_ARROW_DOWN = "be9ae7a4be65fcbaee65181389a2f7d47e2e326db59ea3eb789a92c85ea46";
   public static final String EMPTY_IF_ABOVE = "%empty-if-above%";
   public static final String EMPTY_IF_BELOW = "%empty-if-below%";
   public static final String EMPTY_IF_BOTH = "%empty-if-both%";
   @Deprecated
   public static final String COMMAND_USAGE = "%command_usage%";
   @Deprecated
   public static final String COMMAND_DESCRIPTION = "%command_description%";
   @Deprecated
   public static final String COMMAND_LABEL = "%command_label%";
   public static final String CURRENCY_NAME = "%currency_name%";
   public static final String CURRENCY_ID = "%currency_id%";
   public static final PlaceholderList<Currency> CURRENCY = new PlaceholderList<Currency>()
      .add("%currency_id%", Currency::getInternalId)
      .add("%currency_name%", Currency::getName);
   public static final PlaceholderList<Player> PLAYER = new PlaceholderList<Player>()
      .add("%player_name%", Player::getName)
      .add("%player_display_name%", Players::getDisplayNameSerialized)
      .add("%player_prefix%", Players::getPrefixOrEmpty)
      .add("%player_suffix%", Players::getSuffixOrEmpty);
   public static final PlaceholderList<Location> LOCATION = new PlaceholderList<Location>()
      .add("%location_x%", location -> String.valueOf(location.getBlockX()))
      .add("%location_y%", location -> String.valueOf(location.getBlockY()))
      .add("%location_z%", location -> String.valueOf(location.getBlockZ()))
      .add("%location_world%", LocationUtil::getWorldName);
   public static final PlaceholderList<CommandSender> SENDER = new PlaceholderList<CommandSender>()
      .add("%player_name%", CommandSender::getName)
      .add("%player_display_name%", CommandSender::getName);

   @NonNull
   public static UnaryOperator<String> forLocation(@NonNull Location location) {
      return LOCATION.replacer(location);
   }

   @NonNull
   public static UnaryOperator<String> forPlayer(@NonNull Player player) {
      return PLAYER.replacer(player);
   }

   @NonNull
   public static UnaryOperator<String> forPlayerWithPAPI(@NonNull Player player) {
      return str -> setPlaceholderAPI(forPlayer(player).apply(str), player);
   }

   @NonNull
   public static UnaryOperator<String> forSender(@NonNull CommandSender sender) {
      return sender instanceof Player player ? forPlayer(player) : SENDER.replacer(sender);
   }

   @NonNull
   public static UnaryOperator<String> forPlaceholderAPI(@NonNull Player player) {
      return CommonPlaceholders.forPlaceholderAPI(player);
   }

   @NonNull
   public static String setPlaceholderAPI(@NonNull String str, @NonNull Player player) {
      return forPlaceholderAPI(player).apply(str);
   }
}
