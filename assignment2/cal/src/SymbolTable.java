import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SymbolTable {
    public Map<String, Symbol> ST = new HashMap<>();
    public Stack<String> stack = new Stack<>();

    // signal local scope by pushing special marker to stack
    public void enterScope() {
        stack.push("#");
    }

    // exit local scope and remove local declarations
    public List<String> exitScope() {
        List<String> idList = new ArrayList<>();
        while(stack.size() > 0) {
            String id = stack.pop();
            if (id == "#")
                break;
            ST.remove(id);
            idList.add(id);
        }

        return idList;
    }

    // add symbol to symbol table and push to stack
    public void addSymbol(String id, String type, String scope, String kind) {
        type = type.toLowerCase();
        Symbol symbol = new Symbol(id, type, scope, kind);
        ST.put(id, symbol);
        stack.push(id);
    }

    // get symbol from symbol table with id key
    public Symbol getSymbol(String id) {
        return ST.get(id);
    }

    // get type of symbol
    public String getType(String id) {
        return getSymbol(id).type;
    }

    // compare identifier to a specified type
    public boolean compareType(String id, String type) {
        Symbol symbol = getSymbol(id);
        return type == symbol.type;
    }

    // constant can't be updated
    public boolean checkConstError(String id) {
        Symbol symbol = getSymbol(id);
        boolean isConst = symbol.kind == "constant";
        if (!isConst)
            return true;
        throw new RuntimeException("Error: constant " + id + " can't be updated once declared, use variable instead");
    }

    // compares type of declaration to type of value being assigned
    public boolean CompareTypeValue(String type, String value) throws RuntimeException {
        boolean typesMatch = false;
        // id
        if (getSymbol(value) != null) {
            typesMatch = type.equalsIgnoreCase(getSymbol(value).type);
        }
        // boolean
        else if((value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) && (type.equalsIgnoreCase("boolean"))) {
            typesMatch = true;
        }
        // number
        else if(value != null && type.equalsIgnoreCase("integer")) {
            typesMatch = true;
        }
        if (typesMatch)
            return true;
        // non matching types
        throw new RuntimeException("Error: constant type and value type do not match. Please assign a value of the correct type");
    }
}
