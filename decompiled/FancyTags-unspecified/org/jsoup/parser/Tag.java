package org.jsoup.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import org.jsoup.helper.Validate;
import org.jsoup.internal.Normalizer;

public class Tag implements Cloneable {
   private static final Map<String, Tag> Tags = new HashMap<>();
   private String tagName;
   private final String normalName;
   private String namespace;
   private boolean isBlock = true;
   private boolean formatAsBlock = true;
   private boolean empty = false;
   private boolean selfClosing = false;
   private boolean preserveWhitespace = false;
   private boolean formList = false;
   private boolean formSubmit = false;
   private static final String[] blockTags = new String[]{
      "html",
      "head",
      "body",
      "frameset",
      "script",
      "noscript",
      "style",
      "meta",
      "link",
      "title",
      "frame",
      "noframes",
      "section",
      "nav",
      "aside",
      "hgroup",
      "header",
      "footer",
      "p",
      "h1",
      "h2",
      "h3",
      "h4",
      "h5",
      "h6",
      "ul",
      "ol",
      "pre",
      "div",
      "blockquote",
      "hr",
      "address",
      "figure",
      "figcaption",
      "form",
      "fieldset",
      "ins",
      "del",
      "dl",
      "dt",
      "dd",
      "li",
      "table",
      "caption",
      "thead",
      "tfoot",
      "tbody",
      "colgroup",
      "col",
      "tr",
      "th",
      "td",
      "video",
      "audio",
      "canvas",
      "details",
      "menu",
      "plaintext",
      "template",
      "article",
      "main",
      "svg",
      "math",
      "center",
      "template",
      "dir",
      "applet",
      "marquee",
      "listing"
   };
   private static final String[] inlineTags = new String[]{
      "object",
      "base",
      "font",
      "tt",
      "i",
      "b",
      "u",
      "big",
      "small",
      "em",
      "strong",
      "dfn",
      "code",
      "samp",
      "kbd",
      "var",
      "cite",
      "abbr",
      "time",
      "acronym",
      "mark",
      "ruby",
      "rt",
      "rp",
      "rtc",
      "a",
      "img",
      "br",
      "wbr",
      "map",
      "q",
      "sub",
      "sup",
      "bdo",
      "iframe",
      "embed",
      "span",
      "input",
      "select",
      "textarea",
      "label",
      "button",
      "optgroup",
      "option",
      "legend",
      "datalist",
      "keygen",
      "output",
      "progress",
      "meter",
      "area",
      "param",
      "source",
      "track",
      "summary",
      "command",
      "device",
      "area",
      "basefont",
      "bgsound",
      "menuitem",
      "param",
      "source",
      "track",
      "data",
      "bdi",
      "s",
      "strike",
      "nobr",
      "rb",
      "text",
      "mi",
      "mo",
      "msup",
      "mn",
      "mtext"
   };
   private static final String[] emptyTags = new String[]{
      "meta",
      "link",
      "base",
      "frame",
      "img",
      "br",
      "wbr",
      "embed",
      "hr",
      "input",
      "keygen",
      "col",
      "command",
      "device",
      "area",
      "basefont",
      "bgsound",
      "menuitem",
      "param",
      "source",
      "track"
   };
   private static final String[] formatAsInlineTags = new String[]{
      "title", "a", "p", "h1", "h2", "h3", "h4", "h5", "h6", "pre", "address", "li", "th", "td", "script", "style", "ins", "del", "s"
   };
   private static final String[] preserveWhitespaceTags = new String[]{"pre", "plaintext", "title", "textarea"};
   private static final String[] formListedTags = new String[]{"button", "fieldset", "input", "keygen", "object", "output", "select", "textarea"};
   private static final String[] formSubmitTags = new String[]{"input", "keygen", "object", "select", "textarea"};
   private static final Map<String, String[]> namespaces = new HashMap<>();

   private Tag(String tagName, String namespace) {
      this.tagName = tagName;
      this.normalName = Normalizer.lowerCase(tagName);
      this.namespace = namespace;
   }

   public String getName() {
      return this.tagName;
   }

   public String normalName() {
      return this.normalName;
   }

   public String namespace() {
      return this.namespace;
   }

   public static Tag valueOf(String tagName, String namespace, ParseSettings settings) {
      Validate.notEmpty(tagName);
      Validate.notNull(namespace);
      Tag tag = Tags.get(tagName);
      if (tag != null && tag.namespace.equals(namespace)) {
         return tag;
      } else {
         tagName = settings.normalizeTag(tagName);
         Validate.notEmpty(tagName);
         String normalName = Normalizer.lowerCase(tagName);
         tag = Tags.get(normalName);
         if (tag != null && tag.namespace.equals(namespace)) {
            if (settings.preserveTagCase() && !tagName.equals(normalName)) {
               tag = tag.clone();
               tag.tagName = tagName;
            }

            return tag;
         } else {
            tag = new Tag(tagName, namespace);
            tag.isBlock = false;
            return tag;
         }
      }
   }

   public static Tag valueOf(String tagName) {
      return valueOf(tagName, "http://www.w3.org/1999/xhtml", ParseSettings.preserveCase);
   }

   public static Tag valueOf(String tagName, ParseSettings settings) {
      return valueOf(tagName, "http://www.w3.org/1999/xhtml", settings);
   }

   public boolean isBlock() {
      return this.isBlock;
   }

   public boolean formatAsBlock() {
      return this.formatAsBlock;
   }

   public boolean isInline() {
      return !this.isBlock;
   }

   public boolean isEmpty() {
      return this.empty;
   }

   public boolean isSelfClosing() {
      return this.empty || this.selfClosing;
   }

   public boolean isKnownTag() {
      return Tags.containsKey(this.tagName);
   }

   public static boolean isKnownTag(String tagName) {
      return Tags.containsKey(tagName);
   }

   public boolean preserveWhitespace() {
      return this.preserveWhitespace;
   }

   public boolean isFormListed() {
      return this.formList;
   }

   public boolean isFormSubmittable() {
      return this.formSubmit;
   }

   Tag setSelfClosing() {
      this.selfClosing = true;
      return this;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Tag)) {
         return false;
      } else {
         Tag tag = (Tag)o;
         if (!this.tagName.equals(tag.tagName)) {
            return false;
         } else if (this.empty != tag.empty) {
            return false;
         } else if (this.formatAsBlock != tag.formatAsBlock) {
            return false;
         } else if (this.isBlock != tag.isBlock) {
            return false;
         } else if (this.preserveWhitespace != tag.preserveWhitespace) {
            return false;
         } else if (this.selfClosing != tag.selfClosing) {
            return false;
         } else {
            return this.formList != tag.formList ? false : this.formSubmit == tag.formSubmit;
         }
      }
   }

   @Override
   public int hashCode() {
      int result = this.tagName.hashCode();
      result = 31 * result + (this.isBlock ? 1 : 0);
      result = 31 * result + (this.formatAsBlock ? 1 : 0);
      result = 31 * result + (this.empty ? 1 : 0);
      result = 31 * result + (this.selfClosing ? 1 : 0);
      result = 31 * result + (this.preserveWhitespace ? 1 : 0);
      result = 31 * result + (this.formList ? 1 : 0);
      return 31 * result + (this.formSubmit ? 1 : 0);
   }

   @Override
   public String toString() {
      return this.tagName;
   }

   protected Tag clone() {
      try {
         return (Tag)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new RuntimeException(var2);
      }
   }

   private static void setupTags(String[] tagNames, Consumer<Tag> tagModifier) {
      for (String tagName : tagNames) {
         Tag tag = Tags.get(tagName);
         if (tag == null) {
            tag = new Tag(tagName, "http://www.w3.org/1999/xhtml");
            Tags.put(tag.tagName, tag);
         }

         tagModifier.accept(tag);
      }
   }

   static {
      namespaces.put("http://www.w3.org/1998/Math/MathML", new String[]{"math", "mi", "mo", "msup", "mn", "mtext"});
      namespaces.put("http://www.w3.org/2000/svg", new String[]{"svg", "text"});
      setupTags(blockTags, tag -> {
         tag.isBlock = true;
         tag.formatAsBlock = true;
      });
      setupTags(inlineTags, tag -> {
         tag.isBlock = false;
         tag.formatAsBlock = false;
      });
      setupTags(emptyTags, tag -> tag.empty = true);
      setupTags(formatAsInlineTags, tag -> tag.formatAsBlock = false);
      setupTags(preserveWhitespaceTags, tag -> tag.preserveWhitespace = true);
      setupTags(formListedTags, tag -> tag.formList = true);
      setupTags(formSubmitTags, tag -> tag.formSubmit = true);

      for (Entry<String, String[]> ns : namespaces.entrySet()) {
         setupTags(ns.getValue(), tag -> tag.namespace = ns.getKey());
      }
   }
}
