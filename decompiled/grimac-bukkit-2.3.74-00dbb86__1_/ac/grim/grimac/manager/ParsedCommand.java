package ac.grim.grimac.manager;

class ParsedCommand {
   public final int threshold;
   public final int interval;
   public final String command;
   public int executeCount;
   public int nextBoundary;

   public ParsedCommand(int threshold, int interval, String command) {
      this.threshold = threshold;
      this.interval = interval;
      this.command = command;
      this.nextBoundary = threshold;
   }
}
