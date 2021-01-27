import java.util.*;

public class MySampleVisitor extends SampleBaseVisitor<String>{

	@Override
	public String visitText(SampleParser.TextContext ctx) {
		StringBuilder functions = new StringBuilder();
		StringBuilder mainBody = new StringBuilder();
		for (var expression : ctx.global()) {
			boolean isFunction = expression.func_def() != null;
			if (isFunction) {
				SampleParser.Func_defContext function = expression.func_def();
				String funName = function.VAR().getText();
				String args = visitT_args(function.t_args());
				ArrayList<String> functionBodyParts = new ArrayList<>();
				for (var statement : function.statement()) {
					functionBodyParts.add(visitStatement(statement));
				}
				String functionBody = String.join("\n", functionBodyParts);
				String returnType;
				String  returnValue;
				if (function.RET() != null) {
					List<String> typeAndExpression = Arrays.asList(visitSingleAE(function.singleAE()).split(":"));
					returnType = typeAndExpression.get(0);
					returnValue = "return " + typeAndExpression.get(1) + ";";
				} else {
					returnType = "void";
					returnValue = "";
				}
				functions.append(printFunction(returnType, returnValue, funName, args, functionBody)).append("\n");
				existingVars.clear();
			} else {
				mainBody.append(visitStatement(expression.statement())).append("\n");
			}
		}
		return "#include <stdio.h>\n\n" + functions +
				"void main () {\n" +
				mainBody +
				"}";
	}

	private String printFunction(String returnType, String returnValue, String name, String args, String body) {
		return returnType + " " + name + " " + args + " {\n" + body + "\n" + returnValue + "\n}\n";
	}

	@Override
	public String visitStatement(SampleParser.StatementContext ctx) {
		boolean isLiteral = ctx.literal() != null;
		if (isLiteral) {
			SampleParser.LiteralContext literal = ctx.literal();
			return visitLiteral(literal);
		} else {
			return visitWriteOutput(ctx.writeOutput());
		}
	}

	@Override
	public String visitLiteral(SampleParser.LiteralContext ctx) {
		if (ctx.asgn() != null) {
			return visitAsgn(ctx.asgn());
		}
		if (ctx.condition() != null) {
			return visitCondition(ctx.condition());
		}
		assert false;
		return "";
	}

	@Override
	public String visitCondition(SampleParser.ConditionContext ctx) {
		if (ctx.ifStatement() != null) {
			return visitIfStatement(ctx.ifStatement());
		}
		assert false;
		return "";
	}


    @Override
	public String visitIfStatement(SampleParser.IfStatementContext ctx) {
		String condition = visitArithmeticExpressionLogic(ctx.arithmeticExpressionLogic());
		StringBuilder ifBody = new StringBuilder();
		for (var statement : ctx.statement()) {
			ifBody.append(visitStatement(statement)).append("\n");
		}
		return "if (" + condition + ") {\n" + ifBody.toString() + "} ";
	}

	@Override
	public String visitAsgn(SampleParser.AsgnContext ctx) {
		List<String> argNames = Arrays.asList(ctx.args().getText().split(","));
		List<String> expressions = Arrays.asList(visitArithmeticExpressions(ctx.arithmeticExpressions()).split(","));
		assert (argNames.size() == expressions.size());
		int n = argNames.size();
		List<String> result = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			String currentVar = argNames.get(i);
			switch (expressions.get(i)) {
				case "readint()": {
					if (!existingVars.contains(currentVar)) {
						result.add(String.format("int %s;", currentVar));
						existingVars.add(currentVar);
					}
					result.add("scanf(\"%d\", &" + currentVar + ");");
					break;
				}
				case "readdouble()": {
					if (!existingVars.contains(currentVar)) {
						result.add(String.format("double %s;", currentVar));
						existingVars.add(currentVar);
					}
					result.add("scanf(\"%d\", &" + currentVar + ");");
					break;
				}
				case "readbool()": {
					if (!existingVars.contains(currentVar)) {
						result.add(String.format("bool %s;", currentVar));
						existingVars.add(currentVar);
					}
					result.add("scanf(\"%d\", &" + currentVar + ");");
					break;
				}
				case "readstring()": {
					if (!existingVars.contains(currentVar)) {
						result.add(String.format("char* %s;", currentVar));
						existingVars.add(currentVar);
					}
					result.add("scanf(\"%s\", &" + currentVar + ");");
					break;
				}
				default: {
					List<String> typeAndExpression = Arrays.asList(expressions.get(i).split(":"));
					String type = typeAndExpression.get(0);
					String expression = typeAndExpression.get(1);
					if (!existingVars.contains(currentVar)) {
						result.add(String.format("%s %s;", type, currentVar));
						existingVars.add(currentVar);
					}
					result.add(currentVar + " = " + expression + ";");
				}
			}
		}
		return String.join("\n", result);
	}

	@Override
	public String visitArithmeticExpressions(SampleParser.ArithmeticExpressionsContext ctx) {
		List<String> arithmeticExpressions = new ArrayList<>();
		for (var ae : ctx.singleAE()) {
			arithmeticExpressions.add(visitSingleAE(ae));
		}
		return String.join(",", arithmeticExpressions);
	}

	@Override
	public String visitSingleAE(SampleParser.SingleAEContext ctx) {
		if (ctx.arithmeticExpressionCompare() != null) {
			return "bool:" + visitArithmeticExpressionCompare(ctx.arithmeticExpressionCompare());
		}
		if (ctx.arithmeticExpressionLogic() != null) {
			return "bool:" + visitArithmeticExpressionLogic(ctx.arithmeticExpressionLogic());
		}
		if (ctx.arithmeticExpressionNumber() != null) {
			return "double:" + visitArithmeticExpressionNumber(ctx.arithmeticExpressionNumber());
		}
		if (ctx.readInput() != null) {
			return visitReadInput(ctx.readInput());
		}
		assert false;
		return "";
	}

	@Override
	public String visitArithmeticExpressionNumber(SampleParser.ArithmeticExpressionNumberContext ctx) {
		StringBuilder result = new StringBuilder();
		int n = ctx.number().size();
		for (int i = 0; i < n; i++) {
			result.append(ctx.number(i).getText());
			if (i < n - 1) {
				result.append(" ").append(ctx.operationNumber(i).getText()).append(" ");
			}
		}
		return result.toString();
	}

	@Override
	public String visitArithmeticExpressionCompare(SampleParser.ArithmeticExpressionCompareContext ctx) {
		String lhs = visitArithmeticExpressionNumber(ctx.arithmeticExpressionNumber(0));
		String operation = ctx.compareOp().getText();
		String rhs = visitArithmeticExpressionNumber(ctx.arithmeticExpressionNumber(1));
		return  lhs + " " + operation + " " + rhs;
	}

	@Override
	public String visitArithmeticExpressionLogic(SampleParser.ArithmeticExpressionLogicContext ctx) {
		int n = ctx.compareOrLogicVar().size();
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < n; i++) {
			var colv = ctx.compareOrLogicVar(i);
			boolean isLogicalVar = colv.logicVar() != null;
			if (isLogicalVar) {
				result.append(visitLogicVar(colv.logicVar()));
			} else {
				result.append(visitArithmeticExpressionCompare(colv.arithmeticExpressionCompare()));
			}
			if (i < n - 1) {
				result.append(" ").append(ctx.operationLogic(i).getText()).append(" ");
			}
		}
		return result.toString();
	}

	@Override
	public String visitLogicVar(SampleParser.LogicVarContext ctx) {
		if (ctx.FALSE() != null) {
			return "0";
		}
		if (ctx.TRUE() != null) {
			return "1";
		}
		return ctx.VAR().getText();
	}

	@Override
	public String visitReadInput(SampleParser.ReadInputContext ctx) {
		//ctx.nextRead().get
		return ctx.getText();
	}

    @Override
    public String visitT_args(SampleParser.T_argsContext ctx) {
        if (ctx.typedArg() == null) {
            return "()";
        }
        String firstArgType = ctx.typedArg().type().getText();
        if (firstArgType.equals("bool")) {
            firstArgType = "bool";
        }
        String firstArgName = ctx.typedArg().VAR().getText();
        ArrayList<String> followingArgsType = new ArrayList<>();
        ArrayList<String> followingArgsNames = new ArrayList<>();
        for (var typedArg : ctx.nextTypedArg()) {
            var curArg = typedArg.typedArg();
            String argType = curArg.type().getText();
            if (argType.equals("bool")) {
                argType = "bool";
            }
            followingArgsType.add(argType);
            String argName = curArg.VAR().getText();
            followingArgsNames.add(argName);
        }
        return printTypedArgs(firstArgType, firstArgName, followingArgsType, followingArgsNames);
    }

	private String printTypedArgs(String firstArgType, String firstArgName, ArrayList<String> argTypes, ArrayList<String> argNames) {
		StringBuilder result = new StringBuilder("(");
		result.append(firstArgType);
		result.append(' ');
		result.append(firstArgName);
		for (int i = 0; i < argTypes.size(); i++) {
			result.append(", ");
			result.append(argTypes.get(i));
			result.append(" ");
			result.append(argNames.get(i));
		}
		result.append(")");
		return result.toString();
	}

	private Set<String> existingVars = new HashSet<>();
}
