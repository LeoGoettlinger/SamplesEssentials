package org.sethomegui.SetHomeGUI.libs.mysql.cj.xdevapi;

import java.util.Iterator;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.QueryResult;

public interface Result extends QueryResult {
   long getAffectedItemsCount();

   int getWarningsCount();

   Iterator<Warning> getWarnings();
}
