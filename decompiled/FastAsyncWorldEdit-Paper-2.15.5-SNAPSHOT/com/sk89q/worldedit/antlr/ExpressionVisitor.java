package com.sk89q.worldedit.antlr;

import com.sk89q.worldedit.antlr4.runtime.tree.ParseTreeVisitor;

public interface ExpressionVisitor<T> extends ParseTreeVisitor<T> {
   T visitAllStatements(ExpressionParser.AllStatementsContext ctx);

   T visitStatements(ExpressionParser.StatementsContext ctx);

   T visitStatement(ExpressionParser.StatementContext ctx);

   T visitBlock(ExpressionParser.BlockContext ctx);

   T visitIfStatement(ExpressionParser.IfStatementContext ctx);

   T visitWhileStatement(ExpressionParser.WhileStatementContext ctx);

   T visitDoStatement(ExpressionParser.DoStatementContext ctx);

   T visitForStatement(ExpressionParser.ForStatementContext ctx);

   T visitSimpleForStatement(ExpressionParser.SimpleForStatementContext ctx);

   T visitBreakStatement(ExpressionParser.BreakStatementContext ctx);

   T visitContinueStatement(ExpressionParser.ContinueStatementContext ctx);

   T visitReturnStatement(ExpressionParser.ReturnStatementContext ctx);

   T visitSwitchStatement(ExpressionParser.SwitchStatementContext ctx);

   T visitCase(ExpressionParser.CaseContext ctx);

   T visitDefault(ExpressionParser.DefaultContext ctx);

   T visitExpressionStatement(ExpressionParser.ExpressionStatementContext ctx);

   T visitEmptyStatement(ExpressionParser.EmptyStatementContext ctx);

   T visitExpression(ExpressionParser.ExpressionContext ctx);

   T visitAssignmentExpression(ExpressionParser.AssignmentExpressionContext ctx);

   T visitAssignment(ExpressionParser.AssignmentContext ctx);

   T visitAssignmentOperator(ExpressionParser.AssignmentOperatorContext ctx);

   T visitCEFallthrough(ExpressionParser.CEFallthroughContext ctx);

   T visitTernaryExpr(ExpressionParser.TernaryExprContext ctx);

   T visitCOFallthrough(ExpressionParser.COFallthroughContext ctx);

   T visitConditionalOrExpr(ExpressionParser.ConditionalOrExprContext ctx);

   T visitCAFallthrough(ExpressionParser.CAFallthroughContext ctx);

   T visitConditionalAndExpr(ExpressionParser.ConditionalAndExprContext ctx);

   T visitEqualityExpr(ExpressionParser.EqualityExprContext ctx);

   T visitEqFallthrough(ExpressionParser.EqFallthroughContext ctx);

   T visitReFallthrough(ExpressionParser.ReFallthroughContext ctx);

   T visitRelationalExpr(ExpressionParser.RelationalExprContext ctx);

   T visitShFallthrough(ExpressionParser.ShFallthroughContext ctx);

   T visitShiftExpr(ExpressionParser.ShiftExprContext ctx);

   T visitAddExpr(ExpressionParser.AddExprContext ctx);

   T visitAdFallthrough(ExpressionParser.AdFallthroughContext ctx);

   T visitMultiplicativeExpr(ExpressionParser.MultiplicativeExprContext ctx);

   T visitMuFallthrough(ExpressionParser.MuFallthroughContext ctx);

   T visitPowerExpr(ExpressionParser.PowerExprContext ctx);

   T visitPwFallthrough(ExpressionParser.PwFallthroughContext ctx);

   T visitPreCrementExpr(ExpressionParser.PreCrementExprContext ctx);

   T visitPlusMinusExpr(ExpressionParser.PlusMinusExprContext ctx);

   T visitUaFallthrough(ExpressionParser.UaFallthroughContext ctx);

   T visitComplementExpr(ExpressionParser.ComplementExprContext ctx);

   T visitNotExpr(ExpressionParser.NotExprContext ctx);

   T visitPostfixExpr(ExpressionParser.PostfixExprContext ctx);

   T visitPoFallthrough(ExpressionParser.PoFallthroughContext ctx);

   T visitPostCrementExpr(ExpressionParser.PostCrementExprContext ctx);

   T visitFunctionCallExpr(ExpressionParser.FunctionCallExprContext ctx);

   T visitConstantExpr(ExpressionParser.ConstantExprContext ctx);

   T visitIdExpr(ExpressionParser.IdExprContext ctx);

   T visitWrappedExpr(ExpressionParser.WrappedExprContext ctx);

   T visitConstantExpression(ExpressionParser.ConstantExpressionContext ctx);

   T visitFunctionCall(ExpressionParser.FunctionCallContext ctx);
}
