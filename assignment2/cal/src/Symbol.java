public class Symbol
{
    String id;
    String type;
    String scope;
    String kind;

    public Symbol (String id, String type, String scope, String kind) {
        this.id = id;
        this.type = type;
        this.scope = scope;
        this.kind = kind;
    }
}
