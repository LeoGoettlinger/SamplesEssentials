package su.nightexpress.nightcore.util.problem;

import java.util.List;
import java.util.logging.Logger;
import org.jspecify.annotations.NonNull;

public interface ProblemReporter {
   @NonNull
   String getSubject();

   @NonNull
   String getPath();

   @NonNull
   List<Problem> getProblems();

   @NonNull
   String getReport();

   List<Problem.ChildReport> getChildren();

   boolean isEmpty();

   void print(@NonNull Logger var1);

   void report(@NonNull Problem var1);

   void report(@NonNull String var1);

   void children(@NonNull String var1, @NonNull ProblemReporter var2);

   int countProblems();
}
