package Token;
public class Token {
    private int lineNumber;
    private TokenCategory category;
    private String lexeme;

    private Opcode opCode;
    public Token(int lineNumber, TokenCategory category, String lexeme){
        this.lineNumber = lineNumber;
        this.category = category;
        this.lexeme = lexeme;

        switch (lexeme) {
            case "load":
                this.opCode = Opcode.load;
                break;
            case "loadI":
                this.opCode = Opcode.loadI;
                break;
            case "store":
                this.opCode = Opcode.store;
                break;
            case "add":
                this.opCode = Opcode.add;
                break;
            case "sub":
                this.opCode = Opcode.sub;
                break;
            case "mult":
                this.opCode = Opcode.mult;
                break;
            case "lshift":
                this.opCode = Opcode.lshift;
                break;
            case "rshift":
                this.opCode = Opcode.rshift;
                break;
            case "nop":
                this.opCode = Opcode.nop;
                break;
            case "output":
                this.opCode = Opcode.output;
                break;
        }
    }
    public TokenCategory getTokenCategory(){
        return this.category;
    }
    public int getLineNumber(){
        return this.lineNumber;
    }
    public String getLexeme(){
        return this.lexeme;
    }
    public Opcode getOpCode(){
        return this.opCode;
    }
    public String toString(){
        if (this.category.ordinal() == 11) {
            return String.format("ERROR %s:\t\t \"%s\" is not a valid word.", this.lineNumber, lexeme);
        }
        return String.format("%d: < %s, %s >", this.lineNumber, this.category, this.lexeme);
    }
    public static void main(String[] args){
        Token token = new Token(10, TokenCategory.ARITHOP, "wef");
        System.out.println(token.toString());
    }
}
