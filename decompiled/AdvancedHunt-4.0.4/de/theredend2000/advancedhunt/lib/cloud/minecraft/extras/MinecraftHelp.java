package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.cloud.CommandManager;
import de.theredend2000.advancedhunt.lib.cloud.caption.Caption;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionProvider;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionRegistry;
import de.theredend2000.advancedhunt.lib.cloud.caption.CaptionVariable;
import de.theredend2000.advancedhunt.lib.cloud.component.CommandComponent;
import de.theredend2000.advancedhunt.lib.cloud.description.CommandDescription;
import de.theredend2000.advancedhunt.lib.cloud.description.Description;
import de.theredend2000.advancedhunt.lib.cloud.help.CommandPredicate;
import de.theredend2000.advancedhunt.lib.cloud.help.HelpHandler;
import de.theredend2000.advancedhunt.lib.cloud.help.HelpQuery;
import de.theredend2000.advancedhunt.lib.cloud.help.result.CommandEntry;
import de.theredend2000.advancedhunt.lib.cloud.help.result.HelpQueryResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.IndexCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.MultipleCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.help.result.VerboseCommandResult;
import de.theredend2000.advancedhunt.lib.cloud.minecraft.extras.caption.ComponentCaptionFormatter;
import de.theredend2000.advancedhunt.lib.cloud.type.tuple.Pair;
import de.theredend2000.advancedhunt.lib.cloud.util.StringUtils;
import de.theredend2000.advancedhunt.lib.kyori.adventure.audience.Audience;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.LinearComponents;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.event.ClickEvent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.NamedTextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextDecoration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;

@Immutable
public abstract class MinecraftHelp<C> {
   public static final int DEFAULT_HEADER_FOOTER_LENGTH = 46;
   public static final int DEFAULT_MAX_RESULTS_PER_PAGE = 6;
   public static final MinecraftHelp.HelpColors DEFAULT_HELP_COLORS = helpColors(
      NamedTextColor.GOLD, NamedTextColor.GREEN, NamedTextColor.YELLOW, NamedTextColor.GRAY, NamedTextColor.DARK_GRAY
   );
   public static final String MESSAGE_HELP_TITLE = "help";
   public static final String MESSAGE_COMMAND = "command";
   public static final String MESSAGE_DESCRIPTION = "description";
   public static final String MESSAGE_NO_DESCRIPTION = "no_description";
   public static final String MESSAGE_ARGUMENTS = "arguments";
   public static final String MESSAGE_OPTIONAL = "optional";
   public static final String MESSAGE_SHOWING_RESULTS_FOR_QUERY = "showing_results_for_query";
   public static final String MESSAGE_NO_RESULTS_FOR_QUERY = "no_results_for_query";
   public static final String MESSAGE_AVAILABLE_COMMANDS = "available_commands";
   public static final String MESSAGE_CLICK_TO_SHOW_HELP = "click_to_show_help";
   public static final String MESSAGE_PAGE_OUT_OF_RANGE = "page_out_of_range";
   public static final String MESSAGE_CLICK_FOR_NEXT_PAGE = "click_for_next_page";
   public static final String MESSAGE_CLICK_FOR_PREVIOUS_PAGE = "click_for_previous_page";
   private static final Pattern STRING_PLACEHOLDER_PATTERN = Pattern.compile("<([a-z_]+)>");
   private static final Map<String, String> DEFAULT_MESSAGES = new HashMap<>();

   public static <C> CaptionProvider<C> defaultCaptionsProvider() {
      return CaptionProvider.<C>constantProvider()
         .putAllCaptions(
            DEFAULT_MESSAGES.entrySet()
               .stream()
               .map(e -> Pair.of(Caption.of("help.minecraft." + e.getKey()), e.getValue()))
               .collect(Collectors.toMap(Pair::first, Pair::second))
         )
         .build();
   }

   @API(
      status = API.Status.STABLE,
      since = "1.5.0"
   )
   @NonNull
   public static <C extends Audience> MinecraftHelp<C> createNative(@NonNull final String commandPrefix, @NonNull final CommandManager<C> commandManager) {
      return ImmutableMinecraftHelp.<C>builder()
         .commandManager(commandManager)
         .audienceProvider(AudienceProvider.nativeAudience())
         .commandPrefix(commandPrefix)
         .build();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> MinecraftHelp<C> create(
      @NonNull final String commandPrefix, @NonNull final CommandManager<C> commandManager, @NonNull final AudienceProvider<C> audienceProvider
   ) {
      return ImmutableMinecraftHelp.<C>builder().commandManager(commandManager).audienceProvider(audienceProvider).commandPrefix(commandPrefix).build();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public static <C> ImmutableMinecraftHelp.CommandManagerBuildStage<C> builder() {
      return ImmutableMinecraftHelp.builder();
   }

   MinecraftHelp() {
   }

   @NonNull
   public abstract CommandManager<C> commandManager();

   @NonNull
   public abstract AudienceProvider<C> audienceProvider();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public abstract String commandPrefix();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Derived
   @NonNull
   public HelpHandler<C> helpHandler() {
      return this.commandManager().createHelpHandler(this.commandFilter());
   }

   @NonNull
   public Audience audience(@NonNull final C sender) {
      return this.audienceProvider().apply(sender);
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNull
   public CommandPredicate<C> commandFilter() {
      return CommandPredicate.acceptAll();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNull
   public MinecraftHelp.DescriptionDecorator<C> descriptionDecorator() {
      return MinecraftHelp.DescriptionDecorator.text();
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @NonNull
   public abstract Map<String, String> messages();

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNull
   public MinecraftHelp.MessageProvider<C> messageProvider() {
      return (sender, key, args) -> {
         String message = this.messageOrDefault(key);
         return args.isEmpty()
            ? Component.text(message)
            : Component.text(StringUtils.replaceAll(message, STRING_PLACEHOLDER_PATTERN, matchResult -> (String)args.get(matchResult.group(1))));
      };
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNull
   public MinecraftHelp.HelpColors colors() {
      return DEFAULT_HELP_COLORS;
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNegative
   public int headerFooterLength() {
      return 46;
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @Default
   @NonNegative
   public int maxResultsPerPage() {
      return 6;
   }

   @NonNull
   protected final String messageOrDefault(@NonNull final String key) {
      return this.messages().getOrDefault(key, DEFAULT_MESSAGES.get(key));
   }

   public void queryCommands(@NonNull final String rawQuery, @NonNull final C recipient) {
      String[] splitQuery = rawQuery.split(" ");

      int page;
      String query;
      try {
         String pageText = splitQuery[splitQuery.length - 1];
         page = Integer.parseInt(pageText);
         query = rawQuery.substring(0, Math.max(rawQuery.lastIndexOf(pageText) - 1, 0));
      } catch (NumberFormatException var7) {
         page = 1;
         query = rawQuery;
      }

      Audience audience = this.audience(recipient);
      this.printTopic(recipient, query, page, this.helpHandler().query(HelpQuery.of(recipient, query)));
   }

   private void printTopic(@NonNull final C sender, @NonNull final String query, final int page, @NonNull final HelpQueryResult<C> helpTopic) {
      if (helpTopic instanceof IndexCommandResult) {
         this.printIndexHelpTopic(sender, query, page, (IndexCommandResult<C>)helpTopic);
      } else if (helpTopic instanceof MultipleCommandResult) {
         this.printMultiHelpTopic(sender, query, page, (MultipleCommandResult<C>)helpTopic);
      } else {
         if (!(helpTopic instanceof VerboseCommandResult)) {
            throw new IllegalArgumentException("Unknown help topic type");
         }

         this.printVerboseHelpTopic(sender, query, (VerboseCommandResult<C>)helpTopic);
      }
   }

   private void printNoResults(@NonNull final C sender, @NonNull final String query) {
      Audience audience = this.audience(sender);
      audience.sendMessage(this.basicHeader(sender));
      audience.sendMessage(
         LinearComponents.linear(
            this.messageProvider().provide(sender, "no_results_for_query").color(this.colors().text()),
            Component.text(": \"", this.colors().text()),
            this.highlight(Component.text("/" + query, this.colors().highlight())),
            Component.text("\"", this.colors().text())
         )
      );
      audience.sendMessage(this.footer(sender));
   }

   private void printIndexHelpTopic(@NonNull final C sender, @NonNull final String query, final int page, @NonNull final IndexCommandResult<C> helpTopic) {
      if (helpTopic.isEmpty()) {
         this.printNoResults(sender, query);
      } else {
         Audience audience = this.audience(sender);
         new Pagination<CommandEntry>(
               (currentPage, maxPages) -> {
                  List<Component> header = new ArrayList<>();
                  header.add(this.paginatedHeader(sender, currentPage, maxPages));
                  header.add(this.showingResults(sender, query));
                  header.add(
                     Component.text()
                        .append(this.lastBranch())
                        .append(Component.space())
                        .append(this.messageProvider().provide(sender, "available_commands").color(this.colors().text()))
                        .append(Component.text(":", this.colors().text()))
                        .build()
                  );
                  return header;
               },
               (helpEntry, isLastOfPage) -> {
                  CommandDescription commandDescription = helpEntry.command().commandDescription();
                  Component description;
                  if (commandDescription.description() instanceof RichDescription) {
                     description = ((RichDescription)commandDescription.description()).contents();
                  } else if (commandDescription.isEmpty()) {
                     description = this.messageProvider().provide(sender, "click_to_show_help");
                  } else {
                     description = this.descriptionDecorator().decorate(sender, commandDescription.description().textDescription());
                  }

                  boolean lastBranch = isLastOfPage || helpTopic.entries().indexOf(helpEntry) == helpTopic.entries().size() - 1;
                  return Component.text()
                     .append(Component.text("   "))
                     .append(lastBranch ? this.lastBranch() : this.branch())
                     .append(
                        this.highlight(Component.text(String.format(" /%s", helpEntry.syntax()), this.colors().highlight()))
                           .hoverEvent(description.color(this.colors().text()))
                           .clickEvent(ClickEvent.runCommand(this.commandPrefix() + " " + helpEntry.syntax()))
                     )
                     .build();
               },
               (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
               (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
            )
            .render(helpTopic.entries(), page, this.maxResultsPerPage())
            .forEach(audience::sendMessage);
      }
   }

   private void printMultiHelpTopic(@NonNull final C sender, @NonNull final String query, final int page, @NonNull final MultipleCommandResult<C> helpTopic) {
      if (helpTopic.childSuggestions().isEmpty()) {
         this.printNoResults(sender, query);
      } else {
         Audience audience = this.audience(sender);
         int headerIndentation = helpTopic.longestPath().length();
         new Pagination<String>(
               (currentPage, maxPages) -> {
                  List<Component> header = new ArrayList<>();
                  header.add(this.paginatedHeader(sender, currentPage, maxPages));
                  header.add(this.showingResults(sender, query));
                  header.add(this.lastBranch().append(this.highlight(Component.text(" /" + helpTopic.longestPath(), this.colors().highlight()))));
                  return header;
               },
               (suggestion, isLastOfPage) -> {
                  boolean lastBranch = isLastOfPage || helpTopic.childSuggestions().indexOf(suggestion) == helpTopic.childSuggestions().size() - 1;
                  return ComponentHelper.repeat(Component.space(), headerIndentation)
                     .append(lastBranch ? this.lastBranch() : this.branch())
                     .append(
                        this.highlight(Component.text(" /" + suggestion, this.colors().highlight()))
                           .hoverEvent(this.messageProvider().provide(sender, "click_to_show_help").color(this.colors().text()))
                           .clickEvent(ClickEvent.runCommand(this.commandPrefix() + " " + suggestion))
                     );
               },
               (currentPage, maxPages) -> this.paginatedFooter(sender, currentPage, maxPages, query),
               (attemptedPage, maxPages) -> this.pageOutOfRange(sender, attemptedPage, maxPages)
            )
            .render(helpTopic.childSuggestions(), page, this.maxResultsPerPage())
            .forEach(audience::sendMessage);
      }
   }

   private void printVerboseHelpTopic(@NonNull final C sender, @NonNull final String query, @NonNull final VerboseCommandResult<C> helpTopic) {
      Audience audience = this.audience(sender);
      audience.sendMessage(this.basicHeader(sender));
      audience.sendMessage(this.showingResults(sender, query));
      String command = this.commandManager().commandSyntaxFormatter().apply(sender, helpTopic.entry().command().components(), null);
      audience.sendMessage(
         Component.text()
            .append(this.lastBranch())
            .append(Component.space())
            .append(this.messageProvider().provide(sender, "command").color(this.colors().primary()))
            .append(Component.text(": ", this.colors().primary()))
            .append(this.highlight(Component.text("/" + command, this.colors().highlight())))
      );
      Description commandDescription = helpTopic.entry().command().commandDescription().verboseDescription();
      Component topicDescription;
      if (commandDescription instanceof RichDescription) {
         topicDescription = ((RichDescription)commandDescription).contents();
      } else if (commandDescription.isEmpty()) {
         topicDescription = this.messageProvider().provide(sender, "no_description");
      } else {
         topicDescription = this.descriptionDecorator().decorate(sender, commandDescription.textDescription());
      }

      boolean hasArguments = helpTopic.entry().command().components().size() > 1;
      audience.sendMessage(
         Component.text()
            .append(Component.text("   "))
            .append(hasArguments ? this.branch() : this.lastBranch())
            .append(Component.space())
            .append(this.messageProvider().provide(sender, "description").color(this.colors().primary()))
            .append(Component.text(": ", this.colors().primary()))
            .append(topicDescription.color(this.colors().text()))
      );
      if (hasArguments) {
         audience.sendMessage(
            Component.text()
               .append(Component.text("   "))
               .append(this.lastBranch())
               .append(Component.space())
               .append(this.messageProvider().provide(sender, "arguments").color(this.colors().primary()))
               .append(Component.text(":", this.colors().primary()))
         );
         Iterator<CommandComponent<C>> iterator = helpTopic.entry().command().components().iterator();
         iterator.next();

         while (iterator.hasNext()) {
            CommandComponent<C> component = iterator.next();
            String syntax = this.commandManager().commandSyntaxFormatter().apply(sender, Collections.singletonList(component), null);
            TextComponent.Builder textComponent = Component.text()
               .append(Component.text("       "))
               .append(iterator.hasNext() ? this.branch() : this.lastBranch())
               .append(this.highlight(Component.text(" " + syntax, this.colors().highlight())));
            if (component.optional()) {
               textComponent.append(Component.text(" (", this.colors().alternateHighlight()));
               textComponent.append(this.messageProvider().provide(sender, "optional").color(this.colors().alternateHighlight()));
               textComponent.append(Component.text(")", this.colors().alternateHighlight()));
            }

            Description description = component.description();
            if (!description.isEmpty()) {
               textComponent.append(Component.text(" - ", this.colors().accent()));
               textComponent.append(this.formatDescription(sender, description).colorIfAbsent(this.colors().text()));
            }

            audience.sendMessage(textComponent);
         }
      }

      audience.sendMessage(this.footer(sender));
   }

   private Component formatDescription(final C sender, final Description description) {
      return description instanceof RichDescription
         ? ((RichDescription)description).contents()
         : this.descriptionDecorator().decorate(sender, description.textDescription());
   }

   @NonNull
   private Component showingResults(@NonNull final C sender, @NonNull final String query) {
      return Component.text()
         .append(this.messageProvider().provide(sender, "showing_results_for_query").color(this.colors().text()))
         .append(Component.text(": \"", this.colors().text()))
         .append(this.highlight(Component.text("/" + query, this.colors().highlight())))
         .append(Component.text("\"", this.colors().text()))
         .build();
   }

   @NonNull
   private Component button(final char icon, @NonNull final String command, @NonNull final Component hoverText) {
      return Component.text()
         .append(Component.space())
         .append(Component.text('[', this.colors().accent()))
         .append(Component.text(icon, this.colors().alternateHighlight()))
         .append(Component.text(']', this.colors().accent()))
         .append(Component.space())
         .clickEvent(ClickEvent.runCommand(command))
         .hoverEvent(hoverText)
         .build();
   }

   @NonNull
   private Component footer(@NonNull final C sender) {
      return this.paginatedFooter(sender, 1, 1, "");
   }

   @NonNull
   private Component paginatedFooter(@NonNull final C sender, final int currentPage, final int maxPages, @NonNull final String query) {
      boolean firstPage = currentPage == 1;
      boolean lastPage = currentPage == maxPages;
      if (firstPage && lastPage) {
         return this.line(this.headerFooterLength());
      } else {
         Component nextPageButton = this.button(
            '→', this.pageCommand(query, currentPage + 1), this.messageProvider().provide(sender, "click_for_next_page").color(this.colors().text())
         );
         if (firstPage) {
            return this.header(sender, nextPageButton);
         } else {
            Component previousPageButton = this.button(
               '←', this.pageCommand(query, currentPage - 1), this.messageProvider().provide(sender, "click_for_previous_page").color(this.colors().text())
            );
            if (lastPage) {
               return this.header(sender, previousPageButton);
            } else {
               Component buttons = Component.text().append(previousPageButton).append(this.line(3)).append(nextPageButton).build();
               return this.header(sender, buttons);
            }
         }
      }
   }

   private String pageCommand(final String query, final int page) {
      return query.isEmpty() ? String.format("%s %s", this.commandPrefix(), page) : String.format("%s %s %s", this.commandPrefix(), query, page);
   }

   @NonNull
   private Component header(@NonNull final C sender, @NonNull final Component title) {
      int sideLength = (this.headerFooterLength() - ComponentHelper.length(title)) / 2;
      return Component.text().append(this.line(sideLength)).append(title).append(this.line(sideLength)).build();
   }

   @NonNull
   private Component basicHeader(@NonNull final C sender) {
      return this.header(
         sender, LinearComponents.linear(Component.space(), this.messageProvider().provide(sender, "help").color(this.colors().highlight()), Component.space())
      );
   }

   @NonNull
   private Component paginatedHeader(@NonNull final C sender, final int currentPage, final int pages) {
      return this.header(
         sender,
         Component.text()
            .append(Component.space())
            .append(this.messageProvider().provide(sender, "help").color(this.colors().highlight()))
            .append(Component.space())
            .append(Component.text("(", this.colors().alternateHighlight()))
            .append(Component.text(currentPage, this.colors().text()))
            .append(Component.text("/", this.colors().alternateHighlight()))
            .append(Component.text(pages, this.colors().text()))
            .append(Component.text(")", this.colors().alternateHighlight()))
            .append(Component.space())
            .build()
      );
   }

   @NonNull
   private Component line(final int length) {
      return ComponentHelper.repeat(Component.text("-", this.colors().primary(), TextDecoration.STRIKETHROUGH), length);
   }

   @NonNull
   private Component branch() {
      return Component.text("├─", this.colors().accent());
   }

   @NonNull
   private Component lastBranch() {
      return Component.text("└─", this.colors().accent());
   }

   @NonNull
   private Component highlight(@NonNull final Component component) {
      return ComponentHelper.highlight(component, this.colors().alternateHighlight());
   }

   @NonNull
   private Component pageOutOfRange(@NonNull final C sender, final int attemptedPage, final int maxPages) {
      Map<String, String> args = new LinkedHashMap<>();
      args.put("page", String.valueOf(attemptedPage));
      args.put("max_pages", String.valueOf(maxPages));
      return this.highlight(this.messageProvider().provide(sender, "page_out_of_range", args).color(this.colors().text()));
   }

   public static <C> MinecraftHelp.MessageProvider<C> captionMessageProvider(final CaptionRegistry<C> registry, final ComponentCaptionFormatter<C> formatter) {
      return (sender, key, args) -> {
         Caption caption = Caption.of("help.minecraft." + key);
         String resolved = registry.caption(caption, sender);
         return formatter.formatCaption(
            caption, sender, resolved, args.entrySet().stream().map(e -> CaptionVariable.of(e.getKey(), e.getValue())).collect(Collectors.toList())
         );
      };
   }

   @NonNull
   public static MinecraftHelp.HelpColors helpColors(
      @NonNull final TextColor primary,
      @NonNull final TextColor highlight,
      @NonNull final TextColor alternateHighlight,
      @NonNull final TextColor text,
      @NonNull final TextColor accent
   ) {
      return HelpColorsImpl.of(primary, highlight, alternateHighlight, text, accent);
   }

   static {
      DEFAULT_MESSAGES.put("help", "Help");
      DEFAULT_MESSAGES.put("command", "Command");
      DEFAULT_MESSAGES.put("description", "Description");
      DEFAULT_MESSAGES.put("no_description", "No description");
      DEFAULT_MESSAGES.put("arguments", "Arguments");
      DEFAULT_MESSAGES.put("optional", "Optional");
      DEFAULT_MESSAGES.put("showing_results_for_query", "Showing search results for query");
      DEFAULT_MESSAGES.put("no_results_for_query", "No results for query");
      DEFAULT_MESSAGES.put("available_commands", "Available Commands");
      DEFAULT_MESSAGES.put("click_to_show_help", "Click to show help for this command");
      DEFAULT_MESSAGES.put("page_out_of_range", "Error: Page <page> is not in range. Must be in range [1, <max_pages>]");
      DEFAULT_MESSAGES.put("click_for_next_page", "Click for next page");
      DEFAULT_MESSAGES.put("click_for_previous_page", "Click for previous page");
   }

   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   @FunctionalInterface
   public interface DescriptionDecorator<C> {
      @NonNull
      static <C> MinecraftHelp.DescriptionDecorator<C> wrap(@NonNull final Function<String, Component> function) {
         return (sender, description) -> function.apply(description);
      }

      @NonNull
      static <C> MinecraftHelp.DescriptionDecorator<C> text() {
         return (sender, description) -> Component.text(description);
      }

      @NonNull
      Component decorate(@NonNull C sender, @NonNull String description);
   }

   @Immutable
   public interface HelpColors {
      @NonNull
      TextColor primary();

      @NonNull
      TextColor highlight();

      @NonNull
      TextColor alternateHighlight();

      @NonNull
      TextColor text();

      @NonNull
      TextColor accent();
   }

   @FunctionalInterface
   @API(
      status = API.Status.STABLE,
      since = "2.0.0"
   )
   public interface MessageProvider<C> {
      @NonNull
      Component provide(@NonNull C sender, @NonNull String key, @NonNull Map<String, String> args);

      @NonNull
      default Component provide(@NonNull final C sender, @NonNull final String key) {
         return this.provide(sender, key, Collections.emptyMap());
      }
   }
}
