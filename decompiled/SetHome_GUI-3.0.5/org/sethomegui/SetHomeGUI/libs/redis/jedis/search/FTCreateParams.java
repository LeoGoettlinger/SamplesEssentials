package org.sethomegui.SetHomeGUI.libs.redis.jedis.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.CommandArguments;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.args.Rawable;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.params.IParams;

public class FTCreateParams implements IParams {
   private IndexDataType dataType;
   private Collection<String> prefix;
   private String filter;
   private String language;
   private String languageField;
   private Double score;
   private String scoreField;
   private boolean maxTextFields;
   private boolean noOffsets;
   private Long temporary;
   private boolean noHL;
   private boolean noFields;
   private boolean noFreqs;
   private Collection<String> stopwords;
   private boolean skipInitialScan;

   public static FTCreateParams createParams() {
      return new FTCreateParams();
   }

   public FTCreateParams on(IndexDataType dataType) {
      this.dataType = dataType;
      return this;
   }

   public FTCreateParams prefix(String... prefixes) {
      if (this.prefix == null) {
         this.prefix = new ArrayList<>(prefixes.length);
      }

      Arrays.stream(prefixes).forEach(p -> this.prefix.add(p));
      return this;
   }

   public FTCreateParams addPrefix(String prefix) {
      if (this.prefix == null) {
         this.prefix = new ArrayList<>();
      }

      this.prefix.add(prefix);
      return this;
   }

   public FTCreateParams filter(String filter) {
      this.filter = filter;
      return this;
   }

   public FTCreateParams language(String defaultLanguage) {
      this.language = defaultLanguage;
      return this;
   }

   public FTCreateParams languageField(String languageAttribute) {
      this.languageField = languageAttribute;
      return this;
   }

   public FTCreateParams score(double defaultScore) {
      this.score = defaultScore;
      return this;
   }

   public FTCreateParams scoreField(String scoreField) {
      this.scoreField = scoreField;
      return this;
   }

   public FTCreateParams maxTextFields() {
      this.maxTextFields = true;
      return this;
   }

   public FTCreateParams noOffsets() {
      this.noOffsets = true;
      return this;
   }

   public FTCreateParams temporary(long seconds) {
      this.temporary = seconds;
      return this;
   }

   public FTCreateParams noHL() {
      this.noHL = true;
      return this;
   }

   public FTCreateParams noHighlights() {
      return this.noHL();
   }

   public FTCreateParams noFields() {
      this.noFields = true;
      return this;
   }

   public FTCreateParams noFreqs() {
      this.noFreqs = true;
      return this;
   }

   public FTCreateParams stopwords(String... stopwords) {
      this.stopwords = Arrays.asList(stopwords);
      return this;
   }

   public FTCreateParams noStopwords() {
      this.stopwords = Collections.emptyList();
      return this;
   }

   public FTCreateParams skipInitialScan() {
      this.skipInitialScan = true;
      return this;
   }

   @Override
   public void addParams(CommandArguments args) {
      if (this.dataType != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.ON).add(this.dataType);
      }

      if (this.prefix != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.PREFIX).add(this.prefix.size()).addObjects(this.prefix);
      }

      if (this.filter != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.FILTER).add(this.filter);
      }

      if (this.language != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.LANGUAGE).add(this.language);
      }

      if (this.languageField != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.LANGUAGE_FIELD).add(this.languageField);
      }

      if (this.score != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SCORE).add(this.score);
      }

      if (this.scoreField != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SCORE_FIELD).add(this.scoreField);
      }

      if (this.maxTextFields) {
         args.add((Rawable)SearchProtocol.SearchKeyword.MAXTEXTFIELDS);
      }

      if (this.noOffsets) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOOFFSETS);
      }

      if (this.temporary != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.TEMPORARY).add(this.temporary);
      }

      if (this.noHL) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOHL);
      }

      if (this.noFields) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOFIELDS);
      }

      if (this.noFreqs) {
         args.add((Rawable)SearchProtocol.SearchKeyword.NOFREQS);
      }

      if (this.stopwords != null) {
         args.add((Rawable)SearchProtocol.SearchKeyword.STOPWORDS).add(this.stopwords.size());
         this.stopwords.forEach(w -> args.add(w));
      }

      if (this.skipInitialScan) {
         args.add((Rawable)SearchProtocol.SearchKeyword.SKIPINITIALSCAN);
      }
   }
}
