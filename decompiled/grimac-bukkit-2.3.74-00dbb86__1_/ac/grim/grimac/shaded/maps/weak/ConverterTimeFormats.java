package ac.grim.grimac.shaded.maps.weak;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.function.Function;

public class ConverterTimeFormats {
   public static DateTimeFormatter ISO_PERMISSIVE = new DateTimeFormatterBuilder()
      .parseLenient()
      .parseCaseInsensitive()
      .appendValue(ChronoField.YEAR, 1, 10, SignStyle.EXCEEDS_PAD)
      .appendLiteral('-')
      .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral('-')
      .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral('T')
      .optionalEnd()
      .optionalStart()
      .appendLiteral(' ')
      .optionalEnd()
      .optionalStart()
      .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalEnd()
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalEnd()
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0L)
      .optionalEnd()
      .optionalStart()
      .appendZoneOrOffsetId()
      .optionalEnd()
      .optionalStart()
      .appendOffset("+HHmm", "+0000")
      .optionalEnd()
      .optionalStart()
      .appendLiteral('[')
      .parseCaseSensitive()
      .appendZoneRegionId()
      .appendLiteral(']')
      .toFormatter();
   public static DateTimeFormatter ISO_PERMISSIVE_WITH_DEFAULTS = new DateTimeFormatterBuilder()
      .append(ISO_PERMISSIVE)
      .parseDefaulting(ChronoField.DAY_OF_MONTH, 1L)
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0L)
      .toFormatter();
   public static DateTimeFormatter ISO_LONESOME_YEAR = new DateTimeFormatterBuilder()
      .parseStrict()
      .appendValue(ChronoField.YEAR, 1, 4, SignStyle.NORMAL)
      .toFormatter();
   public static DateTimeFormatter ISO_LONESOME_YEAR_WITH_DEFAULTS = new DateTimeFormatterBuilder()
      .append(ISO_LONESOME_YEAR)
      .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1L)
      .parseDefaulting(ChronoField.DAY_OF_MONTH, 1L)
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0L)
      .toFormatter();
   public static DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE_DASH = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .parseLenient()
      .appendPattern("dd")
      .appendLiteral('-')
      .appendPattern("MMM")
      .appendLiteral('-')
      .appendValue(ChronoField.YEAR, 1, 10, SignStyle.EXCEEDS_PAD)
      .optionalStart()
      .appendLiteral(' ')
      .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .optionalStart()
      .appendOffsetId()
      .toFormatter();
   public static DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE_DASH_WITH_DEFAULTS = new DateTimeFormatterBuilder()
      .append(DAY_MONTH_YEAR_PERMISSIVE_DASH)
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0L)
      .toFormatter();
   public static DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE_SLASH = new DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .parseLenient()
      .appendPattern("dd")
      .appendLiteral('/')
      .appendPattern("MMM")
      .appendLiteral('/')
      .appendValue(ChronoField.YEAR, 1, 10, SignStyle.EXCEEDS_PAD)
      .optionalStart()
      .appendLiteral(' ')
      .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendLiteral(':')
      .appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .optionalStart()
      .appendOffsetId()
      .toFormatter();
   public static DateTimeFormatter DAY_MONTH_YEAR_PERMISSIVE_SLASH_WITH_DEFAULTS = new DateTimeFormatterBuilder()
      .append(DAY_MONTH_YEAR_PERMISSIVE_SLASH)
      .parseDefaulting(ChronoField.HOUR_OF_DAY, 0L)
      .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0L)
      .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0L)
      .parseDefaulting(ChronoField.NANO_OF_SECOND, 0L)
      .toFormatter();
   public static DateTimeFormatter UTIL_DATE_TO_STRING = new DateTimeFormatterBuilder()
      .parseLenient()
      .appendPattern("EEE")
      .appendLiteral(' ')
      .appendPattern("MMM")
      .appendLiteral(' ')
      .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
      .appendLiteral(' ')
      .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
      .appendLiteral(':')
      .appendPattern("mm")
      .optionalStart()
      .appendLiteral(':')
      .appendPattern("ss")
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .optionalEnd()
      .optionalEnd()
      .appendLiteral(' ')
      .appendPattern("zzz")
      .appendLiteral(' ')
      .appendPattern("yyyy")
      .toFormatter();
   public static DateTimeFormatter UTIL_DATE_WITHOUT_ZONE_TO_STRING = new DateTimeFormatterBuilder()
      .parseLenient()
      .appendPattern("EEE")
      .appendLiteral(' ')
      .appendPattern("MMM")
      .appendLiteral(' ')
      .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
      .appendLiteral(' ')
      .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
      .appendLiteral(':')
      .appendPattern("mm")
      .optionalStart()
      .appendLiteral(':')
      .appendPattern("ss")
      .optionalStart()
      .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
      .optionalEnd()
      .optionalEnd()
      .appendLiteral(' ')
      .appendPattern("yyyy")
      .toFormatter();
   static final Function<CharSequence, TemporalAccessor> EPOCH_MILLIS_PARSER = s -> {
      Long millis = Converter.convert(s).intoLong();
      if (millis > -10000L && millis < 10000L) {
         throw new IllegalArgumentException("Small value '" + s + "' indicates it is not valid epoch millis");
      } else {
         return LocalDateTime.ofInstant(Instant.ofEpochMilli(Converter.convert(s).intoLong()), ZoneId.systemDefault());
      }
   };
   public static final Function<CharSequence, TemporalAccessor> ALL_PARSER = orderedParseAttempter(
      ISO_PERMISSIVE::parse,
      DAY_MONTH_YEAR_PERMISSIVE_DASH::parse,
      DAY_MONTH_YEAR_PERMISSIVE_SLASH::parse,
      UTIL_DATE_TO_STRING::parse,
      UTIL_DATE_WITHOUT_ZONE_TO_STRING::parse,
      ISO_LONESOME_YEAR::parse,
      EPOCH_MILLIS_PARSER
   );
   public static final Function<CharSequence, TemporalAccessor> ALL_PARSER_WITH_DEFAULTS = orderedParseAttempter(
      ISO_PERMISSIVE_WITH_DEFAULTS::parse,
      DAY_MONTH_YEAR_PERMISSIVE_DASH_WITH_DEFAULTS::parse,
      DAY_MONTH_YEAR_PERMISSIVE_SLASH_WITH_DEFAULTS::parse,
      UTIL_DATE_TO_STRING::parse,
      UTIL_DATE_WITHOUT_ZONE_TO_STRING::parse,
      ISO_LONESOME_YEAR_WITH_DEFAULTS::parse,
      EPOCH_MILLIS_PARSER
   );

   @SafeVarargs
   public static Function<CharSequence, TemporalAccessor> orderedParseAttempter(Function<CharSequence, TemporalAccessor>... parsers) {
      return date -> {
         RuntimeException first = null;

         for (Function<CharSequence, TemporalAccessor> parser : parsers) {
            try {
               return parser.apply(date);
            } catch (RuntimeException var8) {
               if (first == null) {
                  first = var8;
               }
            }
         }

         if (first == null) {
            throw new IllegalStateException("Empty parse attempter");
         } else {
            throw first;
         }
      };
   }

   public static TemporalAccessor parse(CharSequence dateChars) {
      return ALL_PARSER.apply(dateChars);
   }

   public static TemporalAccessor parseWithDefaults(CharSequence dateChars) {
      return ALL_PARSER_WITH_DEFAULTS.apply(dateChars);
   }

   private ConverterTimeFormats() {
   }
}
