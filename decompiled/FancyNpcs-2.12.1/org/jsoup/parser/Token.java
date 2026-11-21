package org.jsoup.parser;

import java.util.HashMap;
import java.util.Map;
import org.jsoup.helper.Validate;
import org.jsoup.internal.Normalizer;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Range;
import org.jspecify.annotations.Nullable;

abstract class Token {
   final Token.TokenType type;
   static final int Unset = -1;
   private int startPos;
   private int endPos = -1;

   private Token(Token.TokenType type) {
      this.type = type;
   }

   String tokenType() {
      return this.getClass().getSimpleName();
   }

   Token reset() {
      this.startPos = -1;
      this.endPos = -1;
      return this;
   }

   int startPos() {
      return this.startPos;
   }

   void startPos(int pos) {
      this.startPos = pos;
   }

   int endPos() {
      return this.endPos;
   }

   void endPos(int pos) {
      this.endPos = pos;
   }

   static void reset(StringBuilder sb) {
      if (sb != null) {
         sb.delete(0, sb.length());
      }
   }

   final boolean isDoctype() {
      return this.type == Token.TokenType.Doctype;
   }

   final Token.Doctype asDoctype() {
      return (Token.Doctype)this;
   }

   final boolean isStartTag() {
      return this.type == Token.TokenType.StartTag;
   }

   final Token.StartTag asStartTag() {
      return (Token.StartTag)this;
   }

   final boolean isEndTag() {
      return this.type == Token.TokenType.EndTag;
   }

   final Token.EndTag asEndTag() {
      return (Token.EndTag)this;
   }

   final boolean isComment() {
      return this.type == Token.TokenType.Comment;
   }

   final Token.Comment asComment() {
      return (Token.Comment)this;
   }

   final boolean isCharacter() {
      return this.type == Token.TokenType.Character;
   }

   final boolean isCData() {
      return this instanceof Token.CData;
   }

   final Token.Character asCharacter() {
      return (Token.Character)this;
   }

   final boolean isEOF() {
      return this.type == Token.TokenType.EOF;
   }

   static final class CData extends Token.Character {
      CData(String data) {
         this.data(data);
      }

      @Override
      public String toString() {
         return "<![CDATA[" + this.getData() + "]]>";
      }
   }

   static class Character extends Token implements Cloneable {
      private String data;

      Character() {
         super(Token.TokenType.Character);
      }

      @Override
      Token reset() {
         super.reset();
         this.data = null;
         return this;
      }

      Token.Character data(String data) {
         this.data = data;
         return this;
      }

      String getData() {
         return this.data;
      }

      @Override
      public String toString() {
         return this.getData();
      }

      protected Token.Character clone() {
         try {
            return (Token.Character)super.clone();
         } catch (CloneNotSupportedException var2) {
            throw new RuntimeException(var2);
         }
      }
   }

   static final class Comment extends Token {
      private final StringBuilder data = new StringBuilder();
      private String dataS;
      boolean bogus = false;

      @Override
      Token reset() {
         super.reset();
         reset(this.data);
         this.dataS = null;
         this.bogus = false;
         return this;
      }

      Comment() {
         super(Token.TokenType.Comment);
      }

      String getData() {
         return this.dataS != null ? this.dataS : this.data.toString();
      }

      Token.Comment append(String append) {
         this.ensureData();
         if (this.data.length() == 0) {
            this.dataS = append;
         } else {
            this.data.append(append);
         }

         return this;
      }

      Token.Comment append(char append) {
         this.ensureData();
         this.data.append(append);
         return this;
      }

      private void ensureData() {
         if (this.dataS != null) {
            this.data.append(this.dataS);
            this.dataS = null;
         }
      }

      @Override
      public String toString() {
         return "<!--" + this.getData() + "-->";
      }
   }

   static final class Doctype extends Token {
      final StringBuilder name = new StringBuilder();
      String pubSysKey = null;
      final StringBuilder publicIdentifier = new StringBuilder();
      final StringBuilder systemIdentifier = new StringBuilder();
      boolean forceQuirks = false;

      Doctype() {
         super(Token.TokenType.Doctype);
      }

      @Override
      Token reset() {
         super.reset();
         reset(this.name);
         this.pubSysKey = null;
         reset(this.publicIdentifier);
         reset(this.systemIdentifier);
         this.forceQuirks = false;
         return this;
      }

      String getName() {
         return this.name.toString();
      }

      String getPubSysKey() {
         return this.pubSysKey;
      }

      String getPublicIdentifier() {
         return this.publicIdentifier.toString();
      }

      public String getSystemIdentifier() {
         return this.systemIdentifier.toString();
      }

      public boolean isForceQuirks() {
         return this.forceQuirks;
      }

      @Override
      public String toString() {
         return "<!doctype " + this.getName() + ">";
      }
   }

   static final class EOF extends Token {
      EOF() {
         super(Token.TokenType.EOF);
      }

      @Override
      Token reset() {
         super.reset();
         return this;
      }

      @Override
      public String toString() {
         return "";
      }
   }

   static final class EndTag extends Token.Tag {
      EndTag(TreeBuilder treeBuilder) {
         super(Token.TokenType.EndTag, treeBuilder);
      }

      @Override
      public String toString() {
         return "</" + this.toStringName() + ">";
      }
   }

   static final class StartTag extends Token.Tag {
      StartTag(TreeBuilder treeBuilder) {
         super(Token.TokenType.StartTag, treeBuilder);
      }

      @Override
      Token.Tag reset() {
         super.reset();
         this.attributes = null;
         return this;
      }

      Token.StartTag nameAttr(String name, Attributes attributes) {
         this.tagName = name;
         this.attributes = attributes;
         this.normalName = ParseSettings.normalName(this.tagName);
         return this;
      }

      @Override
      public String toString() {
         String closer = this.isSelfClosing() ? "/>" : ">";
         return this.hasAttributes() && this.attributes.size() > 0
            ? "<" + this.toStringName() + " " + this.attributes.toString() + closer
            : "<" + this.toStringName() + closer;
      }
   }

   abstract static class Tag extends Token {
      @Nullable
      protected String tagName;
      @Nullable
      protected String normalName;
      boolean selfClosing = false;
      @Nullable
      Attributes attributes;
      @Nullable
      private String attrName;
      private final StringBuilder attrNameSb = new StringBuilder();
      private boolean hasAttrName = false;
      @Nullable
      private String attrValue;
      private final StringBuilder attrValueSb = new StringBuilder();
      private boolean hasAttrValue = false;
      private boolean hasEmptyAttrValue = false;
      final TreeBuilder treeBuilder;
      final boolean trackSource;
      int attrNameStart;
      int attrNameEnd;
      int attrValStart;
      int attrValEnd;
      private static final int MaxAttributes = 512;

      Tag(Token.TokenType type, TreeBuilder treeBuilder) {
         super(type);
         this.treeBuilder = treeBuilder;
         this.trackSource = treeBuilder.trackSourceRange;
      }

      Token.Tag reset() {
         super.reset();
         this.tagName = null;
         this.normalName = null;
         this.selfClosing = false;
         this.attributes = null;
         this.resetPendingAttr();
         return this;
      }

      private void resetPendingAttr() {
         reset(this.attrNameSb);
         this.attrName = null;
         this.hasAttrName = false;
         reset(this.attrValueSb);
         this.attrValue = null;
         this.hasEmptyAttrValue = false;
         this.hasAttrValue = false;
         if (this.trackSource) {
            this.attrNameStart = this.attrNameEnd = this.attrValStart = this.attrValEnd = -1;
         }
      }

      final void newAttribute() {
         if (this.attributes == null) {
            this.attributes = new Attributes();
         }

         if (this.hasAttrName && this.attributes.size() < 512) {
            String name = this.attrNameSb.length() > 0 ? this.attrNameSb.toString() : this.attrName;
            name = name.trim();
            if (name.length() > 0) {
               String value;
               if (this.hasAttrValue) {
                  value = this.attrValueSb.length() > 0 ? this.attrValueSb.toString() : this.attrValue;
               } else if (this.hasEmptyAttrValue) {
                  value = "";
               } else {
                  value = null;
               }

               this.attributes.add(name, value);
               this.trackAttributeRange(name);
            }
         }

         this.resetPendingAttr();
      }

      private void trackAttributeRange(String name) {
         if (this.trackSource && this.isStartTag()) {
            Token.StartTag start = this.asStartTag();
            CharacterReader r = start.treeBuilder.reader;
            boolean preserve = start.treeBuilder.settings.preserveAttributeCase();

            assert this.attributes != null;

            Map<String, Range.AttributeRange> attrRanges = (Map<String, Range.AttributeRange>)this.attributes.userData("jsoup.attrs");
            if (attrRanges == null) {
               attrRanges = new HashMap<>();
               this.attributes.userData("jsoup.attrs", attrRanges);
            }

            if (!preserve) {
               name = Normalizer.lowerCase(name);
            }

            if (attrRanges.containsKey(name)) {
               return;
            }

            if (!this.hasAttrValue) {
               this.attrValStart = this.attrValEnd = this.attrNameEnd;
            }

            Range.AttributeRange range = new Range.AttributeRange(
               new Range(
                  new Range.Position(this.attrNameStart, r.lineNumber(this.attrNameStart), r.columnNumber(this.attrNameStart)),
                  new Range.Position(this.attrNameEnd, r.lineNumber(this.attrNameEnd), r.columnNumber(this.attrNameEnd))
               ),
               new Range(
                  new Range.Position(this.attrValStart, r.lineNumber(this.attrValStart), r.columnNumber(this.attrValStart)),
                  new Range.Position(this.attrValEnd, r.lineNumber(this.attrValEnd), r.columnNumber(this.attrValEnd))
               )
            );
            attrRanges.put(name, range);
         }
      }

      final boolean hasAttributes() {
         return this.attributes != null;
      }

      final boolean hasAttribute(String key) {
         return this.attributes != null && this.attributes.hasKey(key);
      }

      final boolean hasAttributeIgnoreCase(String key) {
         return this.attributes != null && this.attributes.hasKeyIgnoreCase(key);
      }

      final void finaliseTag() {
         if (this.hasAttrName) {
            this.newAttribute();
         }
      }

      final String name() {
         Validate.isFalse(this.tagName == null || this.tagName.length() == 0);
         return this.tagName;
      }

      final String normalName() {
         return this.normalName;
      }

      final String toStringName() {
         return this.tagName != null ? this.tagName : "[unset]";
      }

      final Token.Tag name(String name) {
         this.tagName = name;
         this.normalName = ParseSettings.normalName(this.tagName);
         return this;
      }

      final boolean isSelfClosing() {
         return this.selfClosing;
      }

      final void appendTagName(String append) {
         append = append.replace('\u0000', '�');
         this.tagName = this.tagName == null ? append : this.tagName.concat(append);
         this.normalName = ParseSettings.normalName(this.tagName);
      }

      final void appendTagName(char append) {
         this.appendTagName(String.valueOf(append));
      }

      final void appendAttributeName(String append, int startPos, int endPos) {
         append = append.replace('\u0000', '�');
         this.ensureAttrName(startPos, endPos);
         if (this.attrNameSb.length() == 0) {
            this.attrName = append;
         } else {
            this.attrNameSb.append(append);
         }
      }

      final void appendAttributeName(char append, int startPos, int endPos) {
         this.ensureAttrName(startPos, endPos);
         this.attrNameSb.append(append);
      }

      final void appendAttributeValue(String append, int startPos, int endPos) {
         this.ensureAttrValue(startPos, endPos);
         if (this.attrValueSb.length() == 0) {
            this.attrValue = append;
         } else {
            this.attrValueSb.append(append);
         }
      }

      final void appendAttributeValue(char append, int startPos, int endPos) {
         this.ensureAttrValue(startPos, endPos);
         this.attrValueSb.append(append);
      }

      final void appendAttributeValue(int[] appendCodepoints, int startPos, int endPos) {
         this.ensureAttrValue(startPos, endPos);

         for (int codepoint : appendCodepoints) {
            this.attrValueSb.appendCodePoint(codepoint);
         }
      }

      final void setEmptyAttributeValue() {
         this.hasEmptyAttrValue = true;
      }

      private void ensureAttrName(int startPos, int endPos) {
         this.hasAttrName = true;
         if (this.attrName != null) {
            this.attrNameSb.append(this.attrName);
            this.attrName = null;
         }

         if (this.trackSource) {
            this.attrNameStart = this.attrNameStart > -1 ? this.attrNameStart : startPos;
            this.attrNameEnd = endPos;
         }
      }

      private void ensureAttrValue(int startPos, int endPos) {
         this.hasAttrValue = true;
         if (this.attrValue != null) {
            this.attrValueSb.append(this.attrValue);
            this.attrValue = null;
         }

         if (this.trackSource) {
            this.attrValStart = this.attrValStart > -1 ? this.attrValStart : startPos;
            this.attrValEnd = endPos;
         }
      }

      @Override
      public abstract String toString();
   }

   public static enum TokenType {
      Doctype,
      StartTag,
      EndTag,
      Comment,
      Character,
      EOF;
   }
}
