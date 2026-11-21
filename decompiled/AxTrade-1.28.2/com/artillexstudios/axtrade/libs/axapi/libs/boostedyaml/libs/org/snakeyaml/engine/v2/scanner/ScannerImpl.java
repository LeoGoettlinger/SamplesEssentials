package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.scanner;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.api.LoadSettings;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentType;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ArrayStack;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.CharConstants;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.UriEncoder;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.ScannerException;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.YamlEngineException;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.AliasToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.AnchorToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.BlockEndToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.BlockEntryToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.BlockMappingStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.BlockSequenceStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.CommentToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.DirectiveToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.DocumentEndToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.DocumentStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.FlowEntryToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.FlowMappingEndToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.FlowMappingStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.FlowSequenceEndToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.FlowSequenceStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.KeyToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.ScalarToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.StreamEndToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.StreamStartToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.TagToken;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.TagTuple;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.Token;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens.ValueToken;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

public final class ScannerImpl implements Scanner {
   private static final String DIRECTIVE_PREFIX = "while scanning a directive";
   private static final String EXPECTED_ALPHA_ERROR_PREFIX = "expected alphabetic or numeric character, but found ";
   private static final String SCANNING_SCALAR = "while scanning a block scalar";
   private static final String SCANNING_PREFIX = "while scanning a ";
   private static final Pattern NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");
   private final StreamReader reader;
   private final List<Token> tokens;
   private final ArrayStack<Integer> indents;
   private final Map<Integer, SimpleKey> possibleSimpleKeys;
   private final LoadSettings settings;
   private boolean done = false;
   private int flowLevel = 0;
   private Token lastToken;
   private int tokensTaken = 0;
   private int indent = -1;
   private boolean allowSimpleKey = true;

   @Deprecated
   public ScannerImpl(StreamReader reader, LoadSettings settings) {
      this(settings, reader);
   }

   public ScannerImpl(LoadSettings settings, StreamReader reader) {
      this.reader = reader;
      this.settings = settings;
      this.tokens = new ArrayList<>(100);
      this.indents = new ArrayStack<>(10);
      this.possibleSimpleKeys = new LinkedHashMap<>();
      this.fetchStreamStart();
   }

   @Deprecated
   public ScannerImpl(StreamReader reader) {
      this(LoadSettings.builder().build(), reader);
   }

   @Override
   public boolean checkToken(Token.ID... choices) {
      while (this.needMoreTokens()) {
         this.fetchMoreTokens();
      }

      if (!this.tokens.isEmpty()) {
         if (choices.length == 0) {
            return true;
         }

         Token firstToken = this.tokens.get(0);
         Token.ID first = firstToken.getTokenId();

         for (int i = 0; i < choices.length; i++) {
            if (first == choices[i]) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public Token peekToken() {
      while (this.needMoreTokens()) {
         this.fetchMoreTokens();
      }

      return this.tokens.get(0);
   }

   @Override
   public boolean hasNext() {
      return this.checkToken();
   }

   @Override
   public Token next() {
      this.tokensTaken++;
      if (this.tokens.isEmpty()) {
         throw new NoSuchElementException("No more Tokens found.");
      } else {
         return this.tokens.remove(0);
      }
   }

   private void addToken(Token token) {
      this.lastToken = token;
      this.tokens.add(token);
   }

   private void addToken(int index, Token token) {
      if (index == this.tokens.size()) {
         this.lastToken = token;
      }

      this.tokens.add(index, token);
   }

   private void addAllTokens(List<Token> tokens) {
      this.lastToken = tokens.get(tokens.size() - 1);
      this.tokens.addAll(tokens);
   }

   private boolean isBlockContext() {
      return this.flowLevel == 0;
   }

   private boolean isFlowContext() {
      return !this.isBlockContext();
   }

   private boolean needMoreTokens() {
      if (this.done) {
         return false;
      } else if (this.tokens.isEmpty()) {
         return true;
      } else {
         this.stalePossibleSimpleKeys();
         return this.nextPossibleSimpleKey() == this.tokensTaken;
      }
   }

   private void fetchMoreTokens() {
      if (this.reader.getDocumentIndex() > this.settings.getCodePointLimit()) {
         throw new YamlEngineException("The incoming YAML document exceeds the limit: " + this.settings.getCodePointLimit() + " code points.");
      } else {
         this.scanToNextToken();
         this.stalePossibleSimpleKeys();
         this.unwindIndent(this.reader.getColumn());
         int c = this.reader.peek();
         switch (c) {
            case 0:
               this.fetchStreamEnd();
               return;
            case 33:
               this.fetchTag();
               return;
            case 34:
               this.fetchDouble();
               return;
            case 37:
               if (this.checkDirective()) {
                  this.fetchDirective();
                  return;
               }
               break;
            case 38:
               this.fetchAnchor();
               return;
            case 39:
               this.fetchSingle();
               return;
            case 42:
               this.fetchAlias();
               return;
            case 44:
               this.fetchFlowEntry();
               return;
            case 45:
               if (this.checkDocumentStart()) {
                  this.fetchDocumentStart();
                  return;
               }

               if (this.checkBlockEntry()) {
                  this.fetchBlockEntry();
                  return;
               }
               break;
            case 46:
               if (this.checkDocumentEnd()) {
                  this.fetchDocumentEnd();
                  return;
               }
               break;
            case 58:
               if (this.checkValue()) {
                  this.fetchValue();
                  return;
               }
               break;
            case 62:
               if (this.isBlockContext()) {
                  this.fetchFolded();
                  return;
               }
               break;
            case 63:
               if (this.checkKey()) {
                  this.fetchKey();
                  return;
               }
               break;
            case 91:
               this.fetchFlowSequenceStart();
               return;
            case 93:
               this.fetchFlowSequenceEnd();
               return;
            case 123:
               this.fetchFlowMappingStart();
               return;
            case 124:
               if (this.isBlockContext()) {
                  this.fetchLiteral();
                  return;
               }
               break;
            case 125:
               this.fetchFlowMappingEnd();
               return;
         }

         if (this.checkPlain()) {
            this.fetchPlain();
         } else {
            String chRepresentation = CharConstants.escapeChar(String.valueOf(Character.toChars(c)));
            if (c == 9) {
               chRepresentation = chRepresentation + "(TAB)";
            }

            String text = String.format("found character '%s' that cannot start any token. (Do not use %s for indentation)", chRepresentation, chRepresentation);
            throw new ScannerException("while scanning for the next token", Optional.empty(), text, this.reader.getMark());
         }
      }
   }

   private int nextPossibleSimpleKey() {
      return !this.possibleSimpleKeys.isEmpty() ? this.possibleSimpleKeys.values().iterator().next().getTokenNumber() : -1;
   }

   private void stalePossibleSimpleKeys() {
      if (!this.possibleSimpleKeys.isEmpty()) {
         Iterator<SimpleKey> iterator = this.possibleSimpleKeys.values().iterator();

         while (iterator.hasNext()) {
            SimpleKey key = iterator.next();
            if (key.getLine() != this.reader.getLine() || this.reader.getIndex() - key.getIndex() > 1024) {
               if (key.isRequired()) {
                  throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
               }

               iterator.remove();
            }
         }
      }
   }

   private void savePossibleSimpleKey() {
      boolean required = this.isBlockContext() && this.indent == this.reader.getColumn();
      if (!this.allowSimpleKey && required) {
         throw new YamlEngineException("A simple key is required only if it is the first token in the current line");
      } else {
         if (this.allowSimpleKey) {
            this.removePossibleSimpleKey();
            int tokenNumber = this.tokensTaken + this.tokens.size();
            SimpleKey key = new SimpleKey(tokenNumber, required, this.reader.getIndex(), this.reader.getLine(), this.reader.getColumn(), this.reader.getMark());
            this.possibleSimpleKeys.put(this.flowLevel, key);
         }
      }
   }

   private void removePossibleSimpleKey() {
      SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
      if (key != null && key.isRequired()) {
         throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
      }
   }

   private void unwindIndent(int col) {
      if (!this.isFlowContext()) {
         while (this.indent > col) {
            Optional<Mark> mark = this.reader.getMark();
            this.indent = this.indents.pop();
            this.addToken(new BlockEndToken(mark, mark));
         }
      }
   }

   private boolean addIndent(int column) {
      if (this.indent < column) {
         this.indents.push(this.indent);
         this.indent = column;
         return true;
      } else {
         return false;
      }
   }

   private void fetchStreamStart() {
      Optional<Mark> mark = this.reader.getMark();
      Token token = new StreamStartToken(mark, mark);
      this.addToken(token);
   }

   private void fetchStreamEnd() {
      this.unwindIndent(-1);
      this.removePossibleSimpleKey();
      this.allowSimpleKey = false;
      this.possibleSimpleKeys.clear();
      Optional<Mark> mark = this.reader.getMark();
      Token token = new StreamEndToken(mark, mark);
      this.addToken(token);
      this.done = true;
   }

   private void fetchDirective() {
      this.unwindIndent(-1);
      this.removePossibleSimpleKey();
      this.allowSimpleKey = false;
      List<Token> tok = this.scanDirective();
      this.addAllTokens(tok);
   }

   private void fetchDocumentStart() {
      this.fetchDocumentIndicator(true);
   }

   private void fetchDocumentEnd() {
      this.fetchDocumentIndicator(false);
   }

   private void fetchDocumentIndicator(boolean isDocumentStart) {
      this.unwindIndent(-1);
      this.removePossibleSimpleKey();
      this.allowSimpleKey = false;
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward(3);
      Optional<Mark> endMark = this.reader.getMark();
      Token token;
      if (isDocumentStart) {
         token = new DocumentStartToken(startMark, endMark);
      } else {
         token = new DocumentEndToken(startMark, endMark);
      }

      this.addToken(token);
   }

   private void fetchFlowSequenceStart() {
      this.fetchFlowCollectionStart(false);
   }

   private void fetchFlowMappingStart() {
      this.fetchFlowCollectionStart(true);
   }

   private void fetchFlowCollectionStart(boolean isMappingStart) {
      this.savePossibleSimpleKey();
      this.flowLevel++;
      this.allowSimpleKey = true;
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward(1);
      Optional<Mark> endMark = this.reader.getMark();
      Token token;
      if (isMappingStart) {
         token = new FlowMappingStartToken(startMark, endMark);
      } else {
         token = new FlowSequenceStartToken(startMark, endMark);
      }

      this.addToken(token);
   }

   private void fetchFlowSequenceEnd() {
      this.fetchFlowCollectionEnd(false);
   }

   private void fetchFlowMappingEnd() {
      this.fetchFlowCollectionEnd(true);
   }

   private void fetchFlowCollectionEnd(boolean isMappingEnd) {
      this.removePossibleSimpleKey();
      this.flowLevel--;
      this.allowSimpleKey = false;
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      Token token;
      if (isMappingEnd) {
         token = new FlowMappingEndToken(startMark, endMark);
      } else {
         token = new FlowSequenceEndToken(startMark, endMark);
      }

      this.addToken(token);
   }

   private void fetchFlowEntry() {
      this.allowSimpleKey = true;
      this.removePossibleSimpleKey();
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      Token token = new FlowEntryToken(startMark, endMark);
      this.addToken(token);
   }

   private void fetchBlockEntry() {
      if (this.isBlockContext()) {
         if (!this.allowSimpleKey) {
            throw new ScannerException("", Optional.empty(), "sequence entries are not allowed here", this.reader.getMark());
         }

         if (this.addIndent(this.reader.getColumn())) {
            Optional<Mark> mark = this.reader.getMark();
            this.addToken(new BlockSequenceStartToken(mark, mark));
         }
      }

      this.allowSimpleKey = true;
      this.removePossibleSimpleKey();
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      Token token = new BlockEntryToken(startMark, endMark);
      this.addToken(token);
   }

   private void fetchKey() {
      if (this.isBlockContext()) {
         if (!this.allowSimpleKey) {
            throw new ScannerException("mapping keys are not allowed here", this.reader.getMark());
         }

         if (this.addIndent(this.reader.getColumn())) {
            Optional<Mark> mark = this.reader.getMark();
            this.addToken(new BlockMappingStartToken(mark, mark));
         }
      }

      this.allowSimpleKey = this.isBlockContext();
      this.removePossibleSimpleKey();
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      Token token = new KeyToken(startMark, endMark);
      this.addToken(token);
   }

   private void fetchValue() {
      SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
      if (key != null) {
         this.addToken(key.getTokenNumber() - this.tokensTaken, new KeyToken(key.getMark(), key.getMark()));
         if (this.isBlockContext() && this.addIndent(key.getColumn())) {
            this.addToken(key.getTokenNumber() - this.tokensTaken, new BlockMappingStartToken(key.getMark(), key.getMark()));
         }

         this.allowSimpleKey = false;
      } else {
         if (this.isBlockContext() && !this.allowSimpleKey) {
            throw new ScannerException("mapping values are not allowed here", this.reader.getMark());
         }

         if (this.isBlockContext() && this.addIndent(this.reader.getColumn())) {
            Optional<Mark> mark = this.reader.getMark();
            this.addToken(new BlockMappingStartToken(mark, mark));
         }

         this.allowSimpleKey = this.isBlockContext();
         this.removePossibleSimpleKey();
      }

      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      Token token = new ValueToken(startMark, endMark);
      this.addToken(token);
   }

   private void fetchAlias() {
      this.savePossibleSimpleKey();
      this.allowSimpleKey = false;
      Token tok = this.scanAnchor(false);
      this.addToken(tok);
   }

   private void fetchAnchor() {
      this.savePossibleSimpleKey();
      this.allowSimpleKey = false;
      Token tok = this.scanAnchor(true);
      this.addToken(tok);
   }

   private void fetchTag() {
      this.savePossibleSimpleKey();
      this.allowSimpleKey = false;
      Token tok = this.scanTag();
      this.addToken(tok);
   }

   private void fetchLiteral() {
      this.fetchBlockScalar(ScalarStyle.LITERAL);
   }

   private void fetchFolded() {
      this.fetchBlockScalar(ScalarStyle.FOLDED);
   }

   private void fetchBlockScalar(ScalarStyle style) {
      this.allowSimpleKey = true;
      this.removePossibleSimpleKey();
      List<Token> tok = this.scanBlockScalar(style);
      this.addAllTokens(tok);
   }

   private void fetchSingle() {
      this.fetchFlowScalar(ScalarStyle.SINGLE_QUOTED);
   }

   private void fetchDouble() {
      this.fetchFlowScalar(ScalarStyle.DOUBLE_QUOTED);
   }

   private void fetchFlowScalar(ScalarStyle style) {
      this.savePossibleSimpleKey();
      this.allowSimpleKey = false;
      Token tok = this.scanFlowScalar(style);
      this.addToken(tok);
   }

   private void fetchPlain() {
      this.savePossibleSimpleKey();
      this.allowSimpleKey = false;
      Token tok = this.scanPlain();
      this.addToken(tok);
   }

   private boolean checkDirective() {
      return this.reader.getColumn() == 0;
   }

   private boolean checkDocumentStart() {
      return this.reader.getColumn() != 0 ? false : "---".equals(this.reader.prefix(3)) && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(3));
   }

   private boolean checkDocumentEnd() {
      return this.reader.getColumn() != 0 ? false : "...".equals(this.reader.prefix(3)) && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(3));
   }

   private boolean checkBlockEntry() {
      return CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(1));
   }

   private boolean checkKey() {
      return CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(1));
   }

   private boolean checkValue() {
      return this.isFlowContext() ? true : CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(1));
   }

   private boolean checkPlain() {
      int c = this.reader.peek();
      boolean notForbidden = CharConstants.NULL_BL_T_LINEBR.hasNo(c, "-?:,[]{}#&*!|>'\"%@`");
      if (notForbidden) {
         return true;
      } else {
         return this.isBlockContext()
            ? CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(1)) && "-?:".indexOf(c) != -1
            : CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(1), ",]") && "-?".indexOf(c) != -1;
      }
   }

   private void scanToNextToken() {
      if (this.reader.getIndex() == 0 && this.reader.peek() == 65279) {
         this.reader.forward();
      }

      boolean found = false;
      int inlineStartColumn = -1;

      while (!found) {
         Optional<Mark> startMark = this.reader.getMark();
         int columnBeforeComment = this.reader.getColumn();
         boolean commentSeen = false;
         int ff = 0;

         while (this.reader.peek(ff) == 32) {
            ff++;
         }

         if (ff > 0) {
            this.reader.forward(ff);
         }

         if (this.reader.peek() == 35) {
            commentSeen = true;
            CommentType type;
            if (columnBeforeComment == 0 || this.lastToken != null && this.lastToken.getTokenId() == Token.ID.BlockEntry) {
               if (inlineStartColumn == this.reader.getColumn()) {
                  type = CommentType.IN_LINE;
               } else {
                  inlineStartColumn = -1;
                  type = CommentType.BLOCK;
               }
            } else {
               type = CommentType.IN_LINE;
               inlineStartColumn = this.reader.getColumn();
            }

            CommentToken token = this.scanComment(type);
            if (this.settings.getParseComments()) {
               this.addToken(token);
            }
         }

         Optional<String> breaksOpt = this.scanLineBreak();
         if (breaksOpt.isPresent()) {
            if (this.settings.getParseComments() && !commentSeen && columnBeforeComment == 0) {
               this.addToken(new CommentToken(CommentType.BLANK_LINE, breaksOpt.get(), startMark, this.reader.getMark()));
            }

            if (this.isBlockContext()) {
               this.allowSimpleKey = true;
            }
         } else {
            found = true;
         }
      }
   }

   private CommentToken scanComment(CommentType type) {
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      int length = 0;

      while (CharConstants.NULL_OR_LINEBR.hasNo(this.reader.peek(length))) {
         length++;
      }

      String value = this.reader.prefixForward(length);
      Optional<Mark> endMark = this.reader.getMark();
      return new CommentToken(type, value, startMark, endMark);
   }

   private List<Token> scanDirective() {
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      String name = this.scanDirectiveName(startMark);
      Optional<Mark> endMark;
      Optional<List<?>> value;
      if ("YAML".equals(name)) {
         value = Optional.of(this.scanYamlDirectiveValue(startMark));
         endMark = this.reader.getMark();
      } else if ("TAG".equals(name)) {
         value = Optional.of(this.scanTagDirectiveValue(startMark));
         endMark = this.reader.getMark();
      } else {
         endMark = this.reader.getMark();
         int ff = 0;

         while (CharConstants.NULL_OR_LINEBR.hasNo(this.reader.peek(ff))) {
            ff++;
         }

         if (ff > 0) {
            this.reader.forward(ff);
         }

         value = Optional.empty();
      }

      CommentToken commentToken = this.scanDirectiveIgnoredLine(startMark);
      DirectiveToken token = new DirectiveToken<>(name, value, startMark, endMark);
      return this.makeTokenList(token, commentToken);
   }

   private String scanDirectiveName(Optional<Mark> startMark) {
      int length = 0;
      int c = this.reader.peek(length);

      while (CharConstants.ALPHA.has(c)) {
         c = this.reader.peek(++length);
      }

      if (length == 0) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException(
            "while scanning a directive", startMark, "expected alphabetic or numeric character, but found " + s + "(" + c + ")", this.reader.getMark()
         );
      } else {
         String value = this.reader.prefixForward(length);
         c = this.reader.peek();
         if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException(
               "while scanning a directive", startMark, "expected alphabetic or numeric character, but found " + s + "(" + c + ")", this.reader.getMark()
            );
         } else {
            return value;
         }
      }
   }

   private List<Integer> scanYamlDirectiveValue(Optional<Mark> startMark) {
      while (this.reader.peek() == 32) {
         this.reader.forward();
      }

      Integer major = this.scanYamlDirectiveNumber(startMark);
      int c = this.reader.peek();
      if (c != 46) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a directive", startMark, "expected a digit or '.', but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         this.reader.forward();
         Integer minor = this.scanYamlDirectiveNumber(startMark);
         c = this.reader.peek();
         if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException(
               "while scanning a directive", startMark, "expected a digit or ' ', but found " + s + "(" + c + ")", this.reader.getMark()
            );
         } else {
            List<Integer> result = new ArrayList<>(2);
            result.add(major);
            result.add(minor);
            return result;
         }
      }
   }

   private Integer scanYamlDirectiveNumber(Optional<Mark> startMark) {
      int c = this.reader.peek();
      if (!Character.isDigit(c)) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a directive", startMark, "expected a digit, but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         int length = 0;

         while (Character.isDigit(this.reader.peek(length))) {
            length++;
         }

         String number = this.reader.prefixForward(length);
         if (length > 3) {
            throw new ScannerException(
               "while scanning a YAML directive", startMark, "found a number which cannot represent a valid version: " + number, this.reader.getMark()
            );
         } else {
            return Integer.parseInt(number);
         }
      }
   }

   private List<String> scanTagDirectiveValue(Optional<Mark> startMark) {
      while (this.reader.peek() == 32) {
         this.reader.forward();
      }

      String handle = this.scanTagDirectiveHandle(startMark);

      while (this.reader.peek() == 32) {
         this.reader.forward();
      }

      String prefix = this.scanTagDirectivePrefix(startMark);
      List<String> result = new ArrayList<>(2);
      result.add(handle);
      result.add(prefix);
      return result;
   }

   private String scanTagDirectiveHandle(Optional<Mark> startMark) {
      String value = this.scanTagHandle("directive", startMark);
      int c = this.reader.peek();
      if (c != 32) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a directive", startMark, "expected ' ', but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         return value;
      }
   }

   private String scanTagDirectivePrefix(Optional<Mark> startMark) {
      String value = this.scanTagUri("directive", CharConstants.URI_CHARS_FOR_TAG_PREFIX, startMark);
      int c = this.reader.peek();
      if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a directive", startMark, "expected ' ', but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         return value;
      }
   }

   private CommentToken scanDirectiveIgnoredLine(Optional<Mark> startMark) {
      while (this.reader.peek() == 32) {
         this.reader.forward();
      }

      CommentToken commentToken = null;
      if (this.reader.peek() == 35) {
         CommentToken comment = this.scanComment(CommentType.IN_LINE);
         if (this.settings.getParseComments()) {
            commentToken = comment;
         }
      }

      int c = this.reader.peek();
      if (!this.scanLineBreak().isPresent() && c != 0) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException(
            "while scanning a directive", startMark, "expected a comment or a line break, but found " + s + "(" + c + ")", this.reader.getMark()
         );
      } else {
         return commentToken;
      }
   }

   private Token scanAnchor(boolean isAnchor) {
      Optional<Mark> startMark = this.reader.getMark();
      int indicator = this.reader.peek();
      String name = indicator == 42 ? "alias" : "anchor";
      this.reader.forward();
      int length = 0;
      int c = this.reader.peek(length);

      while (CharConstants.NULL_BL_T_LINEBR.hasNo(c, ",[]{}/.*&")) {
         c = this.reader.peek(++length);
      }

      if (length == 0) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning an " + name, startMark, "unexpected character found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         String value = this.reader.prefixForward(length);
         c = this.reader.peek();
         if (CharConstants.NULL_BL_T_LINEBR.hasNo(c, "?:,]}%@`")) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning an " + name, startMark, "unexpected character found " + s + "(" + c + ")", this.reader.getMark());
         } else {
            Optional<Mark> endMark = this.reader.getMark();
            Token tok;
            if (isAnchor) {
               tok = new AnchorToken(new Anchor(value), startMark, endMark);
            } else {
               tok = new AliasToken(new Anchor(value), startMark, endMark);
            }

            return tok;
         }
      }
   }

   private Token scanTag() {
      Optional<Mark> startMark = this.reader.getMark();
      int c = this.reader.peek(1);
      String handle = null;
      String suffix = null;
      if (c == 60) {
         this.reader.forward(2);
         suffix = this.scanTagUri("tag", CharConstants.URI_CHARS_FOR_TAG_PREFIX, startMark);
         c = this.reader.peek();
         if (c != 62) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a tag", startMark, "expected '>', but found '" + s + "' (" + c + ")", this.reader.getMark());
         }

         this.reader.forward();
      } else if (CharConstants.NULL_BL_T_LINEBR.has(c)) {
         suffix = "!";
         this.reader.forward();
      } else {
         int length = 1;

         boolean useHandle;
         for (useHandle = false; CharConstants.NULL_BL_LINEBR.hasNo(c); c = this.reader.peek(++length)) {
            if (c == 33) {
               useHandle = true;
               break;
            }
         }

         if (useHandle) {
            handle = this.scanTagHandle("tag", startMark);
         } else {
            handle = "!";
            this.reader.forward();
         }

         suffix = this.scanTagUri("tag", CharConstants.URI_CHARS_FOR_TAG_SUFFIX, startMark);
      }

      c = this.reader.peek();
      if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a tag", startMark, "expected ' ', but found '" + s + "' (" + c + ")", this.reader.getMark());
      } else {
         TagTuple value = new TagTuple(Optional.ofNullable(handle), suffix);
         Optional<Mark> endMark = this.reader.getMark();
         return new TagToken(value, startMark, endMark);
      }
   }

   private List<Token> scanBlockScalar(ScalarStyle style) {
      StringBuilder stringBuilder = new StringBuilder();
      Optional<Mark> startMark = this.reader.getMark();
      this.reader.forward();
      ScannerImpl.Chomping chomping = this.scanBlockScalarIndicators(startMark);
      CommentToken commentToken = this.scanBlockScalarIgnoredLine(startMark);
      int minIndent = this.indent + 1;
      if (minIndent < 1) {
         minIndent = 1;
      }

      String breaks;
      int blockIndent;
      Optional<Mark> endMark;
      if (chomping.increment.isPresent()) {
         blockIndent = minIndent + chomping.increment.get() - 1;
         ScannerImpl.BreakIntentHolder brme = this.scanBlockScalarBreaks(blockIndent);
         breaks = brme.breaks;
         endMark = brme.endMark;
      } else {
         ScannerImpl.BreakIntentHolder brme = this.scanBlockScalarIndentation();
         breaks = brme.breaks;
         int maxIndent = brme.maxIndent;
         endMark = brme.endMark;
         blockIndent = Math.max(minIndent, maxIndent);
      }

      Optional<String> lineBreakOpt = Optional.empty();
      if (this.reader.getColumn() < blockIndent && this.indent != this.reader.getColumn()) {
         throw new ScannerException(
            "while scanning a block scalar",
            startMark,
            " the leading empty lines contain more spaces (" + blockIndent + ") than the first non-empty line.",
            this.reader.getMark()
         );
      } else {
         while (this.reader.getColumn() == blockIndent && this.reader.peek() != 0) {
            stringBuilder.append(breaks);
            boolean leadingNonSpace = " \t".indexOf(this.reader.peek()) == -1;
            int length = 0;

            while (CharConstants.NULL_OR_LINEBR.hasNo(this.reader.peek(length))) {
               length++;
            }

            stringBuilder.append(this.reader.prefixForward(length));
            lineBreakOpt = this.scanLineBreak();
            ScannerImpl.BreakIntentHolder brme = this.scanBlockScalarBreaks(blockIndent);
            breaks = brme.breaks;
            endMark = brme.endMark;
            if (this.reader.getColumn() != blockIndent || this.reader.peek() == 0) {
               break;
            }

            if (style != ScalarStyle.FOLDED || !"\n".equals(lineBreakOpt.orElse("")) || !leadingNonSpace || " \t".indexOf(this.reader.peek()) != -1) {
               stringBuilder.append(lineBreakOpt.orElse(""));
            } else if (breaks.isEmpty()) {
               stringBuilder.append(" ");
            }
         }

         if (chomping.value == ScannerImpl.Chomping.Indicator.CLIP || chomping.value == ScannerImpl.Chomping.Indicator.KEEP) {
            stringBuilder.append(lineBreakOpt.orElse(""));
         }

         if (chomping.value == ScannerImpl.Chomping.Indicator.KEEP) {
            stringBuilder.append(breaks);
         }

         ScalarToken scalarToken = new ScalarToken(stringBuilder.toString(), false, style, startMark, endMark);
         return this.makeTokenList(commentToken, scalarToken);
      }
   }

   private ScannerImpl.Chomping scanBlockScalarIndicators(Optional<Mark> startMark) {
      int indicator = Integer.MIN_VALUE;
      Optional<Integer> increment = Optional.empty();
      int c = this.reader.peek();
      if (c == 45 || c == 43) {
         indicator = c;
         this.reader.forward();
         c = this.reader.peek();
         if (Character.isDigit(c)) {
            int incr = Integer.parseInt(String.valueOf(Character.toChars(c)));
            if (incr == 0) {
               throw new ScannerException(
                  "while scanning a block scalar", startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark()
               );
            }

            increment = Optional.of(incr);
            this.reader.forward();
         }
      } else if (Character.isDigit(c)) {
         int incr = Integer.parseInt(String.valueOf(Character.toChars(c)));
         if (incr == 0) {
            throw new ScannerException(
               "while scanning a block scalar", startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark()
            );
         }

         increment = Optional.of(incr);
         this.reader.forward();
         c = this.reader.peek();
         if (c == 45 || c == 43) {
            indicator = c;
            this.reader.forward();
         }
      }

      c = this.reader.peek();
      if (CharConstants.NULL_BL_LINEBR.hasNo(c)) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException(
            "while scanning a block scalar", startMark, "expected chomping or indentation indicators, but found " + s + "(" + c + ")", this.reader.getMark()
         );
      } else {
         return new ScannerImpl.Chomping(indicator, increment);
      }
   }

   private CommentToken scanBlockScalarIgnoredLine(Optional<Mark> startMark) {
      while (this.reader.peek() == 32) {
         this.reader.forward();
      }

      CommentToken commentToken = null;
      if (this.reader.peek() == 35) {
         commentToken = this.scanComment(CommentType.IN_LINE);
      }

      int c = this.reader.peek();
      if (!this.scanLineBreak().isPresent() && c != 0) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException(
            "while scanning a block scalar", startMark, "expected a comment or a line break, but found " + s + "(" + c + ")", this.reader.getMark()
         );
      } else {
         return commentToken;
      }
   }

   private ScannerImpl.BreakIntentHolder scanBlockScalarIndentation() {
      StringBuilder chunks = new StringBuilder();
      int maxIndent = 0;
      Optional<Mark> endMark = this.reader.getMark();

      while (CharConstants.LINEBR.has(this.reader.peek(), " \r")) {
         if (this.reader.peek() != 32) {
            chunks.append(this.scanLineBreak().orElse(""));
            endMark = this.reader.getMark();
         } else {
            this.reader.forward();
            if (this.reader.getColumn() > maxIndent) {
               maxIndent = this.reader.getColumn();
            }
         }
      }

      return new ScannerImpl.BreakIntentHolder(chunks.toString(), maxIndent, endMark);
   }

   private ScannerImpl.BreakIntentHolder scanBlockScalarBreaks(int indent) {
      StringBuilder chunks = new StringBuilder();
      Optional<Mark> endMark = this.reader.getMark();

      for (int col = this.reader.getColumn(); col < indent && this.reader.peek() == 32; col++) {
         this.reader.forward();
      }

      Optional<String> lineBreakOpt;
      while ((lineBreakOpt = this.scanLineBreak()).isPresent()) {
         chunks.append(lineBreakOpt.get());
         endMark = this.reader.getMark();

         for (int var6 = this.reader.getColumn(); var6 < indent && this.reader.peek() == 32; var6++) {
            this.reader.forward();
         }
      }

      return new ScannerImpl.BreakIntentHolder(chunks.toString(), -1, endMark);
   }

   private Token scanFlowScalar(ScalarStyle style) {
      boolean doubleValue = style == ScalarStyle.DOUBLE_QUOTED;
      StringBuilder chunks = new StringBuilder();
      Optional<Mark> startMark = this.reader.getMark();
      int quote = this.reader.peek();
      this.reader.forward();
      chunks.append(this.scanFlowScalarNonSpaces(doubleValue, startMark));

      while (this.reader.peek() != quote) {
         chunks.append(this.scanFlowScalarSpaces(startMark));
         chunks.append(this.scanFlowScalarNonSpaces(doubleValue, startMark));
      }

      this.reader.forward();
      Optional<Mark> endMark = this.reader.getMark();
      return new ScalarToken(chunks.toString(), false, style, startMark, endMark);
   }

   private String scanFlowScalarNonSpaces(boolean doubleQuoted, Optional<Mark> startMark) {
      StringBuilder chunks = new StringBuilder();

      while (true) {
         int length = 0;

         while (CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(length), "'\"\\")) {
            length++;
         }

         if (length != 0) {
            chunks.append(this.reader.prefixForward(length));
         }

         int c = this.reader.peek();
         if (!doubleQuoted && c == 39 && this.reader.peek(1) == 39) {
            chunks.append("'");
            this.reader.forward(2);
         } else if ((!doubleQuoted || c != 39) && (doubleQuoted || "\"\\".indexOf(c) == -1)) {
            if (!doubleQuoted || c != 92) {
               return chunks.toString();
            }

            this.reader.forward();
            c = this.reader.peek();
            if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_REPLACEMENTS.containsKey((char)c)) {
               chunks.append(CharConstants.ESCAPE_REPLACEMENTS.get((char)c));
               this.reader.forward();
            } else if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_CODES.containsKey((char)c)) {
               length = CharConstants.ESCAPE_CODES.get((char)c);
               this.reader.forward();
               String hex = this.reader.prefix(length);
               if (NOT_HEXA.matcher(hex).find()) {
                  throw new ScannerException(
                     "while scanning a double-quoted scalar",
                     startMark,
                     "expected escape sequence of " + length + " hexadecimal numbers, but found: " + hex,
                     this.reader.getMark()
                  );
               }

               int decimal = Integer.parseInt(hex, 16);

               try {
                  String unicode = new String(Character.toChars(decimal));
                  chunks.append(unicode);
                  this.reader.forward(length);
               } catch (IllegalArgumentException var9) {
                  throw new ScannerException("while scanning a double-quoted scalar", startMark, "found unknown escape character " + hex, this.reader.getMark());
               }
            } else {
               if (!this.scanLineBreak().isPresent()) {
                  String s = String.valueOf(Character.toChars(c));
                  throw new ScannerException(
                     "while scanning a double-quoted scalar", startMark, "found unknown escape character " + s + "(" + c + ")", this.reader.getMark()
                  );
               }

               chunks.append(this.scanFlowScalarBreaks(startMark));
            }
         } else {
            chunks.appendCodePoint(c);
            this.reader.forward();
         }
      }
   }

   private String scanFlowScalarSpaces(Optional<Mark> startMark) {
      StringBuilder chunks = new StringBuilder();
      int length = 0;

      while (" \t".indexOf(this.reader.peek(length)) != -1) {
         length++;
      }

      String whitespaces = this.reader.prefixForward(length);
      int c = this.reader.peek();
      if (c == 0) {
         throw new ScannerException("while scanning a quoted scalar", startMark, "found unexpected end of stream", this.reader.getMark());
      } else {
         Optional<String> lineBreakOpt = this.scanLineBreak();
         if (lineBreakOpt.isPresent()) {
            String breaks = this.scanFlowScalarBreaks(startMark);
            if (!"\n".equals(lineBreakOpt.get())) {
               chunks.append(lineBreakOpt.get());
            } else if (breaks.isEmpty()) {
               chunks.append(" ");
            }

            chunks.append(breaks);
         } else {
            chunks.append(whitespaces);
         }

         return chunks.toString();
      }
   }

   private String scanFlowScalarBreaks(Optional<Mark> startMark) {
      StringBuilder chunks = new StringBuilder();

      while (true) {
         String prefix = this.reader.prefix(3);
         if (("---".equals(prefix) || "...".equals(prefix)) && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(3))) {
            throw new ScannerException("while scanning a quoted scalar", startMark, "found unexpected document separator", this.reader.getMark());
         }

         while (" \t".indexOf(this.reader.peek()) != -1) {
            this.reader.forward();
         }

         Optional<String> lineBreakOpt = this.scanLineBreak();
         if (!lineBreakOpt.isPresent()) {
            return chunks.toString();
         }

         chunks.append(lineBreakOpt.get());
      }
   }

   private Token scanPlain() {
      StringBuilder chunks = new StringBuilder();
      Optional<Mark> startMark = this.reader.getMark();
      Optional<Mark> endMark = startMark;
      int plainIndent = this.indent + 1;
      String spaces = "";

      while (true) {
         int length = 0;
         if (this.reader.peek() == 35) {
            return new ScalarToken(chunks.toString(), true, startMark, endMark);
         }

         while (true) {
            int c = this.reader.peek(length);
            if (CharConstants.NULL_BL_T_LINEBR.has(c)
               || c == 58 && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(length + 1), this.isFlowContext() ? ",[]{}" : "")
               || this.isFlowContext() && ",[]{}".indexOf(c) != -1) {
               if (length == 0) {
                  return new ScalarToken(chunks.toString(), true, startMark, endMark);
               }

               this.allowSimpleKey = false;
               chunks.append(spaces);
               chunks.append(this.reader.prefixForward(length));
               endMark = this.reader.getMark();
               spaces = this.scanPlainSpaces();
               if (spaces.isEmpty() || this.reader.peek() == 35 || this.isBlockContext() && this.reader.getColumn() < plainIndent) {
                  return new ScalarToken(chunks.toString(), true, startMark, endMark);
               }
               break;
            }

            length++;
         }
      }
   }

   private boolean atEndOfPlain() {
      int wsLength = 0;
      int wsColumn = this.reader.getColumn();

      int c;
      while ((c = this.reader.peek(wsLength)) != 0 && CharConstants.NULL_BL_T_LINEBR.has(c)) {
         wsLength++;
         if (!CharConstants.LINEBR.has(c) && (c != 13 || this.reader.peek(wsLength + 1) != 10) && c != 65279) {
            wsColumn++;
         } else {
            wsColumn = 0;
         }
      }

      if (this.reader.peek(wsLength) != 35 && this.reader.peek(wsLength + 1) != 0 && (!this.isBlockContext() || wsColumn >= this.indent)) {
         if (this.isBlockContext()) {
            for (int extra = 1; (c = this.reader.peek(wsLength + extra)) != 0 && !CharConstants.NULL_BL_T_LINEBR.has(c); extra++) {
               if (c == 58 && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(wsLength + extra + 1))) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private String scanPlainSpaces() {
      int length = 0;

      while (this.reader.peek(length) == 32 || this.reader.peek(length) == 9) {
         length++;
      }

      String whitespaces = this.reader.prefixForward(length);
      Optional<String> lineBreakOpt = this.scanLineBreak();
      if (!lineBreakOpt.isPresent()) {
         return whitespaces;
      } else {
         this.allowSimpleKey = true;
         String prefix = this.reader.prefix(3);
         if ("---".equals(prefix) || "...".equals(prefix) && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(3))) {
            return "";
         } else if (this.settings.getParseComments() && this.atEndOfPlain()) {
            return "";
         } else {
            StringBuilder breaks = new StringBuilder();

            do {
               while (this.reader.peek() == 32) {
                  this.reader.forward();
               }

               Optional<String> lbOpt = this.scanLineBreak();
               if (!lbOpt.isPresent()) {
                  if (!"\n".equals(lineBreakOpt.orElse(""))) {
                     return lineBreakOpt.orElse("") + breaks;
                  }

                  if (breaks.length() == 0) {
                     return " ";
                  }

                  return breaks.toString();
               }

               breaks.append(lbOpt.get());
               prefix = this.reader.prefix(3);
            } while (!"---".equals(prefix) && (!"...".equals(prefix) || !CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(3))));

            return "";
         }
      }
   }

   private String scanTagHandle(String name, Optional<Mark> startMark) {
      int c = this.reader.peek();
      if (c != 33) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a " + name, startMark, "expected '!', but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         int length = 1;
         c = this.reader.peek(length);
         if (c != 32) {
            while (CharConstants.ALPHA.has(c)) {
               c = this.reader.peek(++length);
            }

            if (c != 33) {
               this.reader.forward(length);
               String s = String.valueOf(Character.toChars(c));
               throw new ScannerException("while scanning a " + name, startMark, "expected '!', but found " + s + "(" + c + ")", this.reader.getMark());
            }

            length++;
         }

         return this.reader.prefixForward(length);
      }
   }

   private String scanTagUri(String name, CharConstants range, Optional<Mark> startMark) {
      StringBuilder chunks = new StringBuilder();
      int length = 0;

      int c;
      for (c = this.reader.peek(length); range.has(c); c = this.reader.peek(length)) {
         if (c == 37) {
            chunks.append(this.reader.prefixForward(length));
            length = 0;
            chunks.append(this.scanUriEscapes(name, startMark));
         } else {
            length++;
         }
      }

      if (length != 0) {
         chunks.append(this.reader.prefixForward(length));
      }

      if (chunks.length() == 0) {
         String s = String.valueOf(Character.toChars(c));
         throw new ScannerException("while scanning a " + name, startMark, "expected URI, but found " + s + "(" + c + ")", this.reader.getMark());
      } else {
         return chunks.toString();
      }
   }

   private String scanUriEscapes(String name, Optional<Mark> startMark) {
      int length = 1;

      while (this.reader.peek(length * 3) == 37) {
         length++;
      }

      Optional<Mark> beginningMark = this.reader.getMark();

      ByteBuffer buff;
      for (buff = ByteBuffer.allocate(length); this.reader.peek() == 37; this.reader.forward(2)) {
         this.reader.forward();

         try {
            byte code = (byte)Integer.parseInt(this.reader.prefix(2), 16);
            buff.put(code);
         } catch (NumberFormatException var12) {
            int c1 = this.reader.peek();
            String s1 = String.valueOf(Character.toChars(c1));
            int c2 = this.reader.peek(1);
            String s2 = String.valueOf(Character.toChars(c2));
            throw new ScannerException(
               "while scanning a " + name,
               startMark,
               "expected URI escape sequence of 2 hexadecimal numbers, but found " + s1 + "(" + c1 + ") and " + s2 + "(" + c2 + ")",
               this.reader.getMark()
            );
         }
      }

      buff.flip();

      try {
         return UriEncoder.decode(buff);
      } catch (CharacterCodingException var11) {
         throw new ScannerException("while scanning a " + name, startMark, "expected URI in UTF-8: " + var11.getMessage(), beginningMark);
      }
   }

   private Optional<String> scanLineBreak() {
      int c = this.reader.peek();
      if (c != 13 && c != 10 && c != 133) {
         return Optional.empty();
      } else {
         if (c == 13 && 10 == this.reader.peek(1)) {
            this.reader.forward(2);
         } else {
            this.reader.forward();
         }

         return Optional.of("\n");
      }
   }

   private List<Token> makeTokenList(Token... tokens) {
      List<Token> tokenList = new ArrayList<>();

      for (int ix = 0; ix < tokens.length; ix++) {
         if (tokens[ix] != null && (this.settings.getParseComments() || !(tokens[ix] instanceof CommentToken))) {
            tokenList.add(tokens[ix]);
         }
      }

      return tokenList;
   }

   @Override
   public void resetDocumentIndex() {
      this.reader.resetDocumentIndex();
   }

   static class BreakIntentHolder {
      private final String breaks;
      private final int maxIndent;
      private final Optional<Mark> endMark;

      public BreakIntentHolder(String breaks, int maxIndent, Optional<Mark> endMark) {
         this.breaks = breaks;
         this.maxIndent = maxIndent;
         this.endMark = endMark;
      }
   }

   static class Chomping {
      private final ScannerImpl.Chomping.Indicator value;
      private final Optional<Integer> increment;

      public Chomping(ScannerImpl.Chomping.Indicator value, Optional<Integer> increment) {
         this.value = value;
         this.increment = increment;
      }

      public Chomping(int indicatorCodePoint, Optional<Integer> increment) {
         this(parse(indicatorCodePoint), increment);
      }

      private static ScannerImpl.Chomping.Indicator parse(int codePoint) {
         if (codePoint == 43) {
            return ScannerImpl.Chomping.Indicator.KEEP;
         } else if (codePoint == 45) {
            return ScannerImpl.Chomping.Indicator.STRIP;
         } else if (codePoint == Integer.MIN_VALUE) {
            return ScannerImpl.Chomping.Indicator.CLIP;
         } else {
            throw new IllegalArgumentException("Unexpected block chomping indicator: " + codePoint);
         }
      }

      static enum Indicator {
         STRIP,
         CLIP,
         KEEP;
      }
   }
}
