import java.io.File;
import java.util.ArrayList;
import java.util.List;

import InternalRepresentation.OpRecord;
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
                createMemopIR(operationTokenLst);
            case LOADI:
                createLoadiIR(operationTokenLst);
            case ARITHOP:
                createArithopIR(operationTokenLst);
            case OUTPUT:
                createOutputIR(operationTokenLst);
            case NOP:
                createNopIR(operationTokenLst);
            case CONSTANT:
                createConstantIR(operationTokenLst);
            case REGISTER:
                createRegisterIR(operationTokenLst);
            case COMMA:
                createCommaIR(operationTokenLst);
            case INTO:
                createIntoIR(operationTokenLst);
            case EOF:
                createEofIR(operationTokenLst);
            case EOL:
                createEolIR(operationTokenLst);
        }
        return null;
    }
}
