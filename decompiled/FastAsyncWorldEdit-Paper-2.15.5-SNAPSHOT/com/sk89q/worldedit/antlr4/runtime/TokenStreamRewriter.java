package com.sk89q.worldedit.antlr4.runtime;

import com.sk89q.worldedit.antlr4.runtime.misc.Interval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenStreamRewriter {
   public static final String DEFAULT_PROGRAM_NAME = "default";
   public static final int PROGRAM_INIT_SIZE = 100;
   public static final int MIN_TOKEN_INDEX = 0;
   protected final TokenStream tokens;
   protected final Map<String, List<TokenStreamRewriter.RewriteOperation>> programs;
   protected final Map<String, Integer> lastRewriteTokenIndexes;

   public TokenStreamRewriter(TokenStream tokens) {
      this.tokens = tokens;
      this.programs = new HashMap<>();
      this.programs.put("default", new ArrayList<>(100));
      this.lastRewriteTokenIndexes = new HashMap<>();
   }

   public final TokenStream getTokenStream() {
      return this.tokens;
   }

   public void rollback(int instructionIndex) {
      this.rollback("default", instructionIndex);
   }

   public void rollback(String programName, int instructionIndex) {
      List<TokenStreamRewriter.RewriteOperation> is = this.programs.get(programName);
      if (is != null) {
         this.programs.put(programName, is.subList(0, instructionIndex));
      }
   }

   public void deleteProgram() {
      this.deleteProgram("default");
   }

   public void deleteProgram(String programName) {
      this.rollback(programName, 0);
   }

   public void insertAfter(Token t, Object text) {
      this.insertAfter("default", t, text);
   }

   public void insertAfter(int index, Object text) {
      this.insertAfter("default", index, text);
   }

   public void insertAfter(String programName, Token t, Object text) {
      this.insertAfter(programName, t.getTokenIndex(), text);
   }

   public void insertAfter(String programName, int index, Object text) {
      TokenStreamRewriter.RewriteOperation op = new TokenStreamRewriter.InsertAfterOp(index, text);
      List<TokenStreamRewriter.RewriteOperation> rewrites = this.getProgram(programName);
      op.instructionIndex = rewrites.size();
      rewrites.add(op);
   }

   public void insertBefore(Token t, Object text) {
      this.insertBefore("default", t, text);
   }

   public void insertBefore(int index, Object text) {
      this.insertBefore("default", index, text);
   }

   public void insertBefore(String programName, Token t, Object text) {
      this.insertBefore(programName, t.getTokenIndex(), text);
   }

   public void insertBefore(String programName, int index, Object text) {
      TokenStreamRewriter.RewriteOperation op = new TokenStreamRewriter.InsertBeforeOp(index, text);
      List<TokenStreamRewriter.RewriteOperation> rewrites = this.getProgram(programName);
      op.instructionIndex = rewrites.size();
      rewrites.add(op);
   }

   public void replace(int index, Object text) {
      this.replace("default", index, index, text);
   }

   public void replace(int from, int to, Object text) {
      this.replace("default", from, to, text);
   }

   public void replace(Token indexT, Object text) {
      this.replace("default", indexT, indexT, text);
   }

   public void replace(Token from, Token to, Object text) {
      this.replace("default", from, to, text);
   }

   public void replace(String programName, int from, int to, Object text) {
      if (from <= to && from >= 0 && to >= 0 && to < this.tokens.size()) {
         TokenStreamRewriter.RewriteOperation op = new TokenStreamRewriter.ReplaceOp(from, to, text);
         List<TokenStreamRewriter.RewriteOperation> rewrites = this.getProgram(programName);
         op.instructionIndex = rewrites.size();
         rewrites.add(op);
      } else {
         throw new IllegalArgumentException("replace: range invalid: " + from + ".." + to + "(size=" + this.tokens.size() + ")");
      }
   }

   public void replace(String programName, Token from, Token to, Object text) {
      this.replace(programName, from.getTokenIndex(), to.getTokenIndex(), text);
   }

   public void delete(int index) {
      this.delete("default", index, index);
   }

   public void delete(int from, int to) {
      this.delete("default", from, to);
   }

   public void delete(Token indexT) {
      this.delete("default", indexT, indexT);
   }

   public void delete(Token from, Token to) {
      this.delete("default", from, to);
   }

   public void delete(String programName, int from, int to) {
      this.replace(programName, from, to, null);
   }

   public void delete(String programName, Token from, Token to) {
      this.replace(programName, from, to, null);
   }

   public int getLastRewriteTokenIndex() {
      return this.getLastRewriteTokenIndex("default");
   }

   protected int getLastRewriteTokenIndex(String programName) {
      Integer I = this.lastRewriteTokenIndexes.get(programName);
      return I == null ? -1 : I;
   }

   protected void setLastRewriteTokenIndex(String programName, int i) {
      this.lastRewriteTokenIndexes.put(programName, i);
   }

   protected List<TokenStreamRewriter.RewriteOperation> getProgram(String name) {
      List<TokenStreamRewriter.RewriteOperation> is = this.programs.get(name);
      if (is == null) {
         is = this.initializeProgram(name);
      }

      return is;
   }

   private List<TokenStreamRewriter.RewriteOperation> initializeProgram(String name) {
      List<TokenStreamRewriter.RewriteOperation> is = new ArrayList<>(100);
      this.programs.put(name, is);
      return is;
   }

   public String getText() {
      return this.getText("default", Interval.of(0, this.tokens.size() - 1));
   }

   public String getText(String programName) {
      return this.getText(programName, Interval.of(0, this.tokens.size() - 1));
   }

   public String getText(Interval interval) {
      return this.getText("default", interval);
   }

   public String getText(String programName, Interval interval) {
      List<TokenStreamRewriter.RewriteOperation> rewrites = this.programs.get(programName);
      int start = interval.a;
      int stop = interval.b;
      if (stop > this.tokens.size() - 1) {
         stop = this.tokens.size() - 1;
      }

      if (start < 0) {
         start = 0;
      }

      if (rewrites != null && !rewrites.isEmpty()) {
         StringBuilder buf = new StringBuilder();
         Map<Integer, TokenStreamRewriter.RewriteOperation> indexToOp = this.reduceToSingleOperationPerIndex(rewrites);
         int i = start;

         while (i <= stop && i < this.tokens.size()) {
            TokenStreamRewriter.RewriteOperation op = indexToOp.get(i);
            indexToOp.remove(i);
            Token t = this.tokens.get(i);
            if (op == null) {
               if (t.getType() != -1) {
                  buf.append(t.getText());
               }

               i++;
            } else {
               i = op.execute(buf);
            }
         }

         if (stop == this.tokens.size() - 1) {
            for (TokenStreamRewriter.RewriteOperation op : indexToOp.values()) {
               if (op.index >= this.tokens.size() - 1) {
                  buf.append(op.text);
               }
            }
         }

         return buf.toString();
      } else {
         return this.tokens.getText(interval);
      }
   }

   protected Map<Integer, TokenStreamRewriter.RewriteOperation> reduceToSingleOperationPerIndex(List<TokenStreamRewriter.RewriteOperation> rewrites) {
      for (int i = 0; i < rewrites.size(); i++) {
         TokenStreamRewriter.RewriteOperation op = rewrites.get(i);
         if (op != null && op instanceof TokenStreamRewriter.ReplaceOp) {
            TokenStreamRewriter.ReplaceOp rop = (TokenStreamRewriter.ReplaceOp)rewrites.get(i);

            for (TokenStreamRewriter.InsertBeforeOp iop : this.getKindOfOps(rewrites, TokenStreamRewriter.InsertBeforeOp.class, i)) {
               if (iop.index == rop.index) {
                  rewrites.set(iop.instructionIndex, null);
                  rop.text = iop.text.toString() + (rop.text != null ? rop.text.toString() : "");
               } else if (iop.index > rop.index && iop.index <= rop.lastIndex) {
                  rewrites.set(iop.instructionIndex, null);
               }
            }

            for (TokenStreamRewriter.ReplaceOp prevRop : this.getKindOfOps(rewrites, TokenStreamRewriter.ReplaceOp.class, i)) {
               if (prevRop.index >= rop.index && prevRop.lastIndex <= rop.lastIndex) {
                  rewrites.set(prevRop.instructionIndex, null);
               } else {
                  boolean disjoint = prevRop.lastIndex < rop.index || prevRop.index > rop.lastIndex;
                  if (prevRop.text == null && rop.text == null && !disjoint) {
                     rewrites.set(prevRop.instructionIndex, null);
                     rop.index = Math.min(prevRop.index, rop.index);
                     rop.lastIndex = Math.max(prevRop.lastIndex, rop.lastIndex);
                     System.out.println("new rop " + rop);
                  } else if (!disjoint) {
                     throw new IllegalArgumentException("replace op boundaries of " + rop + " overlap with previous " + prevRop);
                  }
               }
            }
         }
      }

      for (int ix = 0; ix < rewrites.size(); ix++) {
         TokenStreamRewriter.RewriteOperation op = rewrites.get(ix);
         if (op != null && op instanceof TokenStreamRewriter.InsertBeforeOp) {
            TokenStreamRewriter.InsertBeforeOp iopx = (TokenStreamRewriter.InsertBeforeOp)rewrites.get(ix);

            for (TokenStreamRewriter.InsertBeforeOp prevIop : this.getKindOfOps(rewrites, TokenStreamRewriter.InsertBeforeOp.class, ix)) {
               if (prevIop.index == iopx.index) {
                  if (TokenStreamRewriter.InsertAfterOp.class.isInstance(prevIop)) {
                     iopx.text = this.catOpText(prevIop.text, iopx.text);
                     rewrites.set(prevIop.instructionIndex, null);
                  } else if (TokenStreamRewriter.InsertBeforeOp.class.isInstance(prevIop)) {
                     iopx.text = this.catOpText(iopx.text, prevIop.text);
                     rewrites.set(prevIop.instructionIndex, null);
                  }
               }
            }

            for (TokenStreamRewriter.ReplaceOp rop : this.getKindOfOps(rewrites, TokenStreamRewriter.ReplaceOp.class, ix)) {
               if (iopx.index == rop.index) {
                  rop.text = this.catOpText(iopx.text, rop.text);
                  rewrites.set(ix, null);
               } else if (iopx.index >= rop.index && iopx.index <= rop.lastIndex) {
                  throw new IllegalArgumentException("insert op " + iopx + " within boundaries of previous " + rop);
               }
            }
         }
      }

      Map<Integer, TokenStreamRewriter.RewriteOperation> m = new HashMap<>();

      for (int ixx = 0; ixx < rewrites.size(); ixx++) {
         TokenStreamRewriter.RewriteOperation op = rewrites.get(ixx);
         if (op != null) {
            if (m.get(op.index) != null) {
               throw new Error("should only be one op per index");
            }

            m.put(op.index, op);
         }
      }

      return m;
   }

   protected String catOpText(Object a, Object b) {
      String x = "";
      String y = "";
      if (a != null) {
         x = a.toString();
      }

      if (b != null) {
         y = b.toString();
      }

      return x + y;
   }

   protected <T extends TokenStreamRewriter.RewriteOperation> List<? extends T> getKindOfOps(
      List<? extends TokenStreamRewriter.RewriteOperation> rewrites, Class<T> kind, int before
   ) {
      List<T> ops = new ArrayList<>();

      for (int i = 0; i < before && i < rewrites.size(); i++) {
         TokenStreamRewriter.RewriteOperation op = rewrites.get(i);
         if (op != null && kind.isInstance(op)) {
            ops.add(kind.cast(op));
         }
      }

      return ops;
   }

   class InsertAfterOp extends TokenStreamRewriter.InsertBeforeOp {
      public InsertAfterOp(int index, Object text) {
         super(index + 1, text);
      }
   }

   class InsertBeforeOp extends TokenStreamRewriter.RewriteOperation {
      public InsertBeforeOp(int index, Object text) {
         super(index, text);
      }

      @Override
      public int execute(StringBuilder buf) {
         buf.append(this.text);
         if (TokenStreamRewriter.this.tokens.get(this.index).getType() != -1) {
            buf.append(TokenStreamRewriter.this.tokens.get(this.index).getText());
         }

         return this.index + 1;
      }
   }

   class ReplaceOp extends TokenStreamRewriter.RewriteOperation {
      protected int lastIndex;

      public ReplaceOp(int from, int to, Object text) {
         super(from, text);
         this.lastIndex = to;
      }

      @Override
      public int execute(StringBuilder buf) {
         if (this.text != null) {
            buf.append(this.text);
         }

         return this.lastIndex + 1;
      }

      @Override
      public String toString() {
         return this.text == null
            ? "<DeleteOp@" + TokenStreamRewriter.this.tokens.get(this.index) + ".." + TokenStreamRewriter.this.tokens.get(this.lastIndex) + ">"
            : "<ReplaceOp@"
               + TokenStreamRewriter.this.tokens.get(this.index)
               + ".."
               + TokenStreamRewriter.this.tokens.get(this.lastIndex)
               + ":\""
               + this.text
               + "\">";
      }
   }

   public class RewriteOperation {
      protected int instructionIndex;
      protected int index;
      protected Object text;

      protected RewriteOperation(int index) {
         this.index = index;
      }

      protected RewriteOperation(int index, Object text) {
         this.index = index;
         this.text = text;
      }

      public int execute(StringBuilder buf) {
         return this.index;
      }

      @Override
      public String toString() {
         String opName = this.getClass().getName();
         int $index = opName.indexOf(36);
         opName = opName.substring($index + 1, opName.length());
         return "<" + opName + "@" + TokenStreamRewriter.this.tokens.get(this.index) + ":\"" + this.text + "\">";
      }
   }
}
