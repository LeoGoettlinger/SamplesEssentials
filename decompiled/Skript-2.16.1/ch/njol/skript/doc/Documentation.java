package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.Parameter;

@SuppressFBWarnings({"ES_COMPARING_STRINGS_WITH_EQ"})
public class Documentation {
   private static final Pattern CP_PARSE_MARKS_PATTERN = Pattern.compile("(?<=[(|])[-0-9]+?¦");
   private static final Pattern CP_EMPTY_PARSE_MARKS_PATTERN = Pattern.compile("\\(\\)");
   private static final Pattern CP_PARSE_TAGS_PATTERN = Pattern.compile("(?<=[(|\\[ ])[-a-zA-Z0-9!$#%^&*_+~=\"'<>?,.]*?:");
   private static final Pattern CP_EXTRA_OPTIONAL_PATTERN = Pattern.compile("\\[\\(((\\w+? ?)+)\\)]");
   private static final File DOCS_TEMPLATE_DIRECTORY = new File(Skript.getInstance().getDataFolder(), "docs/templates");
   private static final File DOCS_OUTPUT_DIRECTORY = new File(Skript.getInstance().getDataFolder(), "docs");
   public static final boolean FORCE_HOOKS_SYSTEM_PROPERTY = "true".equals(System.getProperty("skript.forceregisterhooks"));
   public static final boolean generate = Skript.testing() && new File(Skript.getInstance().getDataFolder(), "generate-doc").exists();
   private static ArrayList<Pattern> validation = new ArrayList<>();
   private static final String[] urls = new String[]{"expressions", "effects", "conditions"};

   public static boolean isDocsTemplateFound() {
      return getDocsTemplateDirectory().isDirectory();
   }

   public static boolean canGenerateUnsafeDocs() {
      return isDocsTemplateFound() && FORCE_HOOKS_SYSTEM_PROPERTY;
   }

   public static File getDocsTemplateDirectory() {
      String environmentTemplateDir = System.getenv("SKRIPT_DOCS_TEMPLATE_DIR");
      return environmentTemplateDir == null ? DOCS_TEMPLATE_DIRECTORY : new File(environmentTemplateDir);
   }

   public static File getDocsOutputDirectory() {
      String environmentOutputDir = System.getenv("SKRIPT_DOCS_OUTPUT_DIR");
      return environmentOutputDir == null ? DOCS_OUTPUT_DIRECTORY : new File(environmentOutputDir);
   }

   public static void generate() {
      if (generate) {
         try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(Skript.getInstance().getDataFolder(), "doc.sql")), "UTF-8"));
            asSql(pw);
            pw.flush();
            pw.close();
         } catch (FileNotFoundException var1) {
            var1.printStackTrace();
         } catch (UnsupportedEncodingException var2) {
            var2.printStackTrace();
         }
      }
   }

   private static void asSql(PrintWriter pw) {
      pw.println("-- syntax elements");
      pw.println(
         "CREATE TABLE IF NOT EXISTS syntax_elements (id VARCHAR(20) NOT NULL PRIMARY KEY,name VARCHAR(100) NOT NULL,type ENUM('condition','effect','expression','event') NOT NULL,patterns VARCHAR(2000) NOT NULL,description VARCHAR(2000) NOT NULL,examples VARCHAR(2000) NOT NULL,since VARCHAR(100) NOT NULL);"
      );
      pw.println("UPDATE syntax_elements SET patterns='';");
      pw.println();
      pw.println("-- expressions");

      for (ExpressionInfo<?, ?> e : new IteratorIterable<>(Skript.getExpressions())) {
         assert e != null;

         insertSyntaxElement(pw, e, "expression");
      }

      pw.println();
      pw.println("-- effects");

      for (SyntaxElementInfo<?> info : Skript.getEffects()) {
         assert info != null;

         insertSyntaxElement(pw, info, "effect");
      }

      pw.println();
      pw.println("-- conditions");

      for (SyntaxElementInfo<?> info : Skript.getConditions()) {
         assert info != null;

         insertSyntaxElement(pw, info, "condition");
      }

      pw.println();
      pw.println("-- events");

      for (SkriptEventInfo<?> info : Skript.getEvents()) {
         assert info != null;

         insertEvent(pw, info);
      }

      pw.println();
      pw.println();
      pw.println("-- classes");
      pw.println(
         "CREATE TABLE IF NOT EXISTS classes (id VARCHAR(20) NOT NULL PRIMARY KEY,name VARCHAR(100) NOT NULL,description VARCHAR(2000) NOT NULL,patterns VARCHAR(2000) NOT NULL,`usage` VARCHAR(2000) NOT NULL,examples VARCHAR(2000) NOT NULL,since VARCHAR(100) NOT NULL);"
      );
      pw.println("UPDATE classes SET patterns='';");
      pw.println();

      for (ClassInfo<?> info : Classes.getClassInfos()) {
         assert info != null;

         insertClass(pw, info);
      }

      pw.println();
      pw.println();
      pw.println("-- functions");
      pw.println(
         "CREATE TABLE IF NOT EXISTS functions (name VARCHAR(100) NOT NULL,parameters VARCHAR(2000) NOT NULL,description VARCHAR(2000) NOT NULL,examples VARCHAR(2000) NOT NULL,since VARCHAR(100) NOT NULL);"
      );

      for (Function<?> func : Functions.getFunctions()) {
         assert func != null;

         insertFunction(pw, func);
      }
   }

   private static String convertRegex(String regex, boolean escapeHTML) {
      if (StringUtils.containsAny(regex, ".[]\\*+")) {
         Skript.error("Regex '" + regex + "' contains unconverted Regex syntax");
      }

      regex = escapeHTML ? escapeHTML(regex) : regex;
      return regex.replaceAll("\\((.+?)\\)\\?", "[$1]").replaceAll("(.)\\?", "[$1]");
   }

   private static String convertRegex(String regex) {
      return convertRegex(regex, true);
   }

   protected static String cleanPatterns(String patterns) {
      return cleanPatterns(patterns, true);
   }

   protected static String cleanPatterns(String patterns, boolean escapeHTML) {
      return cleanPatterns(patterns, escapeHTML, true);
   }

   protected static String cleanPatterns(String patterns, boolean escapeHTML, boolean useLinks) {
      String cleanedPatterns = escapeHTML ? escapeHTML(patterns) : patterns;
      cleanedPatterns = CP_PARSE_MARKS_PATTERN.matcher(cleanedPatterns).replaceAll("");
      cleanedPatterns = CP_EMPTY_PARSE_MARKS_PATTERN.matcher(cleanedPatterns).replaceAll("");
      cleanedPatterns = CP_PARSE_TAGS_PATTERN.matcher(cleanedPatterns).replaceAll("");
      cleanedPatterns = CP_EXTRA_OPTIONAL_PATTERN.matcher(cleanedPatterns).replaceAll("[$1]");
      java.util.function.Function<Matcher, String> callback = m -> {
         String group = m.group();
         boolean startToEnd = group.contains("(|");
         int depth = 0;
         int charIndex = 0;
         char[] chars = group.toCharArray();

         for (int i = startToEnd ? 0 : chars.length - 1; startToEnd ? i < chars.length : i >= 0; i = startToEnd ? i + 1 : i - 1) {
            char c = chars[i];
            if (c == ')') {
               if (startToEnd && ++depth == 0) {
                  charIndex = i;
                  break;
               }
            } else if (c == '(') {
               if (!startToEnd && --depth == 0) {
                  charIndex = i;
                  break;
               }
            } else if (c == '\\') {
               i--;
            }
         }

         if (depth != 0 || charIndex == 0) {
            return group;
         } else {
            return startToEnd
               ? "[" + group.substring(0, charIndex).replace("(|", "") + "]" + group.substring(charIndex + 1, chars.length)
               : group.substring(0, charIndex) + "[" + group.substring(charIndex + 1, chars.length).replace("|)", "") + "]";
         }
      };
      cleanedPatterns = cleanedPatterns.replaceAll("\\(([^()]+?)\\|\\)", "[($1)]");
      cleanedPatterns = cleanedPatterns.replaceAll("\\(\\|([^()]+?)\\)", "[($1)]");
      cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\((.+)\\|\\)", callback);

      assert cleanedPatterns != null;

      cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\((.+?)\\|\\)", callback);

      assert cleanedPatterns != null;

      cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\(\\|(.+)\\)", callback);

      assert cleanedPatterns != null;

      cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\(\\|(.+?)\\)", callback);

      assert cleanedPatterns != null;

      String s = StringUtils.replaceAll(cleanedPatterns, "(?<!\\\\)%(.+?)(?<!\\\\)%", m -> {
         String s1 = m.group(1);
         if (s1.startsWith("-")) {
            s1 = s1.substring(1);
         }

         String flag = "";
         if (s1.startsWith("*") || s1.startsWith("~")) {
            flag = s1.substring(0, 1);
            s1 = s1.substring(1);
         }

         int a = s1.indexOf("@");
         if (a != -1) {
            s1 = s1.substring(0, a);
         }

         StringBuilder b = new StringBuilder("%");
         b.append(flag);
         boolean first = true;

         for (String c : s1.split("/")) {
            assert c != null;

            if (!first) {
               b.append("/");
            }

            first = false;
            NonNullPair<String, Boolean> p = Utils.getEnglishPlural(c);
            ClassInfo<?> ci = Classes.getClassInfoNoError(p.getFirst());
            if (ci == null || !ci.hasDocs()) {
               b.append(c);
               if (ci != null && ci.hasDocs()) {
                  Skript.warning("Used class " + p.getFirst() + " has no docName/name defined");
               }
            } else if (useLinks) {
               b.append("<a href='./classes.html#").append(p.getFirst()).append("'>").append(ci.getName().toString(p.getSecond())).append("</a>");
            } else {
               b.append(ci.getName().toString(p.getSecond()));
            }
         }

         return b.append("%").toString();
      });

      assert s != null : patterns;

      return s;
   }

   private static void insertSyntaxElement(PrintWriter pw, SyntaxElementInfo<?> info, String type) {
      Class<?> elementClass = info.getElementClass();
      if (elementClass.getAnnotation(NoDoc.class) == null) {
         if (elementClass.getAnnotation(Name.class) != null
            && elementClass.getAnnotation(Description.class) != null
            && (
               elementClass.isAnnotationPresent(Examples.class)
                  || elementClass.isAnnotationPresent(Example.class)
                  || elementClass.isAnnotationPresent(Example.Examples.class)
            )
            && elementClass.getAnnotation(Since.class) != null) {
            String desc = validateHTML(StringUtils.join(elementClass.getAnnotation(Description.class).value(), "<br/>"), type + "s");
            String since = validateHTML(StringUtils.join(elementClass.getAnnotation(Since.class).value(), "<br/>"), type + "s");
            if (desc != null && since != null) {
               String patterns = cleanPatterns(StringUtils.join(info.patterns, "\n", 0, elementClass == CondCompare.class ? 8 : info.getPatterns().length));
               insertOnDuplicateKeyUpdate(
                  pw,
                  "syntax_elements",
                  "id, name, type, patterns, description, examples, since",
                  "patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
                  escapeHTML(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(elementClass.getSimpleName())),
                  escapeHTML(elementClass.getAnnotation(Name.class).value()),
                  type,
                  patterns,
                  desc,
                  escapeHTML(StringUtils.join(elementClass.getAnnotation(Examples.class).value(), "\n")),
                  since
               );
            } else {
               Skript.warning(elementClass.getSimpleName() + "'s description or 'since' is invalid");
            }
         } else {
            Skript.warning(elementClass.getSimpleName() + " is missing information");
         }
      }
   }

   private static void insertEvent(PrintWriter pw, SkriptEventInfo<?> info) {
      if (info.getDescription() != SkriptEventInfo.NO_DOC) {
         if (info.getDescription() != null && info.getExamples() != null && info.getSince() != null) {
            for (SkriptEventInfo<?> i : Skript.getEvents()) {
               if (info.getId().equals(i.getId()) && info != i && i.getDescription() != null && i.getDescription() != SkriptEventInfo.NO_DOC) {
                  Skript.warning("Duplicate event id '" + info.getId() + "'");
                  return;
               }
            }

            String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "events");
            String since = validateHTML(StringUtils.join(info.getSince(), "<br/>"), "events");
            if (desc != null && since != null) {
               String patterns = cleanPatterns(
                  info.getName().startsWith("On ") ? "[on] " + StringUtils.join(info.getPatterns(), "\n[on] ") : StringUtils.join(info.patterns, "\n")
               );
               insertOnDuplicateKeyUpdate(
                  pw,
                  "syntax_elements",
                  "id, name, type, patterns, description, examples, since",
                  "patterns = '" + escapeSQL(patterns) + "'",
                  escapeHTML(info.getId()),
                  escapeHTML(info.getName()),
                  "event",
                  patterns,
                  desc,
                  escapeHTML(StringUtils.join(info.getExamples(), "\n")),
                  since
               );
            } else {
               Skript.warning("description or 'since' of " + info.getName() + " (" + info.getElementClass().getSimpleName() + ") is invalid");
            }
         } else {
            Skript.warning(info.getName() + " (" + info.getElementClass().getSimpleName() + ") is missing information");
         }
      }
   }

   private static void insertClass(PrintWriter pw, ClassInfo<?> info) {
      if (info.getDocName() != ClassInfo.NO_DOC) {
         if (info.getDocName() != null && info.getDescription() != null && info.getUsage() != null && info.getExamples() != null && info.getSince() != null) {
            String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "classes");
            String usage = validateHTML(StringUtils.join(info.getUsage(), "<br/>"), "classes");
            String since = info.getSince() == null ? "" : validateHTML(info.getSince(), "classes");
            if (desc != null && usage != null && since != null) {
               String patterns = info.getUserInputPatterns() == null ? "" : convertRegex(StringUtils.join(info.getUserInputPatterns(), "\n"));
               insertOnDuplicateKeyUpdate(
                  pw,
                  "classes",
                  "id, name, description, patterns, `usage`, examples, since",
                  "patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
                  escapeHTML(info.getCodeName()),
                  escapeHTML(info.getDocName()),
                  desc,
                  patterns,
                  usage,
                  escapeHTML(StringUtils.join(info.getExamples(), "\n")),
                  since
               );
            } else {
               Skript.warning("Class " + info.getCodeName() + "'s description, usage or 'since' is invalid");
            }
         } else {
            Skript.warning("Class " + info.getCodeName() + " is missing information");
         }
      }
   }

   private static void insertFunction(PrintWriter pw, Function<?> func) {
      if (func instanceof Documentable documentable) {
         String[] typeSince = documentable.since().toArray(new String[0]);
         String[] typeDescription = documentable.description().toArray(new String[0]);
         String[] typeExamples = documentable.examples().toArray(new String[0]);
         StringBuilder params = new StringBuilder();

         for (Parameter<?> p : func.getSignature().parameters().all()) {
            if (!params.isEmpty()) {
               params.append(", ");
            }

            params.append(p.toString());
         }

         String desc = validateHTML(StringUtils.join(typeDescription, "<br/>"), "functions");
         String since = validateHTML(StringUtils.join(typeSince, "\n"), "functions");
         if (desc != null && since != null) {
            replaceInto(
               pw,
               "functions",
               "name, parameters, description, examples, since",
               escapeHTML(func.getName()),
               escapeHTML(params.toString()),
               desc,
               escapeHTML(StringUtils.join(typeExamples, "\n")),
               since
            );
         } else {
            Skript.warning("Function " + func.getName() + "'s description or 'since' is invalid");
         }
      } else {
         assert false;
      }
   }

   private static void insertOnDuplicateKeyUpdate(PrintWriter pw, String table, String fields, String update, String... values) {
      for (int i = 0; i < values.length; i++) {
         values[i] = escapeSQL(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(values[i]));
      }

      pw.println("INSERT INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "') ON DUPLICATE KEY UPDATE " + update + ";");
   }

   private static void replaceInto(PrintWriter pw, String table, String fields, String... values) {
      for (int i = 0; i < values.length; i++) {
         values[i] = escapeSQL(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(values[i]));
      }

      pw.println("REPLACE INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "');");
   }

   @Nullable
   private static String validateHTML(@Nullable String html, String baseURL) {
      if (html == null) {
         assert false;

         return null;
      } else {
         for (Pattern p : validation) {
            if (p.matcher(html).find()) {
               return null;
            }
         }

         html = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(html.replaceAll("&(?!(amp|lt|gt|quot);)", "&amp;"));
         Matcher m = Pattern.compile("<a href='(.*?)'>").matcher(html);

         label78:
         while (m.find()) {
            String url = m.group(1);
            String[] s = url.split("#");
            if (s.length != 1) {
               if (s[0].isEmpty()) {
                  s[0] = "../" + baseURL + "/";
               }

               if (s[0].startsWith("../") && s[0].endsWith("/")) {
                  if (s[0].equals("../classes/")) {
                     if (Classes.getClassInfoNoError(s[1]) != null) {
                        continue;
                     }
                  } else if (s[0].equals("../events/")) {
                     for (SkriptEventInfo<?> i : Skript.getEvents()) {
                        if (s[1].equals(i.getId())) {
                           continue label78;
                        }
                     }
                  } else if (s[0].equals("../functions/")) {
                     if (Functions.getGlobalFunction(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s[1])) != null) {
                        continue;
                     }
                  } else {
                     int ix = CollectionUtils.indexOf(urls, s[0].substring("../".length(), s[0].length() - 1));
                     if (ix != -1) {
                        try {
                           Class.forName("ch.njol.skript." + urls[ix] + "." + s[1]);
                           continue;
                        } catch (ClassNotFoundException var7) {
                        }
                     }
                  }
               }

               Skript.warning("invalid link '" + url + "' found in '" + html + "'");
            }
         }

         return html;
      }
   }

   private static String escapeSQL(String value) {
      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value.replace("'", "\\'").replace("\"", "\\\""));
   }

   public static String escapeHTML(@Nullable String value) {
      if (value == null) {
         assert false;

         return "";
      } else {
         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(
            value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
         );
      }
   }

   public static String[] escapeHTML(@Nullable String[] values) {
      for (int i = 0; i < values.length; i++) {
         values[i] = escapeHTML(values[i]);
      }

      return values;
   }

   static {
      validation.add(Pattern.compile("<(?!a href='|/a>|br ?/|/?(i|b|u|code|pre|ul|li|em)>)"));
      validation.add(Pattern.compile("(?<!</a|'|br ?/|/?(i|b|u|code|pre|ul|li|em))>"));
   }
}
