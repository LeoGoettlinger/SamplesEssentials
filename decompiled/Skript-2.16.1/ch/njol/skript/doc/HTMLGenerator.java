package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.StructureInfo;
import org.skriptlang.skript.registration.SyntaxInfo;

public class HTMLGenerator extends DocumentationGenerator {
   private static final String SKRIPT_VERSION = Skript.getVersion().toString().replaceAll("-(dev|alpha|beta|pre)\\d*", "").replace(".0", "");
   private static final Pattern NEW_TAG_PATTERN = Pattern.compile(SKRIPT_VERSION + "(?!\\.[1-9])");
   private static final Pattern RETURN_TYPE_LINK_PATTERN = Pattern.compile("( ?href=\"(classes\\.html|)#|)\\$\\{element\\.return-type-linkcheck}");
   private final String skeleton = readFile(new File(this.templateDir + "/template.html"));
   private static final Comparator<? super SyntaxElementInfo<?>> annotatedComparator = (o1, o2) -> {
      if (o1 != null && o2 != null) {
         if (o1.getElementClass().getAnnotation(NoDoc.class) != null) {
            return o2.getElementClass().getAnnotation(NoDoc.class) != null ? 0 : 1;
         } else if (o2.getElementClass().getAnnotation(NoDoc.class) != null) {
            return -1;
         } else {
            Name name1 = o1.getElementClass().getAnnotation(Name.class);
            Name name2 = o2.getElementClass().getAnnotation(Name.class);
            if (name1 == null) {
               throw new SkriptAPIException("Name annotation expected: " + o1.getElementClass());
            } else if (name2 == null) {
               throw new SkriptAPIException("Name annotation expected: " + o2.getElementClass());
            } else {
               return name1.value().compareTo(name2.value());
            }
         }
      } else {
         assert false;

         throw new NullPointerException();
      }
   };
   private static final HTMLGenerator.EventComparator eventComparator = new HTMLGenerator.EventComparator();
   private static final HTMLGenerator.ClassInfoComparator classInfoComparator = new HTMLGenerator.ClassInfoComparator();
   private static final HTMLGenerator.FunctionComparator functionComparator = new HTMLGenerator.FunctionComparator();

   public HTMLGenerator(File templateDir, File outputDir) {
      super(templateDir, outputDir);
   }

   private static <T> Iterator<SyntaxElementInfo<? extends T>> sortedAnnotatedIterator(Iterator<SyntaxElementInfo<? extends T>> it) {
      List<SyntaxElementInfo<? extends T>> list = new ArrayList<>();

      while (it.hasNext()) {
         SyntaxElementInfo<? extends T> item = it.next();
         if (item.getElementClass().getAnnotation(Name.class) == null && item.getElementClass().getAnnotation(NoDoc.class) == null) {
            Skript.warning("Skipped generating '" + item.getElementClass() + "' class due to missing Name annotation");
         } else {
            list.add(item);
         }
      }

      list.sort((Comparator<? super SyntaxElementInfo<? extends T>>)annotatedComparator);
      return list.iterator();
   }

   @Override
   public void generate() {
      for (File f : this.templateDir.listFiles()) {
         if (f.getName().matches("css|js|assets")) {
            String slashName = "/" + f.getName();
            File fileTo = new File(this.outputDir + slashName);
            fileTo.mkdirs();

            for (File filesInside : new File(this.templateDir + slashName).listFiles()) {
               if (!filesInside.isDirectory()) {
                  if (!filesInside.getName().toLowerCase(Locale.ENGLISH).endsWith(".png")) {
                     writeFile(new File(fileTo + "/" + filesInside.getName()), readFile(filesInside));
                  } else if (!filesInside.getName().matches("(?i)(.*)\\.(html?|js|css|json)")) {
                     try {
                        Files.copy(filesInside, new File(fileTo + "/" + filesInside.getName()));
                     } catch (IOException var19) {
                        var19.printStackTrace();
                     }
                  }
               }
            }
         } else if (!f.isDirectory() && !f.getName().endsWith("template.html") && !f.getName().endsWith(".md")) {
            Skript.info("Creating documentation for " + f.getName());
            String content = readFile(f);
            String page;
            if (f.getName().endsWith(".html")) {
               page = this.skeleton.replace("${content}", content);
            } else {
               page = content;
            }

            page = page.replace("${skript.version}", Skript.getVersion().toString());
            page = page.replace("${skript.build.date}", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            page = page.replace("${pagename}", f.getName().replace(".html", ""));
            List<String> replace = Lists.newArrayList();
            int include = page.indexOf("${include");

            while (include != -1) {
               int endIncl = page.indexOf("}", include);
               String name = page.substring(include + 10, endIncl);
               replace.add(name);
               include = page.indexOf("${include", endIncl);
            }

            for (String name : replace) {
               String temp = readFile(new File(this.templateDir + "/templates/" + name));
               temp = temp.replace("${skript.version}", Skript.getVersion().toString());
               page = page.replace("${include " + name + "}", temp);
            }

            int generate = page.indexOf("${generate");

            while (generate != -1) {
               int nextBracket = page.indexOf("}", generate);
               String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
               StringBuilder generated = new StringBuilder();
               String descTemp = readFile(new File(this.templateDir + "/templates/" + genParams[1]));
               String genType = genParams[0];
               boolean isDocsPage = genType.equals("docs");
               if (genType.equals("structures") || isDocsPage) {
                  Iterator<StructureInfo<?>> it = sortedAnnotatedIterator(
                     Skript.getStructures().stream().filter(structure -> structure.getClass() == StructureInfo.class).iterator()
                  );

                  while (it.hasNext()) {
                     StructureInfo<?> info = it.next();

                     assert info != null;

                     if (info.getElementClass().getAnnotation(NoDoc.class) == null) {
                        String desc = this.generateAnnotated(descTemp, info, generated.toString(), "Structure");
                        generated.append(desc);
                     }
                  }
               }

               if (genType.equals("expressions") || isDocsPage) {
                  Iterator<ExpressionInfo<?, ?>> it = sortedAnnotatedIterator(Skript.getExpressions());

                  while (it.hasNext()) {
                     ExpressionInfo<?, ?> infox = it.next();

                     assert infox != null;

                     if (infox.getElementClass().getAnnotation(NoDoc.class) == null) {
                        String desc = this.generateAnnotated(descTemp, infox, generated.toString(), "Expression");
                        generated.append(desc);
                     }
                  }
               }

               if (genType.equals("effects") || isDocsPage) {
                  Iterator<SyntaxElementInfo<? extends Effect>> it = sortedAnnotatedIterator(Skript.getEffects().iterator());

                  while (it.hasNext()) {
                     SyntaxElementInfo<? extends Effect> infoxx = it.next();

                     assert infoxx != null;

                     if (infoxx.getElementClass().getAnnotation(NoDoc.class) == null) {
                        generated.append(this.generateAnnotated(descTemp, infoxx, generated.toString(), "Effect"));
                     }
                  }

                  it = sortedAnnotatedIterator(Skript.getSections().iterator());

                  while (it.hasNext()) {
                     SyntaxElementInfo<? extends Section> infoxxx = it.next();

                     assert infoxxx != null;

                     if (EffectSection.class.isAssignableFrom(infoxxx.getElementClass()) && infoxxx.getElementClass().getAnnotation(NoDoc.class) == null) {
                        generated.append(this.generateAnnotated(descTemp, infoxxx, generated.toString(), "EffectSection"));
                     }
                  }
               }

               if (genType.equals("conditions") || isDocsPage) {
                  Iterator<SyntaxElementInfo<? extends Condition>> it = sortedAnnotatedIterator(Skript.getConditions().iterator());

                  while (it.hasNext()) {
                     SyntaxElementInfo<? extends Condition> infoxxxx = it.next();

                     assert infoxxxx != null;

                     if (infoxxxx.getElementClass().getAnnotation(NoDoc.class) == null) {
                        generated.append(this.generateAnnotated(descTemp, infoxxxx, generated.toString(), "Condition"));
                     }
                  }
               }

               if (genType.equals("sections") || isDocsPage) {
                  Iterator<SyntaxElementInfo<? extends Section>> it = sortedAnnotatedIterator(Skript.getSections().iterator());

                  while (it.hasNext()) {
                     SyntaxElementInfo<? extends Section> infoxxxxx = it.next();

                     assert infoxxxxx != null;

                     boolean isEffectSection = EffectSection.class.isAssignableFrom(infoxxxxx.getElementClass());
                     if ((!isEffectSection || !isDocsPage) && infoxxxxx.getElementClass().getAnnotation(NoDoc.class) == null) {
                        generated.append(this.generateAnnotated(descTemp, infoxxxxx, generated.toString(), (isEffectSection ? "Effect" : "") + "Section"));
                     }
                  }
               }

               if (genType.equals("events") || isDocsPage) {
                  List<BukkitSyntaxInfos.Event<?>> events = new ArrayList<>(Skript.instance().syntaxRegistry().syntaxes(BukkitSyntaxInfos.Event.KEY));
                  events.sort(eventComparator);

                  for (BukkitSyntaxInfos.Event<?> infoxxxxxx : events) {
                     assert infoxxxxxx != null;

                     if (infoxxxxxx.type().getAnnotation(NoDoc.class) == null) {
                        generated.append(this.generateEvent(descTemp, infoxxxxxx, generated.toString()));
                     }
                  }
               }

               if (genType.equals("classes") || isDocsPage) {
                  List<ClassInfo<?>> classes = new ArrayList<>(Classes.getClassInfos());
                  classes.sort(classInfoComparator);

                  for (ClassInfo<?> infoxxxxxx : classes) {
                     if (infoxxxxxx.hasDocs()) {
                        assert infoxxxxxx != null;

                        generated.append(this.generateClass(descTemp, infoxxxxxx, generated.toString()));
                     }
                  }
               }

               if (genType.equals("functions") || isDocsPage) {
                  List<Function<?>> functions = new ArrayList<>(Functions.getFunctions());
                  functions.sort(functionComparator);

                  for (Function<?> infox : functions) {
                     assert infox != null;

                     generated.append(this.generateFunction(descTemp, infox));
                  }
               }

               page = page.replace(page.substring(generate, nextBracket + 1), generated.toString());
               generate = page.indexOf("${generate", nextBracket);
            }

            String name = f.getName();
            if (name.endsWith(".html")) {
               page = page.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

               assert page != null;

               page = minifyHtml(page);
            }

            assert page != null;

            writeFile(new File(this.outputDir + File.separator + name), page);
         }
      }
   }

   private static String minifyHtml(String page) {
      StringBuilder sb = new StringBuilder(page.length());
      boolean space = false;
      int i = 0;

      while (i < page.length()) {
         int c = page.codePointAt(i);
         if (c != 10 && c != 32) {
            space = false;
            sb.appendCodePoint(c);
         } else if (!space) {
            sb.append(' ');
            space = true;
         }

         i += Character.charCount(c);
      }

      return sb.toString();
   }

   private static String handleIf(String desc, String condition, boolean value) {
      assert desc != null;

      int ifStart = desc.indexOf(condition);

      while (ifStart != -1) {
         int ifEnd = desc.indexOf("${end}", ifStart);
         String data = desc.substring(ifStart + condition.length() + 1, ifEnd);
         String before = desc.substring(0, ifStart);
         String after = desc.substring(ifEnd + 6);
         if (value) {
            desc = before + data + after;
         } else {
            desc = before + after;
         }

         ifStart = desc.indexOf(condition, ifEnd);
      }

      return desc;
   }

   private String generateAnnotated(String descTemp, SyntaxElementInfo<?> info, @Nullable String page, String type) {
      Class<?> c = info.getElementClass();
      Name name = c.getAnnotation(Name.class);
      String desc = descTemp.replace("${element.name}", this.getDefaultIfNullOrEmpty(name != null ? name.value() : null, "Unknown Name"));
      Since since = c.getAnnotation(Since.class);
      desc = desc.replace("${element.since}", Joiner.on("<br/>").join(this.getDefaultIfNullOrEmpty(since != null ? since.value() : null, "Unknown")));
      Keywords keywords = c.getAnnotation(Keywords.class);
      desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords.value()));
      Description description = c.getAnnotation(Description.class);
      desc = desc.replace(
         "${element.desc}",
         Joiner.on("<br>").join(this.getDefaultIfNullOrEmpty(description != null ? description.value() : null, "Unknown description.")).replace("\n\n", "<p>")
      );
      desc = desc.replace(
         "${element.desc-safe}",
         Joiner.on("<br>")
            .join(this.getDefaultIfNullOrEmpty(description != null ? description.value() : null, "Unknown description."))
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\t", "    ")
      );
      desc = this.extractExamples(desc, c);
      desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));
      desc = handleIf(desc, "${if cancellable}", false);
      Events events = c.getAnnotation(Events.class);
      desc = handleIf(desc, "${if events}", events != null);
      if (events != null) {
         String[] eventNames = events != null ? events.value() : null;
         String[] eventLinks = new String[eventNames.length];

         for (int i = 0; i < eventNames.length; i++) {
            String eventName = eventNames[i];
            eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll("( ?/ ?| +)", "_") + "\">" + eventName + "</a>";
         }

         desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
      }

      desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join(events != null ? events.value() : null));
      RequiredPlugins plugins = c.getAnnotation(RequiredPlugins.class);
      desc = handleIf(desc, "${if required-plugins}", plugins != null);
      desc = desc.replace("${element.required-plugins}", plugins == null ? "" : Joiner.on(", ").join(plugins != null ? plugins.value() : null));
      ClassInfo<?> returnType = info instanceof ExpressionInfo ? Classes.getSuperClassInfo(((ExpressionInfo)info).getReturnType()) : null;
      desc = this.replaceReturnType(desc, returnType);
      desc = handleIf(desc, "${if by-addon}", false);
      if (since != null) {
         String[] value = since.value();
         String s = value[value.length - 1];
         desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(s).find());
      } else {
         desc = handleIf(desc, "${if new-element}", false);
      }

      if (info instanceof StructureInfo) {
         EntryValidator entryValidator = ((StructureInfo)info).entryValidator;
         List<EntryData<?>> entryDataList = new ArrayList<>();
         if (entryValidator != null) {
            entryDataList.addAll(entryValidator.getEntryData());
         }

         desc = handleIf(desc, "${if structure-optional-entrydata}", entryValidator != null);
         desc = desc.replace(
            "${element.structure-optional-entrydata}",
            entryValidator == null
               ? ""
               : Joiner.on(", ").join(entryDataList.stream().filter(EntryData::isOptional).map(EntryData::getKey).collect(Collectors.toList()))
         );
         desc = handleIf(desc, "${if structure-required-entrydata}", entryValidator != null);
         desc = desc.replace(
            "${element.structure-required-entrydata}",
            entryValidator == null
               ? ""
               : Joiner.on(", ").join(entryDataList.stream().filter(entryData -> !entryData.isOptional()).map(EntryData::getKey).collect(Collectors.toList()))
         );
      } else {
         desc = handleIf(desc, "${if structure-optional-entrydata}", false);
         desc = handleIf(desc, "${if structure-required-entrydata}", false);
      }

      desc = desc.replace("${element.type}", type);
      List<String> toGen = Lists.newArrayList();
      int generate = desc.indexOf("${generate");

      while (generate != -1) {
         int nextBracket = desc.indexOf("}", generate);
         String data = desc.substring(generate + 11, nextBracket);
         toGen.add(data);
         generate = desc.indexOf("${generate", nextBracket);
      }

      for (String data : toGen) {
         String[] split = data.split(" ");
         String pattern = readFile(new File(this.templateDir + "/templates/" + split[1]));
         StringBuilder patterns = new StringBuilder();

         for (String line : this.getDefaultIfNullOrEmpty(info.getPatterns(), "Missing patterns.")) {
            assert line != null;

            line = cleanPatterns(line);
            String parsed = pattern.replace("${element.pattern}", line);
            patterns.append(parsed);
         }

         String toReplace = "${generate element.patterns " + split[1] + "}";
         desc = desc.replace(toReplace, patterns.toString());
         desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
      }

      assert desc != null;

      return desc;
   }

   @NotNull
   private String extractExamples(String desc, Class<?> syntax) {
      if (syntax.isAnnotationPresent(Example.class)) {
         Example examples = syntax.getAnnotation(Example.class);
         return this.addExamples(desc, examples.value());
      } else if (syntax.isAnnotationPresent(Example.Examples.class)) {
         Example.Examples examples = syntax.getAnnotation(Example.Examples.class);
         Example[] examplesArray = examples.value();
         String[] mappedExamples = new String[examplesArray.length * 2 - 1];

         for (int i = 0; i < mappedExamples.length; i++) {
            if (i % 2 == 0) {
               String example = examplesArray[i / 2].value();
               if (example.endsWith("\n")) {
                  example = example.substring(0, example.length() - "\n".length());
               }

               mappedExamples[i] = example;
            } else {
               mappedExamples[i] = "";
            }
         }

         return this.addExamples(desc, mappedExamples);
      } else if (syntax.isAnnotationPresent(Examples.class)) {
         Examples examples = syntax.getAnnotation(Examples.class);
         return this.addExamples(desc, examples.value());
      } else {
         return this.addExamples(desc, (String[])null);
      }
   }

   @NotNull
   private String addExamples(String desc, @Nullable String... examples) {
      if (examples != null) {
         Documentation.escapeHTML(examples);

         for (int i = 0; i < examples.length; i++) {
            examples[i] = examples[i].replace("\n", "<br>");
         }
      }

      String mergedExamples = Joiner.on("<br>").join(this.getDefaultIfNullOrEmpty(examples, "Missing examples."));
      return desc.replace("${element.examples}", mergedExamples).replace("${element.examples-safe}", mergedExamples);
   }

   private String generateEvent(String descTemp, BukkitSyntaxInfos.Event<?> modernInfo, @Nullable String page) {
      SkriptEventInfo<?> info = SyntaxElementInfo.fromModern((SyntaxInfo<? extends SkriptEvent>)modernInfo);
      Class<?> c = info.getElementClass();
      String docName = this.getDefaultIfNullOrEmpty(info.getName(), "Unknown Name");
      String desc = descTemp.replace("${element.name}", docName);
      String[] since = this.getDefaultIfNullOrEmpty(info.getSince(), "Unknown");
      desc = desc.replace("${element.since}", Joiner.on("<br/>").join(since));
      String[] description = this.getDefaultIfNullOrEmpty(info.getDescription(), "Missing description.");
      desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description).replace("\n\n", "<p>"));
      desc = desc.replace("${element.desc-safe}", Joiner.on("<br>").join(description).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
      desc = handleIf(desc, "${if by-addon}", false);
      String[] examples = this.getDefaultIfNullOrEmpty(info.getExamples(), "Missing examples.");
      desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
      desc = desc.replace("${element.examples-safe}", Joiner.on("<br>").join(examples).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
      String[] keywords = info.getKeywords();
      desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords));
      boolean cancellable = false;

      for (Class<? extends Event> event : info.events) {
         if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event)) {
            cancellable = true;
            break;
         }
      }

      desc = handleIf(desc, "${if cancellable}", cancellable);
      desc = desc.replace("${element.cancellable}", cancellable ? "Yes" : "");
      desc = desc.replace("${element.id}", DocumentationIdProvider.getId(modernInfo));
      Events events = c.getAnnotation(Events.class);
      desc = handleIf(desc, "${if events}", events != null);
      if (events != null) {
         String[] eventNames = events != null ? events.value() : null;
         String[] eventLinks = new String[eventNames.length];

         for (int i = 0; i < eventNames.length; i++) {
            String eventName = eventNames[i];
            eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll(" ?/ ?", "_").replaceAll(" +", "_") + "\">" + eventName + "</a>";
         }

         desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
      }

      desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join(events != null ? events.value() : null));
      String[] plugins = info.getRequiredPlugins();
      desc = handleIf(desc, "${if required-plugins}", plugins != null && plugins.length > 0);
      if (plugins == null) {
         desc = desc.replace("${element.required-plugins}", "");
      } else {
         desc = desc.replace("${element.required-plugins}", Joiner.on(", ").join(plugins));
      }

      String lastValue = since[since.length - 1];
      desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(lastValue).find());
      desc = desc.replace("${element.type}", "Event");
      desc = handleIf(desc, "${if return-type}", false);
      desc = handleIf(desc, "${if structure-optional-entrydata}", false);
      desc = handleIf(desc, "${if structure-required-entrydata}", false);
      List<String> toGen = Lists.newArrayList();
      int generate = desc.indexOf("${generate");

      while (generate != -1) {
         int nextBracket = desc.indexOf("}", generate);
         String data = desc.substring(generate + 11, nextBracket);
         toGen.add(data);
         generate = desc.indexOf("${generate", nextBracket);
      }

      for (String data : toGen) {
         String[] split = data.split(" ");
         String pattern = readFile(new File(this.templateDir + "/templates/" + split[1]));
         StringBuilder patterns = new StringBuilder();

         for (String line : this.getDefaultIfNullOrEmpty(info.getPatterns(), "Missing patterns.")) {
            assert line != null;

            line = "[on] " + cleanPatterns(line);
            String parsed = pattern.replace("${element.pattern}", line);
            patterns.append(parsed);
         }

         desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
         desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
      }

      assert desc != null;

      return desc;
   }

   private String generateClass(String descTemp, ClassInfo<?> info, @Nullable String page) {
      Class<?> c = info.getC();
      String docName = this.getDefaultIfNullOrEmpty(info.getDocName(), "Unknown Name");
      String desc = descTemp.replace("${element.name}", docName);
      String since = this.getDefaultIfNullOrEmpty(info.getSince(), "Unknown");
      desc = desc.replace("${element.since}", since);
      String[] description = this.getDefaultIfNullOrEmpty(info.getDescription(), "Missing description.");
      desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description).replace("\n\n", "<p>"));
      desc = desc.replace("${element.desc-safe}", Joiner.on("<br>").join(description).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
      desc = handleIf(desc, "${if by-addon}", false);
      String[] examples = this.getDefaultIfNullOrEmpty(info.getExamples(), "Missing examples.");
      desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
      desc = desc.replace(
         "${element.examples-safe}",
         Joiner.on("<br>").join(Documentation.escapeHTML(examples)).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    ")
      );
      Keywords keywords = c.getAnnotation(Keywords.class);
      desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords.value()));
      desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));
      desc = handleIf(desc, "${if cancellable}", false);
      Events events = c.getAnnotation(Events.class);
      desc = handleIf(desc, "${if events}", events != null);
      if (events != null) {
         String[] eventNames = events != null ? events.value() : null;
         String[] eventLinks = new String[eventNames.length];

         for (int i = 0; i < eventNames.length; i++) {
            String eventName = eventNames[i];
            eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll(" ?/ ?", "_").replaceAll(" +", "_") + "\">" + eventName + "</a>";
         }

         desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
      }

      desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join(events != null ? events.value() : null));
      String[] requiredPlugins = info.getRequiredPlugins();
      desc = handleIf(desc, "${if required-plugins}", requiredPlugins != null);
      desc = desc.replace("${element.required-plugins}", Joiner.on(", ").join(requiredPlugins == null ? new String[0] : requiredPlugins));
      desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(since).find());
      desc = desc.replace("${element.type}", "Type");
      desc = handleIf(desc, "${if return-type}", false);
      desc = handleIf(desc, "${if structure-optional-entrydata}", false);
      desc = handleIf(desc, "${if structure-required-entrydata}", false);
      List<String> toGen = Lists.newArrayList();
      int generate = desc.indexOf("${generate");

      while (generate != -1) {
         int nextBracket = desc.indexOf("}", generate);
         String data = desc.substring(generate + 11, nextBracket);
         toGen.add(data);
         generate = desc.indexOf("${generate", nextBracket);
      }

      for (String data : toGen) {
         String[] split = data.split(" ");
         String pattern = readFile(new File(this.templateDir + "/templates/" + split[1]));
         StringBuilder patterns = new StringBuilder();
         String[] lines = this.getDefaultIfNullOrEmpty(info.getUsage(), "Missing patterns.");
         if (lines != null) {
            for (String line : lines) {
               assert line != null;

               String parsed = pattern.replace("${element.pattern}", line);
               patterns.append(parsed);
            }

            desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
            desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
         }
      }

      assert desc != null;

      return desc;
   }

   private String generateFunction(String descTemp, Function<?> info) {
      if (!(info instanceof Documentable documentable)) {
         assert false;

         return "";
      } else {
         String[] typeSince = documentable.since().toArray(new String[0]);
         String[] typeDescription = documentable.description().toArray(new String[0]);
         String[] typeExamples = documentable.examples().toArray(new String[0]);
         String[] typeKeywords = documentable.keywords().toArray(new String[0]);
         String docName = this.getDefaultIfNullOrEmpty(info.getName(), "Unknown Name");
         String desc = descTemp.replace("${element.name}", docName);
         String[] since = this.getDefaultIfNullOrEmpty(typeSince, "Unknown");
         desc = desc.replace("${element.since}", Joiner.on("<br>").join(since));
         String[] description = this.getDefaultIfNullOrEmpty(typeDescription, "Missing description.");
         desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description));
         desc = desc.replace("${element.desc-safe}", Joiner.on("<br>").join(description).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
         desc = handleIf(desc, "${if by-addon}", false);
         String[] examples = this.getDefaultIfNullOrEmpty(typeExamples, "Missing examples.");
         desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
         desc = desc.replace("${element.examples-safe}", Joiner.on("<br>").join(examples).replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
         desc = desc.replace("${element.keywords}", typeKeywords == null ? "" : Joiner.on(", ").join(typeKeywords));
         desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));
         desc = handleIf(desc, "${if cancellable}", false);
         desc = handleIf(desc, "${if events}", false);
         desc = handleIf(desc, "${if required-plugins}", false);
         ClassInfo<?> returnType = info.getReturnType();
         desc = this.replaceReturnType(desc, returnType);
         desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(Joiner.on(" ").join(since)).find());
         desc = desc.replace("${element.type}", "Function");
         desc = handleIf(desc, "${if structure-optional-entrydata}", false);
         desc = handleIf(desc, "${if structure-required-entrydata}", false);
         List<String> toGen = Lists.newArrayList();
         int generate = desc.indexOf("${generate");

         while (generate != -1) {
            int nextBracket = desc.indexOf("}", generate);
            String data = desc.substring(generate + 11, nextBracket);
            toGen.add(data);
            generate = desc.indexOf("${generate", nextBracket);
         }

         for (String data : toGen) {
            String[] split = data.split(" ");
            String pattern = readFile(new File(this.templateDir + "/templates/" + split[1]));
            String patterns = "";
            String line = info.getSignature().toString(false, false);
            patterns = patterns + pattern.replace("${element.pattern}", line);
            desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns);
            desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
         }

         assert desc != null;

         return desc;
      }
   }

   private static String readFile(File f) {
      try {
         return Files.toString(f, StandardCharsets.UTF_8);
      } catch (IOException var2) {
         var2.printStackTrace();
         return "";
      }
   }

   private static void writeFile(File f, String data) {
      try {
         Files.write(data, f, StandardCharsets.UTF_8);
      } catch (IOException var3) {
         var3.printStackTrace();
      }
   }

   private static String cleanPatterns(String patterns) {
      return Documentation.cleanPatterns(patterns);
   }

   private static String cleanPatterns(String patterns, boolean escapeHTML) {
      return escapeHTML ? Documentation.cleanPatterns(patterns) : Documentation.cleanPatterns(patterns, false);
   }

   public String getDefaultIfNullOrEmpty(@Nullable String string, String message) {
      return string != null && !string.isEmpty() ? string : message;
   }

   public String[] getDefaultIfNullOrEmpty(@Nullable String[] string, String message) {
      return string != null && string.length != 0 && !string[0].equals("") ? string : new String[]{message};
   }

   private String replaceReturnType(String desc, @Nullable ClassInfo<?> returnType) {
      if (returnType == null) {
         return handleIf(desc, "${if return-type}", false);
      } else {
         boolean noDoc = !returnType.hasDocs();
         String returnTypeName = noDoc ? returnType.getCodeName() : returnType.getDocName();
         String returnTypeLink = noDoc ? "" : "$1" + this.getDefaultIfNullOrEmpty(returnType.getDocumentationID(), returnType.getCodeName());
         desc = handleIf(desc, "${if return-type}", true);
         desc = RETURN_TYPE_LINK_PATTERN.matcher(desc).replaceAll(returnTypeLink);
         return desc.replace("${element.return-type}", returnTypeName);
      }
   }

   private static class ClassInfoComparator implements Comparator<ClassInfo<?>> {
      public ClassInfoComparator() {
      }

      public int compare(@Nullable ClassInfo<?> o1, @Nullable ClassInfo<?> o2) {
         if (o1 != null && o2 != null) {
            String name1 = o1.getDocName();
            if (name1 == null) {
               name1 = o1.getCodeName();
            }

            String name2 = o2.getDocName();
            if (name2 == null) {
               name2 = o2.getCodeName();
            }

            return name1.compareTo(name2);
         } else {
            assert false;

            throw new NullPointerException();
         }
      }
   }

   private static class EventComparator implements Comparator<BukkitSyntaxInfos.Event<?>> {
      public EventComparator() {
      }

      public int compare(@Nullable BukkitSyntaxInfos.Event<?> o1, @Nullable BukkitSyntaxInfos.Event<?> o2) {
         if (o1 != null && o2 != null) {
            if (o1.type().getAnnotation(NoDoc.class) != null) {
               return 1;
            } else {
               return o2.type().getAnnotation(NoDoc.class) != null ? -1 : o1.name().compareTo(o2.name());
            }
         } else {
            assert false;

            throw new NullPointerException();
         }
      }
   }

   private static class FunctionComparator implements Comparator<Function<?>> {
      public FunctionComparator() {
      }

      public int compare(@Nullable Function<?> o1, @Nullable Function<?> o2) {
         if (o1 != null && o2 != null) {
            return o1.getName().compareTo(o2.getName());
         } else {
            assert false;

            throw new NullPointerException();
         }
      }
   }
}
