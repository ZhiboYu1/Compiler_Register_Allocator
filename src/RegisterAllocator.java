import java.util.Deque;
import java.util.List;

import IntermediateRepresentation.OpRecord;
import IntermediateRepresentation.Operand;
import Token.Opcode;

public class RegisterAllocator {
    /** number of physical registers that can be allocated */
    private int k;
    /** max live of the code block */
    private int maxLive;
    /** potential reserved register */
    private int reservedRegister;
    private OpRecord IRHead;
    private int maxVRNumber;
    private int[] VRToPR;
    private int[] PRToVR;
    private int[] PRNU;
    private int[] VRToSpillLoc;
    private Deque<Integer> PRStack;
    boolean[] curOpRecordPRs;
    private int spilledAddr;
    public RegisterAllocator(int k, int maxLive, OpRecord IRHead, int maxVRNumber){
        this.k = k;
        this.maxLive = maxLive;
        // reserve a register when k is smaller than maxlive
        if (k < maxLive){
            this.k--;
            this.reservedRegister = k - 1;
        }
        this.IRHead = IRHead;
        this.maxVRNumber = maxVRNumber;
        this.VRToPR = new int[this.maxVRNumber + 1];
        this.VRToSpillLoc = new int[this.maxVRNumber + 1];
        this.spilledAddr = 32768;
        for (int vr = 0; vr <= this.maxVRNumber; vr++){
            this.VRToPR[vr] = -1;
            this.VRToSpillLoc[vr] = -1;
        }
        this.PRToVR = new int[this.k];
        this.PRNU = new int[this.k];

        for (int pr = 0; pr < this.k; pr++){
            this.PRToVR[pr] = -1;
            this.PRNU[pr] = Integer.MAX_VALUE;
            this.PRStack.offerLast(pr);
        }

    }

    public void allocateRegister(){
        
        OpRecord curOpRecord = this.IRHead;
        while (curOpRecord.getNext() != null) {
            //clear the mark in each PR
            this.curOpRecordPRs = new boolean[k];
            
            List<Operand> curRecordOperands = curOpRecord.getOperands();
            int operandsSize = curRecordOperands.size();

            Operand definedRegister = curRecordOperands.get(operandsSize - 1);

            // Now handle used registers
            if (curOpRecord.getOpCode() != Opcode.store){
                curRecordOperands.remove(operandsSize - 1);
            }
            // System.out.println("currentRecordOperands: " + curRecordOperands);
            // System.out.println("currentRecordOp size: " + curRecordOperands.size());
            for (Operand usedOperand : curRecordOperands){
                if (!usedOperand.isRegister()) {
                    continue; 
                }//skip non-register operands
                int pr = this.VRToPR[usedOperand.getVR()];
                if (pr == -1) {
                    pr = getAPR(usedOperand.getVR(), usedOperand.getNU(), curOpRecord);
                    usedOperand.setPR(pr);
                    restore(usedOperand.getVR(), usedOperand.getPR(), curOpRecord);
                } else {
                    usedOperand.setPR(pr);
                }
                //TODO: set the mark in U.PR
                this.curOpRecordPRs[pr] = true;
            }
            for (Operand usedOperand : curRecordOperands){
                if (!usedOperand.isRegister()) {
                    continue; 
                }//skip non-register operands
                if (usedOperand.getNU() == Integer.MAX_VALUE && this.PRToVR[usedOperand.getPR()] != -1){
                    freeAPR(usedOperand.getPR());
                }
                
            }
            //TODO: clear the mark in each PR
            this.curOpRecordPRs = new boolean[k];
            //now handle defined register  
            if (curOpRecord.getOpCode() != Opcode.store){
                int pr = getAPR(definedRegister.getVR(), definedRegister.getNU(), curOpRecord);
                definedRegister.setPR(pr);
                //TODO: set the mark in definedRegister.PR
                this.curOpRecordPRs[pr] = true;
            }
            
            curOpRecord = curOpRecord.getNext();
        }
        
    }
    private Integer getAPR(Integer VR, Integer NU, OpRecord curOpRecord){
        int x = -1;
        if (!this.PRStack.isEmpty()){
            x = this.PRStack.pollLast();
        } else {
            //TODO: pick an unmarked x to spill; what if there is a tie between multiple PRs
            //int toBeSpilledPR = -1;
            int farthestNextUse = -1;
            for (int i = 0; i < this.PRNU.length; i++){
                if (this.PRNU[i] > farthestNextUse && !this.curOpRecordPRs[i]) {
                    farthestNextUse = this.PRNU[i];
                    x = i;
                }
            }

            spill(x, curOpRecord);
        }
        this.VRToPR[VR] = x;
        this.PRToVR[x] = VR;
        this.PRNU[x] = NU;
        return x;
    }
    private void freeAPR(Integer PR) {
        this.VRToPR[this.PRToVR[PR]] = -1;
        this.PRToVR[PR] = -1;
        this.PRNU[PR] = Integer.MAX_VALUE;
        this.PRStack.offerLast(PR);
    }
    /**
     * 
     * @param x
     * @param curOpRecord
     */
    private void spill(Integer x, OpRecord curOpRecord) {
        int curOpRecordIndex = curOpRecord.getLine();
        OpRecord preOpRecord = curOpRecord.getPrev();
        OpRecord loadIRecord = new OpRecord(-1, Opcode.loadI, 
                                            new Operand(this.spilledAddr, this.spilledAddr, this.spilledAddr, null, false), 
                                            null, 
                                            new Operand(null, null, this.reservedRegister, curOpRecordIndex, false));
        OpRecord storeRecord = new OpRecord(-1, Opcode.store, 
                                            new Operand(null, this.PRToVR[x], x, null, false), 
                                            null, 
                                            new Operand(null, null, this.reservedRegister, Integer.MAX_VALUE, false));
        
        //update spilledAddr and VRToSpilledLoc
        this.VRToSpillLoc[this.PRToVR[x]] = this.spilledAddr;
        this.spilledAddr += 4;
        
        //set each operation's prev and next
        preOpRecord.setNext(loadIRecord);

        loadIRecord.setPrev(preOpRecord);
        loadIRecord.setNext(storeRecord);

        storeRecord.setPrev(loadIRecord);
        storeRecord.setNext(curOpRecord);

        curOpRecord.setPrev(storeRecord);
    }
    /**
     * 
     * @param VR
     * @param PR
     * @param curOpRecord
     */
    private void restore(Integer VR, Integer PR, OpRecord curOpRecord){
        int curOpRecordIndex = curOpRecord.getLine();
        OpRecord preOpRecord = curOpRecord.getPrev();
        OpRecord loadIRecord = new OpRecord(-1, Opcode.loadI, 
                                            new Operand(this.VRToSpillLoc[VR], this.VRToSpillLoc[VR], this.VRToSpillLoc[VR], null, false), 
                                            null, 
                                            new Operand(null, null, this.reservedRegister, curOpRecordIndex, false));
        OpRecord loadRecord = new OpRecord(-1, Opcode.load, 
                                            new Operand(null, null, this.reservedRegister, null, false), 
                                            null, 
                                            new Operand(null, VR, PR, curOpRecordIndex, false));
        //update spilledAddr and VRToSpilledLoc
        this.VRToSpillLoc[VR] = -1;

        preOpRecord.setNext(loadIRecord);

        loadIRecord.setPrev(preOpRecord);
        loadIRecord.setNext(loadRecord);

        loadRecord.setPrev(loadIRecord);
        loadRecord.setNext(curOpRecord);

        curOpRecord.setPrev(loadRecord);
    }
}
