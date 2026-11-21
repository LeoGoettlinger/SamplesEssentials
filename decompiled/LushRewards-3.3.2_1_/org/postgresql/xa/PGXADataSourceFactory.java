package org.postgresql.xa;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.ds.common.PGObjectFactory;

public class PGXADataSourceFactory extends PGObjectFactory {
   @Nullable
   @Override
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
      Reference ref = (Reference)obj;
      String className = ref.getClassName();
      return "org.postgresql.xa.PGXADataSource".equals(className) ? this.loadXADataSource(ref) : null;
   }

   private Object loadXADataSource(Reference ref) {
      PGXADataSource ds = new PGXADataSource();
      return this.loadBaseDataSource(ds, ref);
   }
}
