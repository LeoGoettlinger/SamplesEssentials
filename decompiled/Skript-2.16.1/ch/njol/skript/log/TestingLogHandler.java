package ch.njol.skript.log;

import ch.njol.skript.config.Node;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.test.runner.EvtTestCase;
import ch.njol.skript.test.runner.TestTracker;
import java.util.logging.Level;
import org.skriptlang.skript.lang.structure.Structure;

public class TestingLogHandler extends LogHandler {
   private final int minimum;
   private int count;
   private final ParserInstance parser;

   public TestingLogHandler(Level minimum) {
      this.minimum = minimum.intValue();
      this.parser = ParserInstance.get();
   }

   @Override
   public LogHandler.LogResult log(LogEntry entry) {
      if (entry.level.intValue() >= this.minimum) {
         this.count++;
         Structure struct = this.parser.getCurrentStructure();
         Node node = this.parser.getNode();
         String name = struct instanceof EvtTestCase test ? test.getTestName() : (struct != null ? struct.getSyntaxTypeName() : null);
         TestTracker.parsingStarted(name);
         if (node != null && this.parser.isActive()) {
            TestTracker.testFailed(entry.getMessage(), this.parser.getCurrentScript(), node.getLine());
         } else {
            TestTracker.testFailed(entry.getMessage());
         }
      }

      return LogHandler.LogResult.LOG;
   }

   public TestingLogHandler start() {
      SkriptLogger.startLogHandler(this);
      return this;
   }

   public int getCount() {
      return this.count;
   }
}
