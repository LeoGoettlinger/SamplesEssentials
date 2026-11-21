package com.sk89q.worldedit.antlr;

import com.sk89q.worldedit.antlr4.runtime.tree.ParseTreeListener;

public interface ExpressionListener extends ParseTreeListener {
   void enterAllStatements(ExpressionParser.AllStatementsContext ctx);

   void exitAllStatements(ExpressionParser.AllStatementsContext ctx);

   void enterStatements(ExpressionParser.StatementsContext ctx);

   void exitStatements(ExpressionParser.StatementsContext ctx);

   void enterStatement(ExpressionParser.StatementContext ctx);

   void exitStatement(ExpressionParser.StatementContext ctx);

   void enterBlock(ExpressionParser.BlockContext ctx);

   void exitBlock(ExpressionParser.BlockContext ctx);

   void enterIfStatement(ExpressionParser.IfStatementContext ctx);

   void exitIfStatement(ExpressionParser.IfStatementContext ctx);

   void enterWhileStatement(ExpressionParser.WhileStatementContext ctx);

   void exitWhileStatement(ExpressionParser.WhileStatementContext ctx);

   void enterDoStatement(ExpressionParser.DoStatementContext ctx);

   void exitDoStatement(ExpressionParser.DoStatementContext ctx);

   void enterForStatement(ExpressionParser.ForStatementContext ctx);

   void exitForStatement(ExpressionParser.ForStatementContext ctx);

   void enterSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx);

   void exitSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx);

   void enterBreakStatement(ExpressionParser.BreakStatementContext ctx);

   void exitBreakStatement(ExpressionParser.BreakStatementContext ctx);

   void enterContinueStatement(ExpressionParser.ContinueStatementContext ctx);

   void exitContinueStatement(ExpressionParser.ContinueStatementContext ctx);

   void enterReturnStatement(ExpressionParser.ReturnStatementContext ctx);

   void exitReturnStatement(ExpressionParser.ReturnStatementContext ctx);

   void enterSwitchStatement(ExpressionParser.SwitchStatementContext ctx);

   void exitSwitchStatement(ExpressionParser.SwitchStatementContext ctx);

   void enterCase(ExpressionParser.CaseContext ctx);

   void exitCase(ExpressionParser.CaseContext ctx);

   void enterDefault(ExpressionParser.DefaultContext ctx);

   void exitDefault(ExpressionParser.DefaultContext ctx);

   void enterExpressionStatement(ExpressionParser.ExpressionStatementContext ctx);

   void exitExpressionStatement(ExpressionParser.ExpressionStatementContext ctx);

   void enterEmptyStatement(ExpressionParser.EmptyStatementContext ctx);

   void exitEmptyStatement(ExpressionParser.EmptyStatementContext ctx);

   void enterExpression(ExpressionParser.ExpressionContext ctx);

   void exitExpression(ExpressionParser.ExpressionContext ctx);

   void enterAssignmentExpression(ExpressionParser.AssignmentExpressionContext ctx);

   void exitAssignmentExpression(ExpressionParser.AssignmentExpressionContext ctx);

   void enterAssignment(ExpressionParser.AssignmentContext ctx);

   void exitAssignment(ExpressionParser.AssignmentContext ctx);

   void enterAssignmentOperator(ExpressionParser.AssignmentOperatorContext ctx);

   void exitAssignmentOperator(ExpressionParser.AssignmentOperatorContext ctx);

   void enterCEFallthrough(ExpressionParser.CEFallthroughContext ctx);

   void exitCEFallthrough(ExpressionParser.CEFallthroughContext ctx);

   void enterTernaryExpr(ExpressionParser.TernaryExprContext ctx);

   void exitTernaryExpr(ExpressionParser.TernaryExprContext ctx);

   void enterCOFallthrough(ExpressionParser.COFallthroughContext ctx);

   void exitCOFallthrough(ExpressionParser.COFallthroughContext ctx);

   void enterConditionalOrExpr(ExpressionParser.ConditionalOrExprContext ctx);

   void exitConditionalOrExpr(ExpressionParser.ConditionalOrExprContext ctx);

   void enterCAFallthrough(ExpressionParser.CAFallthroughContext ctx);

   void exitCAFallthrough(ExpressionParser.CAFallthroughContext ctx);

   void enterConditionalAndExpr(ExpressionParser.ConditionalAndExprContext ctx);

   void exitConditionalAndExpr(ExpressionParser.ConditionalAndExprContext ctx);

   void enterEqualityExpr(ExpressionParser.EqualityExprContext ctx);

   void exitEqualityExpr(ExpressionParser.EqualityExprContext ctx);

   void enterEqFallthrough(ExpressionParser.EqFallthroughContext ctx);

   void exitEqFallthrough(ExpressionParser.EqFallthroughContext ctx);

   void enterReFallthrough(ExpressionParser.ReFallthroughContext ctx);

   void exitReFallthrough(ExpressionParser.ReFallthroughContext ctx);

   void enterRelationalExpr(ExpressionParser.RelationalExprContext ctx);

   void exitRelationalExpr(ExpressionParser.RelationalExprContext ctx);

   void enterShFallthrough(ExpressionParser.ShFallthroughContext ctx);

   void exitShFallthrough(ExpressionParser.ShFallthroughContext ctx);

   void enterShiftExpr(ExpressionParser.ShiftExprContext ctx);

   void exitShiftExpr(ExpressionParser.ShiftExprContext ctx);

   void enterAddExpr(ExpressionParser.AddExprContext ctx);

   void exitAddExpr(ExpressionParser.AddExprContext ctx);

   void enterAdFallthrough(ExpressionParser.AdFallthroughContext ctx);

   void exitAdFallthrough(ExpressionParser.AdFallthroughContext ctx);

   void enterMultiplicativeExpr(ExpressionParser.MultiplicativeExprContext ctx);

   void exitMultiplicativeExpr(ExpressionParser.MultiplicativeExprContext ctx);

   void enterMuFallthrough(ExpressionParser.MuFallthroughContext ctx);

   void exitMuFallthrough(ExpressionParser.MuFallthroughContext ctx);

   void enterPowerExpr(ExpressionParser.PowerExprContext ctx);

   void exitPowerExpr(ExpressionParser.PowerExprContext ctx);

   void enterPwFallthrough(ExpressionParser.PwFallthroughContext ctx);

   void exitPwFallthrough(ExpressionParser.PwFallthroughContext ctx);

   void enterPreCrementExpr(ExpressionParser.PreCrementExprContext ctx);

   void exitPreCrementExpr(ExpressionParser.PreCrementExprContext ctx);

   void enterPlusMinusExpr(ExpressionParser.PlusMinusExprContext ctx);

   void exitPlusMinusExpr(ExpressionParser.PlusMinusExprContext ctx);

   void enterUaFallthrough(ExpressionParser.UaFallthroughContext ctx);

   void exitUaFallthrough(ExpressionParser.UaFallthroughContext ctx);

   void enterComplementExpr(ExpressionParser.ComplementExprContext ctx);

   void exitComplementExpr(ExpressionParser.ComplementExprContext ctx);

   void enterNotExpr(ExpressionParser.NotExprContext ctx);

   void exitNotExpr(ExpressionParser.NotExprContext ctx);

   void enterPostfixExpr(ExpressionParser.PostfixExprContext ctx);

   void exitPostfixExpr(ExpressionParser.PostfixExprContext ctx);

   void enterPoFallthrough(ExpressionParser.PoFallthroughContext ctx);

   void exitPoFallthrough(ExpressionParser.PoFallthroughContext ctx);

   void enterPostCrementExpr(ExpressionParser.PostCrementExprContext ctx);

   void exitPostCrementExpr(ExpressionParser.PostCrementExprContext ctx);

   void enterFunctionCallExpr(ExpressionParser.FunctionCallExprContext ctx);

   void exitFunctionCallExpr(ExpressionParser.FunctionCallExprContext ctx);

   void enterConstantExpr(ExpressionParser.ConstantExprContext ctx);

   void exitConstantExpr(ExpressionParser.ConstantExprContext ctx);

   void enterIdExpr(ExpressionParser.IdExprContext ctx);

   void exitIdExpr(ExpressionParser.IdExprContext ctx);

   void enterWrappedExpr(ExpressionParser.WrappedExprContext ctx);

   void exitWrappedExpr(ExpressionParser.WrappedExprContext ctx);

   void enterConstantExpression(ExpressionParser.ConstantExpressionContext ctx);

   void exitConstantExpression(ExpressionParser.ConstantExpressionContext ctx);

   void enterFunctionCall(ExpressionParser.FunctionCallContext ctx);

   void exitFunctionCall(ExpressionParser.FunctionCallContext ctx);
}
