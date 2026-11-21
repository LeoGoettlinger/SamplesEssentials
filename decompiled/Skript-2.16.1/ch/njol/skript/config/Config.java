package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.util.common.AnyNamed;
import ch.njol.skript.log.SkriptLogger;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.skriptlang.skript.util.Validated;

public class Config implements Comparable<Config>, Validated, NodeNavigator, AnyNamed {
   private String indentation = "\t";
   private String indentationName = "tab";
   private final SectionNode main;
   final String defaultSeparator;
   String separator;
   boolean simple;
   int level = 0;
   int errors = 0;
   final boolean allowEmptySections;
   String fileName;
   @Nullable
   Path file = null;
   private final Validated validator = Validated.validator();

   public Config(InputStream source, String fileName, @Nullable File file, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
      InputStream var7 = source;

      label66: {
         try {
            this.fileName = fileName;
            if (file != null) {
               this.file = file.toPath();
            }

            this.simple = simple;
            this.allowEmptySections = allowEmptySections;
            this.defaultSeparator = defaultSeparator;
            this.separator = defaultSeparator;
            if (source.available() == 0) {
               this.main = new SectionNode(this);
               Skript.warning("'" + this.getFileName() + "' is empty");
               break label66;
            }

            if (Skript.logVeryHigh()) {
               Skript.info("loading '" + fileName + "'");
            }

            try (ConfigReader reader = new ConfigReader(source)) {
               this.main = SectionNode.load(this, reader);
            }
         } catch (Throwable var14) {
            if (source != null) {
               try {
                  var7.close();
               } catch (Throwable var11) {
                  var14.addSuppressed(var11);
               }
            }

            throw var14;
         }

         if (source != null) {
            source.close();
         }

         return;
      }

      if (source != null) {
         source.close();
      }
   }

   public Config(InputStream source, String fileName, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
      this(source, fileName, null, simple, allowEmptySections, defaultSeparator);
   }

   public Config(File file, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
      this(Files.newInputStream(file.toPath()), file.getName(), simple, allowEmptySections, defaultSeparator);
      this.file = file.toPath();
   }

   public Config(@NotNull Path file, boolean simple, boolean allowEmptySections, String defaultSeparator) throws IOException {
      this(Channels.newInputStream(FileChannel.open(file)), file.getFileName() + "", simple, allowEmptySections, defaultSeparator);
      this.file = file;
   }

   public void load(Object object) {
      this.load(object.getClass(), object, "");
   }

   @Internal
   public Config(String fileName, @Nullable File file) {
      this.fileName = fileName;
      if (file != null) {
         this.file = file.toPath();
      }

      this.simple = false;
      this.allowEmptySections = false;
      this.separator = this.defaultSeparator = "";
      this.main = new SectionNode(this);
      SkriptLogger.setNode(null);
   }

   public void load(Class<?> clazz) {
      this.load(clazz, null, "");
   }

   private void load(Class<?> clazz, @Nullable Object object, String path) {
      for (Field field : clazz.getDeclaredFields()) {
         field.setAccessible(true);
         if (object != null || Modifier.isStatic(field.getModifiers())) {
            try {
               if (OptionSection.class.isAssignableFrom(field.getType())) {
                  OptionSection section = (OptionSection)field.get(object);
                  Class<?> pc = section.getClass();
                  this.load(pc, section, path + section.key + ".");
               } else if (Option.class.isAssignableFrom(field.getType())) {
                  ((Option)field.get(object)).set(this, path);
               }
            } catch (IllegalAccessException | IllegalArgumentException var10) {
               assert false;
            }
         }
      }
   }

   void setIndentation(String indent) {
      assert indent != null && !indent.isEmpty() : indent;

      this.indentation = indent;
      this.indentationName = indent.charAt(0) == ' ' ? "space" : "tab";
   }

   String getIndentation() {
      return this.indentation;
   }

   String getIndentationName() {
      return this.indentationName;
   }

   public SectionNode getMainNode() {
      return this.main;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void save(File file) throws IOException {
      this.separator = this.defaultSeparator;

      try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
         this.main.save(writer);
         writer.flush();
      }
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public boolean setValues(Config other) {
      return this.getMainNode().setValues(other.getMainNode());
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public boolean setValues(Config other, String... excluded) {
      return this.getMainNode().setValues(other.getMainNode(), excluded);
   }

   public boolean updateNodes(@NotNull Config newer) {
      Skript.debug("Updating config %s", newer.getFileName());
      Set<Node> newNodes = discoverNodes(newer.getMainNode());
      Set<Node> oldNodes = discoverNodes(this.getMainNode());
      newNodes.removeAll(oldNodes);
      Set<Node> nodesToUpdate = new LinkedHashSet<>(newNodes);
      if (nodesToUpdate.isEmpty()) {
         return false;
      } else {
         for (Node node : nodesToUpdate) {
            if (this.get(node.getPathSteps()) == null) {
               Skript.debug("Updating node %s", node);
               SectionNode newParent = node.getParent();
               Preconditions.checkNotNull(newParent);
               SectionNode parent = this.getNode(newParent.getPathSteps());
               Preconditions.checkNotNull(parent);
               int index = node.getIndex();
               if (index >= parent.size()) {
                  Skript.debug("Adding node %s to %s (size mismatch)", node, parent);
                  parent.add(node);
               } else {
                  Node existing = parent.getAt(index);
                  if (existing != null) {
                     Skript.debug("Adding node %s to %s at index %s", node, parent, index);
                     parent.add(index, node);
                  } else {
                     Skript.debug("Adding node %s to %s", node, parent);
                     parent.add(node);
                  }
               }
            }
         }

         return true;
      }
   }

   @Contract(
      pure = true
   )
   @NotNull
   static Set<Node> discoverNodes(@NotNull SectionNode node) {
      Set<Node> nodes = new LinkedHashSet<>();
      Iterator<Node> iterator = node.fullIterator();

      while (iterator.hasNext()) {
         Node child = iterator.next();
         if (child instanceof SectionNode sectionChild) {
            nodes.add(child);
            nodes.addAll(discoverNodes(sectionChild));
         } else if (child instanceof EntryNode || child instanceof VoidNode) {
            nodes.add(child);
         }
      }

      return nodes;
   }

   private SectionNode getNode(String... path) {
      SectionNode node = this.getMainNode();

      for (String key : path) {
         if (!(node.get(key) instanceof SectionNode sectionNode)) {
            return node;
         }

         node = sectionNode;
      }

      return node;
   }

   public boolean compareValues(Config other, String... excluded) {
      return this.getMainNode().compareValues(other.getMainNode(), excluded);
   }

   @Nullable
   public String getByPath(@NotNull String path) {
      return this.get(path.split("\\."));
   }

   @Nullable
   public String get(String... path) {
      SectionNode section = this.main;

      for (int i = 0; i < path.length; i++) {
         Node node = section.get(path[i]);
         if (node == null) {
            return null;
         }

         if (!(node instanceof SectionNode sectionNode)) {
            if (node instanceof EntryNode entryNode && i == path.length - 1) {
               return entryNode.getValue();
            }

            return null;
         }

         if (i == path.length - 1) {
            return null;
         }

         section = sectionNode;
      }

      return null;
   }

   public Map<String, String> toMap(String separator) {
      return this.main.toMap("", separator);
   }

   public boolean validate(SectionValidator validator) {
      return validator.validate(this.getMainNode());
   }

   public boolean isEmpty() {
      return this.main.isEmpty();
   }

   @Nullable
   public File getFile() {
      if (this.file == null) {
         return null;
      } else {
         try {
            return this.file.toFile();
         } catch (Exception var2) {
            return null;
         }
      }
   }

   @Nullable
   public Path getPath() {
      return this.file;
   }

   public String getSeparator() {
      return this.separator;
   }

   public String getSaveSeparator() {
      return this.separator.equals(":") ? ": " : " " + this.separator + " ";
   }

   public int compareTo(@Nullable Config other) {
      return other == null ? 0 : this.fileName.compareTo(other.fileName);
   }

   @Override
   public void invalidate() {
      this.validator.invalidate();
   }

   @Override
   public boolean valid() {
      return this.validator.valid();
   }

   @NotNull
   @Override
   public Node getCurrentNode() {
      return this.main;
   }

   @Nullable
   @Override
   public Node getNodeAt(@NotNull String... steps) {
      return this.main.getNodeAt(steps);
   }

   @NotNull
   @Override
   public Iterator<Node> iterator() {
      return this.main.iterator();
   }

   @Nullable
   @Override
   public Node get(String step) {
      return this.main.get(step);
   }

   @Override
   public String name() {
      String name = this.getFileName();
      if (name == null) {
         return null;
      } else {
         if (name.contains(File.separator)) {
            name = name.substring(name.lastIndexOf(File.separator) + 1);
         }

         return name.contains(".") ? name.substring(0, name.lastIndexOf(46)) : name;
      }
   }
}
