package org.sethomegui.SetHomeGUI.libs.redis.jedis.search.schemafields;

import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.FieldName;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.search.SearchProtocol;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

public class TagField extends SchemaField {
   private boolean indexMissing;
   private boolean indexEmpty;
   private byte[] separator;
   private boolean caseSensitive;
   private boolean withSuffixTrie;
   private boolean sortable;
   private boolean sortableUNF;
   private boolean noIndex;

   public TagField(String fieldName) {
      super(fieldName);
   }

   public TagField(FieldName fieldName) {
      super(fieldName);
   }

   public static TagField of(String fieldName) {
      return new TagField(fieldName);
   }

   public static TagField of(FieldName fieldName) {
      return new TagField(fieldName);
   }

   public TagField as(String attribute) {
      super.as(attribute);
      return this;
   }

   public TagField indexMissing() {
      this.indexMissing = true;
      return this;
   }

   public TagField indexEmpty() {
      this.indexEmpty = true;
      return this;
   }

   public TagField separator(char separator) {
      if (separator < 128) {
         this.separator = new byte[]{(byte)separator};
      } else {
         this.separator = SafeEncoder.encode(String.valueOf(separator));
      }

      return this;
   }

   public TagField caseSensitive() {
      this.caseSensitive = true;
      return this;
   }

   public TagField withSuffixTrie() {
      this.withSuffixTrie = true;
      return this;
   }

   public TagField sortable() {
      this.sortable = true;
      return this;
   }

   public TagField sortableUNF() {
      this.sortableUNF = true;
      return this;
   }

   @Deprecated
   public TagField sortableUnNormalizedForm() {
      return this.sortableUNF();
   }

   public TagField noIndex() {
      this.noIndex = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      args.addParams(this.fieldName);
      args.add((Rawable)SearchProtocol.SearchKeyword.TAG);
      if (this.indexMissing) {
         args.add((Rawable)SearchProtocol.SearchKeyword.INDEXMISSING);
      }

      if (this.indexEmpty) {
         args.add((Rawable)SearchProtocol.SearchKeyword.INDEXEMPTY);
      }

      if (this.separator != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SEPARATOR).add(this.separator);
      }

      if (this.caseSensitive) {
         args.add((Rawable)SearchProtocol.SearchKeyword.CASESENSITIVE);
      }

      if (this.withSuffixTrie) {
         args.add((Rawable)SearchProtocol.SearchKeyword.WITHSUFFIXTRIE);
      }

      if (this.sortableUNF) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SORTABLE).add((Rawable)SearchProtocol.SearchKeyword.UNF);
      } else if (this.sortable) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SORTABLE);
      }

      if (this.noIndex) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOINDEX);
      }
   }
}
