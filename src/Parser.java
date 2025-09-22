import java.io.File;
import java.util.ArrayList;
import java.util.List;

import IntermediateRepresentation.OpRecord;
import IntermediateRepresentation.Operand;
import Token.Token;
import Token.TokenCategory;

public class Parser {
    private OpRecord IRHead; //the head of the doubly linked list of internal representation records
    private Scanner scanner;
    private int numOperationsParsed; //number of operations that have been parsed; will be returned at the end
    private List<Token> operationTokenLst; //a list of tokens that are part of the operations
    private boolean hasError;

    /**
     * 
     * @param toBeParsedFile
     */
    public Parser(File toBeParsedFile){
        this.scanner = new Scanner(toBeParsedFile);
        this.IRHead = new OpRecord(-1, -1, null, null, null);
        this.operationTokenLst = new ArrayList<>();
        this.hasError = false;
    }

    public void parse(){
        System.out.println("parse without printing IR");
        this.hasError = false;
        Token nextToken;
        //set current node to be the head node
        OpRecord curOpIR = this.IRHead;
        OpRecord preOpIR = null;
        while ((nextToken = scanner.nextToken()) != null){

            // reset operationTokenLst to empty
            this.operationTokenLst = new ArrayList<>();
            // when nextToken is EOF, we end reading the next token
            if (nextToken.getTokenCategory().ordinal() == 9) break;

            // otherwise, we keep iterating on each operation
            // we find an operation by keep calling nextToken() until we reach a EOL token
            while (nextToken.getTokenCategory().ordinal() != 10) {
                this.operationTokenLst.add(nextToken);
                nextToken = scanner.nextToken();
            }
            // size 0 means we are reading an empty line or a comment
            if (this.operationTokenLst.size() == 0) {
                continue;
            }

            OpRecord newIR = createIR(this.operationTokenLst);

            // increment the number of operations parsed
            this.numOperationsParsed++;
            // scenario where the newIR is valid
            if (newIR != null) {
                // set the next pointer of curOpIR, and set the prev pointer of newIR
                newIR.setPrev(curOpIR);
                curOpIR.setNext(newIR);
                // move the curOpIR to pre; move the newIR to cur 
                preOpIR = curOpIR;
                curOpIR = newIR;
            } else {
                // scenario where the newIR is invalid
                // TODO: how to report error from createIR method

                // print out error first 
                this.hasError = true;
            }
        }
        if (!this.hasError) {
            System.out.println(String.format("Parse succeeded. Processed %d operations.", this.numOperationsParsed));
        } else {
            System.out.println("Parse found errors.");
        }
        
    }
    public void parseAndPrintIR(){
        System.out.println("parse and print IR");
        parse();
        if (!hasError) {
            OpRecord cur = this.IRHead.getNext();
            while (cur != null) {
                System.out.println(cur.toString());
                cur = cur.getNext();
            }
        }

    }
    /**
     * Helper function to build internal representation object from the list of tokens
     * @param operationTokenLst 
     * @return
     */
    private OpRecord createIR(List<Token> operationTokenLst){
        TokenCategory tokenCategory = operationTokenLst.get(0).getTokenCategory();

        switch (tokenCategory) {
            case MEMOP:
                return createMemopIR(operationTokenLst);
            case LOADI:
                return createLoadiIR(operationTokenLst);
            case ARITHOP:
                return createArithopIR(operationTokenLst);
            case OUTPUT:
                return createOutputIR(operationTokenLst);
            case NOP:
                return createNopIR(operationTokenLst);
            default:
                printErr(operationTokenLst.get(0), operationTokenLst.get(0));
                return null;
        }
    }
    private OpRecord createMemopIR(List<Token> operationTokenLst){
        Token shouldBeRegister1 = expectToken(operationTokenLst, 1, TokenCategory.REGISTER);
        if (shouldBeRegister1 == null) return null;

        Token shouldBeInto = expectToken(operationTokenLst, 2, TokenCategory.INTO);
        if (shouldBeInto == null) return null;

        Token shouldBeRegister2 = expectToken(operationTokenLst, 3, TokenCategory.REGISTER);
        if (shouldBeRegister2 == null) return null;

        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(shouldBeRegister1.getLexeme(), "", "", "");
        Operand operand3 = new Operand(shouldBeRegister2.getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, operand3);
        return opRecord;
    }
    private OpRecord createLoadiIR(List<Token> operationTokenLst){

        Token shouldBeConst = expectToken(operationTokenLst, 1, TokenCategory.CONSTANT);
        if (shouldBeConst == null) return null;

        Token shouldBeInto = expectToken(operationTokenLst, 2, TokenCategory.INTO);
        if (shouldBeInto == null) return null;

        Token shouldBeRegister = expectToken(operationTokenLst, 3, TokenCategory.REGISTER);
        if (shouldBeRegister == null) return null;

        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(shouldBeConst.getLexeme(), "", "", "");
        Operand operand3 = new Operand(shouldBeRegister.getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, operand3);
        return opRecord;
    }
    
    private OpRecord createArithopIR(List<Token> operationTokenLst){

        Token shouldBeRegister1 = expectToken(operationTokenLst, 1, TokenCategory.REGISTER);
        if (shouldBeRegister1 == null) return null;
        
        Token shouldBeComma = expectToken(operationTokenLst, 2, TokenCategory.COMMA);
        if (shouldBeComma == null) return null;

        Token shouldBeRegister2 = expectToken(operationTokenLst, 3, TokenCategory.REGISTER);
        if (shouldBeRegister2 == null) return null;

        Token shouldBeInto = expectToken(operationTokenLst, 4, TokenCategory.INTO);
        if (shouldBeInto == null) return null;

        Token shouldBeRegister3 = expectToken(operationTokenLst, 5, TokenCategory.REGISTER);
        if (shouldBeRegister3 == null) return null;
             
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(shouldBeRegister1.getLexeme(), "", "", "");
        Operand operand2 = new Operand(shouldBeRegister2.getLexeme(), "", "", "");
        Operand operand3 = new Operand(shouldBeRegister3.getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, operand2, operand3);
        return opRecord;
    }
    private OpRecord createOutputIR(List<Token> operationTokenLst){

        Token shouldBeConstant = expectToken(operationTokenLst, 1, TokenCategory.CONSTANT);
        if (shouldBeConstant == null) return null;

        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(shouldBeConstant.getLexeme(), "", "", "");

        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, null);
        return opRecord;
    }
    private OpRecord createNopIR(List<Token> operationTokenLst){
        
        Token operatorToken = operationTokenLst.get(0);
        if (operationTokenLst.size() != 1) {
            System.err.println(String.format("ERROR %d: \tnop operation is not supposed to have any tokens", operatorToken.getLineNumber()));
        }
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        OpRecord opRecord = new OpRecord(lineNum, opCode, null, null, null);
        return opRecord;
    }
    private Token expectToken(List<Token> tokens, int index, TokenCategory expectedCategory) {
        Token tok = (index >= 0 && index < tokens.size()) ? tokens.get(index) : null;
        if (tok == null || tok.getTokenCategory() != expectedCategory) {
            printErr(tokens.get(0), tok);
            return null;
        }
        return tok;
    }
    private void printErr(Token operationToken, Token curToken){
        if (curToken == null) {
            System.err.println(String.format("ERROR %d: \tMissing Token", operationToken.getLineNumber()));
            return;
        }
        System.err.println(String.format("ERROR %d: \t\"%s\" is not a valid word.", operationToken.getLineNumber(), curToken.getLexeme()));
    }
    public static void main(String[] args) {
        Parser parser = new Parser(new File("test_inputs/autoGraderTests/parse.i"));
        parser.parseAndPrintIR();

    }

}
