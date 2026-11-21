package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.SyntaxElement;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import org.skriptlang.skript.registration.SyntaxInfo;

public class ParsingStack implements Iterable<ParsingStack.Element> {
   private final LinkedList<ParsingStack.Element> stack;

   public ParsingStack() {
      this.stack = new LinkedList<>();
   }

   public ParsingStack(ParsingStack parsingStack) {
      this.stack = new LinkedList<>(parsingStack.stack);
   }

   public ParsingStack.Element pop() throws IllegalStateException {
      if (this.stack.isEmpty()) {
         throw new IllegalStateException("Stack is empty");
      } else {
         return this.stack.pop();
      }
   }

   public ParsingStack.Element peek(int index) throws IndexOutOfBoundsException {
      if (index >= 0 && index < this.size()) {
         return this.stack.get(index);
      } else {
         throw new IndexOutOfBoundsException("Index: " + index);
      }
   }

   public ParsingStack.Element peek() throws IllegalStateException {
      if (this.stack.isEmpty()) {
         throw new IllegalStateException("Stack is empty");
      } else {
         return this.stack.peek();
      }
   }

   public void push(ParsingStack.Element element) {
      this.stack.push(element);
   }

   public boolean isEmpty() {
      return this.stack.isEmpty();
   }

   public int size() {
      return this.stack.size();
   }

   public void print(PrintStream printStream) {
      synchronized (printStream) {
         printStream.println("Stack:");
         if (this.stack.isEmpty()) {
            printStream.println("<empty>");
         } else {
            for (ParsingStack.Element element : this.stack) {
               printStream.println("\t" + element.getSyntaxElementClass().getName() + " @ " + element.patternIndex());
            }
         }
      }
   }

   @Override
   public Iterator<ParsingStack.Element> iterator() {
      return Collections.unmodifiableList(this.stack).iterator();
   }

   public record Element(SyntaxInfo<?> syntaxElementInfo, int patternIndex) {
      public Element(SyntaxInfo<?> syntaxElementInfo, int patternIndex) {
         assert patternIndex >= 0 && patternIndex < syntaxElementInfo.patterns().size();

         this.syntaxElementInfo = syntaxElementInfo;
         this.patternIndex = patternIndex;
      }

      public SyntaxInfo<?> syntaxElementInfo() {
         return this.syntaxInfo();
      }

      public SyntaxInfo<?> syntaxInfo() {
         return this.syntaxElementInfo;
      }

      public Class<? extends SyntaxElement> getSyntaxElementClass() {
         return (Class<? extends SyntaxElement>)this.syntaxElementInfo.type();
      }

      public String getPattern() {
         return this.getPatterns()[this.patternIndex];
      }

      public String[] getPatterns() {
         return this.syntaxElementInfo.patterns().toArray(new String[0]);
      }
   }
}
