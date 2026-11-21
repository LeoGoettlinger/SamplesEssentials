package github.nighter.smartspawner.libs.mariadb.util.timeout;

public class NoOpQueryTimeoutHandler implements QueryTimeoutHandler {
   public static final NoOpQueryTimeoutHandler INSTANCE = new NoOpQueryTimeoutHandler();

   @Override
   public QueryTimeoutHandler create(int queryTimeout) {
      return INSTANCE;
   }

   @Override
   public void close() {
   }
}
