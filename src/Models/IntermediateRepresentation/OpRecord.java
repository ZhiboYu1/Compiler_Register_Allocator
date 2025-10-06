package IntermediateRepresentation;

import java.util.ArrayList;
import java.util.List;

public class OpRecord {
    private int line;
    private int opCode;
    private Operand operand1;
    private Operand operand2;
    private Operand operand3;
    private OpRecord prev;
    private OpRecord next;
    public OpRecord(int line, int opCode, Operand operand1, Operand operand2, Operand operand3) {
        this.line = line;
        this.opCode = opCode;
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
    }
    
    public int getLine() {
        return this.line;
    }
    public int getOpCode() {
        return this.opCode;
    }
    public List<Operand> getOperands(){
        List<Operand> operandsList = new ArrayList<>();
        if (this.operand1 != null){
            operandsList.add(operand1);
        }
        if (this.operand2 != null){
            operandsList.add(operand2);
        }
        if (this.operand3 != null){
            operandsList.add(operand3);
        }
        return operandsList;
    }
    public Operand getOperand1() {
        return this.operand1;
    }
    public Operand getOperand2() {
        return this.operand2;
    }
    public Operand getOperand3() {
        return this.operand3;
    }
    public void setPrev(OpRecord prev){
        this.prev = prev;
    }
    public void setNext(OpRecord next){
        this.next = next;
    }
    public OpRecord getNext() {
        return this.next;
    }
    public OpRecord getPrev() {
        return this.prev;
    }
    public String toString() {
        return String.format("%d    [ %s ], [ %s ], [ %s ]", this.opCode, this.operand1 == null ? "" : this.operand1.toString(), 
        this.operand2 == null ? "" : this.operand2.toString(), this.operand3 == null ? "" :this.operand3.toString());
    }
}
