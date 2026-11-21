package ch.njol.skript.config;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.config.validate.EntryValidator;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.util.NonNullPair;
import ch.njol.util.NullableChecker;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.CheckedIterator;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.invoke.StringConcatFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SectionNode extends Node implements Iterable<Node> {
   private final ArrayList<Node> nodes = new ArrayList<>();
   @Nullable
   private NodeMap nodeMap = null;
   private static final Pattern fullLinePattern = Pattern.compile("([^#]|##)*#-#(\\s.*)?");

   public SectionNode(String key, String comment, SectionNode parent, int lineNum) {
      super(key, comment, parent, lineNum);
   }

   SectionNode(Config c) {
      super(c);
   }

   private NodeMap getNodeMap() {
      NodeMap nodeMap = this.nodeMap;
      if (nodeMap == null) {
         nodeMap = this.nodeMap = new NodeMap();

         for (Node node : this.nodes) {
            assert node != null;

            nodeMap.put(node);
         }
      }

      return nodeMap;
   }

   public int size() {
      return this.nodes.size();
   }

   public void add(Node n) {
      n.remove();
      this.nodes.add(n);
      n.parent = this;
      n.config = this.config;
      this.getNodeMap().put(n);
   }

   public void add(int index, @NotNull Node node) {
      Preconditions.checkArgument(index >= 0 && index <= this.size(), "index out of bounds: %s", index);
      node.remove();
      this.nodes.add(index, node);
      node.parent = this;
      node.config = this.config;
      this.getNodeMap().put(node);
   }

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public void insert(Node node, int index) {
      this.add(index, node);
   }

   public void remove(Node n) {
      this.nodes.remove(n);
      n.parent = null;
      this.getNodeMap().remove(n);
   }

   @Nullable
   public Node remove(String key) {
      Node n = this.getNodeMap().remove(key);
      if (n == null) {
         return null;
      } else {
         this.nodes.remove(n);
         n.parent = null;
         return n;
      }
   }

   @Nullable
   Node getAt(int index) {
      Preconditions.checkArgument(index >= 0 && index < this.size(), "index out of bounds: %s", index);
      return this.nodes.get(index);
   }

   @NotNull
   @Override
   public Iterator<Node> iterator() {
      return new CheckedIterator<>(this.fullIterator(), n -> !n.isVoid());
   }

   @NotNull
   public Iterator<Node> fullIterator() {
      return new CheckedIterator<Node>(this.nodes.iterator(), Objects::nonNull) {
         @Override
         public boolean hasNext() {
            boolean hasNext = super.hasNext();
            if (!hasNext) {
               SkriptLogger.setNode(SectionNode.this);
            }

            return hasNext;
         }

         @Nullable
         public Node next() {
            Node node = (Node)super.next();
            SkriptLogger.setNode(node);
            return node;
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      };
   }

   @Nullable
   @Override
   public Node get(@Nullable String key) {
      return this.getNodeMap().get(key);
   }

   @Nullable
   @Override
   public String getValue(String key) {
      Node n = this.get(key);
      return n instanceof EntryNode ? ((EntryNode)n).getValue() : null;
   }

   public String get(String name, String def) {
      Node n = this.get(name);
      return n != null && n instanceof EntryNode ? ((EntryNode)n).getValue() : def;
   }

   public void set(String key, String value) {
      Node n = this.get(key);
      if (n instanceof EntryNode) {
         ((EntryNode)n).setValue(value);
      } else {
         this.add(new EntryNode(key, value, this));
      }
   }

   public void set(String key, @Nullable Node node) {
      if (node == null) {
         this.remove(key);
      } else {
         Node n = this.get(key);
         if (n != null) {
            for (int i = 0; i < this.nodes.size(); i++) {
               if (this.nodes.get(i) == n) {
                  this.nodes.set(i, node);
                  this.remove(n);
                  this.getNodeMap().put(node);
                  node.parent = this;
                  node.config = this.config;
                  return;
               }
            }

            assert false;
         }

         this.add(node);
      }
   }

   void renamed(Node node, @Nullable String oldKey) {
      if (!this.nodes.contains(node)) {
         throw new IllegalArgumentException();
      } else {
         this.getNodeMap().remove(oldKey);
         this.getNodeMap().put(node);
      }
   }

   public boolean isEmpty() {
      for (Node node : this.nodes) {
         if (!node.isVoid()) {
            return false;
         }
      }

      return true;
   }

   static SectionNode load(Config c, ConfigReader r) throws IOException {
      return new SectionNode(c).load_i(r);
   }

   static SectionNode load(String name, String comment, SectionNode parent, ConfigReader r) throws IOException {
      parent.config.level++;
      SectionNode node = new SectionNode(name, comment, parent, r.getLineNum()).load_i(r);
      SkriptLogger.setNode(parent);
      parent.config.level--;
      return node;
   }

   private static String readableWhitespace(String s) {
      if (s.matches(" +")) {
         return s.length() + " space" + (s.length() == 1 ? "" : "s");
      } else {
         return s.matches("\t+")
            ? s.length() + " tab" + (s.length() == 1 ? "" : "s")
            : "'" + s.replace("\t", "->").replace(' ', '_').replaceAll("\\s", "?") + "' [-> = tab, _ = space, ? = other whitespace]";
      }
   }

   private SectionNode load_i(ConfigReader r) throws IOException {
      boolean indentationSet = false;
      AtomicBoolean inBlockComment = new AtomicBoolean(false);
      int blockCommentStartLine = -1;

      String fullLine;
      while ((fullLine = r.readLine()) != null) {
         SkriptLogger.setNode(this);
         if (!inBlockComment.get()) {
            blockCommentStartLine = this.getLine();
         }

         NonNullPair<String, String> line = Node.splitLine(fullLine, inBlockComment);
         String value = line.getFirst();
         String comment = line.getSecond();
         SectionNode parent = this.parent;
         if (!indentationSet && parent != null && parent.parent == null && !value.isEmpty() && !value.matches("\\s*") && !value.matches("\\S.*")) {
            String s = value.replaceFirst("\\S.*$", "");

            assert !s.isEmpty() : fullLine;

            if (!s.matches(" +") && !s.matches("\t+")) {
               this.nodes.add(new InvalidNode(value, comment, this, r.getLineNum()));
               Skript.error("indentation error: indent must only consist of either spaces or tabs, but not mixed (found " + readableWhitespace(s) + ")");
               continue;
            }

            this.config.setIndentation(s);
            indentationSet = true;
         }

         if (!value.matches("\\s*") && !value.matches("^(" + this.config.getIndentation() + "){" + this.config.level + "}\\S.*")) {
            if (!value.matches("^(" + this.config.getIndentation() + "){" + this.config.level + "}\\s.*")
               && value.matches("^(" + this.config.getIndentation() + ")*\\S.*")) {
               if (parent != null && !this.config.allowEmptySections && this.isEmpty()) {
                  Skript.warning(
                     "Empty configuration section! You might want to indent one or more of the subsequent lines to make them belong to this section or remove the colon at the end of the line if you don't want this line to start a section."
                  );
               }

               r.reset();
               return this;
            }

            this.nodes.add(new InvalidNode(value, comment, this, r.getLineNum()));
            String sx = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value.replaceFirst("\\S.*$", ""));
            Skript.error(
               "indentation error: expected "
                  + this.config.level * this.config.getIndentation().length()
                  + " "
                  + this.config.getIndentationName()
                  + (this.config.level * this.config.getIndentation().length() == 1 ? "" : "s")
                  + ", but found "
                  + readableWhitespace(sx)
            );
         } else {
            value = value.trim();
            if (value.isEmpty()) {
               this.nodes.add(new VoidNode(value, comment, this, r.getLineNum()));
            } else {
               if (value.endsWith(":")
                  && (
                     this.config.simple
                        || value.indexOf(this.config.separator) == -1
                        || this.config.separator.endsWith(":") && value.indexOf(this.config.separator) == value.length() - this.config.separator.length()
                  )) {
                  boolean matches = false;

                  try {
                     matches = fullLine.contains("#") && fullLinePattern.matcher(fullLine).matches();
                  } catch (StackOverflowError var12) {
                     Node.handleNodeStackOverflow(var12, fullLine);
                  }

                  if (!matches) {
                     this.nodes
                        .add(
                           load(
                              StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(value.substring(0, value.length() - 1)),
                              comment,
                              this,
                              r
                           )
                        );
                     continue;
                  }
               }

               if (this.config.simple) {
                  this.nodes.add(new SimpleNode(value, comment, r.getLineNum(), this));
               } else {
                  this.nodes.add(this.getEntry(value, comment, r.getLineNum(), this.config.separator));
               }
            }
         }
      }

      if (inBlockComment.get()) {
         Skript.error("A block comment (###) was opened on line " + blockCommentStartLine + " but never closed.");
      }

      SkriptLogger.setNode(this.parent);
      return this;
   }

   private Node getEntry(String keyAndValue, String comment, int lineNum, String separator) {
      int x = keyAndValue.indexOf(separator);
      if (x == -1) {
         InvalidNode in = new InvalidNode(keyAndValue, comment, this, lineNum);
         EntryValidator.notAnEntryError(in, separator);
         SkriptLogger.setNode(this);
         return in;
      } else {
         String key = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(keyAndValue.substring(0, x).trim());
         String value = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(keyAndValue.substring(x + separator.length()).trim());
         return new EntryNode(key, value, comment, this, lineNum);
      }
   }

   public void convertToEntries(int levels) {
      this.convertToEntries(levels, this.config.separator);
   }

   public void convertToEntries(int levels, String separator) {
      if (levels < -1) {
         throw new IllegalArgumentException("levels must be >= -1");
      } else if (!this.config.simple) {
         throw new SkriptAPIException("config is not simple: " + this.config);
      } else {
         for (int i = 0; i < this.nodes.size(); i++) {
            Node n = this.nodes.get(i);
            if (levels != 0 && n instanceof SectionNode) {
               ((SectionNode)n).convertToEntries(levels == -1 ? -1 : levels - 1, separator);
            }

            if (n instanceof SimpleNode) {
               String key = n.key;
               if (key != null) {
                  this.nodes.set(i, this.getEntry(key, n.comment, n.lineNum, separator));
               } else {
                  assert false;
               }
            }
         }
      }
   }

   @Override
   public void save(PrintWriter w) {
      if (this.parent != null) {
         super.save(w);
      }

      for (Node node : this.nodes) {
         node.save(w);
      }
   }

   @Override
   String save_i() {
      assert this.key != null;

      return this.key + ":";
   }

   public boolean validate(SectionValidator validator) {
      return validator.validate(this);
   }

   Map<String, String> toMap(String prefix, String separator) {
      Map<String, String> r = new HashMap<>();

      for (Node n : this) {
         if (n instanceof EntryNode) {
            r.put(prefix + n.getKey(), ((EntryNode)n).getValue());
         } else {
            r.putAll(((SectionNode)n).toMap(prefix + n.getKey() + separator, separator));
         }
      }

      return r;
   }

   public boolean isValid() {
      for (Node node : this.nodes) {
         if (node instanceof SectionNode sectionNode && !sectionNode.isValid() || node instanceof InvalidNode) {
            return false;
         }
      }

      return true;
   }

   public boolean setValues(SectionNode other, String... excluded) {
      return this.modify(other, false, excluded);
   }

   public boolean compareValues(SectionNode other, String... excluded) {
      return !this.modify(other, true, excluded);
   }

   private boolean modify(SectionNode other, boolean compareValues, String... excluded) {
      boolean different = false;

      for (Node node : this) {
         if (!CollectionUtils.containsIgnoreCase(excluded, node.key)) {
            Node otherNode = other.get(node.key);
            if (otherNode != null) {
               if (node instanceof SectionNode) {
                  if (otherNode instanceof SectionNode) {
                     different |= ((SectionNode)node).modify((SectionNode)otherNode, compareValues);
                  } else {
                     different = true;
                     if (compareValues) {
                        break;
                     }
                  }
               } else if (node instanceof EntryNode) {
                  if (otherNode instanceof EntryNode) {
                     String ourValue = ((EntryNode)node).getValue();
                     String theirValue = ((EntryNode)otherNode).getValue();
                     if (compareValues) {
                        if (!ourValue.equals(theirValue)) {
                           different = true;
                           break;
                        }
                     } else {
                        ((EntryNode)node).setValue(theirValue);
                     }
                  } else {
                     different = true;
                     if (compareValues) {
                        break;
                     }
                  }
               }
            } else {
               different = true;
               if (compareValues) {
                  break;
               }
            }
         }
      }

      if (!different) {
         for (Node otherNode : other) {
            if (this.get(otherNode.key) == null) {
               different = true;
               break;
            }
         }
      }

      return different;
   }

   @Nullable
   @Override
   public Node getNodeAt(@NotNull String... keys) {
      Node node = this;

      for (String s : keys) {
         if (!(node instanceof SectionNode sectionNode)) {
            return null;
         }

         node = sectionNode.get(s);
      }

      return node;
   }
}
