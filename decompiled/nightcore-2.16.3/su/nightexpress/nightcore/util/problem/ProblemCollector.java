package su.nightexpress.nightcore.util.problem;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

public class ProblemCollector implements ProblemReporter {
   private static final boolean USE_COLORS = true;
   private static final String RESET = "\u001b[0m";
   private static final String MAGENTA_BRIGHT = "\u001b[95m";
   private static final String YELLOW_BRIGHT = "\u001b[93m";
   private static final String GRAY = "\u001b[90m";
   private static final String WHITE_DIM = "\u001b[37m";
   private final String subject;
   private final String path;
   private final List<Problem> problems;
   private final List<Problem.ChildReport> children;

   public ProblemCollector(@NonNull String subject, @NonNull String path) {
      this.subject = subject;
      this.path = path;
      this.problems = new ArrayList<>();
      this.children = new ArrayList<>();
   }

   @Override
   public int countProblems() {
      return this.problems.size() + this.children.stream().mapToInt(childReport -> childReport.reporter().countProblems()).sum();
   }

   @Override
   public boolean isEmpty() {
      return this.countProblems() <= 0;
   }

   @Override
   public void report(@NonNull String problem) {
      this.report(() -> problem);
   }

   @Override
   public void report(@NonNull Problem problem) {
      this.problems.add(problem);
   }

   @Override
   public void children(@NonNull String description, @NonNull ProblemReporter reporter) {
      this.children.add(new Problem.ChildReport(description, reporter));
   }

   @Override
   public void print(@NonNull Logger logger) {
      if (!this.isEmpty()) {
         logger.warning(this.getReport());
      }
   }

   @NonNull
   @Override
   public String getReport() {
      StringBuilder builder = new StringBuilder();
      builder.append("\n");
      builder.append(color("\u001b[93m", "*".repeat(10) + " PROBLEM REPORT " + "*".repeat(10))).append("\n");
      builder.append(color("\u001b[37m", "> Subject: ")).append(this.subject).append("\n");
      builder.append(color("\u001b[37m", "> Path: ")).append(this.path).append("\n");
      builder.append(color("\u001b[37m", "> Found Problems (" + this.countProblems() + "):")).append("\n");
      appendTree(this, builder, "");
      builder.append(color("\u001b[93m", "*".repeat(35))).append("\n");
      return builder.toString();
   }

   private static void appendTree(@NonNull ProblemReporter root, @NonNull StringBuilder builder, @NonNull String prefix) {
      List<String> entries = new ArrayList<>();
      List<Problem> problems = root.getProblems();
      List<Problem.ChildReport> children = root.getChildren();

      for (Problem problem : root.getProblems()) {
         entries.add(problem.description());
      }

      for (Problem.ChildReport child : root.getChildren()) {
         entries.add(child.description());
      }

      for (int index = 0; index < entries.size(); index++) {
         boolean last = index == entries.size() - 1;
         String connector = last ? "└── " : "├── ";
         builder.append(color("\u001b[90m", prefix + connector));
         builder.append(color(index < problems.size() ? "\u001b[93m" : "\u001b[95m", entries.get(index)));
         builder.append("\u001b[0m").append("\n");
         if (index >= problems.size()) {
            ProblemReporter child = children.get(index - problems.size()).reporter();
            String newPrefix = prefix + (last ? "    " : "│   ");
            appendTree(child, builder, newPrefix);
         }
      }
   }

   @NonNull
   private static String color(@NonNull String code, @NonNull String text) {
      return code + text + "\u001b[0m";
   }

   @NonNull
   @Override
   public String getSubject() {
      return this.subject;
   }

   @NonNull
   @Override
   public String getPath() {
      return this.path;
   }

   @NonNull
   @Override
   public List<Problem> getProblems() {
      return this.problems;
   }

   @NonNull
   @Override
   public List<Problem.ChildReport> getChildren() {
      return this.children;
   }
}
