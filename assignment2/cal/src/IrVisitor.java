

public class IrVisitor extends calBaseVisitor<String>
{
    private OutputFile outFile;
    private int branchCount;
    private boolean inBranch;
    private String block;

    public IrVisitor(String path) {
        // create file for ir
        this.outFile = new OutputFile(path);
        this.branchCount = 0;
        this.inBranch = false;
        this.block = "";
    }

    @Override
    public String visitMain(calParser.MainContext ctx) {
        outFile.write("main:" + "\n");
        return super.visitMain(ctx);
    }

    @Override
    public String visitAssignStm(calParser.AssignStmContext ctx) {
        String id = ctx.ID().getText();
        String value = visit(ctx.expression());
        if (value.charAt(0) == '-') {value = "0 " + value;}

        if (inBranch) {
                this.block += id + "=" + value + "\n";
        }
        else {
            outFile.append(id + " = " + value + "\n");
        }
        return null;
    }

    @Override
    public String visitConst_decl(calParser.Const_declContext ctx) {
        String id = ctx.ID().getText();
        String value = ctx.expression().getText();
        if (value.charAt(0) == '-') {value = "0 " + value;}
        outFile.append(id + " = " + value + "\n");
        return null;
    }

    @Override
    public String visitFragOp(calParser.FragOpContext ctx) {
        return ctx.frag().getText();
    }

    @Override
    public String visitAddMinusOp(calParser.AddMinusOpContext ctx) {
        String left = ctx.frag(0).getText();
        String right = ctx.frag(1).getText();

        String output = null;

        int type = ctx.binary_arith_op().op.getType();
        if (type == calParser.ADD) {
            output = left + " + " + right;
        } else if (type == calParser.MINUS) {
            output = left + " - " + right;
        }
        return output;
    }

    @Override
    public String visitWhileStm(calParser.WhileStmContext ctx) {
        this.inBranch = true;
        branchCount++;

        String condition = visit(ctx.condition());
        visit(ctx.statement_block());

        String blockLabel = "L" + branchCount;
        branchCount++;
        String conditionLabel = "L" + branchCount;

        // if statement
        outFile.prepend(String.format("%s:\n%sgoto %s\n",blockLabel, this.block, conditionLabel));

        // statement block
        outFile.prepend(String.format("%s:\nif %s goto %s\nreturn",conditionLabel, condition, blockLabel));
        outFile.append("goto " + conditionLabel + "\n");

        this.block = "";
        this.inBranch = false;
        return null;
    }

    @Override
    public String visitIfElseStm(calParser.IfElseStmContext ctx) {
        this.inBranch = true;
        this.branchCount++;
        String condition = visit(ctx.condition());
        visit(ctx.statement_block(0));

        String name = "L" + branchCount;

        // if statement
        outFile.prepend(String.format("%s:\n%sreturn\n",name, this.block));
        outFile.append(String.format("if %s goto %s\n", condition, name));
        this.block = "";

        // else statement
        if (ctx.statement_block(1) != null) {
            this.branchCount++;
            name = "L" + branchCount;
            visit(ctx.statement_block(1));
            outFile.prepend(String.format("%s:\n%sreturn\n",name, this.block));
            outFile.append(String.format("ifz %s goto %s\n", condition, name));
            this.block = "";
        }
        this.inBranch = false;
        return null;
    }

    @Override
    public String visitCondition(calParser.ConditionContext ctx) {
        calParser.ConditionContext condition = ctx.condition(0);

        String left;
        String right;
        int type;

        // if with two conditions e.g if x < y or y > x
        if (ctx.condition(0) != null && ctx.condition(1) != null) {
            left = visit(ctx.condition(0));
            right = visit(ctx.condition(1));

            // break up condition into two three address code conditions
            //outFile.prepend();

            String s = "";
            int opType = ctx.op.getType();
            if (opType == calParser.AND) {
                s = left + " && " + right;
            } else if (opType == calParser.OR) {
                s = left + " || " + right;
            }

            return s;
        }

        // if with ()
        if (condition != null) {
            left = condition.expression(0).getText();
            right = condition.expression(1).getText();
            type = ctx.condition(0).comp_op().op.getType();
        }
        // if without ()
        else {
            left = ctx.expression(0).getText();
            right = ctx.expression(1).getText();
            type = ctx.comp_op().op.getType();
        }

        String s = "";

        if (type == calParser.EQUAL) {
            s = left + "==" + right;
        } else if (type == calParser.NOTEQUAL) {
            s = left + "!=" + right;
        } else if (type == calParser.LT) {
            s = left + "<" + right;
        } else if (type == calParser.GT) {
            s = left + ">" + right;
        } else if (type == calParser.LTE) {
            s = left + "<=" + right;
        } else if (type == calParser.GTE) {
            s = left + ">=" + right;
        }
        return s;
    }

    @Override
    public String visitParens (calParser.ParensContext ctx) {
        return visit(ctx.expression());
    }
}
