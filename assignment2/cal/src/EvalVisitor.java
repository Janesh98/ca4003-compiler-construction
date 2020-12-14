import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvalVisitor extends calBaseVisitor<Integer>
{
    Map<String, Integer> memory = new HashMap<>();
    Map<String, Function> function = new HashMap<>();
    SymbolTable ST = new SymbolTable();
    private boolean inFunction = false;

    @Override
    public Integer visitAssignStm(calParser.AssignStmContext ctx)
    {
        String id = ctx.ID().getText();
        int value = visit(ctx.expression());

        if (inFunction)
            id = "#" + id;

        ST.checkConstError(id);
        memory.put(id, value);

        return value;
    }

    @Override
    public Integer visitVar_decl(calParser.Var_declContext ctx) {
        String id = ctx.ID().getText();

        String type = ctx.type().getText();
        if (type.equalsIgnoreCase("void")) throw new RuntimeException(String.format("Error: variable %s cannot be void", id));
        Integer value = null;

        if (inFunction) {
            id = '#' + id;
            if (memory.containsKey(id))
                throw new RuntimeException(String.format("Error: variable %s is already defined", id));
            ST.addSymbol(id, type, "local", "variable");
        }

        else {
            if (memory.containsKey(id))
                throw new RuntimeException(String.format("Error: variable %s is already defined", id));
            ST.addSymbol(id, type, "global", "variable");
        }
        memory.put(id, value);

        return value;
    }

    @Override
    public Integer visitConst_decl(calParser.Const_declContext ctx) {
        String type = ctx.type().getText();
        String id = ctx.ID().getText();
        if (type.equalsIgnoreCase("void")) throw new RuntimeException(String.format("Error: constant %s cannot be void", id));

        Integer value = visit(ctx.expression());

        String expr = ctx.expression().getText().split("\\(")[0];
        ST.CompareTypeValue(type, expr);

        if (inFunction) {
            id = '#' + id;
            if (memory.containsKey(id))
                throw new RuntimeException(String.format("Error: constant %s is already defined", id));
            ST.addSymbol(id, type, "local", "constant");
        }

        else {
            if (memory.containsKey(id))
                throw new RuntimeException(String.format("Error: constant %s is already defined", id));
            ST.addSymbol(id, type, "global", "constant");
        }

        memory.put(id, value);

        return value;
    }

    @Override
    public Integer visitIfElseStm(calParser.IfElseStmContext ctx) {
        if(this.visit(ctx.condition()) == 1) {
            visit(ctx.statement_block(0));
        }

        else if (ctx.statement_block(1) != null) {
            visit(ctx.statement_block(1));
        }

        return 1;
    }

    @Override
    public Integer visitWhileStm(calParser.WhileStmContext ctx) {
        while (visit(ctx.condition()) == 1) {
            visit(ctx.statement_block());
        }
        return 0;
    }

    @Override
    public Integer visitFunction(calParser.FunctionContext ctx) {
        String id = ctx.ID().getText();
        String type = ctx.type().getText();
        calParser.Parameter_listContext params = ctx.parameter_list();
        calParser.Dec_listContext declList = ctx.dec_list();
        calParser.Statement_blockContext block = ctx.statement_block();
        calParser.ExpressionContext expr = ctx.expression();

        ST.addSymbol(id, type, "global", "function");

        Function func = new Function(params, declList, block, expr);
        function.put(id, func);
        return 1;
    }

    @Override
    public Integer visitFuncCallStm(calParser.FuncCallStmContext ctx) {
        inFunction = true;
        ST.enterScope();
        String id = ctx.ID().getText();
        String args = ctx.arg_list().getText();
        String[] argList = args.split(",");

        if (argList[0].isEmpty())
            argList = new String[]{};

        if (!function.containsKey(id))
            throw new RuntimeException(String.format("Error: function %s is not defined", id));

        Function func = function.get(id);

        if (argList.length != func.params.size())
            throw new RuntimeException(String.format("Error: function %s call requires %d arguments but received %d", id, func.params.size(), argList.length));

        for (int i = 0; i < argList.length; i++ ) {
            String argId = argList[i];
            Param param = func.params.get(i);
            int argValue = memory.get(argId);
            String paramId = param.id;
            if (inFunction) {
                paramId = '#' + paramId;
            }

            memory.put(paramId, argValue);
            ST.addSymbol(paramId, param.type, "local", "variable");
        }

        visit(func.declList);
        visit(func.block);

        // list of id's to be removed from global memory.
        List<String> idList = ST.exitScope();
        for (String localID:idList) {
            memory.remove(localID);
        }

        inFunction = false;
        return null;
    }

    @Override
    public Integer visitFuncCallOp(calParser.FuncCallOpContext ctx) {
        inFunction = true;
        ST.enterScope();
        String id = ctx.ID().getText();
        String args = ctx.arg_list().getText();
        String[] argList = args.split(",");

        if (argList[0].isEmpty())
            argList = new String[]{};

        if (!function.containsKey(id))
            throw new RuntimeException(String.format("Error: function %s is not defined", id));

        Function func = function.get(id);

        if (argList.length != func.params.size())
            throw new RuntimeException(String.format("Error: function %s call requires %d arguments but received %d", id, func.params.size(), argList.length));

        for (int i = 0; i < argList.length; i++ ) {
            String argId = argList[i];
            Param param = func.params.get(i);
            int argValue = memory.get(argId);
            String paramId = param.id;
            if (inFunction) {
                paramId = '#' + paramId;
            }

            memory.put(paramId, argValue);
            ST.addSymbol(paramId, param.type, "local", "variable");
        }

        visit(func.declList);
        visit(func.block);
        int returnVal = visit(func.expr);

        // list of id's to be removed from global memory.
        List<String> idList = ST.exitScope();
        for (String localID:idList) {
            memory.remove(localID);
        }

        inFunction = false;
        return returnVal;
    }

    @Override
    public Integer visitCondition(calParser.ConditionContext ctx) {
        int left, right;
        int type;

        // logical negation
        if (ctx.NEG() != null) {
            int output = visit(ctx.condition(0));
            return (output == 1) ? 0 : 1;
        }

        // if with two conditions e.g if x < y or y > x
        if (ctx.condition(0) != null && ctx.condition(1) != null) {
            left = visit(ctx.condition(0));
            right = visit(ctx.condition(1));

            switch (ctx.op.getType()) {
                case calParser.AND:
                    return ((left == 1) && (right == 1)) ? 1 : 0;
                case calParser.OR:
                    return ((left == 1) || (right == 1)) ? 1 : 0;
                default:
                    return 0;
            }
        }

        calParser.ConditionContext condition = ctx.condition(0);
        // if with ()
        if (condition != null) {
            // e.g. if (x < y & y >x
            if (ctx.condition(0) != null) {
                return visit(ctx.condition(0));
            }
            left = visit(condition.expression(0));
            right = visit(condition.expression(1));
            type = ctx.condition(0).comp_op().op.getType();
        }
        // if without ()
        else {
            left = visit(ctx.expression(0));
            right = visit(ctx.expression(1));
            type = ctx.comp_op().op.getType();
        }

        switch (type) {
            case calParser.EQUAL:
                return (left == right) ? 1 : 0;
            case calParser.NOTEQUAL:
                return (left != right) ? 1 : 0;
            case calParser.LT:
                return (left < right) ? 1 : 0;
            case calParser.GT:
                return (left > right) ? 1 : 0;
            case calParser.LTE:
                return (left <= right) ? 1 : 0;
            case calParser.GTE:
                return (left >= right) ? 1 : 0;
            default:
                return null;
        }

    }

    @Override
    public Integer visitFrag(calParser.FragContext ctx) {
        String val = ctx.getText();
        // if a number
        if (Digit.isDigit(val))
            return Integer.parseInt(val);

        // not a number so its a string
        // if boolean
        if (val.equalsIgnoreCase("true")) return 1;
        else if (val.equalsIgnoreCase("false")) return 0;

        // if id
        boolean isNeg = false;
        // check if minus, if so remove minus
        if (val.charAt(0) == '-') {
            isNeg = true;
            val = val.substring(1);
        }
        // get id
        if (memory.containsKey(val) | memory.containsKey('#' + val)) {
            int idVal;
            if (inFunction)
                val = "#" + val;

            idVal = memory.get(val);
            return (isNeg) ? -idVal : idVal;
        }

        // id does not exist, thus is undefined
        throw new RuntimeException(String.format("Error: %s is undefined", val));
    }

    @Override
    public Integer visitAddMinusOp(calParser.AddMinusOpContext ctx) {
        int left = visit(ctx.frag(0));
        int right = visit(ctx.frag(1));

        switch (ctx.binary_arith_op().op.getType()) {
            case calParser.ADD:
                return left + right;
            case calParser.MINUS:
                return left - right;
        }

        return null;
    }

    @Override
    public Integer visitParens (calParser.ParensContext ctx)
    {
        return visit(ctx.expression());
    }
}
