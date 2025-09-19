package Token;
public class Token {
    private int lineNumber;
    private TokenCategory category;
    private String lexeme;
    public Token(int lineNumber, TokenCategory category, String lexeme){
        this.lineNumber = lineNumber;
        this.category = category;
        this.lexeme = lexeme;
    }
    public TokenCategory getTokenCategory(){
        return this.category;
    }
    public int getLineNumber(){
        return this.lineNumber;
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
