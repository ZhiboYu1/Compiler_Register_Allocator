import java.util.Map;


import Token.Token;
import Token.TokenCategory;

import java.io.FileReader;
import java.io.UncheckedIOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Scanner {
    // currentLineIndex is an integer representing the index that scanner is currently on in the current line
    int currentLineIndex;
    String currentLine;
    int currentLineLength;
    int lineNumber;
    boolean hasPrintedEOL;
    boolean prevIsErrToken;
    FileReader fr;
    BufferedReader br;
    File file;

    public static final Map<String, TokenCategory> OPERATORS = Map.of(
        "load",  TokenCategory.MEMOP, 
        "loadI", TokenCategory.LOADI, 
        "store", TokenCategory.MEMOP, 
        "add",   TokenCategory.ARITHOP,
        "sub",   TokenCategory.ARITHOP, 
        "mult",  TokenCategory.ARITHOP, 
        "lshift", TokenCategory.ARITHOP,
        "rshift", TokenCategory.ARITHOP,
        "nop",   TokenCategory.NOP,
        "output", TokenCategory.OUTPUT
    );

    public Scanner(File file) {
        try {
            this.file = file;
            this.fr = new FileReader(file);
            this.br = new BufferedReader(fr);
            this.lineNumber = 0;
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }
    /**
     * notes:
     * 1. the code cannot read the entire file into a buffer,
     */
    public void scanEntireFile(){
        System.out.println(String.format("scanning the entire file: %s", this.file.getName()));

        Token nextToken;
        while ((nextToken = nextToken()) != null){
            
            System.out.println(nextToken.toString());
            if (nextToken.getTokenCategory().ordinal() == 9) break;
        }
    }
    /**
     * 
     * @return 
     * sample return value:
     *      {
     *          ["1: ", "< CONST, "27" >""]
     *      }
     */
    public Token nextToken(){
        try{
            // return a EOL token if the last token that we scanned has error
            if (prevIsErrToken) {
                // reset prevIsErrToken to false
                this.prevIsErrToken = false;
                return new Token(this.lineNumber, TokenCategory.EOL, "\\n");
            }
            
            //System.out.println("currentLineIndex: " + this.currentLineIndex);
            // keep scanning this current line
            if (this.currentLineIndex < this.currentLineLength){
                //have to check whether currentLineLength != 0, otherwise the first line of file will never be read
                //System.out.println("currentLine: " + this.currentLine);
                Token nextToken = scanNextToken();
                return nextToken;
            } else if (this.currentLineIndex == this.currentLineLength && this.currentLineLength != 0 && !this.hasPrintedEOL){
                this.currentLineIndex++;//manually increment to make sure the next nextToken() call can start reading the next line
                return new Token(this.lineNumber, TokenCategory.EOL, "\\n");
            } 
            else { // read a new line if we've finished scanning the previous line
                if ((this.currentLine = br.readLine()) != null){
                    this.hasPrintedEOL = false;
                    this.currentLineLength = this.currentLine.length();
                    this.lineNumber++;
                    this.currentLineIndex = 0;
                    //System.out.println(String.format("Start reading a new line: %s", this.currentLine));
                    Token nextToken = scanNextToken();
                    return nextToken;
                } else {
                    
                    return new Token(++this.lineNumber, TokenCategory.EOF, "");
                }
            }
        }catch (IOException e){
            System.err.println("An error occurred while parsing file: " + this.file.getName());
            return null;
        }
        
    }
    /**
     * if there's an error when scanning the next token (word), report the error
     * @param currentLine
     * @param currentLineIndex
     * @param lineNumber
     * @return
     */
    private Token scanNextToken(){
        char[] currentLineCharArr = this.currentLine.toCharArray();
        // move the start index (currentLineIndex) to the first non-whitespace character
        while (this.currentLineIndex < this.currentLineLength && Character.isWhitespace(currentLineCharArr[this.currentLineIndex])){
            this.currentLineIndex ++;
        }
        //System.out.println("current line index: " + this.currentLineIndex);
        
        if (this.currentLineIndex == this.currentLineLength){
            this.hasPrintedEOL = true;
            return new Token(this.lineNumber, TokenCategory.EOL, "\\n");
         };
        
        int endOfNextTokenIndex = this.currentLineIndex + 1;
        // check whether the following two characters is a comment
        if ((currentLineCharArr[currentLineIndex] == currentLineCharArr[endOfNextTokenIndex]) && 
        (currentLineCharArr[currentLineIndex] == '/')){
            this.currentLineIndex = this.currentLineLength + 1;
            return new Token(this.lineNumber, TokenCategory.EOL, "\\n");
        }
        
        // move the end index (endOfNextTokenIndex) to the last index before a whitespace/register/into sign
        while (endOfNextTokenIndex < this.currentLineLength
            && !Character.isWhitespace(currentLineCharArr[endOfNextTokenIndex])
            && currentLineCharArr[endOfNextTokenIndex] != '='
            && currentLineCharArr[endOfNextTokenIndex] != ','
            && (currentLineCharArr[endOfNextTokenIndex] != 'r' || currentLineCharArr[endOfNextTokenIndex + 1] == 'e')) {
            endOfNextTokenIndex++;
        }
        String tokenLexeme = new String(currentLineCharArr, this.currentLineIndex, endOfNextTokenIndex - this.currentLineIndex);
        //System.out.println("current lexeme is: " + tokenLexeme);
        
        TokenCategory category = null;

        if (OPERATORS.containsKey(tokenLexeme)){
            category = OPERATORS.get(tokenLexeme);

        } else if (isValidInteger(tokenLexeme)) {
            category = TokenCategory.CONSTANT;
            
        } else if (isRegister(tokenLexeme)) {
            category = TokenCategory.REGISTER;
            
        } else if (tokenLexeme.equals(",")){
            category = TokenCategory.COMMA;

        } else if (isInto(tokenLexeme)){
            category = TokenCategory.INTO;

        } else {
            //printErr(tokenLexeme);
            this.currentLineIndex = this.currentLineLength + 1;
            this.prevIsErrToken = true;
            return new Token(this.lineNumber, TokenCategory.ERR, tokenLexeme);
        }
        this.currentLineIndex = endOfNextTokenIndex;
        return new Token(this.lineNumber, category, tokenLexeme);
        // while (this.currentLineIndex < this.currentLineLength){
        //     if (currentLineCharArr[this.currentLineIndex] == 'l'){
        //         if ((this.currentLineIndex + 2 < this.currentLineLength) && currentLineCharArr[this.currentLineIndex + 1] == 'o'){
        //             if ((this.currentLineIndex + 2 < this.currentLineLength) && currentLineCharArr[this.currentLineIndex + 2] == 'a'){
        //                 if ((this.currentLineIndex + 2 < this.currentLineLength) && currentLineCharArr[this.currentLineIndex + 3] == 'd'){
        //                     if (this.currentLineIndex + 4 < this.currentLineLength && currentLineCharArr[currentLineIndex + 4] == 'l'){
        //                         outputToken = new Token(this.lineNumber, 1, "loadl");
        //                         return outputToken;
        //                     } else {
        //                         outputToken = new Token(this.lineNumber, 0, "load");
        //                     }
        //                 } else {
        //                     System.err.println()
        //                 }
        //             }
        //         }
        //     }
        // }
    }
    private static boolean isValidInteger(String str) {
        if (str == null) {
            return false; // Null strings are not integers
        }
        try {
            int num = Integer.parseInt(str); // Attempt to parse the string as an integer
            return num >= 0; // If parsing succeeds, it's an integer
        } catch (NumberFormatException e) {
            return false; // If parsing fails (NumberFormatException), it's not an integer
        }
    }
    private static boolean isRegister(String str){
        if (str == null){
            return false;
        }
        boolean isRegister = str.charAt(0) == 'r' && isValidInteger(str.substring(1));
        //System.out.println("the current str —\t" + str + "\t —is a register: " + isRegister);
        return isRegister;
    }
    private static boolean isInto(String str){
        return str.equals("=>");
    }
    private static boolean isComment(String str){
        return str.equals("//");
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(new File("Compiler_Register_Allocator/test_inputs/autoGraderTests/T8k2.i"));
        scanner.scanEntireFile();
    }
}
