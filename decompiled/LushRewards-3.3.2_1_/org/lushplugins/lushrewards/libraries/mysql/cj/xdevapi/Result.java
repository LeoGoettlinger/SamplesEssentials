package org.lushplugins.lushrewards.libraries.mysql.cj.xdevapi;

import java.util.Iterator;
import org.lushplugins.lushrewards.libraries.mysql.cj.QueryResult;

public interface Result extends QueryResult {
   long getAffectedItemsCount();

   int getWarningsCount();

   Iterator<Warning> getWarnings();
}
