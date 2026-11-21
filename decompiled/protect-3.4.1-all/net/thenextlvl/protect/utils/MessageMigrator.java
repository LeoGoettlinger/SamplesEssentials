package net.thenextlvl.protect.utils;

import java.util.Locale;
import java.util.Set;
import net.thenextlvl.i18n.ResourceMigrator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class MessageMigrator implements ResourceMigrator {
   private final Set<MessageMigrator.MigrationRule> rules = Set.of(
      new MessageMigrator.MigrationRule(Locale.US, "area.list", "<areas>", "<areas:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.list", "<areas>", "<areas:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.list.global", "<areas>", "<areas:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.list.global", "<areas>", "<areas:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.info", "<flags>", "<flags:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.info", "<flags>", "<flags:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.members", "<members>", "<members:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.members", "<members>", "<members:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.flag.list", "<flags>", "<flags:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.flag.list", "<flags>", "<flags:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.list.groups", "<groups>", "<groups:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.list.groups", "<groups>", "<groups:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.US, "area.list.regions", "<regions>", "<regions:'<gray>, </gray>'>"),
      new MessageMigrator.MigrationRule(Locale.GERMANY, "area.list.regions", "<regions>", "<regions:'<gray>, </gray>'>")
   );

   @Nullable
   @Override
   public ResourceMigrator.Migration migrate(Locale locale, String key, String message) {
      return this.rules
         .stream()
         .filter(rule -> rule.locale().equals(locale))
         .filter(rule -> rule.key().equals(key))
         .filter(rule -> message.contains(rule.match()))
         .findAny()
         .map(rule -> message.replace(rule.match(), rule.replacement()))
         .map(string -> new ResourceMigrator.Migration(key, string))
         .orElse(null);
   }

   private record MigrationRule(Locale locale, String key, String match, String replacement) {
   }
}
