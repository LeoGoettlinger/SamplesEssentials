package su.nightexpress.nightcore.util.text;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.core.CoreConfig;
import su.nightexpress.nightcore.util.LegacyColors;
import su.nightexpress.nightcore.util.bridge.wrapper.NightComponent;
import su.nightexpress.nightcore.util.text.night.ParserUtils;
import su.nightexpress.nightcore.util.text.tag.TagPool;
import su.nightexpress.nightcore.util.text.tag.TagUtils;
import su.nightexpress.nightcore.util.text.tag.Tags;
import su.nightexpress.nightcore.util.text.tag.api.ContentTag;
import su.nightexpress.nightcore.util.text.tag.api.PlaceholderTag;
import su.nightexpress.nightcore.util.text.tag.api.Tag;
import su.nightexpress.nightcore.util.text.tag.decorator.Decorator;
import su.nightexpress.nightcore.util.text.tag.impl.ResetTag;
import su.nightexpress.nightcore.util.text.tag.impl.TranslationTag;

@Deprecated
public class TextRoot {
   public static final String ROOT_NAME = "root";
   private final TagPool tagPool;
   private String string;
   private TextGroup rootGroup;
   private TextGroup currentGroup;
   private TextNode currentNode;
   private NightComponent component;

   public TextRoot(@NonNull String string, @NonNull TagPool tagPool) {
      this.tagPool = tagPool;
      this.setString(string);
   }

   public void setString(@NonNull String string) {
      if (CoreConfig.LEGACY_COLOR_SUPPORT.get()) {
         string = LegacyColors.plainColors(string);
         string = ParserUtils.wrapHexCodesAsTags(string);
         string = TagUtils.replaceLegacyColors(string);
      }

      this.string = string;
      this.component = null;
   }

   @NonNull
   public TextRoot copy() {
      return new TextRoot(this.string, this.tagPool);
   }

   @NonNull
   public TextRoot compile() {
      this.parseIfAbsent();
      return this;
   }

   @NonNull
   public TextRoot recompile() {
      this.parse();
      return this;
   }

   @NonNull
   public String toLegacy() {
      this.parseIfAbsent();
      return !this.string.isBlank() && !this.component.isEmpty() ? this.component.toLegacy() : "";
   }

   @NonNull
   public String toJson() {
      return this.parseIfAbsent().toJson();
   }

   @Deprecated
   @NonNull
   public NightComponent toComponent() {
      return this.parseIfAbsent();
   }

   @Deprecated
   @NonNull
   public TextRoot replace(@NonNull String what, @NonNull Object object) {
      return this.replace(str -> str.replace(what, String.valueOf(object)));
   }

   @NonNull
   public TextRoot replace(@NonNull UnaryOperator<String> operator) {
      this.setString(operator.apply(this.string));
      return this;
   }

   public void send(CommandSender... senders) {
      this.parseIfAbsent();

      for (CommandSender sender : senders) {
         this.component.send(sender);
      }
   }

   @NonNull
   public TextGroup toParentGroup() {
      this.currentGroup = this.currentGroup.getParent();
      if (this.currentGroup == null) {
         this.currentGroup = this.rootGroup;
      }

      return this.currentGroup;
   }

   @NonNull
   public TextGroup toParentGroup(@NonNull String name) {
      TextGroup lastGroup = this.currentGroup;

      while (this.currentGroup != null && !this.currentGroup.getName().equalsIgnoreCase(name)) {
         this.currentGroup = this.currentGroup.getParent();
      }

      if (this.currentGroup == null && lastGroup != null) {
         this.currentGroup = lastGroup;
         return this.currentGroup;
      } else {
         if (this.currentGroup != null) {
            this.currentGroup = this.currentGroup.getParent();
         }

         if (this.currentGroup == null) {
            this.currentGroup = this.rootGroup;
         }

         return this.currentGroup;
      }
   }

   @NonNull
   public TextNode currentNode() {
      if (this.currentNode == null) {
         this.currentNode = this.currentGroup.createNode();
      }

      return this.currentNode;
   }

   public NightComponent parseIfAbsent() {
      return this.component == null ? this.parse() : this.component;
   }

   @NonNull
   public NightComponent parse() {
      this.rootGroup = new TextGroup("root");
      this.currentGroup = this.rootGroup;
      this.doJob(TextRoot.Mode.PARSE, letter -> this.currentNode().append(letter));
      this.component = this.rootGroup.toComponent();
      this.rootGroup = null;
      this.currentGroup = null;
      this.currentNode = null;
      return this.component;
   }

   @NonNull
   public String strip() {
      StringBuilder builder = new StringBuilder();
      this.doJob(TextRoot.Mode.STRIP, builder::append);
      return builder.toString();
   }

   private void doJob(@NonNull TextRoot.Mode mode, @NonNull Consumer<Character> consumer) {
      int length = this.string.length();

      for (int index = 0; index < length; index++) {
         char letter = this.string.charAt(index);
         if (letter == '\\' && index != length - 1 && this.string.charAt(index + 1) == '<') {
            index++;
            consumer.accept('<');
         } else {
            if (letter == '<' && index != length - 1) {
               int indexEnd = ParserUtils.findUnescapedUnquotedChar(this.string, '>', index);
               if (indexEnd != -1) {
                  char next = this.string.charAt(index + 1);
                  if (next != '>') {
                     boolean closeTag = false;
                     if (next == '/') {
                        closeTag = true;
                        index++;
                     }

                     String bracketsContent = this.string.substring(index + 1, indexEnd);
                     String tagName = bracketsContent;
                     String tagContent = null;
                     int semicolonIndex = bracketsContent.indexOf(58);
                     if (semicolonIndex >= 0) {
                        tagName = bracketsContent.substring(0, semicolonIndex);
                        tagContent = bracketsContent.substring(semicolonIndex + 1);
                     } else if (bracketsContent.startsWith("#")) {
                        tagName = "#";
                        tagContent = bracketsContent;
                     }

                     Tag tag = Tags.getTag(tagName);
                     if (tag != null) {
                        if (mode == TextRoot.Mode.PARSE) {
                           if (this.tagPool.isGoodTag(tag)) {
                              this.proceedTag(tag, closeTag, tagContent);
                           }

                           index = indexEnd;
                           continue;
                        }

                        if (!this.tagPool.isGoodTag(tag)) {
                           index = indexEnd;
                           continue;
                        }
                     }

                     if (closeTag) {
                        index--;
                     }
                  }
               }
            }

            consumer.accept(letter);
         }
      }
   }

   private void proceedTag(@NonNull Tag tag, boolean closeTag, @Nullable String tagContent) {
      if (!closeTag || tag.isCloseable()) {
         if (tag instanceof PlaceholderTag placeholderTag) {
            this.currentNode().append(placeholderTag.getValue());
         } else if (tag instanceof TranslationTag) {
            if (tagContent != null) {
               if (tagContent.endsWith("/")) {
                  tagContent = tagContent.substring(0, tagContent.length() - 1);
               }

               int index = ParserUtils.findUnescapedUnquotedChar(tagContent, ':', 0);
               String key = index < 0 ? tagContent : tagContent.substring(0, index);
               String fallback = index >= 0 && index < tagContent.length() ? ParserUtils.unquoted(tagContent.substring(index + 1)) : null;
               this.currentGroup.createLangNode(ParserUtils.unquoted(key), fallback);
               this.currentNode = null;
            }
         } else {
            this.currentNode = null;
            if (closeTag) {
               this.toParentGroup(tag.getName());
            } else if (tag instanceof ResetTag) {
               this.currentGroup = this.rootGroup;
            } else {
               this.currentGroup = this.currentGroup.createChildren(tag.getName());
               Decorator decorator = null;
               if (tag instanceof ContentTag contentTag && tagContent != null) {
                  decorator = contentTag.parse(tagContent);
               } else if (tag instanceof Decorator tagDecorator) {
                  decorator = tagDecorator;
               }

               if (decorator != null) {
                  this.currentGroup.addDecorator(decorator);
               }
            }
         }
      }
   }

   public String getString() {
      return this.string;
   }

   public TextGroup getRootGroup() {
      return this.rootGroup;
   }

   public NightComponent getComponent() {
      return this.component;
   }

   public static enum Mode {
      PARSE,
      STRIP;
   }
}
