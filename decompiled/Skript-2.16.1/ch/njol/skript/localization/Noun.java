package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

public class Noun extends Message {
   public static final String GENDERS_SECTION = "genders.";
   public static final int PLURAL = -2;
   public static final int NO_GENDER = -3;
   public static final String PLURAL_TOKEN = "x";
   public static final String NO_GENDER_TOKEN = "-";
   @Nullable
   private String singular;
   @Nullable
   private String plural;
   private int gender = 0;
   static final HashMap<String, Integer> genders = new HashMap<>();
   static final List<String> indefiniteArticles = new ArrayList<>(3);
   static final List<String> definiteArticles = new ArrayList<>(3);
   static String definitePluralArticle = "";

   public Noun(String key) {
      super(key);
   }

   @Override
   protected void onValueChange() {
      String value = this.getValue();
      if (value == null) {
         this.plural = this.singular = this.key;
         this.gender = 0;
      } else {
         int g = value.lastIndexOf(64);
         if (g != -1) {
            this.gender = getGender(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value.substring(g + 1).trim()), this.key);
            value = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value.substring(0, g).trim());
         } else {
            this.gender = 0;
         }

         Noun.PluralPair p = parsePlural(value);
         this.singular = p.singular;
         this.plural = p.plural;
         if (this.gender == -2 && !Objects.equals(this.singular, this.plural)) {
            Skript.warning("Noun '" + this.key + "' is of gender 'plural', but has different singular and plural values.");
         }
      }
   }

   @Override
   public String toString() {
      this.validate();
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.singular);
   }

   public String toString(boolean plural) {
      this.validate();
      return plural
         ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.plural)
         : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.singular);
   }

   public String withIndefiniteArticle() {
      return this.toString(4);
   }

   public String getIndefiniteArticle() {
      this.validate();
      return this.gender != -2 && this.gender != -3
         ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(indefiniteArticles.get(this.gender))
         : "";
   }

   public String withDefiniteArticle() {
      return this.toString(2);
   }

   public String withDefiniteArticle(boolean plural) {
      return this.toString(2 | (plural ? 1 : 0));
   }

   public String getDefiniteArticle() {
      this.validate();
      return this.gender == -2
         ? definitePluralArticle
         : (this.gender == -3 ? "" : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(definiteArticles.get(this.gender)));
   }

   public int getGender() {
      this.validate();
      return this.gender;
   }

   public static String getArticleWithSpace(int gender, int flags) {
      if (gender == -2) {
         if ((flags & 2) != 0) {
            return definitePluralArticle + " ";
         }
      } else if (gender != -3) {
         if ((flags & 2) != 0) {
            if (gender >= 0 && gender < definiteArticles.size()) {
               return definiteArticles.get(gender) + " ";
            }

            assert false : gender;

            return "";
         }

         if ((flags & 4) != 0) {
            if (gender >= 0 && gender < indefiniteArticles.size()) {
               return indefiniteArticles.get(gender) + " ";
            }

            assert false : gender;

            return "";
         }
      }

      return "";
   }

   public final String getArticleWithSpace(int flags) {
      return getArticleWithSpace(this.getGender(), flags);
   }

   public String toString(int flags) {
      this.validate();
      StringBuilder b = new StringBuilder();
      b.append(this.getArticleWithSpace(flags));
      b.append((flags & 1) != 0 ? this.plural : this.singular);
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
   }

   public String withAmount(double amount) {
      this.validate();
      return Skript.toString(amount) + " " + (amount == 1.0 ? this.singular : this.plural);
   }

   public String withAmount(double amount, int flags) {
      this.validate();
      if (amount == 1.0) {
         if (this.gender == -3) {
            return this.toString((flags & 1) != 0);
         }

         if (this.gender == -2) {
            if ((flags & 2) != 0) {
               return definitePluralArticle + " " + this.plural;
            }

            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.plural);
         }

         if ((flags & 2) != 0) {
            return (flags & 1) != 0 ? definitePluralArticle + " " + this.plural : definiteArticles.get(this.gender) + " " + this.singular;
         }

         if ((flags & 4) != 0) {
            return indefiniteArticles.get(this.gender) + " " + this.singular;
         }

         if ((flags & 1) != 0) {
            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.plural);
         }
      }

      return Skript.toString(amount) + " " + (amount == 1.0 ? this.singular : this.plural);
   }

   public String toString(Adjective a, int flags) {
      this.validate();
      StringBuilder b = new StringBuilder();
      b.append(this.getArticleWithSpace(flags));
      b.append(a.toString(this.gender, flags));
      b.append(" ");
      b.append((flags & 1) != 0 ? this.plural : this.singular);
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
   }

   public String toString(Adjective[] adjectives, int flags, boolean and) {
      this.validate();
      if (adjectives.length == 0) {
         return this.toString(flags);
      } else {
         StringBuilder b = new StringBuilder();
         b.append(this.getArticleWithSpace(flags));
         b.append(Adjective.toString(adjectives, this.getGender(), flags, and));
         b.append(" ");
         b.append(this.toString(flags));
         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
      }
   }

   public String getSingular() {
      this.validate();
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.singular);
   }

   public String getPlural() {
      this.validate();
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.plural);
   }

   @Deprecated(
      since = "2.14",
      forRemoval = true
   )
   public static NonNullPair<String, String> getPlural(String input) {
      Noun.PluralPair pair = parsePlural(input);
      return new NonNullPair<>(pair.singular, pair.plural);
   }

   public static Noun.PluralPair parsePlural(String input) {
      StringBuilder singular = new StringBuilder();
      StringBuilder plural = new StringBuilder();
      int part = 3;
      char marker = 166;
      int markerCount = StringUtils.count(input, marker);
      int last = 0;

      for (int c = -1; (c = input.indexOf(marker, c + 1)) != -1; markerCount--) {
         String x = input.substring(last, c);
         if ((part & 1) != 0) {
            singular.append(x);
         }

         if ((part & 2) != 0) {
            plural.append(x);
         }

         part = markerCount >= 2 ? part % 3 + 1 : (part == 2 ? 3 : 2);
         last = c + 1;
      }

      String xx = input.substring(last);
      if ((part & 1) != 0) {
         singular.append(xx);
      }

      if ((part & 2) != 0) {
         plural.append(xx);
      }

      return new Noun.PluralPair(singular.toString(), plural.toString());
   }

   public static String normalizePluralMarkers(String s) {
      int c = StringUtils.count(s, '¦');
      if (c % 3 == 0) {
         return s;
      } else if (c % 3 == 2) {
         int g = s.lastIndexOf(64);
         return g == -1 ? s + "¦" : s.substring(0, g) + "¦" + s.substring(g);
      } else {
         int x = s.lastIndexOf(166);
         int g = s.lastIndexOf(64);
         return g == -1 ? s.substring(0, x) + "¦" + s.substring(x) + "¦" : s.substring(0, x) + "¦" + s.substring(x, g) + "¦" + s.substring(g);
      }
   }

   public static int getGender(String gender, String key) {
      if (gender.equalsIgnoreCase("x")) {
         return -2;
      } else if (gender.equalsIgnoreCase("-")) {
         return -3;
      } else {
         Integer i = genders.get(gender);
         if (i != null) {
            return i;
         } else {
            Skript.warning("Undefined gender '" + gender + "' at " + key);
            return 0;
         }
      }
   }

   @Nullable
   public static String getGenderID(int gender) {
      if (gender == -2) {
         return "x";
      } else {
         return gender == -3 ? "-" : Language.get_("genders." + gender + ".id");
      }
   }

   public static NonNullPair<String, Integer> stripGender(String s, String key) {
      int c = s.lastIndexOf(64);
      int g = -1;
      if (c != -1) {
         g = getGender(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(c + 1).trim()), key);
         s = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(0, c).trim());
      }

      return new NonNullPair<>(s, g);
   }

   public static String stripIndefiniteArticle(String s) {
      for (String a : indefiniteArticles) {
         if (StringUtils.startsWithIgnoreCase(s, a + " ")) {
            return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(a.length() + 1));
         }
      }

      return s;
   }

   public static String stripDefiniteArticle(String string) {
      for (String article : definiteArticles) {
         if (StringUtils.startsWithIgnoreCase(string, article + " ")) {
            return string.substring(article.length() + 1);
         }
      }

      return string;
   }

   public static boolean isIndefiniteArticle(String s) {
      return indefiniteArticles.contains(s.toLowerCase());
   }

   public static boolean isDefiniteArticle(String s) {
      return definiteArticles.contains(s.toLowerCase()) || definitePluralArticle.equalsIgnoreCase(s);
   }

   public static String toString(String singular, String plural, int gender, int flags) {
      return getArticleWithSpace(gender, flags) + ((flags & 1) != 0 ? plural : singular);
   }

   static {
      Language.addListener(() -> {
         genders.clear();
         indefiniteArticles.clear();
         definiteArticles.clear();

         for (int i = 0; i < 100 && Language.keyExistsDefault("genders." + i + ".id"); i++) {
            String g = Language.get("genders." + i + ".id");
            if (!g.equalsIgnoreCase("x") && !g.equalsIgnoreCase("-")) {
               genders.put(g, i);
               String ia = Language.get_("genders." + i + ".indefinite article");
               indefiniteArticles.add(ia == null ? "" : ia);
               String da = Language.get_("genders." + i + ".definite article");
               definiteArticles.add(da == null ? "" : da);
            } else {
               Skript.error("gender #" + i + " uses a reserved character as ID, please use something different!");
            }
         }

         if (genders.isEmpty()) {
            Skript.error("No genders defined in the language file!");
            indefiniteArticles.add("");
            definiteArticles.add("");
         }

         String dpa = Language.get_("genders.plural.definite article");
         if (dpa == null) {
            Skript.error("Missing entry 'genders.plural.definite article' in the language file!");
         }

         definitePluralArticle = dpa == null ? "" : dpa;
      }, Language.LanguageListenerPriority.EARLIEST);
   }

   public record PluralPair(String singular, String plural) {
   }
}
