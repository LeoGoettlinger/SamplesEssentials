package com.sk89q.worldedit.antlr4.runtime;

import com.sk89q.worldedit.antlr4.runtime.atn.ATN;
import com.sk89q.worldedit.antlr4.runtime.atn.ATNType;
import com.sk89q.worldedit.antlr4.runtime.atn.LexerATNSimulator;
import com.sk89q.worldedit.antlr4.runtime.atn.PredictionContextCache;
import com.sk89q.worldedit.antlr4.runtime.dfa.DFA;
import java.util.ArrayList;
import java.util.Collection;

public class LexerInterpreter extends Lexer {
   protected final String grammarFileName;
   protected final ATN atn;
   @Deprecated
   protected final String[] tokenNames;
   protected final String[] ruleNames;
   protected final String[] channelNames;
   protected final String[] modeNames;
   private final Vocabulary vocabulary;
   protected final DFA[] _decisionToDFA;
   protected final PredictionContextCache _sharedContextCache = new PredictionContextCache();

   @Deprecated
   public LexerInterpreter(
      String grammarFileName, Collection<String> tokenNames, Collection<String> ruleNames, Collection<String> modeNames, ATN atn, CharStream input
   ) {
      this(grammarFileName, VocabularyImpl.fromTokenNames(tokenNames.toArray(new String[0])), ruleNames, new ArrayList<>(), modeNames, atn, input);
   }

   @Deprecated
   public LexerInterpreter(String grammarFileName, Vocabulary vocabulary, Collection<String> ruleNames, Collection<String> modeNames, ATN atn, CharStream input) {
      this(grammarFileName, vocabulary, ruleNames, new ArrayList<>(), modeNames, atn, input);
   }

   public LexerInterpreter(
      String grammarFileName,
      Vocabulary vocabulary,
      Collection<String> ruleNames,
      Collection<String> channelNames,
      Collection<String> modeNames,
      ATN atn,
      CharStream input
   ) {
      super(input);
      if (atn.grammarType != ATNType.LEXER) {
         throw new IllegalArgumentException("The ATN must be a lexer ATN.");
      } else {
         this.grammarFileName = grammarFileName;
         this.atn = atn;
         this.tokenNames = new String[atn.maxTokenType];

         for (int i = 0; i < this.tokenNames.length; i++) {
            this.tokenNames[i] = vocabulary.getDisplayName(i);
         }

         this.ruleNames = ruleNames.toArray(new String[0]);
         this.channelNames = channelNames.toArray(new String[0]);
         this.modeNames = modeNames.toArray(new String[0]);
         this.vocabulary = vocabulary;
         this._decisionToDFA = new DFA[atn.getNumberOfDecisions()];

         for (int i = 0; i < this._decisionToDFA.length; i++) {
            this._decisionToDFA[i] = new DFA(atn.getDecisionState(i), i);
         }

         this._interp = new LexerATNSimulator(this, atn, this._decisionToDFA, this._sharedContextCache);
      }
   }

   @Override
   public ATN getATN() {
      return this.atn;
   }

   @Override
   public String getGrammarFileName() {
      return this.grammarFileName;
   }

   @Deprecated
   @Override
   public String[] getTokenNames() {
      return this.tokenNames;
   }

   @Override
   public String[] getRuleNames() {
      return this.ruleNames;
   }

   @Override
   public String[] getChannelNames() {
      return this.channelNames;
   }

   @Override
   public String[] getModeNames() {
      return this.modeNames;
   }

   @Override
   public Vocabulary getVocabulary() {
      return this.vocabulary != null ? this.vocabulary : super.getVocabulary();
   }
}
