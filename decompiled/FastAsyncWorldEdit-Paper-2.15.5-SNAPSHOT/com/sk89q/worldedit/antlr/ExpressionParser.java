package com.sk89q.worldedit.antlr;

import com.sk89q.worldedit.antlr4.runtime.FailedPredicateException;
import com.sk89q.worldedit.antlr4.runtime.NoViableAltException;
import com.sk89q.worldedit.antlr4.runtime.Parser;
import com.sk89q.worldedit.antlr4.runtime.ParserRuleContext;
import com.sk89q.worldedit.antlr4.runtime.RecognitionException;
import com.sk89q.worldedit.antlr4.runtime.RuleContext;
import com.sk89q.worldedit.antlr4.runtime.RuntimeMetaData;
import com.sk89q.worldedit.antlr4.runtime.Token;
import com.sk89q.worldedit.antlr4.runtime.TokenStream;
import com.sk89q.worldedit.antlr4.runtime.Vocabulary;
import com.sk89q.worldedit.antlr4.runtime.VocabularyImpl;
import com.sk89q.worldedit.antlr4.runtime.atn.ATN;
import com.sk89q.worldedit.antlr4.runtime.atn.ATNDeserializer;
import com.sk89q.worldedit.antlr4.runtime.atn.ParserATNSimulator;
import com.sk89q.worldedit.antlr4.runtime.atn.PredictionContextCache;
import com.sk89q.worldedit.antlr4.runtime.dfa.DFA;
import com.sk89q.worldedit.antlr4.runtime.tree.ParseTreeListener;
import com.sk89q.worldedit.antlr4.runtime.tree.ParseTreeVisitor;
import com.sk89q.worldedit.antlr4.runtime.tree.TerminalNode;
import java.util.ArrayList;
import java.util.List;

public class ExpressionParser extends Parser {
   protected static final DFA[] _decisionToDFA;
   protected static final PredictionContextCache _sharedContextCache = new PredictionContextCache();
   public static final int PLUS = 1;
   public static final int MINUS = 2;
   public static final int TIMES = 3;
   public static final int DIVIDE = 4;
   public static final int MODULO = 5;
   public static final int POWER = 6;
   public static final int LEFT_SHIFT = 7;
   public static final int RIGHT_SHIFT = 8;
   public static final int ASSIGN = 9;
   public static final int COMPLEMENT = 10;
   public static final int PLUS_ASSIGN = 11;
   public static final int MINUS_ASSIGN = 12;
   public static final int TIMES_ASSIGN = 13;
   public static final int DIVIDE_ASSIGN = 14;
   public static final int MODULO_ASSIGN = 15;
   public static final int POWER_ASSIGN = 16;
   public static final int EQUAL = 17;
   public static final int NOT_EQUAL = 18;
   public static final int NEAR = 19;
   public static final int LESS_THAN = 20;
   public static final int LESS_THAN_OR_EQUAL = 21;
   public static final int GREATER_THAN = 22;
   public static final int GREATER_THAN_OR_EQUAL = 23;
   public static final int AND_SC = 24;
   public static final int OR_SC = 25;
   public static final int INCREMENT = 26;
   public static final int DECREMENT = 27;
   public static final int COMMA = 28;
   public static final int OPEN_PAREN = 29;
   public static final int CLOSE_PAREN = 30;
   public static final int OPEN_BRACKET = 31;
   public static final int CLOSE_BRACKET = 32;
   public static final int SEMI_COLON = 33;
   public static final int QUESTION_MARK = 34;
   public static final int COLON = 35;
   public static final int EXCLAMATION_MARK = 36;
   public static final int IF = 37;
   public static final int ELSE = 38;
   public static final int WHILE = 39;
   public static final int DO = 40;
   public static final int FOR = 41;
   public static final int BREAK = 42;
   public static final int CONTINUE = 43;
   public static final int RETURN = 44;
   public static final int SWITCH = 45;
   public static final int CASE = 46;
   public static final int DEFAULT = 47;
   public static final int NUMBER = 48;
   public static final int ID = 49;
   public static final int WS = 50;
   public static final int RULE_allStatements = 0;
   public static final int RULE_statements = 1;
   public static final int RULE_statement = 2;
   public static final int RULE_block = 3;
   public static final int RULE_ifStatement = 4;
   public static final int RULE_whileStatement = 5;
   public static final int RULE_doStatement = 6;
   public static final int RULE_forStatement = 7;
   public static final int RULE_simpleForStatement = 8;
   public static final int RULE_breakStatement = 9;
   public static final int RULE_continueStatement = 10;
   public static final int RULE_returnStatement = 11;
   public static final int RULE_switchStatement = 12;
   public static final int RULE_switchLabel = 13;
   public static final int RULE_expressionStatement = 14;
   public static final int RULE_emptyStatement = 15;
   public static final int RULE_expression = 16;
   public static final int RULE_assignmentExpression = 17;
   public static final int RULE_assignment = 18;
   public static final int RULE_assignmentOperator = 19;
   public static final int RULE_conditionalExpression = 20;
   public static final int RULE_conditionalOrExpression = 21;
   public static final int RULE_conditionalAndExpression = 22;
   public static final int RULE_equalityExpression = 23;
   public static final int RULE_relationalExpression = 24;
   public static final int RULE_shiftExpression = 25;
   public static final int RULE_additiveExpression = 26;
   public static final int RULE_multiplicativeExpression = 27;
   public static final int RULE_powerExpression = 28;
   public static final int RULE_unaryExpression = 29;
   public static final int RULE_postfixExpression = 30;
   public static final int RULE_unprioritizedExpression = 31;
   public static final int RULE_constantExpression = 32;
   public static final int RULE_functionCall = 33;
   public static final String[] ruleNames = makeRuleNames();
   private static final String[] _LITERAL_NAMES = makeLiteralNames();
   private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
   public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
   @Deprecated
   public static final String[] tokenNames = new String[_SYMBOLIC_NAMES.length];
   public static final String _serializedATN = "\u0004\u00012ŉ\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001I\b\u0001\u000b\u0001\f\u0001J\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002Y\b\u0002\u0001\u0002\u0003\u0002\\\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004i\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0004\f\u009c\b\f\u000b\f\f\f\u009d\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0003\r¥\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0003\u0011¯\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014¾\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015Æ\b\u0015\n\u0015\f\u0015É\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016Ñ\b\u0016\n\u0016\f\u0016Ô\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017Ü\b\u0017\n\u0017\f\u0017ß\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018ç\b\u0018\n\u0018\f\u0018ê\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019ò\b\u0019\n\u0019\f\u0019õ\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001aý\b\u001a\n\u001a\f\u001aĀ\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001bĈ\b\u001b\n\u001b\f\u001bċ\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001cē\b\u001c\n\u001c\f\u001cĖ\t\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001dġ\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001eħ\b\u001e\u0001\u001e\u0001\u001e\u0005\u001eī\b\u001e\n\u001e\f\u001eĮ\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001fķ\b\u001f\u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0001!\u0005!ŀ\b!\n!\f!Ń\t!\u0003!Ņ\b!\u0001!\u0001!\u0001!\u0000\t*,.02468<\"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B\u0000\u0007\u0002\u0000\t\t\u000b\u0010\u0001\u0000\u0011\u0013\u0001\u0000\u0014\u0017\u0001\u0000\u0007\b\u0001\u0000\u0001\u0002\u0001\u0000\u0003\u0005\u0001\u0000\u001a\u001bŋ\u0000D\u0001\u0000\u0000\u0000\u0002H\u0001\u0000\u0000\u0000\u0004X\u0001\u0000\u0000\u0000\u0006]\u0001\u0000\u0000\u0000\ba\u0001\u0000\u0000\u0000\nj\u0001\u0000\u0000\u0000\fp\u0001\u0000\u0000\u0000\u000ew\u0001\u0000\u0000\u0000\u0010\u0081\u0001\u0000\u0000\u0000\u0012\u008b\u0001\u0000\u0000\u0000\u0014\u008d\u0001\u0000\u0000\u0000\u0016\u008f\u0001\u0000\u0000\u0000\u0018\u0092\u0001\u0000\u0000\u0000\u001a¤\u0001\u0000\u0000\u0000\u001c¦\u0001\u0000\u0000\u0000\u001e¨\u0001\u0000\u0000\u0000 ª\u0001\u0000\u0000\u0000\"®\u0001\u0000\u0000\u0000$°\u0001\u0000\u0000\u0000&´\u0001\u0000\u0000\u0000(½\u0001\u0000\u0000\u0000*¿\u0001\u0000\u0000\u0000,Ê\u0001\u0000\u0000\u0000.Õ\u0001\u0000\u0000\u00000à\u0001\u0000\u0000\u00002ë\u0001\u0000\u0000\u00004ö\u0001\u0000\u0000\u00006ā\u0001\u0000\u0000\u00008Č\u0001\u0000\u0000\u0000:Ġ\u0001\u0000\u0000\u0000<Ħ\u0001\u0000\u0000\u0000>Ķ\u0001\u0000\u0000\u0000@ĸ\u0001\u0000\u0000\u0000Bĺ\u0001\u0000\u0000\u0000DE\u0003\u0002\u0001\u0000EF\u0005\u0000\u0000\u0001F\u0001\u0001\u0000\u0000\u0000GI\u0003\u0004\u0002\u0000HG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000K\u0003\u0001\u0000\u0000\u0000LY\u0003\u0006\u0003\u0000MY\u0003\b\u0004\u0000NY\u0003\n\u0005\u0000OY\u0003\f\u0006\u0000PY\u0003\u000e\u0007\u0000QY\u0003\u0010\b\u0000RY\u0003\u0012\t\u0000SY\u0003\u0014\n\u0000TY\u0003\u0016\u000b\u0000UY\u0003\u0018\f\u0000VY\u0003\u001c\u000e\u0000WY\u0003\u001e\u000f\u0000XL\u0001\u0000\u0000\u0000XM\u0001\u0000\u0000\u0000XN\u0001\u0000\u0000\u0000XO\u0001\u0000\u0000\u0000XP\u0001\u0000\u0000\u0000XQ\u0001\u0000\u0000\u0000XR\u0001\u0000\u0000\u0000XS\u0001\u0000\u0000\u0000XT\u0001\u0000\u0000\u0000XU\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000Z\\\u0005!\u0000\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\\u0005\u0001\u0000\u0000\u0000]^\u0005\u001f\u0000\u0000^_\u0003\u0002\u0001\u0000_`\u0005 \u0000\u0000`\u0007\u0001\u0000\u0000\u0000ab\u0005%\u0000\u0000bc\u0005\u001d\u0000\u0000cd\u0003 \u0010\u0000de\u0005\u001e\u0000\u0000eh\u0003\u0004\u0002\u0000fg\u0005&\u0000\u0000gi\u0003\u0004\u0002\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\t\u0001\u0000\u0000\u0000jk\u0005'\u0000\u0000kl\u0005\u001d\u0000\u0000lm\u0003 \u0010\u0000mn\u0005\u001e\u0000\u0000no\u0003\u0004\u0002\u0000o\u000b\u0001\u0000\u0000\u0000pq\u0005(\u0000\u0000qr\u0003\u0004\u0002\u0000rs\u0005'\u0000\u0000st\u0005\u001d\u0000\u0000tu\u0003 \u0010\u0000uv\u0005\u001e\u0000\u0000v\r\u0001\u0000\u0000\u0000wx\u0005)\u0000\u0000xy\u0005\u001d\u0000\u0000yz\u0003 \u0010\u0000z{\u0005!\u0000\u0000{|\u0003 \u0010\u0000|}\u0005!\u0000\u0000}~\u0003 \u0010\u0000~\u007f\u0005\u001e\u0000\u0000\u007f\u0080\u0003\u0004\u0002\u0000\u0080\u000f\u0001\u0000\u0000\u0000\u0081\u0082\u0005)\u0000\u0000\u0082\u0083\u0005\u001d\u0000\u0000\u0083\u0084\u00051\u0000\u0000\u0084\u0085\u0005\t\u0000\u0000\u0085\u0086\u0003 \u0010\u0000\u0086\u0087\u0005\u001c\u0000\u0000\u0087\u0088\u0003 \u0010\u0000\u0088\u0089\u0005\u001e\u0000\u0000\u0089\u008a\u0003\u0004\u0002\u0000\u008a\u0011\u0001\u0000\u0000\u0000\u008b\u008c\u0005*\u0000\u0000\u008c\u0013\u0001\u0000\u0000\u0000\u008d\u008e\u0005+\u0000\u0000\u008e\u0015\u0001\u0000\u0000\u0000\u008f\u0090\u0005,\u0000\u0000\u0090\u0091\u0003 \u0010\u0000\u0091\u0017\u0001\u0000\u0000\u0000\u0092\u0093\u0005-\u0000\u0000\u0093\u0094\u0005\u001d\u0000\u0000\u0094\u0095\u0003 \u0010\u0000\u0095\u0096\u0005\u001e\u0000\u0000\u0096\u009b\u0005\u001f\u0000\u0000\u0097\u0098\u0003\u001a\r\u0000\u0098\u0099\u0005#\u0000\u0000\u0099\u009a\u0003\u0002\u0001\u0000\u009a\u009c\u0001\u0000\u0000\u0000\u009b\u0097\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f \u0005 \u0000\u0000 \u0019\u0001\u0000\u0000\u0000¡¢\u0005.\u0000\u0000¢¥\u0003@ \u0000£¥\u0005/\u0000\u0000¤¡\u0001\u0000\u0000\u0000¤£\u0001\u0000\u0000\u0000¥\u001b\u0001\u0000\u0000\u0000¦§\u0003 \u0010\u0000§\u001d\u0001\u0000\u0000\u0000¨©\u0005!\u0000\u0000©\u001f\u0001\u0000\u0000\u0000ª«\u0003\"\u0011\u0000«!\u0001\u0000\u0000\u0000¬¯\u0003(\u0014\u0000\u00ad¯\u0003$\u0012\u0000®¬\u0001\u0000\u0000\u0000®\u00ad\u0001\u0000\u0000\u0000¯#\u0001\u0000\u0000\u0000°±\u00051\u0000\u0000±²\u0003&\u0013\u0000²³\u0003 \u0010\u0000³%\u0001\u0000\u0000\u0000´µ\u0007\u0000\u0000\u0000µ'\u0001\u0000\u0000\u0000¶¾\u0003*\u0015\u0000·¸\u0003*\u0015\u0000¸¹\u0005\"\u0000\u0000¹º\u0003 \u0010\u0000º»\u0005#\u0000\u0000»¼\u0003(\u0014\u0000¼¾\u0001\u0000\u0000\u0000½¶\u0001\u0000\u0000\u0000½·\u0001\u0000\u0000\u0000¾)\u0001\u0000\u0000\u0000¿À\u0006\u0015\uffff\uffff\u0000ÀÁ\u0003,\u0016\u0000ÁÇ\u0001\u0000\u0000\u0000ÂÃ\n\u0001\u0000\u0000ÃÄ\u0005\u0019\u0000\u0000ÄÆ\u0003,\u0016\u0000ÅÂ\u0001\u0000\u0000\u0000ÆÉ\u0001\u0000\u0000\u0000ÇÅ\u0001\u0000\u0000\u0000ÇÈ\u0001\u0000\u0000\u0000È+\u0001\u0000\u0000\u0000ÉÇ\u0001\u0000\u0000\u0000ÊË\u0006\u0016\uffff\uffff\u0000ËÌ\u0003.\u0017\u0000ÌÒ\u0001\u0000\u0000\u0000ÍÎ\n\u0001\u0000\u0000ÎÏ\u0005\u0018\u0000\u0000ÏÑ\u0003.\u0017\u0000ÐÍ\u0001\u0000\u0000\u0000ÑÔ\u0001\u0000\u0000\u0000ÒÐ\u0001\u0000\u0000\u0000ÒÓ\u0001\u0000\u0000\u0000Ó-\u0001\u0000\u0000\u0000ÔÒ\u0001\u0000\u0000\u0000ÕÖ\u0006\u0017\uffff\uffff\u0000Ö×\u00030\u0018\u0000×Ý\u0001\u0000\u0000\u0000ØÙ\n\u0001\u0000\u0000ÙÚ\u0007\u0001\u0000\u0000ÚÜ\u00030\u0018\u0000ÛØ\u0001\u0000\u0000\u0000Üß\u0001\u0000\u0000\u0000ÝÛ\u0001\u0000\u0000\u0000ÝÞ\u0001\u0000\u0000\u0000Þ/\u0001\u0000\u0000\u0000ßÝ\u0001\u0000\u0000\u0000àá\u0006\u0018\uffff\uffff\u0000áâ\u00032\u0019\u0000âè\u0001\u0000\u0000\u0000ãä\n\u0001\u0000\u0000äå\u0007\u0002\u0000\u0000åç\u00032\u0019\u0000æã\u0001\u0000\u0000\u0000çê\u0001\u0000\u0000\u0000èæ\u0001\u0000\u0000\u0000èé\u0001\u0000\u0000\u0000é1\u0001\u0000\u0000\u0000êè\u0001\u0000\u0000\u0000ëì\u0006\u0019\uffff\uffff\u0000ìí\u00034\u001a\u0000íó\u0001\u0000\u0000\u0000îï\n\u0001\u0000\u0000ïð\u0007\u0003\u0000\u0000ðò\u00034\u001a\u0000ñî\u0001\u0000\u0000\u0000òõ\u0001\u0000\u0000\u0000óñ\u0001\u0000\u0000\u0000óô\u0001\u0000\u0000\u0000ô3\u0001\u0000\u0000\u0000õó\u0001\u0000\u0000\u0000ö÷\u0006\u001a\uffff\uffff\u0000÷ø\u00036\u001b\u0000øþ\u0001\u0000\u0000\u0000ùú\n\u0001\u0000\u0000úû\u0007\u0004\u0000\u0000ûý\u00036\u001b\u0000üù\u0001\u0000\u0000\u0000ýĀ\u0001\u0000\u0000\u0000þü\u0001\u0000\u0000\u0000þÿ\u0001\u0000\u0000\u0000ÿ5\u0001\u0000\u0000\u0000Āþ\u0001\u0000\u0000\u0000āĂ\u0006\u001b\uffff\uffff\u0000Ăă\u00038\u001c\u0000ăĉ\u0001\u0000\u0000\u0000Ąą\n\u0001\u0000\u0000ąĆ\u0007\u0005\u0000\u0000ĆĈ\u00038\u001c\u0000ćĄ\u0001\u0000\u0000\u0000Ĉċ\u0001\u0000\u0000\u0000ĉć\u0001\u0000\u0000\u0000ĉĊ\u0001\u0000\u0000\u0000Ċ7\u0001\u0000\u0000\u0000ċĉ\u0001\u0000\u0000\u0000Čč\u0006\u001c\uffff\uffff\u0000čĎ\u0003:\u001d\u0000ĎĔ\u0001\u0000\u0000\u0000ďĐ\n\u0001\u0000\u0000Đđ\u0005\u0006\u0000\u0000đē\u0003:\u001d\u0000Ēď\u0001\u0000\u0000\u0000ēĖ\u0001\u0000\u0000\u0000ĔĒ\u0001\u0000\u0000\u0000Ĕĕ\u0001\u0000\u0000\u0000ĕ9\u0001\u0000\u0000\u0000ĖĔ\u0001\u0000\u0000\u0000ėĘ\u0007\u0006\u0000\u0000Ęġ\u00051\u0000\u0000ęĚ\u0007\u0004\u0000\u0000Ěġ\u0003:\u001d\u0000ěġ\u0003<\u001e\u0000Ĝĝ\u0005\n\u0000\u0000ĝġ\u0003:\u001d\u0000Ğğ\u0005$\u0000\u0000ğġ\u0003:\u001d\u0000Ġė\u0001\u0000\u0000\u0000Ġę\u0001\u0000\u0000\u0000Ġě\u0001\u0000\u0000\u0000ĠĜ\u0001\u0000\u0000\u0000ĠĞ\u0001\u0000\u0000\u0000ġ;\u0001\u0000\u0000\u0000Ģģ\u0006\u001e\uffff\uffff\u0000ģħ\u0003>\u001f\u0000Ĥĥ\u00051\u0000\u0000ĥħ\u0007\u0006\u0000\u0000ĦĢ\u0001\u0000\u0000\u0000ĦĤ\u0001\u0000\u0000\u0000ħĬ\u0001\u0000\u0000\u0000Ĩĩ\n\u0001\u0000\u0000ĩī\u0005$\u0000\u0000ĪĨ\u0001\u0000\u0000\u0000īĮ\u0001\u0000\u0000\u0000ĬĪ\u0001\u0000\u0000\u0000Ĭĭ\u0001\u0000\u0000\u0000ĭ=\u0001\u0000\u0000\u0000ĮĬ\u0001\u0000\u0000\u0000įķ\u0003B!\u0000İķ\u0003@ \u0000ıķ\u00051\u0000\u0000Ĳĳ\u0005\u001d\u0000\u0000ĳĴ\u0003 \u0010\u0000Ĵĵ\u0005\u001e\u0000\u0000ĵķ\u0001\u0000\u0000\u0000Ķį\u0001\u0000\u0000\u0000Ķİ\u0001\u0000\u0000\u0000Ķı\u0001\u0000\u0000\u0000ĶĲ\u0001\u0000\u0000\u0000ķ?\u0001\u0000\u0000\u0000ĸĹ\u00050\u0000\u0000ĹA\u0001\u0000\u0000\u0000ĺĻ\u00051\u0000\u0000Ļń\u0005\u001d\u0000\u0000ļŁ\u0003 \u0010\u0000Ľľ\u0005\u001c\u0000\u0000ľŀ\u0003 \u0010\u0000ĿĽ\u0001\u0000\u0000\u0000ŀŃ\u0001\u0000\u0000\u0000ŁĿ\u0001\u0000\u0000\u0000Łł\u0001\u0000\u0000\u0000łŅ\u0001\u0000\u0000\u0000ŃŁ\u0001\u0000\u0000\u0000ńļ\u0001\u0000\u0000\u0000ńŅ\u0001\u0000\u0000\u0000Ņņ\u0001\u0000\u0000\u0000ņŇ\u0005\u001e\u0000\u0000ŇC\u0001\u0000\u0000\u0000\u0016JX[h\u009d¤®½ÇÒÝèóþĉĔĠĦĬĶŁń";
   public static final ATN _ATN;

   private static String[] makeRuleNames() {
      return new String[]{
         "allStatements",
         "statements",
         "statement",
         "block",
         "ifStatement",
         "whileStatement",
         "doStatement",
         "forStatement",
         "simpleForStatement",
         "breakStatement",
         "continueStatement",
         "returnStatement",
         "switchStatement",
         "switchLabel",
         "expressionStatement",
         "emptyStatement",
         "expression",
         "assignmentExpression",
         "assignment",
         "assignmentOperator",
         "conditionalExpression",
         "conditionalOrExpression",
         "conditionalAndExpression",
         "equalityExpression",
         "relationalExpression",
         "shiftExpression",
         "additiveExpression",
         "multiplicativeExpression",
         "powerExpression",
         "unaryExpression",
         "postfixExpression",
         "unprioritizedExpression",
         "constantExpression",
         "functionCall"
      };
   }

   private static String[] makeLiteralNames() {
      return new String[]{
         null,
         "'+'",
         "'-'",
         "'*'",
         "'/'",
         "'%'",
         null,
         "'<<'",
         "'>>'",
         "'='",
         "'~'",
         "'+='",
         "'-='",
         "'*='",
         "'/='",
         "'%='",
         "'^='",
         "'=='",
         "'!='",
         "'~='",
         "'<'",
         "'<='",
         "'>'",
         "'>='",
         "'&&'",
         "'||'",
         "'++'",
         "'--'",
         "','",
         "'('",
         "')'",
         "'{'",
         "'}'",
         "';'",
         "'?'",
         "':'",
         "'!'",
         "'if'",
         "'else'",
         "'while'",
         "'do'",
         "'for'",
         "'break'",
         "'continue'",
         "'return'",
         "'switch'",
         "'case'",
         "'default'"
      };
   }

   private static String[] makeSymbolicNames() {
      return new String[]{
         null,
         "PLUS",
         "MINUS",
         "TIMES",
         "DIVIDE",
         "MODULO",
         "POWER",
         "LEFT_SHIFT",
         "RIGHT_SHIFT",
         "ASSIGN",
         "COMPLEMENT",
         "PLUS_ASSIGN",
         "MINUS_ASSIGN",
         "TIMES_ASSIGN",
         "DIVIDE_ASSIGN",
         "MODULO_ASSIGN",
         "POWER_ASSIGN",
         "EQUAL",
         "NOT_EQUAL",
         "NEAR",
         "LESS_THAN",
         "LESS_THAN_OR_EQUAL",
         "GREATER_THAN",
         "GREATER_THAN_OR_EQUAL",
         "AND_SC",
         "OR_SC",
         "INCREMENT",
         "DECREMENT",
         "COMMA",
         "OPEN_PAREN",
         "CLOSE_PAREN",
         "OPEN_BRACKET",
         "CLOSE_BRACKET",
         "SEMI_COLON",
         "QUESTION_MARK",
         "COLON",
         "EXCLAMATION_MARK",
         "IF",
         "ELSE",
         "WHILE",
         "DO",
         "FOR",
         "BREAK",
         "CONTINUE",
         "RETURN",
         "SWITCH",
         "CASE",
         "DEFAULT",
         "NUMBER",
         "ID",
         "WS"
      };
   }

   @Deprecated
   @Override
   public String[] getTokenNames() {
      return tokenNames;
   }

   @Override
   public Vocabulary getVocabulary() {
      return VOCABULARY;
   }

   @Override
   public String getGrammarFileName() {
      return "Expression.g4";
   }

   @Override
   public String[] getRuleNames() {
      return ruleNames;
   }

   @Override
   public String getSerializedATN() {
      return "\u0004\u00012ŉ\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001I\b\u0001\u000b\u0001\f\u0001J\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002Y\b\u0002\u0001\u0002\u0003\u0002\\\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004i\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0004\f\u009c\b\f\u000b\f\f\f\u009d\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0003\r¥\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0003\u0011¯\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014¾\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015Æ\b\u0015\n\u0015\f\u0015É\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016Ñ\b\u0016\n\u0016\f\u0016Ô\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017Ü\b\u0017\n\u0017\f\u0017ß\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018ç\b\u0018\n\u0018\f\u0018ê\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019ò\b\u0019\n\u0019\f\u0019õ\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001aý\b\u001a\n\u001a\f\u001aĀ\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001bĈ\b\u001b\n\u001b\f\u001bċ\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001cē\b\u001c\n\u001c\f\u001cĖ\t\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001dġ\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001eħ\b\u001e\u0001\u001e\u0001\u001e\u0005\u001eī\b\u001e\n\u001e\f\u001eĮ\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001fķ\b\u001f\u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0001!\u0005!ŀ\b!\n!\f!Ń\t!\u0003!Ņ\b!\u0001!\u0001!\u0001!\u0000\t*,.02468<\"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B\u0000\u0007\u0002\u0000\t\t\u000b\u0010\u0001\u0000\u0011\u0013\u0001\u0000\u0014\u0017\u0001\u0000\u0007\b\u0001\u0000\u0001\u0002\u0001\u0000\u0003\u0005\u0001\u0000\u001a\u001bŋ\u0000D\u0001\u0000\u0000\u0000\u0002H\u0001\u0000\u0000\u0000\u0004X\u0001\u0000\u0000\u0000\u0006]\u0001\u0000\u0000\u0000\ba\u0001\u0000\u0000\u0000\nj\u0001\u0000\u0000\u0000\fp\u0001\u0000\u0000\u0000\u000ew\u0001\u0000\u0000\u0000\u0010\u0081\u0001\u0000\u0000\u0000\u0012\u008b\u0001\u0000\u0000\u0000\u0014\u008d\u0001\u0000\u0000\u0000\u0016\u008f\u0001\u0000\u0000\u0000\u0018\u0092\u0001\u0000\u0000\u0000\u001a¤\u0001\u0000\u0000\u0000\u001c¦\u0001\u0000\u0000\u0000\u001e¨\u0001\u0000\u0000\u0000 ª\u0001\u0000\u0000\u0000\"®\u0001\u0000\u0000\u0000$°\u0001\u0000\u0000\u0000&´\u0001\u0000\u0000\u0000(½\u0001\u0000\u0000\u0000*¿\u0001\u0000\u0000\u0000,Ê\u0001\u0000\u0000\u0000.Õ\u0001\u0000\u0000\u00000à\u0001\u0000\u0000\u00002ë\u0001\u0000\u0000\u00004ö\u0001\u0000\u0000\u00006ā\u0001\u0000\u0000\u00008Č\u0001\u0000\u0000\u0000:Ġ\u0001\u0000\u0000\u0000<Ħ\u0001\u0000\u0000\u0000>Ķ\u0001\u0000\u0000\u0000@ĸ\u0001\u0000\u0000\u0000Bĺ\u0001\u0000\u0000\u0000DE\u0003\u0002\u0001\u0000EF\u0005\u0000\u0000\u0001F\u0001\u0001\u0000\u0000\u0000GI\u0003\u0004\u0002\u0000HG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000K\u0003\u0001\u0000\u0000\u0000LY\u0003\u0006\u0003\u0000MY\u0003\b\u0004\u0000NY\u0003\n\u0005\u0000OY\u0003\f\u0006\u0000PY\u0003\u000e\u0007\u0000QY\u0003\u0010\b\u0000RY\u0003\u0012\t\u0000SY\u0003\u0014\n\u0000TY\u0003\u0016\u000b\u0000UY\u0003\u0018\f\u0000VY\u0003\u001c\u000e\u0000WY\u0003\u001e\u000f\u0000XL\u0001\u0000\u0000\u0000XM\u0001\u0000\u0000\u0000XN\u0001\u0000\u0000\u0000XO\u0001\u0000\u0000\u0000XP\u0001\u0000\u0000\u0000XQ\u0001\u0000\u0000\u0000XR\u0001\u0000\u0000\u0000XS\u0001\u0000\u0000\u0000XT\u0001\u0000\u0000\u0000XU\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000Z\\\u0005!\u0000\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\\u0005\u0001\u0000\u0000\u0000]^\u0005\u001f\u0000\u0000^_\u0003\u0002\u0001\u0000_`\u0005 \u0000\u0000`\u0007\u0001\u0000\u0000\u0000ab\u0005%\u0000\u0000bc\u0005\u001d\u0000\u0000cd\u0003 \u0010\u0000de\u0005\u001e\u0000\u0000eh\u0003\u0004\u0002\u0000fg\u0005&\u0000\u0000gi\u0003\u0004\u0002\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\t\u0001\u0000\u0000\u0000jk\u0005'\u0000\u0000kl\u0005\u001d\u0000\u0000lm\u0003 \u0010\u0000mn\u0005\u001e\u0000\u0000no\u0003\u0004\u0002\u0000o\u000b\u0001\u0000\u0000\u0000pq\u0005(\u0000\u0000qr\u0003\u0004\u0002\u0000rs\u0005'\u0000\u0000st\u0005\u001d\u0000\u0000tu\u0003 \u0010\u0000uv\u0005\u001e\u0000\u0000v\r\u0001\u0000\u0000\u0000wx\u0005)\u0000\u0000xy\u0005\u001d\u0000\u0000yz\u0003 \u0010\u0000z{\u0005!\u0000\u0000{|\u0003 \u0010\u0000|}\u0005!\u0000\u0000}~\u0003 \u0010\u0000~\u007f\u0005\u001e\u0000\u0000\u007f\u0080\u0003\u0004\u0002\u0000\u0080\u000f\u0001\u0000\u0000\u0000\u0081\u0082\u0005)\u0000\u0000\u0082\u0083\u0005\u001d\u0000\u0000\u0083\u0084\u00051\u0000\u0000\u0084\u0085\u0005\t\u0000\u0000\u0085\u0086\u0003 \u0010\u0000\u0086\u0087\u0005\u001c\u0000\u0000\u0087\u0088\u0003 \u0010\u0000\u0088\u0089\u0005\u001e\u0000\u0000\u0089\u008a\u0003\u0004\u0002\u0000\u008a\u0011\u0001\u0000\u0000\u0000\u008b\u008c\u0005*\u0000\u0000\u008c\u0013\u0001\u0000\u0000\u0000\u008d\u008e\u0005+\u0000\u0000\u008e\u0015\u0001\u0000\u0000\u0000\u008f\u0090\u0005,\u0000\u0000\u0090\u0091\u0003 \u0010\u0000\u0091\u0017\u0001\u0000\u0000\u0000\u0092\u0093\u0005-\u0000\u0000\u0093\u0094\u0005\u001d\u0000\u0000\u0094\u0095\u0003 \u0010\u0000\u0095\u0096\u0005\u001e\u0000\u0000\u0096\u009b\u0005\u001f\u0000\u0000\u0097\u0098\u0003\u001a\r\u0000\u0098\u0099\u0005#\u0000\u0000\u0099\u009a\u0003\u0002\u0001\u0000\u009a\u009c\u0001\u0000\u0000\u0000\u009b\u0097\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f \u0005 \u0000\u0000 \u0019\u0001\u0000\u0000\u0000¡¢\u0005.\u0000\u0000¢¥\u0003@ \u0000£¥\u0005/\u0000\u0000¤¡\u0001\u0000\u0000\u0000¤£\u0001\u0000\u0000\u0000¥\u001b\u0001\u0000\u0000\u0000¦§\u0003 \u0010\u0000§\u001d\u0001\u0000\u0000\u0000¨©\u0005!\u0000\u0000©\u001f\u0001\u0000\u0000\u0000ª«\u0003\"\u0011\u0000«!\u0001\u0000\u0000\u0000¬¯\u0003(\u0014\u0000\u00ad¯\u0003$\u0012\u0000®¬\u0001\u0000\u0000\u0000®\u00ad\u0001\u0000\u0000\u0000¯#\u0001\u0000\u0000\u0000°±\u00051\u0000\u0000±²\u0003&\u0013\u0000²³\u0003 \u0010\u0000³%\u0001\u0000\u0000\u0000´µ\u0007\u0000\u0000\u0000µ'\u0001\u0000\u0000\u0000¶¾\u0003*\u0015\u0000·¸\u0003*\u0015\u0000¸¹\u0005\"\u0000\u0000¹º\u0003 \u0010\u0000º»\u0005#\u0000\u0000»¼\u0003(\u0014\u0000¼¾\u0001\u0000\u0000\u0000½¶\u0001\u0000\u0000\u0000½·\u0001\u0000\u0000\u0000¾)\u0001\u0000\u0000\u0000¿À\u0006\u0015\uffff\uffff\u0000ÀÁ\u0003,\u0016\u0000ÁÇ\u0001\u0000\u0000\u0000ÂÃ\n\u0001\u0000\u0000ÃÄ\u0005\u0019\u0000\u0000ÄÆ\u0003,\u0016\u0000ÅÂ\u0001\u0000\u0000\u0000ÆÉ\u0001\u0000\u0000\u0000ÇÅ\u0001\u0000\u0000\u0000ÇÈ\u0001\u0000\u0000\u0000È+\u0001\u0000\u0000\u0000ÉÇ\u0001\u0000\u0000\u0000ÊË\u0006\u0016\uffff\uffff\u0000ËÌ\u0003.\u0017\u0000ÌÒ\u0001\u0000\u0000\u0000ÍÎ\n\u0001\u0000\u0000ÎÏ\u0005\u0018\u0000\u0000ÏÑ\u0003.\u0017\u0000ÐÍ\u0001\u0000\u0000\u0000ÑÔ\u0001\u0000\u0000\u0000ÒÐ\u0001\u0000\u0000\u0000ÒÓ\u0001\u0000\u0000\u0000Ó-\u0001\u0000\u0000\u0000ÔÒ\u0001\u0000\u0000\u0000ÕÖ\u0006\u0017\uffff\uffff\u0000Ö×\u00030\u0018\u0000×Ý\u0001\u0000\u0000\u0000ØÙ\n\u0001\u0000\u0000ÙÚ\u0007\u0001\u0000\u0000ÚÜ\u00030\u0018\u0000ÛØ\u0001\u0000\u0000\u0000Üß\u0001\u0000\u0000\u0000ÝÛ\u0001\u0000\u0000\u0000ÝÞ\u0001\u0000\u0000\u0000Þ/\u0001\u0000\u0000\u0000ßÝ\u0001\u0000\u0000\u0000àá\u0006\u0018\uffff\uffff\u0000áâ\u00032\u0019\u0000âè\u0001\u0000\u0000\u0000ãä\n\u0001\u0000\u0000äå\u0007\u0002\u0000\u0000åç\u00032\u0019\u0000æã\u0001\u0000\u0000\u0000çê\u0001\u0000\u0000\u0000èæ\u0001\u0000\u0000\u0000èé\u0001\u0000\u0000\u0000é1\u0001\u0000\u0000\u0000êè\u0001\u0000\u0000\u0000ëì\u0006\u0019\uffff\uffff\u0000ìí\u00034\u001a\u0000íó\u0001\u0000\u0000\u0000îï\n\u0001\u0000\u0000ïð\u0007\u0003\u0000\u0000ðò\u00034\u001a\u0000ñî\u0001\u0000\u0000\u0000òõ\u0001\u0000\u0000\u0000óñ\u0001\u0000\u0000\u0000óô\u0001\u0000\u0000\u0000ô3\u0001\u0000\u0000\u0000õó\u0001\u0000\u0000\u0000ö÷\u0006\u001a\uffff\uffff\u0000÷ø\u00036\u001b\u0000øþ\u0001\u0000\u0000\u0000ùú\n\u0001\u0000\u0000úû\u0007\u0004\u0000\u0000ûý\u00036\u001b\u0000üù\u0001\u0000\u0000\u0000ýĀ\u0001\u0000\u0000\u0000þü\u0001\u0000\u0000\u0000þÿ\u0001\u0000\u0000\u0000ÿ5\u0001\u0000\u0000\u0000Āþ\u0001\u0000\u0000\u0000āĂ\u0006\u001b\uffff\uffff\u0000Ăă\u00038\u001c\u0000ăĉ\u0001\u0000\u0000\u0000Ąą\n\u0001\u0000\u0000ąĆ\u0007\u0005\u0000\u0000ĆĈ\u00038\u001c\u0000ćĄ\u0001\u0000\u0000\u0000Ĉċ\u0001\u0000\u0000\u0000ĉć\u0001\u0000\u0000\u0000ĉĊ\u0001\u0000\u0000\u0000Ċ7\u0001\u0000\u0000\u0000ċĉ\u0001\u0000\u0000\u0000Čč\u0006\u001c\uffff\uffff\u0000čĎ\u0003:\u001d\u0000ĎĔ\u0001\u0000\u0000\u0000ďĐ\n\u0001\u0000\u0000Đđ\u0005\u0006\u0000\u0000đē\u0003:\u001d\u0000Ēď\u0001\u0000\u0000\u0000ēĖ\u0001\u0000\u0000\u0000ĔĒ\u0001\u0000\u0000\u0000Ĕĕ\u0001\u0000\u0000\u0000ĕ9\u0001\u0000\u0000\u0000ĖĔ\u0001\u0000\u0000\u0000ėĘ\u0007\u0006\u0000\u0000Ęġ\u00051\u0000\u0000ęĚ\u0007\u0004\u0000\u0000Ěġ\u0003:\u001d\u0000ěġ\u0003<\u001e\u0000Ĝĝ\u0005\n\u0000\u0000ĝġ\u0003:\u001d\u0000Ğğ\u0005$\u0000\u0000ğġ\u0003:\u001d\u0000Ġė\u0001\u0000\u0000\u0000Ġę\u0001\u0000\u0000\u0000Ġě\u0001\u0000\u0000\u0000ĠĜ\u0001\u0000\u0000\u0000ĠĞ\u0001\u0000\u0000\u0000ġ;\u0001\u0000\u0000\u0000Ģģ\u0006\u001e\uffff\uffff\u0000ģħ\u0003>\u001f\u0000Ĥĥ\u00051\u0000\u0000ĥħ\u0007\u0006\u0000\u0000ĦĢ\u0001\u0000\u0000\u0000ĦĤ\u0001\u0000\u0000\u0000ħĬ\u0001\u0000\u0000\u0000Ĩĩ\n\u0001\u0000\u0000ĩī\u0005$\u0000\u0000ĪĨ\u0001\u0000\u0000\u0000īĮ\u0001\u0000\u0000\u0000ĬĪ\u0001\u0000\u0000\u0000Ĭĭ\u0001\u0000\u0000\u0000ĭ=\u0001\u0000\u0000\u0000ĮĬ\u0001\u0000\u0000\u0000įķ\u0003B!\u0000İķ\u0003@ \u0000ıķ\u00051\u0000\u0000Ĳĳ\u0005\u001d\u0000\u0000ĳĴ\u0003 \u0010\u0000Ĵĵ\u0005\u001e\u0000\u0000ĵķ\u0001\u0000\u0000\u0000Ķį\u0001\u0000\u0000\u0000Ķİ\u0001\u0000\u0000\u0000Ķı\u0001\u0000\u0000\u0000ĶĲ\u0001\u0000\u0000\u0000ķ?\u0001\u0000\u0000\u0000ĸĹ\u00050\u0000\u0000ĹA\u0001\u0000\u0000\u0000ĺĻ\u00051\u0000\u0000Ļń\u0005\u001d\u0000\u0000ļŁ\u0003 \u0010\u0000Ľľ\u0005\u001c\u0000\u0000ľŀ\u0003 \u0010\u0000ĿĽ\u0001\u0000\u0000\u0000ŀŃ\u0001\u0000\u0000\u0000ŁĿ\u0001\u0000\u0000\u0000Łł\u0001\u0000\u0000\u0000łŅ\u0001\u0000\u0000\u0000ŃŁ\u0001\u0000\u0000\u0000ńļ\u0001\u0000\u0000\u0000ńŅ\u0001\u0000\u0000\u0000Ņņ\u0001\u0000\u0000\u0000ņŇ\u0005\u001e\u0000\u0000ŇC\u0001\u0000\u0000\u0000\u0016JX[h\u009d¤®½ÇÒÝèóþĉĔĠĦĬĶŁń";
   }

   @Override
   public ATN getATN() {
      return _ATN;
   }

   public ExpressionParser(TokenStream input) {
      super(input);
      this._interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
   }

   public final ExpressionParser.AllStatementsContext allStatements() throws RecognitionException {
      ExpressionParser.AllStatementsContext _localctx = new ExpressionParser.AllStatementsContext(this._ctx, this.getState());
      this.enterRule(_localctx, 0, 0);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(68);
         this.statements();
         this.setState(69);
         this.match(-1);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.StatementsContext statements() throws RecognitionException {
      ExpressionParser.StatementsContext _localctx = new ExpressionParser.StatementsContext(this._ctx, this.getState());
      this.enterRule(_localctx, 2, 1);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(72);
         this._errHandler.sync(this);
         int _la = this._input.LA(1);

         do {
            this.setState(71);
            this.statement();
            this.setState(74);
            this._errHandler.sync(this);
            _la = this._input.LA(1);
         } while ((_la & -64) == 0 && (1L << _la & 914461552542726L) != 0L);
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.StatementContext statement() throws RecognitionException {
      ExpressionParser.StatementContext _localctx = new ExpressionParser.StatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 4, 2);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(88);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 1, this._ctx)) {
            case 1:
               this.setState(76);
               this.block();
               break;
            case 2:
               this.setState(77);
               this.ifStatement();
               break;
            case 3:
               this.setState(78);
               this.whileStatement();
               break;
            case 4:
               this.setState(79);
               this.doStatement();
               break;
            case 5:
               this.setState(80);
               this.forStatement();
               break;
            case 6:
               this.setState(81);
               this.simpleForStatement();
               break;
            case 7:
               this.setState(82);
               this.breakStatement();
               break;
            case 8:
               this.setState(83);
               this.continueStatement();
               break;
            case 9:
               this.setState(84);
               this.returnStatement();
               break;
            case 10:
               this.setState(85);
               this.switchStatement();
               break;
            case 11:
               this.setState(86);
               this.expressionStatement();
               break;
            case 12:
               this.setState(87);
               this.emptyStatement();
         }

         this.setState(91);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 2, this._ctx)) {
            case 1:
               this.setState(90);
               this.match(33);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.BlockContext block() throws RecognitionException {
      ExpressionParser.BlockContext _localctx = new ExpressionParser.BlockContext(this._ctx, this.getState());
      this.enterRule(_localctx, 6, 3);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(93);
         this.match(31);
         this.setState(94);
         this.statements();
         this.setState(95);
         this.match(32);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.IfStatementContext ifStatement() throws RecognitionException {
      ExpressionParser.IfStatementContext _localctx = new ExpressionParser.IfStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 8, 4);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(97);
         this.match(37);
         this.setState(98);
         this.match(29);
         this.setState(99);
         _localctx.condition = this.expression();
         this.setState(100);
         this.match(30);
         this.setState(101);
         _localctx.trueBranch = this.statement();
         this.setState(104);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 3, this._ctx)) {
            case 1:
               this.setState(102);
               this.match(38);
               this.setState(103);
               _localctx.falseBranch = this.statement();
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.WhileStatementContext whileStatement() throws RecognitionException {
      ExpressionParser.WhileStatementContext _localctx = new ExpressionParser.WhileStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 10, 5);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(106);
         this.match(39);
         this.setState(107);
         this.match(29);
         this.setState(108);
         _localctx.condition = this.expression();
         this.setState(109);
         this.match(30);
         this.setState(110);
         _localctx.body = this.statement();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.DoStatementContext doStatement() throws RecognitionException {
      ExpressionParser.DoStatementContext _localctx = new ExpressionParser.DoStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 12, 6);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(112);
         this.match(40);
         this.setState(113);
         _localctx.body = this.statement();
         this.setState(114);
         this.match(39);
         this.setState(115);
         this.match(29);
         this.setState(116);
         _localctx.condition = this.expression();
         this.setState(117);
         this.match(30);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ForStatementContext forStatement() throws RecognitionException {
      ExpressionParser.ForStatementContext _localctx = new ExpressionParser.ForStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 14, 7);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(119);
         this.match(41);
         this.setState(120);
         this.match(29);
         this.setState(121);
         _localctx.init = this.expression();
         this.setState(122);
         this.match(33);
         this.setState(123);
         _localctx.condition = this.expression();
         this.setState(124);
         this.match(33);
         this.setState(125);
         _localctx.update = this.expression();
         this.setState(126);
         this.match(30);
         this.setState(127);
         _localctx.body = this.statement();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.SimpleForStatementContext simpleForStatement() throws RecognitionException {
      ExpressionParser.SimpleForStatementContext _localctx = new ExpressionParser.SimpleForStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 16, 8);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(129);
         this.match(41);
         this.setState(130);
         this.match(29);
         this.setState(131);
         _localctx.counter = this.match(49);
         this.setState(132);
         this.match(9);
         this.setState(133);
         _localctx.first = this.expression();
         this.setState(134);
         this.match(28);
         this.setState(135);
         _localctx.last = this.expression();
         this.setState(136);
         this.match(30);
         this.setState(137);
         _localctx.body = this.statement();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.BreakStatementContext breakStatement() throws RecognitionException {
      ExpressionParser.BreakStatementContext _localctx = new ExpressionParser.BreakStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 18, 9);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(139);
         this.match(42);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ContinueStatementContext continueStatement() throws RecognitionException {
      ExpressionParser.ContinueStatementContext _localctx = new ExpressionParser.ContinueStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 20, 10);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(141);
         this.match(43);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ReturnStatementContext returnStatement() throws RecognitionException {
      ExpressionParser.ReturnStatementContext _localctx = new ExpressionParser.ReturnStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 22, 11);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(143);
         this.match(44);
         this.setState(144);
         _localctx.value = this.expression();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.SwitchStatementContext switchStatement() throws RecognitionException {
      ExpressionParser.SwitchStatementContext _localctx = new ExpressionParser.SwitchStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 24, 12);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(146);
         this.match(45);
         this.setState(147);
         this.match(29);
         this.setState(148);
         _localctx.target = this.expression();
         this.setState(149);
         this.match(30);
         this.setState(150);
         this.match(31);
         this.setState(155);
         this._errHandler.sync(this);
         int _la = this._input.LA(1);

         do {
            this.setState(151);
            _localctx.switchLabel = this.switchLabel();
            _localctx.labels.add(_localctx.switchLabel);
            this.setState(152);
            this.match(35);
            this.setState(153);
            _localctx.statements = this.statements();
            _localctx.bodies.add(_localctx.statements);
            this.setState(157);
            this._errHandler.sync(this);
            _la = this._input.LA(1);
         } while (_la == 46 || _la == 47);

         this.setState(159);
         this.match(32);
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.SwitchLabelContext switchLabel() throws RecognitionException {
      ExpressionParser.SwitchLabelContext _localctx = new ExpressionParser.SwitchLabelContext(this._ctx, this.getState());
      this.enterRule(_localctx, 26, 13);

      try {
         this.setState(164);
         this._errHandler.sync(this);
         switch (this._input.LA(1)) {
            case 46:
               _localctx = new ExpressionParser.CaseContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(161);
               this.match(46);
               this.setState(162);
               ((ExpressionParser.CaseContext)_localctx).constant = this.constantExpression();
               break;
            case 47:
               _localctx = new ExpressionParser.DefaultContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(163);
               this.match(47);
               break;
            default:
               throw new NoViableAltException(this);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ExpressionStatementContext expressionStatement() throws RecognitionException {
      ExpressionParser.ExpressionStatementContext _localctx = new ExpressionParser.ExpressionStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 28, 14);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(166);
         this.expression();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.EmptyStatementContext emptyStatement() throws RecognitionException {
      ExpressionParser.EmptyStatementContext _localctx = new ExpressionParser.EmptyStatementContext(this._ctx, this.getState());
      this.enterRule(_localctx, 30, 15);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(168);
         this.match(33);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ExpressionContext expression() throws RecognitionException {
      ExpressionParser.ExpressionContext _localctx = new ExpressionParser.ExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 32, 16);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(170);
         this.assignmentExpression();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.AssignmentExpressionContext assignmentExpression() throws RecognitionException {
      ExpressionParser.AssignmentExpressionContext _localctx = new ExpressionParser.AssignmentExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 34, 17);

      try {
         this.setState(174);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 6, this._ctx)) {
            case 1:
               this.enterOuterAlt(_localctx, 1);
               this.setState(172);
               this.conditionalExpression();
               break;
            case 2:
               this.enterOuterAlt(_localctx, 2);
               this.setState(173);
               this.assignment();
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.AssignmentContext assignment() throws RecognitionException {
      ExpressionParser.AssignmentContext _localctx = new ExpressionParser.AssignmentContext(this._ctx, this.getState());
      this.enterRule(_localctx, 36, 18);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(176);
         _localctx.target = this.match(49);
         this.setState(177);
         this.assignmentOperator();
         this.setState(178);
         _localctx.source = this.expression();
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.AssignmentOperatorContext assignmentOperator() throws RecognitionException {
      ExpressionParser.AssignmentOperatorContext _localctx = new ExpressionParser.AssignmentOperatorContext(this._ctx, this.getState());
      this.enterRule(_localctx, 38, 19);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(180);
         int _la = this._input.LA(1);
         if ((_la & -64) == 0 && (1L << _la & 129536L) != 0L) {
            if (this._input.LA(1) == -1) {
               this.matchedEOF = true;
            }

            this._errHandler.reportMatch(this);
            this.consume();
         } else {
            this._errHandler.recoverInline(this);
         }
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ConditionalExpressionContext conditionalExpression() throws RecognitionException {
      ExpressionParser.ConditionalExpressionContext _localctx = new ExpressionParser.ConditionalExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 40, 20);

      try {
         this.setState(189);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 7, this._ctx)) {
            case 1:
               _localctx = new ExpressionParser.CEFallthroughContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(182);
               this.conditionalOrExpression(0);
               break;
            case 2:
               _localctx = new ExpressionParser.TernaryExprContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(183);
               ((ExpressionParser.TernaryExprContext)_localctx).condition = this.conditionalOrExpression(0);
               this.setState(184);
               this.match(34);
               this.setState(185);
               ((ExpressionParser.TernaryExprContext)_localctx).trueBranch = this.expression();
               this.setState(186);
               this.match(35);
               this.setState(187);
               ((ExpressionParser.TernaryExprContext)_localctx).falseBranch = this.conditionalExpression();
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ConditionalOrExpressionContext conditionalOrExpression() throws RecognitionException {
      return this.conditionalOrExpression(0);
   }

   private ExpressionParser.ConditionalOrExpressionContext conditionalOrExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.ConditionalOrExpressionContext _localctx = new ExpressionParser.ConditionalOrExpressionContext(this._ctx, _parentState);
      int _startState = 42;
      this.enterRecursionRule(_localctx, 42, 21, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.COFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(192);
         this.conditionalAndExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(199);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 8, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 8, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.ConditionalOrExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.ConditionalOrExprContext(new ExpressionParser.ConditionalOrExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.ConditionalOrExprContext)_localctx).left = (ExpressionParser.ConditionalOrExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 21);
               this.setState(194);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(195);
               this.match(25);
               this.setState(196);
               ((ExpressionParser.ConditionalOrExprContext)_localctx).right = this.conditionalAndExpression(0);
            }

            this.setState(201);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var11) {
         _localctx.exception = var11;
         this._errHandler.reportError(this, var11);
         this._errHandler.recover(this, var11);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.ConditionalAndExpressionContext conditionalAndExpression() throws RecognitionException {
      return this.conditionalAndExpression(0);
   }

   private ExpressionParser.ConditionalAndExpressionContext conditionalAndExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.ConditionalAndExpressionContext _localctx = new ExpressionParser.ConditionalAndExpressionContext(this._ctx, _parentState);
      int _startState = 44;
      this.enterRecursionRule(_localctx, 44, 22, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.CAFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(203);
         this.equalityExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(210);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 9, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 9, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.ConditionalAndExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.ConditionalAndExprContext(new ExpressionParser.ConditionalAndExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.ConditionalAndExprContext)_localctx).left = (ExpressionParser.ConditionalAndExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 22);
               this.setState(205);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(206);
               this.match(24);
               this.setState(207);
               ((ExpressionParser.ConditionalAndExprContext)_localctx).right = this.equalityExpression(0);
            }

            this.setState(212);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var11) {
         _localctx.exception = var11;
         this._errHandler.reportError(this, var11);
         this._errHandler.recover(this, var11);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.EqualityExpressionContext equalityExpression() throws RecognitionException {
      return this.equalityExpression(0);
   }

   private ExpressionParser.EqualityExpressionContext equalityExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.EqualityExpressionContext _localctx = new ExpressionParser.EqualityExpressionContext(this._ctx, _parentState);
      int _startState = 46;
      this.enterRecursionRule(_localctx, 46, 23, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.EqFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(214);
         this.relationalExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(221);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 10, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 10, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.EqualityExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.EqualityExprContext(new ExpressionParser.EqualityExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.EqualityExprContext)_localctx).left = (ExpressionParser.EqualityExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 23);
               this.setState(216);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(217);
               ((ExpressionParser.EqualityExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if ((_la & -64) == 0 && (1L << _la & 917504L) != 0L) {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               } else {
                  ((ExpressionParser.EqualityExprContext)_localctx).op = this._errHandler.recoverInline(this);
               }

               this.setState(218);
               ((ExpressionParser.EqualityExprContext)_localctx).right = this.relationalExpression(0);
            }

            this.setState(223);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.RelationalExpressionContext relationalExpression() throws RecognitionException {
      return this.relationalExpression(0);
   }

   private ExpressionParser.RelationalExpressionContext relationalExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.RelationalExpressionContext _localctx = new ExpressionParser.RelationalExpressionContext(this._ctx, _parentState);
      int _startState = 48;
      this.enterRecursionRule(_localctx, 48, 24, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.ReFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(225);
         this.shiftExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(232);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 11, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 11, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.RelationalExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.RelationalExprContext(new ExpressionParser.RelationalExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.RelationalExprContext)_localctx).left = (ExpressionParser.RelationalExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 24);
               this.setState(227);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(228);
               ((ExpressionParser.RelationalExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if ((_la & -64) == 0 && (1L << _la & 15728640L) != 0L) {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               } else {
                  ((ExpressionParser.RelationalExprContext)_localctx).op = this._errHandler.recoverInline(this);
               }

               this.setState(229);
               ((ExpressionParser.RelationalExprContext)_localctx).right = this.shiftExpression(0);
            }

            this.setState(234);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.ShiftExpressionContext shiftExpression() throws RecognitionException {
      return this.shiftExpression(0);
   }

   private ExpressionParser.ShiftExpressionContext shiftExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.ShiftExpressionContext _localctx = new ExpressionParser.ShiftExpressionContext(this._ctx, _parentState);
      int _startState = 50;
      this.enterRecursionRule(_localctx, 50, 25, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.ShFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(236);
         this.additiveExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(243);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 12, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 12, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.ShiftExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.ShiftExprContext(new ExpressionParser.ShiftExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.ShiftExprContext)_localctx).left = (ExpressionParser.ShiftExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 25);
               this.setState(238);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(239);
               ((ExpressionParser.ShiftExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 7 && _la != 8) {
                  ((ExpressionParser.ShiftExprContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }

               this.setState(240);
               ((ExpressionParser.ShiftExprContext)_localctx).right = this.additiveExpression(0);
            }

            this.setState(245);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.AdditiveExpressionContext additiveExpression() throws RecognitionException {
      return this.additiveExpression(0);
   }

   private ExpressionParser.AdditiveExpressionContext additiveExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.AdditiveExpressionContext _localctx = new ExpressionParser.AdditiveExpressionContext(this._ctx, _parentState);
      int _startState = 52;
      this.enterRecursionRule(_localctx, 52, 26, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.AdFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(247);
         this.multiplicativeExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(254);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 13, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 13, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.AdditiveExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.AddExprContext(new ExpressionParser.AdditiveExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.AddExprContext)_localctx).left = (ExpressionParser.AdditiveExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 26);
               this.setState(249);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(250);
               ((ExpressionParser.AddExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 1 && _la != 2) {
                  ((ExpressionParser.AddExprContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }

               this.setState(251);
               ((ExpressionParser.AddExprContext)_localctx).right = this.multiplicativeExpression(0);
            }

            this.setState(256);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.MultiplicativeExpressionContext multiplicativeExpression() throws RecognitionException {
      return this.multiplicativeExpression(0);
   }

   private ExpressionParser.MultiplicativeExpressionContext multiplicativeExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.MultiplicativeExpressionContext _localctx = new ExpressionParser.MultiplicativeExpressionContext(this._ctx, _parentState);
      int _startState = 54;
      this.enterRecursionRule(_localctx, 54, 27, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.MuFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(258);
         this.powerExpression(0);
         this._ctx.stop = this._input.LT(-1);
         this.setState(265);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 14, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 14, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.MultiplicativeExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.MultiplicativeExprContext(new ExpressionParser.MultiplicativeExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.MultiplicativeExprContext)_localctx).left = (ExpressionParser.MultiplicativeExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 27);
               this.setState(260);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(261);
               ((ExpressionParser.MultiplicativeExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if ((_la & -64) == 0 && (1L << _la & 56L) != 0L) {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               } else {
                  ((ExpressionParser.MultiplicativeExprContext)_localctx).op = this._errHandler.recoverInline(this);
               }

               this.setState(262);
               ((ExpressionParser.MultiplicativeExprContext)_localctx).right = this.powerExpression(0);
            }

            this.setState(267);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.PowerExpressionContext powerExpression() throws RecognitionException {
      return this.powerExpression(0);
   }

   private ExpressionParser.PowerExpressionContext powerExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.PowerExpressionContext _localctx = new ExpressionParser.PowerExpressionContext(this._ctx, _parentState);
      int _startState = 56;
      this.enterRecursionRule(_localctx, 56, 28, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         _localctx = new ExpressionParser.PwFallthroughContext(_localctx);
         this._ctx = _localctx;
         this.setState(269);
         this.unaryExpression();
         this._ctx.stop = this._input.LT(-1);
         this.setState(276);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 15, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 15, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.PowerExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.PowerExprContext(new ExpressionParser.PowerExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.PowerExprContext)_localctx).left = (ExpressionParser.PowerExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 28);
               this.setState(271);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(272);
               this.match(6);
               this.setState(273);
               ((ExpressionParser.PowerExprContext)_localctx).right = this.unaryExpression();
            }

            this.setState(278);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var11) {
         _localctx.exception = var11;
         this._errHandler.reportError(this, var11);
         this._errHandler.recover(this, var11);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.UnaryExpressionContext unaryExpression() throws RecognitionException {
      ExpressionParser.UnaryExpressionContext _localctx = new ExpressionParser.UnaryExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 58, 29);

      try {
         this.setState(288);
         this._errHandler.sync(this);
         switch (this._input.LA(1)) {
            case 1:
            case 2:
               _localctx = new ExpressionParser.PlusMinusExprContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(281);
               ((ExpressionParser.PlusMinusExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 1 && _la != 2) {
                  ((ExpressionParser.PlusMinusExprContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }

               this.setState(282);
               ((ExpressionParser.PlusMinusExprContext)_localctx).expr = this.unaryExpression();
               break;
            case 10:
               _localctx = new ExpressionParser.ComplementExprContext(_localctx);
               this.enterOuterAlt(_localctx, 4);
               this.setState(284);
               this.match(10);
               this.setState(285);
               ((ExpressionParser.ComplementExprContext)_localctx).expr = this.unaryExpression();
               break;
            case 26:
            case 27:
               _localctx = new ExpressionParser.PreCrementExprContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(279);
               ((ExpressionParser.PreCrementExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 26 && _la != 27) {
                  ((ExpressionParser.PreCrementExprContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }

               this.setState(280);
               ((ExpressionParser.PreCrementExprContext)_localctx).target = this.match(49);
               break;
            case 29:
            case 48:
            case 49:
               _localctx = new ExpressionParser.UaFallthroughContext(_localctx);
               this.enterOuterAlt(_localctx, 3);
               this.setState(283);
               this.postfixExpression(0);
               break;
            case 36:
               _localctx = new ExpressionParser.NotExprContext(_localctx);
               this.enterOuterAlt(_localctx, 5);
               this.setState(286);
               this.match(36);
               this.setState(287);
               ((ExpressionParser.NotExprContext)_localctx).expr = this.unaryExpression();
               break;
            default:
               throw new NoViableAltException(this);
         }
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.PostfixExpressionContext postfixExpression() throws RecognitionException {
      return this.postfixExpression(0);
   }

   private ExpressionParser.PostfixExpressionContext postfixExpression(int _p) throws RecognitionException {
      ParserRuleContext _parentctx = this._ctx;
      int _parentState = this.getState();
      ExpressionParser.PostfixExpressionContext _localctx = new ExpressionParser.PostfixExpressionContext(this._ctx, _parentState);
      int _startState = 60;
      this.enterRecursionRule(_localctx, 60, 30, _p);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(294);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 17, this._ctx)) {
            case 1:
               _localctx = new ExpressionParser.PoFallthroughContext(_localctx);
               this._ctx = _localctx;
               this.setState(291);
               this.unprioritizedExpression();
               break;
            case 2:
               _localctx = new ExpressionParser.PostCrementExprContext(_localctx);
               this._ctx = _localctx;
               this.setState(292);
               ((ExpressionParser.PostCrementExprContext)_localctx).target = this.match(49);
               this.setState(293);
               ((ExpressionParser.PostCrementExprContext)_localctx).op = this._input.LT(1);
               int _la = this._input.LA(1);
               if (_la != 26 && _la != 27) {
                  ((ExpressionParser.PostCrementExprContext)_localctx).op = this._errHandler.recoverInline(this);
               } else {
                  if (this._input.LA(1) == -1) {
                     this.matchedEOF = true;
                  }

                  this._errHandler.reportMatch(this);
                  this.consume();
               }
         }

         this._ctx.stop = this._input.LT(-1);
         this.setState(300);
         this._errHandler.sync(this);

         for (int _alt = this.getInterpreter().adaptivePredict(this._input, 18, this._ctx);
            _alt != 2 && _alt != 0;
            _alt = this.getInterpreter().adaptivePredict(this._input, 18, this._ctx)
         ) {
            if (_alt == 1) {
               if (this._parseListeners != null) {
                  this.triggerExitRuleEvent();
               }

               ExpressionParser.PostfixExpressionContext _prevctx = _localctx;
               _localctx = new ExpressionParser.PostfixExprContext(new ExpressionParser.PostfixExpressionContext(_parentctx, _parentState));
               ((ExpressionParser.PostfixExprContext)_localctx).expr = (ExpressionParser.PostfixExpressionContext)_prevctx;
               this.pushNewRecursionContext(_localctx, _startState, 30);
               this.setState(296);
               if (!this.precpred(this._ctx, 1)) {
                  throw new FailedPredicateException(this, "precpred(_ctx, 1)");
               }

               this.setState(297);
               ((ExpressionParser.PostfixExprContext)_localctx).op = this.match(36);
            }

            this.setState(302);
            this._errHandler.sync(this);
         }
      } catch (RecognitionException var12) {
         _localctx.exception = var12;
         this._errHandler.reportError(this, var12);
         this._errHandler.recover(this, var12);
      } finally {
         this.unrollRecursionContexts(_parentctx);
      }

      return _localctx;
   }

   public final ExpressionParser.UnprioritizedExpressionContext unprioritizedExpression() throws RecognitionException {
      ExpressionParser.UnprioritizedExpressionContext _localctx = new ExpressionParser.UnprioritizedExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 62, 31);

      try {
         this.setState(310);
         this._errHandler.sync(this);
         switch (this.getInterpreter().adaptivePredict(this._input, 19, this._ctx)) {
            case 1:
               _localctx = new ExpressionParser.FunctionCallExprContext(_localctx);
               this.enterOuterAlt(_localctx, 1);
               this.setState(303);
               this.functionCall();
               break;
            case 2:
               _localctx = new ExpressionParser.ConstantExprContext(_localctx);
               this.enterOuterAlt(_localctx, 2);
               this.setState(304);
               this.constantExpression();
               break;
            case 3:
               _localctx = new ExpressionParser.IdExprContext(_localctx);
               this.enterOuterAlt(_localctx, 3);
               this.setState(305);
               ((ExpressionParser.IdExprContext)_localctx).source = this.match(49);
               break;
            case 4:
               _localctx = new ExpressionParser.WrappedExprContext(_localctx);
               this.enterOuterAlt(_localctx, 4);
               this.setState(306);
               this.match(29);
               this.setState(307);
               this.expression();
               this.setState(308);
               this.match(30);
         }
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.ConstantExpressionContext constantExpression() throws RecognitionException {
      ExpressionParser.ConstantExpressionContext _localctx = new ExpressionParser.ConstantExpressionContext(this._ctx, this.getState());
      this.enterRule(_localctx, 64, 32);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(312);
         this.match(48);
      } catch (RecognitionException var6) {
         _localctx.exception = var6;
         this._errHandler.reportError(this, var6);
         this._errHandler.recover(this, var6);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   public final ExpressionParser.FunctionCallContext functionCall() throws RecognitionException {
      ExpressionParser.FunctionCallContext _localctx = new ExpressionParser.FunctionCallContext(this._ctx, this.getState());
      this.enterRule(_localctx, 66, 33);

      try {
         this.enterOuterAlt(_localctx, 1);
         this.setState(314);
         _localctx.name = this.match(49);
         this.setState(315);
         this.match(29);
         this.setState(324);
         this._errHandler.sync(this);
         int _la = this._input.LA(1);
         if ((_la & -64) == 0 && (1L << _la & 844494387807238L) != 0L) {
            this.setState(316);
            _localctx.expression = this.expression();
            _localctx.args.add(_localctx.expression);
            this.setState(321);
            this._errHandler.sync(this);

            for (int var9 = this._input.LA(1); var9 == 28; var9 = this._input.LA(1)) {
               this.setState(317);
               this.match(28);
               this.setState(318);
               _localctx.expression = this.expression();
               _localctx.args.add(_localctx.expression);
               this.setState(323);
               this._errHandler.sync(this);
            }
         }

         this.setState(326);
         this.match(30);
      } catch (RecognitionException var7) {
         _localctx.exception = var7;
         this._errHandler.reportError(this, var7);
         this._errHandler.recover(this, var7);
      } finally {
         this.exitRule();
      }

      return _localctx;
   }

   @Override
   public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
      switch (ruleIndex) {
         case 21:
            return this.conditionalOrExpression_sempred((ExpressionParser.ConditionalOrExpressionContext)_localctx, predIndex);
         case 22:
            return this.conditionalAndExpression_sempred((ExpressionParser.ConditionalAndExpressionContext)_localctx, predIndex);
         case 23:
            return this.equalityExpression_sempred((ExpressionParser.EqualityExpressionContext)_localctx, predIndex);
         case 24:
            return this.relationalExpression_sempred((ExpressionParser.RelationalExpressionContext)_localctx, predIndex);
         case 25:
            return this.shiftExpression_sempred((ExpressionParser.ShiftExpressionContext)_localctx, predIndex);
         case 26:
            return this.additiveExpression_sempred((ExpressionParser.AdditiveExpressionContext)_localctx, predIndex);
         case 27:
            return this.multiplicativeExpression_sempred((ExpressionParser.MultiplicativeExpressionContext)_localctx, predIndex);
         case 28:
            return this.powerExpression_sempred((ExpressionParser.PowerExpressionContext)_localctx, predIndex);
         case 29:
         default:
            return true;
         case 30:
            return this.postfixExpression_sempred((ExpressionParser.PostfixExpressionContext)_localctx, predIndex);
      }
   }

   private boolean conditionalOrExpression_sempred(ExpressionParser.ConditionalOrExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 0:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean conditionalAndExpression_sempred(ExpressionParser.ConditionalAndExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 1:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean equalityExpression_sempred(ExpressionParser.EqualityExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 2:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean relationalExpression_sempred(ExpressionParser.RelationalExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 3:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean shiftExpression_sempred(ExpressionParser.ShiftExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 4:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean additiveExpression_sempred(ExpressionParser.AdditiveExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 5:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean multiplicativeExpression_sempred(ExpressionParser.MultiplicativeExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 6:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean powerExpression_sempred(ExpressionParser.PowerExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 7:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   private boolean postfixExpression_sempred(ExpressionParser.PostfixExpressionContext _localctx, int predIndex) {
      switch (predIndex) {
         case 8:
            return this.precpred(this._ctx, 1);
         default:
            return true;
      }
   }

   static {
      RuntimeMetaData.checkVersion("4.13.2", "4.13.2");

      for (int i = 0; i < tokenNames.length; i++) {
         tokenNames[i] = VOCABULARY.getLiteralName(i);
         if (tokenNames[i] == null) {
            tokenNames[i] = VOCABULARY.getSymbolicName(i);
         }

         if (tokenNames[i] == null) {
            tokenNames[i] = "<INVALID>";
         }
      }

      _ATN = new ATNDeserializer()
         .deserialize(
            "\u0004\u00012ŉ\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0004\u0001I\b\u0001\u000b\u0001\f\u0001J\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002Y\b\u0002\u0001\u0002\u0003\u0002\\\b\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004i\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0001\f\u0004\f\u009c\b\f\u000b\f\f\f\u009d\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0003\r¥\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0011\u0001\u0011\u0003\u0011¯\b\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0013\u0001\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014¾\b\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015Æ\b\u0015\n\u0015\f\u0015É\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0005\u0016Ñ\b\u0016\n\u0016\f\u0016Ô\t\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017Ü\b\u0017\n\u0017\f\u0017ß\t\u0017\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018ç\b\u0018\n\u0018\f\u0018ê\t\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019ò\b\u0019\n\u0019\f\u0019õ\t\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0005\u001aý\b\u001a\n\u001a\f\u001aĀ\t\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0005\u001bĈ\b\u001b\n\u001b\f\u001bċ\t\u001b\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005\u001cē\b\u001c\n\u001c\f\u001cĖ\t\u001c\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001dġ\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0003\u001eħ\b\u001e\u0001\u001e\u0001\u001e\u0005\u001eī\b\u001e\n\u001e\f\u001eĮ\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0003\u001fķ\b\u001f\u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0001!\u0005!ŀ\b!\n!\f!Ń\t!\u0003!Ņ\b!\u0001!\u0001!\u0001!\u0000\t*,.02468<\"\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:<>@B\u0000\u0007\u0002\u0000\t\t\u000b\u0010\u0001\u0000\u0011\u0013\u0001\u0000\u0014\u0017\u0001\u0000\u0007\b\u0001\u0000\u0001\u0002\u0001\u0000\u0003\u0005\u0001\u0000\u001a\u001bŋ\u0000D\u0001\u0000\u0000\u0000\u0002H\u0001\u0000\u0000\u0000\u0004X\u0001\u0000\u0000\u0000\u0006]\u0001\u0000\u0000\u0000\ba\u0001\u0000\u0000\u0000\nj\u0001\u0000\u0000\u0000\fp\u0001\u0000\u0000\u0000\u000ew\u0001\u0000\u0000\u0000\u0010\u0081\u0001\u0000\u0000\u0000\u0012\u008b\u0001\u0000\u0000\u0000\u0014\u008d\u0001\u0000\u0000\u0000\u0016\u008f\u0001\u0000\u0000\u0000\u0018\u0092\u0001\u0000\u0000\u0000\u001a¤\u0001\u0000\u0000\u0000\u001c¦\u0001\u0000\u0000\u0000\u001e¨\u0001\u0000\u0000\u0000 ª\u0001\u0000\u0000\u0000\"®\u0001\u0000\u0000\u0000$°\u0001\u0000\u0000\u0000&´\u0001\u0000\u0000\u0000(½\u0001\u0000\u0000\u0000*¿\u0001\u0000\u0000\u0000,Ê\u0001\u0000\u0000\u0000.Õ\u0001\u0000\u0000\u00000à\u0001\u0000\u0000\u00002ë\u0001\u0000\u0000\u00004ö\u0001\u0000\u0000\u00006ā\u0001\u0000\u0000\u00008Č\u0001\u0000\u0000\u0000:Ġ\u0001\u0000\u0000\u0000<Ħ\u0001\u0000\u0000\u0000>Ķ\u0001\u0000\u0000\u0000@ĸ\u0001\u0000\u0000\u0000Bĺ\u0001\u0000\u0000\u0000DE\u0003\u0002\u0001\u0000EF\u0005\u0000\u0000\u0001F\u0001\u0001\u0000\u0000\u0000GI\u0003\u0004\u0002\u0000HG\u0001\u0000\u0000\u0000IJ\u0001\u0000\u0000\u0000JH\u0001\u0000\u0000\u0000JK\u0001\u0000\u0000\u0000K\u0003\u0001\u0000\u0000\u0000LY\u0003\u0006\u0003\u0000MY\u0003\b\u0004\u0000NY\u0003\n\u0005\u0000OY\u0003\f\u0006\u0000PY\u0003\u000e\u0007\u0000QY\u0003\u0010\b\u0000RY\u0003\u0012\t\u0000SY\u0003\u0014\n\u0000TY\u0003\u0016\u000b\u0000UY\u0003\u0018\f\u0000VY\u0003\u001c\u000e\u0000WY\u0003\u001e\u000f\u0000XL\u0001\u0000\u0000\u0000XM\u0001\u0000\u0000\u0000XN\u0001\u0000\u0000\u0000XO\u0001\u0000\u0000\u0000XP\u0001\u0000\u0000\u0000XQ\u0001\u0000\u0000\u0000XR\u0001\u0000\u0000\u0000XS\u0001\u0000\u0000\u0000XT\u0001\u0000\u0000\u0000XU\u0001\u0000\u0000\u0000XV\u0001\u0000\u0000\u0000XW\u0001\u0000\u0000\u0000Y[\u0001\u0000\u0000\u0000Z\\\u0005!\u0000\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000\u0000\\\u0005\u0001\u0000\u0000\u0000]^\u0005\u001f\u0000\u0000^_\u0003\u0002\u0001\u0000_`\u0005 \u0000\u0000`\u0007\u0001\u0000\u0000\u0000ab\u0005%\u0000\u0000bc\u0005\u001d\u0000\u0000cd\u0003 \u0010\u0000de\u0005\u001e\u0000\u0000eh\u0003\u0004\u0002\u0000fg\u0005&\u0000\u0000gi\u0003\u0004\u0002\u0000hf\u0001\u0000\u0000\u0000hi\u0001\u0000\u0000\u0000i\t\u0001\u0000\u0000\u0000jk\u0005'\u0000\u0000kl\u0005\u001d\u0000\u0000lm\u0003 \u0010\u0000mn\u0005\u001e\u0000\u0000no\u0003\u0004\u0002\u0000o\u000b\u0001\u0000\u0000\u0000pq\u0005(\u0000\u0000qr\u0003\u0004\u0002\u0000rs\u0005'\u0000\u0000st\u0005\u001d\u0000\u0000tu\u0003 \u0010\u0000uv\u0005\u001e\u0000\u0000v\r\u0001\u0000\u0000\u0000wx\u0005)\u0000\u0000xy\u0005\u001d\u0000\u0000yz\u0003 \u0010\u0000z{\u0005!\u0000\u0000{|\u0003 \u0010\u0000|}\u0005!\u0000\u0000}~\u0003 \u0010\u0000~\u007f\u0005\u001e\u0000\u0000\u007f\u0080\u0003\u0004\u0002\u0000\u0080\u000f\u0001\u0000\u0000\u0000\u0081\u0082\u0005)\u0000\u0000\u0082\u0083\u0005\u001d\u0000\u0000\u0083\u0084\u00051\u0000\u0000\u0084\u0085\u0005\t\u0000\u0000\u0085\u0086\u0003 \u0010\u0000\u0086\u0087\u0005\u001c\u0000\u0000\u0087\u0088\u0003 \u0010\u0000\u0088\u0089\u0005\u001e\u0000\u0000\u0089\u008a\u0003\u0004\u0002\u0000\u008a\u0011\u0001\u0000\u0000\u0000\u008b\u008c\u0005*\u0000\u0000\u008c\u0013\u0001\u0000\u0000\u0000\u008d\u008e\u0005+\u0000\u0000\u008e\u0015\u0001\u0000\u0000\u0000\u008f\u0090\u0005,\u0000\u0000\u0090\u0091\u0003 \u0010\u0000\u0091\u0017\u0001\u0000\u0000\u0000\u0092\u0093\u0005-\u0000\u0000\u0093\u0094\u0005\u001d\u0000\u0000\u0094\u0095\u0003 \u0010\u0000\u0095\u0096\u0005\u001e\u0000\u0000\u0096\u009b\u0005\u001f\u0000\u0000\u0097\u0098\u0003\u001a\r\u0000\u0098\u0099\u0005#\u0000\u0000\u0099\u009a\u0003\u0002\u0001\u0000\u009a\u009c\u0001\u0000\u0000\u0000\u009b\u0097\u0001\u0000\u0000\u0000\u009c\u009d\u0001\u0000\u0000\u0000\u009d\u009b\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f \u0005 \u0000\u0000 \u0019\u0001\u0000\u0000\u0000¡¢\u0005.\u0000\u0000¢¥\u0003@ \u0000£¥\u0005/\u0000\u0000¤¡\u0001\u0000\u0000\u0000¤£\u0001\u0000\u0000\u0000¥\u001b\u0001\u0000\u0000\u0000¦§\u0003 \u0010\u0000§\u001d\u0001\u0000\u0000\u0000¨©\u0005!\u0000\u0000©\u001f\u0001\u0000\u0000\u0000ª«\u0003\"\u0011\u0000«!\u0001\u0000\u0000\u0000¬¯\u0003(\u0014\u0000\u00ad¯\u0003$\u0012\u0000®¬\u0001\u0000\u0000\u0000®\u00ad\u0001\u0000\u0000\u0000¯#\u0001\u0000\u0000\u0000°±\u00051\u0000\u0000±²\u0003&\u0013\u0000²³\u0003 \u0010\u0000³%\u0001\u0000\u0000\u0000´µ\u0007\u0000\u0000\u0000µ'\u0001\u0000\u0000\u0000¶¾\u0003*\u0015\u0000·¸\u0003*\u0015\u0000¸¹\u0005\"\u0000\u0000¹º\u0003 \u0010\u0000º»\u0005#\u0000\u0000»¼\u0003(\u0014\u0000¼¾\u0001\u0000\u0000\u0000½¶\u0001\u0000\u0000\u0000½·\u0001\u0000\u0000\u0000¾)\u0001\u0000\u0000\u0000¿À\u0006\u0015\uffff\uffff\u0000ÀÁ\u0003,\u0016\u0000ÁÇ\u0001\u0000\u0000\u0000ÂÃ\n\u0001\u0000\u0000ÃÄ\u0005\u0019\u0000\u0000ÄÆ\u0003,\u0016\u0000ÅÂ\u0001\u0000\u0000\u0000ÆÉ\u0001\u0000\u0000\u0000ÇÅ\u0001\u0000\u0000\u0000ÇÈ\u0001\u0000\u0000\u0000È+\u0001\u0000\u0000\u0000ÉÇ\u0001\u0000\u0000\u0000ÊË\u0006\u0016\uffff\uffff\u0000ËÌ\u0003.\u0017\u0000ÌÒ\u0001\u0000\u0000\u0000ÍÎ\n\u0001\u0000\u0000ÎÏ\u0005\u0018\u0000\u0000ÏÑ\u0003.\u0017\u0000ÐÍ\u0001\u0000\u0000\u0000ÑÔ\u0001\u0000\u0000\u0000ÒÐ\u0001\u0000\u0000\u0000ÒÓ\u0001\u0000\u0000\u0000Ó-\u0001\u0000\u0000\u0000ÔÒ\u0001\u0000\u0000\u0000ÕÖ\u0006\u0017\uffff\uffff\u0000Ö×\u00030\u0018\u0000×Ý\u0001\u0000\u0000\u0000ØÙ\n\u0001\u0000\u0000ÙÚ\u0007\u0001\u0000\u0000ÚÜ\u00030\u0018\u0000ÛØ\u0001\u0000\u0000\u0000Üß\u0001\u0000\u0000\u0000ÝÛ\u0001\u0000\u0000\u0000ÝÞ\u0001\u0000\u0000\u0000Þ/\u0001\u0000\u0000\u0000ßÝ\u0001\u0000\u0000\u0000àá\u0006\u0018\uffff\uffff\u0000áâ\u00032\u0019\u0000âè\u0001\u0000\u0000\u0000ãä\n\u0001\u0000\u0000äå\u0007\u0002\u0000\u0000åç\u00032\u0019\u0000æã\u0001\u0000\u0000\u0000çê\u0001\u0000\u0000\u0000èæ\u0001\u0000\u0000\u0000èé\u0001\u0000\u0000\u0000é1\u0001\u0000\u0000\u0000êè\u0001\u0000\u0000\u0000ëì\u0006\u0019\uffff\uffff\u0000ìí\u00034\u001a\u0000íó\u0001\u0000\u0000\u0000îï\n\u0001\u0000\u0000ïð\u0007\u0003\u0000\u0000ðò\u00034\u001a\u0000ñî\u0001\u0000\u0000\u0000òõ\u0001\u0000\u0000\u0000óñ\u0001\u0000\u0000\u0000óô\u0001\u0000\u0000\u0000ô3\u0001\u0000\u0000\u0000õó\u0001\u0000\u0000\u0000ö÷\u0006\u001a\uffff\uffff\u0000÷ø\u00036\u001b\u0000øþ\u0001\u0000\u0000\u0000ùú\n\u0001\u0000\u0000úû\u0007\u0004\u0000\u0000ûý\u00036\u001b\u0000üù\u0001\u0000\u0000\u0000ýĀ\u0001\u0000\u0000\u0000þü\u0001\u0000\u0000\u0000þÿ\u0001\u0000\u0000\u0000ÿ5\u0001\u0000\u0000\u0000Āþ\u0001\u0000\u0000\u0000āĂ\u0006\u001b\uffff\uffff\u0000Ăă\u00038\u001c\u0000ăĉ\u0001\u0000\u0000\u0000Ąą\n\u0001\u0000\u0000ąĆ\u0007\u0005\u0000\u0000ĆĈ\u00038\u001c\u0000ćĄ\u0001\u0000\u0000\u0000Ĉċ\u0001\u0000\u0000\u0000ĉć\u0001\u0000\u0000\u0000ĉĊ\u0001\u0000\u0000\u0000Ċ7\u0001\u0000\u0000\u0000ċĉ\u0001\u0000\u0000\u0000Čč\u0006\u001c\uffff\uffff\u0000čĎ\u0003:\u001d\u0000ĎĔ\u0001\u0000\u0000\u0000ďĐ\n\u0001\u0000\u0000Đđ\u0005\u0006\u0000\u0000đē\u0003:\u001d\u0000Ēď\u0001\u0000\u0000\u0000ēĖ\u0001\u0000\u0000\u0000ĔĒ\u0001\u0000\u0000\u0000Ĕĕ\u0001\u0000\u0000\u0000ĕ9\u0001\u0000\u0000\u0000ĖĔ\u0001\u0000\u0000\u0000ėĘ\u0007\u0006\u0000\u0000Ęġ\u00051\u0000\u0000ęĚ\u0007\u0004\u0000\u0000Ěġ\u0003:\u001d\u0000ěġ\u0003<\u001e\u0000Ĝĝ\u0005\n\u0000\u0000ĝġ\u0003:\u001d\u0000Ğğ\u0005$\u0000\u0000ğġ\u0003:\u001d\u0000Ġė\u0001\u0000\u0000\u0000Ġę\u0001\u0000\u0000\u0000Ġě\u0001\u0000\u0000\u0000ĠĜ\u0001\u0000\u0000\u0000ĠĞ\u0001\u0000\u0000\u0000ġ;\u0001\u0000\u0000\u0000Ģģ\u0006\u001e\uffff\uffff\u0000ģħ\u0003>\u001f\u0000Ĥĥ\u00051\u0000\u0000ĥħ\u0007\u0006\u0000\u0000ĦĢ\u0001\u0000\u0000\u0000ĦĤ\u0001\u0000\u0000\u0000ħĬ\u0001\u0000\u0000\u0000Ĩĩ\n\u0001\u0000\u0000ĩī\u0005$\u0000\u0000ĪĨ\u0001\u0000\u0000\u0000īĮ\u0001\u0000\u0000\u0000ĬĪ\u0001\u0000\u0000\u0000Ĭĭ\u0001\u0000\u0000\u0000ĭ=\u0001\u0000\u0000\u0000ĮĬ\u0001\u0000\u0000\u0000įķ\u0003B!\u0000İķ\u0003@ \u0000ıķ\u00051\u0000\u0000Ĳĳ\u0005\u001d\u0000\u0000ĳĴ\u0003 \u0010\u0000Ĵĵ\u0005\u001e\u0000\u0000ĵķ\u0001\u0000\u0000\u0000Ķį\u0001\u0000\u0000\u0000Ķİ\u0001\u0000\u0000\u0000Ķı\u0001\u0000\u0000\u0000ĶĲ\u0001\u0000\u0000\u0000ķ?\u0001\u0000\u0000\u0000ĸĹ\u00050\u0000\u0000ĹA\u0001\u0000\u0000\u0000ĺĻ\u00051\u0000\u0000Ļń\u0005\u001d\u0000\u0000ļŁ\u0003 \u0010\u0000Ľľ\u0005\u001c\u0000\u0000ľŀ\u0003 \u0010\u0000ĿĽ\u0001\u0000\u0000\u0000ŀŃ\u0001\u0000\u0000\u0000ŁĿ\u0001\u0000\u0000\u0000Łł\u0001\u0000\u0000\u0000łŅ\u0001\u0000\u0000\u0000ŃŁ\u0001\u0000\u0000\u0000ńļ\u0001\u0000\u0000\u0000ńŅ\u0001\u0000\u0000\u0000Ņņ\u0001\u0000\u0000\u0000ņŇ\u0005\u001e\u0000\u0000ŇC\u0001\u0000\u0000\u0000\u0016JX[h\u009d¤®½ÇÒÝèóþĉĔĠĦĬĶŁń"
               .toCharArray()
         );
      _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];

      for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
         _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
      }
   }

   public static class AdFallthroughContext extends ExpressionParser.AdditiveExpressionContext {
      public ExpressionParser.MultiplicativeExpressionContext multiplicativeExpression() {
         return this.getRuleContext(ExpressionParser.MultiplicativeExpressionContext.class, 0);
      }

      public AdFallthroughContext(ExpressionParser.AdditiveExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAdFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAdFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAdFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class AddExprContext extends ExpressionParser.AdditiveExpressionContext {
      public ExpressionParser.AdditiveExpressionContext left;
      public Token op;
      public ExpressionParser.MultiplicativeExpressionContext right;

      public ExpressionParser.AdditiveExpressionContext additiveExpression() {
         return this.getRuleContext(ExpressionParser.AdditiveExpressionContext.class, 0);
      }

      public ExpressionParser.MultiplicativeExpressionContext multiplicativeExpression() {
         return this.getRuleContext(ExpressionParser.MultiplicativeExpressionContext.class, 0);
      }

      public TerminalNode PLUS() {
         return this.getToken(1, 0);
      }

      public TerminalNode MINUS() {
         return this.getToken(2, 0);
      }

      public AddExprContext(ExpressionParser.AdditiveExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAddExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAddExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAddExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class AdditiveExpressionContext extends ParserRuleContext {
      public AdditiveExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 26;
      }

      public AdditiveExpressionContext() {
      }

      public void copyFrom(ExpressionParser.AdditiveExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class AllStatementsContext extends ParserRuleContext {
      public ExpressionParser.StatementsContext statements() {
         return this.getRuleContext(ExpressionParser.StatementsContext.class, 0);
      }

      public TerminalNode EOF() {
         return this.getToken(-1, 0);
      }

      public AllStatementsContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 0;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAllStatements(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAllStatements(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAllStatements(this) : visitor.visitChildren(this));
      }
   }

   public static class AssignmentContext extends ParserRuleContext {
      public Token target;
      public ExpressionParser.ExpressionContext source;

      public ExpressionParser.AssignmentOperatorContext assignmentOperator() {
         return this.getRuleContext(ExpressionParser.AssignmentOperatorContext.class, 0);
      }

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public AssignmentContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 18;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAssignment(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAssignment(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAssignment(this) : visitor.visitChildren(this));
      }
   }

   public static class AssignmentExpressionContext extends ParserRuleContext {
      public ExpressionParser.ConditionalExpressionContext conditionalExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalExpressionContext.class, 0);
      }

      public ExpressionParser.AssignmentContext assignment() {
         return this.getRuleContext(ExpressionParser.AssignmentContext.class, 0);
      }

      public AssignmentExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 17;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAssignmentExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAssignmentExpression(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAssignmentExpression(this) : visitor.visitChildren(this));
      }
   }

   public static class AssignmentOperatorContext extends ParserRuleContext {
      public TerminalNode ASSIGN() {
         return this.getToken(9, 0);
      }

      public TerminalNode POWER_ASSIGN() {
         return this.getToken(16, 0);
      }

      public TerminalNode TIMES_ASSIGN() {
         return this.getToken(13, 0);
      }

      public TerminalNode DIVIDE_ASSIGN() {
         return this.getToken(14, 0);
      }

      public TerminalNode MODULO_ASSIGN() {
         return this.getToken(15, 0);
      }

      public TerminalNode PLUS_ASSIGN() {
         return this.getToken(11, 0);
      }

      public TerminalNode MINUS_ASSIGN() {
         return this.getToken(12, 0);
      }

      public AssignmentOperatorContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 19;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterAssignmentOperator(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitAssignmentOperator(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitAssignmentOperator(this) : visitor.visitChildren(this));
      }
   }

   public static class BlockContext extends ParserRuleContext {
      public TerminalNode OPEN_BRACKET() {
         return this.getToken(31, 0);
      }

      public ExpressionParser.StatementsContext statements() {
         return this.getRuleContext(ExpressionParser.StatementsContext.class, 0);
      }

      public TerminalNode CLOSE_BRACKET() {
         return this.getToken(32, 0);
      }

      public BlockContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 3;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterBlock(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitBlock(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitBlock(this) : visitor.visitChildren(this));
      }
   }

   public static class BreakStatementContext extends ParserRuleContext {
      public TerminalNode BREAK() {
         return this.getToken(42, 0);
      }

      public BreakStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 9;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterBreakStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitBreakStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitBreakStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class CAFallthroughContext extends ExpressionParser.ConditionalAndExpressionContext {
      public ExpressionParser.EqualityExpressionContext equalityExpression() {
         return this.getRuleContext(ExpressionParser.EqualityExpressionContext.class, 0);
      }

      public CAFallthroughContext(ExpressionParser.ConditionalAndExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterCAFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitCAFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitCAFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class CEFallthroughContext extends ExpressionParser.ConditionalExpressionContext {
      public ExpressionParser.ConditionalOrExpressionContext conditionalOrExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalOrExpressionContext.class, 0);
      }

      public CEFallthroughContext(ExpressionParser.ConditionalExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterCEFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitCEFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitCEFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class COFallthroughContext extends ExpressionParser.ConditionalOrExpressionContext {
      public ExpressionParser.ConditionalAndExpressionContext conditionalAndExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalAndExpressionContext.class, 0);
      }

      public COFallthroughContext(ExpressionParser.ConditionalOrExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterCOFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitCOFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitCOFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class CaseContext extends ExpressionParser.SwitchLabelContext {
      public ExpressionParser.ConstantExpressionContext constant;

      public TerminalNode CASE() {
         return this.getToken(46, 0);
      }

      public ExpressionParser.ConstantExpressionContext constantExpression() {
         return this.getRuleContext(ExpressionParser.ConstantExpressionContext.class, 0);
      }

      public CaseContext(ExpressionParser.SwitchLabelContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterCase(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitCase(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitCase(this) : visitor.visitChildren(this));
      }
   }

   public static class ComplementExprContext extends ExpressionParser.UnaryExpressionContext {
      public ExpressionParser.UnaryExpressionContext expr;

      public TerminalNode COMPLEMENT() {
         return this.getToken(10, 0);
      }

      public ExpressionParser.UnaryExpressionContext unaryExpression() {
         return this.getRuleContext(ExpressionParser.UnaryExpressionContext.class, 0);
      }

      public ComplementExprContext(ExpressionParser.UnaryExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterComplementExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitComplementExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitComplementExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class ConditionalAndExprContext extends ExpressionParser.ConditionalAndExpressionContext {
      public ExpressionParser.ConditionalAndExpressionContext left;
      public ExpressionParser.EqualityExpressionContext right;

      public TerminalNode AND_SC() {
         return this.getToken(24, 0);
      }

      public ExpressionParser.ConditionalAndExpressionContext conditionalAndExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalAndExpressionContext.class, 0);
      }

      public ExpressionParser.EqualityExpressionContext equalityExpression() {
         return this.getRuleContext(ExpressionParser.EqualityExpressionContext.class, 0);
      }

      public ConditionalAndExprContext(ExpressionParser.ConditionalAndExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterConditionalAndExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitConditionalAndExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitConditionalAndExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class ConditionalAndExpressionContext extends ParserRuleContext {
      public ConditionalAndExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 22;
      }

      public ConditionalAndExpressionContext() {
      }

      public void copyFrom(ExpressionParser.ConditionalAndExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ConditionalExpressionContext extends ParserRuleContext {
      public ConditionalExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 20;
      }

      public ConditionalExpressionContext() {
      }

      public void copyFrom(ExpressionParser.ConditionalExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ConditionalOrExprContext extends ExpressionParser.ConditionalOrExpressionContext {
      public ExpressionParser.ConditionalOrExpressionContext left;
      public ExpressionParser.ConditionalAndExpressionContext right;

      public TerminalNode OR_SC() {
         return this.getToken(25, 0);
      }

      public ExpressionParser.ConditionalOrExpressionContext conditionalOrExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalOrExpressionContext.class, 0);
      }

      public ExpressionParser.ConditionalAndExpressionContext conditionalAndExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalAndExpressionContext.class, 0);
      }

      public ConditionalOrExprContext(ExpressionParser.ConditionalOrExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterConditionalOrExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitConditionalOrExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitConditionalOrExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class ConditionalOrExpressionContext extends ParserRuleContext {
      public ConditionalOrExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 21;
      }

      public ConditionalOrExpressionContext() {
      }

      public void copyFrom(ExpressionParser.ConditionalOrExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ConstantExprContext extends ExpressionParser.UnprioritizedExpressionContext {
      public ExpressionParser.ConstantExpressionContext constantExpression() {
         return this.getRuleContext(ExpressionParser.ConstantExpressionContext.class, 0);
      }

      public ConstantExprContext(ExpressionParser.UnprioritizedExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterConstantExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitConstantExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitConstantExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class ConstantExpressionContext extends ParserRuleContext {
      public TerminalNode NUMBER() {
         return this.getToken(48, 0);
      }

      public ConstantExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 32;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterConstantExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitConstantExpression(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitConstantExpression(this) : visitor.visitChildren(this));
      }
   }

   public static class ContinueStatementContext extends ParserRuleContext {
      public TerminalNode CONTINUE() {
         return this.getToken(43, 0);
      }

      public ContinueStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 10;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterContinueStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitContinueStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitContinueStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class DefaultContext extends ExpressionParser.SwitchLabelContext {
      public TerminalNode DEFAULT() {
         return this.getToken(47, 0);
      }

      public DefaultContext(ExpressionParser.SwitchLabelContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterDefault(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitDefault(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitDefault(this) : visitor.visitChildren(this));
      }
   }

   public static class DoStatementContext extends ParserRuleContext {
      public ExpressionParser.StatementContext body;
      public ExpressionParser.ExpressionContext condition;

      public TerminalNode DO() {
         return this.getToken(40, 0);
      }

      public TerminalNode WHILE() {
         return this.getToken(39, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public ExpressionParser.StatementContext statement() {
         return this.getRuleContext(ExpressionParser.StatementContext.class, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public DoStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 6;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterDoStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitDoStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitDoStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class EmptyStatementContext extends ParserRuleContext {
      public TerminalNode SEMI_COLON() {
         return this.getToken(33, 0);
      }

      public EmptyStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 15;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterEmptyStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitEmptyStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitEmptyStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class EqFallthroughContext extends ExpressionParser.EqualityExpressionContext {
      public ExpressionParser.RelationalExpressionContext relationalExpression() {
         return this.getRuleContext(ExpressionParser.RelationalExpressionContext.class, 0);
      }

      public EqFallthroughContext(ExpressionParser.EqualityExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterEqFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitEqFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitEqFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class EqualityExprContext extends ExpressionParser.EqualityExpressionContext {
      public ExpressionParser.EqualityExpressionContext left;
      public Token op;
      public ExpressionParser.RelationalExpressionContext right;

      public ExpressionParser.EqualityExpressionContext equalityExpression() {
         return this.getRuleContext(ExpressionParser.EqualityExpressionContext.class, 0);
      }

      public ExpressionParser.RelationalExpressionContext relationalExpression() {
         return this.getRuleContext(ExpressionParser.RelationalExpressionContext.class, 0);
      }

      public TerminalNode EQUAL() {
         return this.getToken(17, 0);
      }

      public TerminalNode NOT_EQUAL() {
         return this.getToken(18, 0);
      }

      public TerminalNode NEAR() {
         return this.getToken(19, 0);
      }

      public EqualityExprContext(ExpressionParser.EqualityExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterEqualityExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitEqualityExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitEqualityExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class EqualityExpressionContext extends ParserRuleContext {
      public EqualityExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 23;
      }

      public EqualityExpressionContext() {
      }

      public void copyFrom(ExpressionParser.EqualityExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ExpressionContext extends ParserRuleContext {
      public ExpressionParser.AssignmentExpressionContext assignmentExpression() {
         return this.getRuleContext(ExpressionParser.AssignmentExpressionContext.class, 0);
      }

      public ExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 16;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterExpression(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitExpression(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitExpression(this) : visitor.visitChildren(this));
      }
   }

   public static class ExpressionStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public ExpressionStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 14;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterExpressionStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitExpressionStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitExpressionStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class ForStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext init;
      public ExpressionParser.ExpressionContext condition;
      public ExpressionParser.ExpressionContext update;
      public ExpressionParser.StatementContext body;

      public TerminalNode FOR() {
         return this.getToken(41, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public List<TerminalNode> SEMI_COLON() {
         return this.getTokens(33);
      }

      public TerminalNode SEMI_COLON(int i) {
         return this.getToken(33, i);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public List<ExpressionParser.ExpressionContext> expression() {
         return this.getRuleContexts(ExpressionParser.ExpressionContext.class);
      }

      public ExpressionParser.ExpressionContext expression(int i) {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, i);
      }

      public ExpressionParser.StatementContext statement() {
         return this.getRuleContext(ExpressionParser.StatementContext.class, 0);
      }

      public ForStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 7;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterForStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitForStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitForStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class FunctionCallContext extends ParserRuleContext {
      public Token name;
      public ExpressionParser.ExpressionContext expression;
      public List<ExpressionParser.ExpressionContext> args = new ArrayList<>();

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public List<ExpressionParser.ExpressionContext> expression() {
         return this.getRuleContexts(ExpressionParser.ExpressionContext.class);
      }

      public ExpressionParser.ExpressionContext expression(int i) {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, i);
      }

      public List<TerminalNode> COMMA() {
         return this.getTokens(28);
      }

      public TerminalNode COMMA(int i) {
         return this.getToken(28, i);
      }

      public FunctionCallContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 33;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterFunctionCall(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitFunctionCall(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitFunctionCall(this) : visitor.visitChildren(this));
      }
   }

   public static class FunctionCallExprContext extends ExpressionParser.UnprioritizedExpressionContext {
      public ExpressionParser.FunctionCallContext functionCall() {
         return this.getRuleContext(ExpressionParser.FunctionCallContext.class, 0);
      }

      public FunctionCallExprContext(ExpressionParser.UnprioritizedExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterFunctionCallExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitFunctionCallExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitFunctionCallExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class IdExprContext extends ExpressionParser.UnprioritizedExpressionContext {
      public Token source;

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public IdExprContext(ExpressionParser.UnprioritizedExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterIdExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitIdExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitIdExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class IfStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext condition;
      public ExpressionParser.StatementContext trueBranch;
      public ExpressionParser.StatementContext falseBranch;

      public TerminalNode IF() {
         return this.getToken(37, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public List<ExpressionParser.StatementContext> statement() {
         return this.getRuleContexts(ExpressionParser.StatementContext.class);
      }

      public ExpressionParser.StatementContext statement(int i) {
         return this.getRuleContext(ExpressionParser.StatementContext.class, i);
      }

      public TerminalNode ELSE() {
         return this.getToken(38, 0);
      }

      public IfStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 4;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterIfStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitIfStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitIfStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class MuFallthroughContext extends ExpressionParser.MultiplicativeExpressionContext {
      public ExpressionParser.PowerExpressionContext powerExpression() {
         return this.getRuleContext(ExpressionParser.PowerExpressionContext.class, 0);
      }

      public MuFallthroughContext(ExpressionParser.MultiplicativeExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterMuFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitMuFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitMuFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class MultiplicativeExprContext extends ExpressionParser.MultiplicativeExpressionContext {
      public ExpressionParser.MultiplicativeExpressionContext left;
      public Token op;
      public ExpressionParser.PowerExpressionContext right;

      public ExpressionParser.MultiplicativeExpressionContext multiplicativeExpression() {
         return this.getRuleContext(ExpressionParser.MultiplicativeExpressionContext.class, 0);
      }

      public ExpressionParser.PowerExpressionContext powerExpression() {
         return this.getRuleContext(ExpressionParser.PowerExpressionContext.class, 0);
      }

      public TerminalNode TIMES() {
         return this.getToken(3, 0);
      }

      public TerminalNode DIVIDE() {
         return this.getToken(4, 0);
      }

      public TerminalNode MODULO() {
         return this.getToken(5, 0);
      }

      public MultiplicativeExprContext(ExpressionParser.MultiplicativeExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterMultiplicativeExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitMultiplicativeExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitMultiplicativeExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class MultiplicativeExpressionContext extends ParserRuleContext {
      public MultiplicativeExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 27;
      }

      public MultiplicativeExpressionContext() {
      }

      public void copyFrom(ExpressionParser.MultiplicativeExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class NotExprContext extends ExpressionParser.UnaryExpressionContext {
      public ExpressionParser.UnaryExpressionContext expr;

      public TerminalNode EXCLAMATION_MARK() {
         return this.getToken(36, 0);
      }

      public ExpressionParser.UnaryExpressionContext unaryExpression() {
         return this.getRuleContext(ExpressionParser.UnaryExpressionContext.class, 0);
      }

      public NotExprContext(ExpressionParser.UnaryExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterNotExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitNotExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitNotExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PlusMinusExprContext extends ExpressionParser.UnaryExpressionContext {
      public Token op;
      public ExpressionParser.UnaryExpressionContext expr;

      public ExpressionParser.UnaryExpressionContext unaryExpression() {
         return this.getRuleContext(ExpressionParser.UnaryExpressionContext.class, 0);
      }

      public TerminalNode PLUS() {
         return this.getToken(1, 0);
      }

      public TerminalNode MINUS() {
         return this.getToken(2, 0);
      }

      public PlusMinusExprContext(ExpressionParser.UnaryExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPlusMinusExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPlusMinusExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPlusMinusExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PoFallthroughContext extends ExpressionParser.PostfixExpressionContext {
      public ExpressionParser.UnprioritizedExpressionContext unprioritizedExpression() {
         return this.getRuleContext(ExpressionParser.UnprioritizedExpressionContext.class, 0);
      }

      public PoFallthroughContext(ExpressionParser.PostfixExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPoFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPoFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPoFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class PostCrementExprContext extends ExpressionParser.PostfixExpressionContext {
      public Token target;
      public Token op;

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public TerminalNode INCREMENT() {
         return this.getToken(26, 0);
      }

      public TerminalNode DECREMENT() {
         return this.getToken(27, 0);
      }

      public PostCrementExprContext(ExpressionParser.PostfixExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPostCrementExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPostCrementExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPostCrementExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PostfixExprContext extends ExpressionParser.PostfixExpressionContext {
      public ExpressionParser.PostfixExpressionContext expr;
      public Token op;

      public ExpressionParser.PostfixExpressionContext postfixExpression() {
         return this.getRuleContext(ExpressionParser.PostfixExpressionContext.class, 0);
      }

      public TerminalNode EXCLAMATION_MARK() {
         return this.getToken(36, 0);
      }

      public PostfixExprContext(ExpressionParser.PostfixExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPostfixExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPostfixExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPostfixExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PostfixExpressionContext extends ParserRuleContext {
      public PostfixExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 30;
      }

      public PostfixExpressionContext() {
      }

      public void copyFrom(ExpressionParser.PostfixExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class PowerExprContext extends ExpressionParser.PowerExpressionContext {
      public ExpressionParser.PowerExpressionContext left;
      public ExpressionParser.UnaryExpressionContext right;

      public TerminalNode POWER() {
         return this.getToken(6, 0);
      }

      public ExpressionParser.PowerExpressionContext powerExpression() {
         return this.getRuleContext(ExpressionParser.PowerExpressionContext.class, 0);
      }

      public ExpressionParser.UnaryExpressionContext unaryExpression() {
         return this.getRuleContext(ExpressionParser.UnaryExpressionContext.class, 0);
      }

      public PowerExprContext(ExpressionParser.PowerExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPowerExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPowerExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPowerExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PowerExpressionContext extends ParserRuleContext {
      public PowerExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 28;
      }

      public PowerExpressionContext() {
      }

      public void copyFrom(ExpressionParser.PowerExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class PreCrementExprContext extends ExpressionParser.UnaryExpressionContext {
      public Token op;
      public Token target;

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public TerminalNode INCREMENT() {
         return this.getToken(26, 0);
      }

      public TerminalNode DECREMENT() {
         return this.getToken(27, 0);
      }

      public PreCrementExprContext(ExpressionParser.UnaryExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPreCrementExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPreCrementExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPreCrementExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class PwFallthroughContext extends ExpressionParser.PowerExpressionContext {
      public ExpressionParser.UnaryExpressionContext unaryExpression() {
         return this.getRuleContext(ExpressionParser.UnaryExpressionContext.class, 0);
      }

      public PwFallthroughContext(ExpressionParser.PowerExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterPwFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitPwFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitPwFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class ReFallthroughContext extends ExpressionParser.RelationalExpressionContext {
      public ExpressionParser.ShiftExpressionContext shiftExpression() {
         return this.getRuleContext(ExpressionParser.ShiftExpressionContext.class, 0);
      }

      public ReFallthroughContext(ExpressionParser.RelationalExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterReFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitReFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitReFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class RelationalExprContext extends ExpressionParser.RelationalExpressionContext {
      public ExpressionParser.RelationalExpressionContext left;
      public Token op;
      public ExpressionParser.ShiftExpressionContext right;

      public ExpressionParser.RelationalExpressionContext relationalExpression() {
         return this.getRuleContext(ExpressionParser.RelationalExpressionContext.class, 0);
      }

      public ExpressionParser.ShiftExpressionContext shiftExpression() {
         return this.getRuleContext(ExpressionParser.ShiftExpressionContext.class, 0);
      }

      public TerminalNode LESS_THAN() {
         return this.getToken(20, 0);
      }

      public TerminalNode GREATER_THAN() {
         return this.getToken(22, 0);
      }

      public TerminalNode LESS_THAN_OR_EQUAL() {
         return this.getToken(21, 0);
      }

      public TerminalNode GREATER_THAN_OR_EQUAL() {
         return this.getToken(23, 0);
      }

      public RelationalExprContext(ExpressionParser.RelationalExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterRelationalExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitRelationalExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitRelationalExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class RelationalExpressionContext extends ParserRuleContext {
      public RelationalExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 24;
      }

      public RelationalExpressionContext() {
      }

      public void copyFrom(ExpressionParser.RelationalExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class ReturnStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext value;

      public TerminalNode RETURN() {
         return this.getToken(44, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public ReturnStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 11;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterReturnStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitReturnStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitReturnStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class ShFallthroughContext extends ExpressionParser.ShiftExpressionContext {
      public ExpressionParser.AdditiveExpressionContext additiveExpression() {
         return this.getRuleContext(ExpressionParser.AdditiveExpressionContext.class, 0);
      }

      public ShFallthroughContext(ExpressionParser.ShiftExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterShFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitShFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitShFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class ShiftExprContext extends ExpressionParser.ShiftExpressionContext {
      public ExpressionParser.ShiftExpressionContext left;
      public Token op;
      public ExpressionParser.AdditiveExpressionContext right;

      public ExpressionParser.ShiftExpressionContext shiftExpression() {
         return this.getRuleContext(ExpressionParser.ShiftExpressionContext.class, 0);
      }

      public ExpressionParser.AdditiveExpressionContext additiveExpression() {
         return this.getRuleContext(ExpressionParser.AdditiveExpressionContext.class, 0);
      }

      public TerminalNode LEFT_SHIFT() {
         return this.getToken(7, 0);
      }

      public TerminalNode RIGHT_SHIFT() {
         return this.getToken(8, 0);
      }

      public ShiftExprContext(ExpressionParser.ShiftExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterShiftExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitShiftExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitShiftExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class ShiftExpressionContext extends ParserRuleContext {
      public ShiftExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 25;
      }

      public ShiftExpressionContext() {
      }

      public void copyFrom(ExpressionParser.ShiftExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class SimpleForStatementContext extends ParserRuleContext {
      public Token counter;
      public ExpressionParser.ExpressionContext first;
      public ExpressionParser.ExpressionContext last;
      public ExpressionParser.StatementContext body;

      public TerminalNode FOR() {
         return this.getToken(41, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode ASSIGN() {
         return this.getToken(9, 0);
      }

      public TerminalNode COMMA() {
         return this.getToken(28, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public TerminalNode ID() {
         return this.getToken(49, 0);
      }

      public List<ExpressionParser.ExpressionContext> expression() {
         return this.getRuleContexts(ExpressionParser.ExpressionContext.class);
      }

      public ExpressionParser.ExpressionContext expression(int i) {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, i);
      }

      public ExpressionParser.StatementContext statement() {
         return this.getRuleContext(ExpressionParser.StatementContext.class, 0);
      }

      public SimpleForStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 8;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterSimpleForStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitSimpleForStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitSimpleForStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class StatementContext extends ParserRuleContext {
      public ExpressionParser.BlockContext block() {
         return this.getRuleContext(ExpressionParser.BlockContext.class, 0);
      }

      public ExpressionParser.IfStatementContext ifStatement() {
         return this.getRuleContext(ExpressionParser.IfStatementContext.class, 0);
      }

      public ExpressionParser.WhileStatementContext whileStatement() {
         return this.getRuleContext(ExpressionParser.WhileStatementContext.class, 0);
      }

      public ExpressionParser.DoStatementContext doStatement() {
         return this.getRuleContext(ExpressionParser.DoStatementContext.class, 0);
      }

      public ExpressionParser.ForStatementContext forStatement() {
         return this.getRuleContext(ExpressionParser.ForStatementContext.class, 0);
      }

      public ExpressionParser.SimpleForStatementContext simpleForStatement() {
         return this.getRuleContext(ExpressionParser.SimpleForStatementContext.class, 0);
      }

      public ExpressionParser.BreakStatementContext breakStatement() {
         return this.getRuleContext(ExpressionParser.BreakStatementContext.class, 0);
      }

      public ExpressionParser.ContinueStatementContext continueStatement() {
         return this.getRuleContext(ExpressionParser.ContinueStatementContext.class, 0);
      }

      public ExpressionParser.ReturnStatementContext returnStatement() {
         return this.getRuleContext(ExpressionParser.ReturnStatementContext.class, 0);
      }

      public ExpressionParser.SwitchStatementContext switchStatement() {
         return this.getRuleContext(ExpressionParser.SwitchStatementContext.class, 0);
      }

      public ExpressionParser.ExpressionStatementContext expressionStatement() {
         return this.getRuleContext(ExpressionParser.ExpressionStatementContext.class, 0);
      }

      public ExpressionParser.EmptyStatementContext emptyStatement() {
         return this.getRuleContext(ExpressionParser.EmptyStatementContext.class, 0);
      }

      public TerminalNode SEMI_COLON() {
         return this.getToken(33, 0);
      }

      public StatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 2;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class StatementsContext extends ParserRuleContext {
      public List<ExpressionParser.StatementContext> statement() {
         return this.getRuleContexts(ExpressionParser.StatementContext.class);
      }

      public ExpressionParser.StatementContext statement(int i) {
         return this.getRuleContext(ExpressionParser.StatementContext.class, i);
      }

      public StatementsContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 1;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterStatements(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitStatements(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitStatements(this) : visitor.visitChildren(this));
      }
   }

   public static class SwitchLabelContext extends ParserRuleContext {
      public SwitchLabelContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 13;
      }

      public SwitchLabelContext() {
      }

      public void copyFrom(ExpressionParser.SwitchLabelContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class SwitchStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext target;
      public ExpressionParser.SwitchLabelContext switchLabel;
      public List<ExpressionParser.SwitchLabelContext> labels = new ArrayList<>();
      public ExpressionParser.StatementsContext statements;
      public List<ExpressionParser.StatementsContext> bodies = new ArrayList<>();

      public TerminalNode SWITCH() {
         return this.getToken(45, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public TerminalNode OPEN_BRACKET() {
         return this.getToken(31, 0);
      }

      public TerminalNode CLOSE_BRACKET() {
         return this.getToken(32, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public List<TerminalNode> COLON() {
         return this.getTokens(35);
      }

      public TerminalNode COLON(int i) {
         return this.getToken(35, i);
      }

      public List<ExpressionParser.SwitchLabelContext> switchLabel() {
         return this.getRuleContexts(ExpressionParser.SwitchLabelContext.class);
      }

      public ExpressionParser.SwitchLabelContext switchLabel(int i) {
         return this.getRuleContext(ExpressionParser.SwitchLabelContext.class, i);
      }

      public List<ExpressionParser.StatementsContext> statements() {
         return this.getRuleContexts(ExpressionParser.StatementsContext.class);
      }

      public ExpressionParser.StatementsContext statements(int i) {
         return this.getRuleContext(ExpressionParser.StatementsContext.class, i);
      }

      public SwitchStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 12;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterSwitchStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitSwitchStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitSwitchStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class TernaryExprContext extends ExpressionParser.ConditionalExpressionContext {
      public ExpressionParser.ConditionalOrExpressionContext condition;
      public ExpressionParser.ExpressionContext trueBranch;
      public ExpressionParser.ConditionalExpressionContext falseBranch;

      public TerminalNode QUESTION_MARK() {
         return this.getToken(34, 0);
      }

      public TerminalNode COLON() {
         return this.getToken(35, 0);
      }

      public ExpressionParser.ConditionalOrExpressionContext conditionalOrExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalOrExpressionContext.class, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public ExpressionParser.ConditionalExpressionContext conditionalExpression() {
         return this.getRuleContext(ExpressionParser.ConditionalExpressionContext.class, 0);
      }

      public TernaryExprContext(ExpressionParser.ConditionalExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterTernaryExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitTernaryExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitTernaryExpr(this) : visitor.visitChildren(this));
      }
   }

   public static class UaFallthroughContext extends ExpressionParser.UnaryExpressionContext {
      public ExpressionParser.PostfixExpressionContext postfixExpression() {
         return this.getRuleContext(ExpressionParser.PostfixExpressionContext.class, 0);
      }

      public UaFallthroughContext(ExpressionParser.UnaryExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterUaFallthrough(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitUaFallthrough(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitUaFallthrough(this) : visitor.visitChildren(this));
      }
   }

   public static class UnaryExpressionContext extends ParserRuleContext {
      public UnaryExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 29;
      }

      public UnaryExpressionContext() {
      }

      public void copyFrom(ExpressionParser.UnaryExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class UnprioritizedExpressionContext extends ParserRuleContext {
      public UnprioritizedExpressionContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 31;
      }

      public UnprioritizedExpressionContext() {
      }

      public void copyFrom(ExpressionParser.UnprioritizedExpressionContext ctx) {
         super.copyFrom(ctx);
      }
   }

   public static class WhileStatementContext extends ParserRuleContext {
      public ExpressionParser.ExpressionContext condition;
      public ExpressionParser.StatementContext body;

      public TerminalNode WHILE() {
         return this.getToken(39, 0);
      }

      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public ExpressionParser.StatementContext statement() {
         return this.getRuleContext(ExpressionParser.StatementContext.class, 0);
      }

      public WhileStatementContext(ParserRuleContext parent, int invokingState) {
         super(parent, invokingState);
      }

      @Override
      public int getRuleIndex() {
         return 5;
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterWhileStatement(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitWhileStatement(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitWhileStatement(this) : visitor.visitChildren(this));
      }
   }

   public static class WrappedExprContext extends ExpressionParser.UnprioritizedExpressionContext {
      public TerminalNode OPEN_PAREN() {
         return this.getToken(29, 0);
      }

      public ExpressionParser.ExpressionContext expression() {
         return this.getRuleContext(ExpressionParser.ExpressionContext.class, 0);
      }

      public TerminalNode CLOSE_PAREN() {
         return this.getToken(30, 0);
      }

      public WrappedExprContext(ExpressionParser.UnprioritizedExpressionContext ctx) {
         this.copyFrom(ctx);
      }

      @Override
      public void enterRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).enterWrappedExpr(this);
         }
      }

      @Override
      public void exitRule(ParseTreeListener listener) {
         if (listener instanceof ExpressionListener) {
            ((ExpressionListener)listener).exitWrappedExpr(this);
         }
      }

      @Override
      public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
         return (T)(visitor instanceof ExpressionVisitor ? ((ExpressionVisitor)visitor).visitWrappedExpr(this) : visitor.visitChildren(this));
      }
   }
}
