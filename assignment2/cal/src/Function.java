import java.util.ArrayList;
import java.util.List;

public class Function {
    public List<Param> params;
    public calParser.Dec_listContext declList;
    public calParser.Statement_blockContext block;
    public calParser.ExpressionContext expr;

    public Function(calParser.Parameter_listContext params, calParser.Dec_listContext declList, calParser.Statement_blockContext block, calParser.ExpressionContext expr) {
        this.params = getIds(params);
        this.declList = declList;
        this.block = block;
        this.expr = expr;
    }

    public List<Param> getIds(calParser.Parameter_listContext parameters) {
        List<Param> paramsList = new ArrayList<>();
        if (parameters.getText().isEmpty()) return paramsList;
        for (String param: parameters.getText().split(",")) {
            String[] decl = param.split(":");
            String id = decl[0];
            String type = decl[1];
            paramsList.add(new Param(id, type));
        }

        return paramsList;
    }
}
