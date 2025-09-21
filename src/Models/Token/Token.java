package Token;
public class Token {
    private int lineNumber;
    private TokenCategory category;
    private String lexeme;
    public final static String[] OP_CODE = {""};
    private int opCode;
    public Token(int lineNumber, TokenCategory category, String lexeme){
        this.lineNumber = lineNumber;
        this.category = category;
        this.lexeme = lexeme;

        switch (lexeme) {
            case "load":
                this.opCode = 1;
                break;
            case "loadI":
                this.opCode = 2;
                break;
            case "store":
                this.opCode = 3;
                break;
            case "add":
                this.opCode = 4;
                break;
            case "sub":
                this.opCode = 5;
                break;
            case "mult":
                this.opCode = 6;
                break;
            case "lshilft":
                this.opCode = 7;
                break;
            case "rshift":
                this.opCode = 8;
                break;
            case "nop":
                this.opCode = 9;
                break;
            case "output":
                this.opCode = 10;
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
    public int getOpCode(){
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
