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
                return null;
        }
    }
    private OpRecord createMemopIR(List<Token> operationTokenLst){
        if (operationTokenLst.size() != 4) return null;
        if (operationTokenLst.get(1).getTokenCategory() != TokenCategory.REGISTER){
            printErr(operationTokenLst.get(1));
            return null;    
        } else if (operationTokenLst.get(2).getTokenCategory() != TokenCategory.INTO) {
            printErr(operationTokenLst.get(2));
            return null;
        } else if (operationTokenLst.get(3).getTokenCategory() != TokenCategory.REGISTER) {
            printErr(operationTokenLst.get(3));
            return null;
        }
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(operationTokenLst.get(1).getLexeme(), "", "", "");
        Operand operand3 = new Operand(operationTokenLst.get(3).getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, operand3);
        return opRecord;
    }
    private OpRecord createLoadiIR(List<Token> operationTokenLst){
        if (operationTokenLst.size() != 4) return null;
        if (operationTokenLst.get(1).getTokenCategory() != TokenCategory.CONSTANT){
            printErr(operationTokenLst.get(1));
            return null;    
        } else if (operationTokenLst.get(2).getTokenCategory() != TokenCategory.INTO) {
            printErr(operationTokenLst.get(2));
            return null;
        } else if (operationTokenLst.get(3).getTokenCategory() != TokenCategory.REGISTER) {
            printErr(operationTokenLst.get(3));
            return null;
        }
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(operationTokenLst.get(1).getLexeme(), "", "", "");
        Operand operand3 = new Operand(operationTokenLst.get(3).getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, operand3);
        return opRecord;
    }
    private OpRecord createArithopIR(List<Token> operationTokenLst){
        if (operationTokenLst.size() != 6) return null;
        if (operationTokenLst.get(1).getTokenCategory() != TokenCategory.REGISTER){
            printErr(operationTokenLst.get(1));
            return null;    
        } else if (operationTokenLst.get(2).getTokenCategory() != TokenCategory.COMMA) {
            printErr(operationTokenLst.get(2));
            return null;
        } else if (operationTokenLst.get(3).getTokenCategory() != TokenCategory.REGISTER) {
            printErr(operationTokenLst.get(3));
            return null;
        } else if (operationTokenLst.get(4).getTokenCategory() != TokenCategory.INTO) {
            printErr(operationTokenLst.get(4));
            return null;
        } else if (operationTokenLst.get(5).getTokenCategory() != TokenCategory.REGISTER) {
            printErr(operationTokenLst.get(5));
            return null;
        }
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(operationTokenLst.get(1).getLexeme(), "", "", "");
        Operand operand2 = new Operand(operationTokenLst.get(3).getLexeme(), "", "", "");
        Operand operand3 = new Operand(operationTokenLst.get(5).getLexeme(), "", "", "");
        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, operand2, operand3);
        return opRecord;
    }
    private OpRecord createOutputIR(List<Token> operationTokenLst){
        if (operationTokenLst.size() != 2) return null;
        if (operationTokenLst.get(1).getTokenCategory() != TokenCategory.CONSTANT){
            printErr(operationTokenLst.get(1));
            return null;    
        } 
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        Operand operand1 = new Operand(operationTokenLst.get(1).getLexeme(), "", "", "");

        OpRecord opRecord = new OpRecord(lineNum, opCode, operand1, null, null);
        return opRecord;
    }
    private OpRecord createNopIR(List<Token> operationTokenLst){
        if (operationTokenLst.size() != 1) return null;
        
        Token operatorToken = operationTokenLst.get(0);
        int lineNum = operatorToken.getLineNumber();
        int opCode = operatorToken.getOpCode();
        OpRecord opRecord = new OpRecord(lineNum, opCode, null, null, null);
        return opRecord;
    }

    private void printErr(Token operationToken){
        System.out.println(operationToken.getTokenCategory().name());
        System.err.println(String.format("Error %d: \t%s is not a valid word.", operationToken.getLineNumber(), operationToken.getLexeme()));
    }
    // public static void main(String[] args) {
    //     Parser parser = new Parser(new File("test_inputs/t1.i"));
    //     parser.parseAndPrintIR();

    // }

}
