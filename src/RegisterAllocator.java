import java.util.ArrayDeque;
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
        this.PRStack = new ArrayDeque<>();
        for (int vr = 0; vr <= this.maxVRNumber; vr++){
            this.VRToPR[vr] = -1;
            this.VRToSpillLoc[vr] = Integer.MAX_VALUE;
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
        while (curOpRecord != null) {
            //clear the mark in each PR
            this.curOpRecordPRs = new boolean[this.k];
            
            List<Operand> curRecordOperands = curOpRecord.getOperands();
            if (curRecordOperands == null) {
                curOpRecord = curOpRecord.getNext();
                //System.out.println("curOpRecord: "+ curOpRecord.toString());
                continue;
            }
            int operandsSize = curRecordOperands.size();

            // edge case when the current record is EOF/EOL/NOP
            if (operandsSize == 0) {
                curOpRecord = curOpRecord.getNext();
                //System.out.println("curOpRecord: "+ curOpRecord.toString());
                continue;
            }
            //System.out.println("current operation operands: " + curRecordOperands);

            //special handling for rematerializable values
            if (curOpRecord.getOpCode().equals(Opcode.loadI)){
                int curVR = curOpRecord.getOperand3().getVR();
                int addr = curOpRecord.getOperand1().getSR();
                this.VRToSpillLoc[curVR] = -addr;
                // System.out.println("curOpRecord: " + curOpRecord);
                // System.out.println(String.format("VRToSpillLoc[%d] = %d", curVR, this.VRToSpillLoc[curVR]));
            }

            Operand definedRegister = curRecordOperands.get(operandsSize - 1);
            if (!definedRegister.isRegister()) {
                    curOpRecord = curOpRecord.getNext();
                    //System.out.println("curOpRecord: "+ curOpRecord.toString());
                    continue;
            }//it means that the current operation is OUTPUT, and the only operand is an integer

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
                    //System.out.println("current operation record where a restore should take place right after: " + curOpRecord);
                    OpRecord prev = curOpRecord.getPrev();
                    restore(usedOperand.getVR(), usedOperand.getPR(), curOpRecord);
                    // System.out.println("prev: " + prev);
                    // System.out.println("is supposed to be loadI: " + prev.getNext());
                    // System.out.println("is supposed to be load: " + prev.getNext().getNext());
                    // System.out.println("is supposed to be cur: " + prev.getNext().getNext().getNext());
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
            // clear the mark in each PR
            this.curOpRecordPRs = new boolean[this.k];
            //now handle defined register  
            if (curOpRecord.getOpCode() != Opcode.store){
                int pr = getAPR(definedRegister.getVR(), definedRegister.getNU(), curOpRecord);
                definedRegister.setPR(pr);
                // set the mark in definedRegister.PR
                this.curOpRecordPRs[pr] = true;
            }
            
            curOpRecord = curOpRecord.getNext();
        }
        
    }
    private Integer getAPR(Integer VR, Integer NU, OpRecord curOpRecord){
        int x = -1;
        if (!this.PRStack.isEmpty()){
            x = this.PRStack.pollFirst();
        } else {
            //pick an unmarked x to spill; what if there is a tie between multiple PRs
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
        //special handling for the scenario when curOpRecord is LoadI, since it can be rematerialized
        //System.out.println(String.format("current value of VRToSpillLoc[%d]: %d", this.PRToVR[x], this.VRToSpillLoc[this.PRToVR[x]]));
        if (this.VRToSpillLoc[this.PRToVR[x]] <= 0){
            //System.out.println("rematerialize " + curOpRecord);
            //since the VR is now stored in memory, we should reset its value in VRToPR
        } else {
            //go here: " + curOpRecord);
            //general cases
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
        
        //since the VR is now stored in memory, we should reset its value in VRToPR
        this.VRToPR[this.PRToVR[x]] = -1;
        // this.PRToVR[x] = -1;
        // this.PRNU[this.PRToVR[x]] = Integer.MAX_VALUE;
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
        //System.out.println("current VRToSpillLoc: ");
        // for (int i = 0; i < this.VRToSpillLoc.length; i++) {
        //     System.out.println(String.format("%d: %d", i, this.VRToSpillLoc[i]));
        // }
        //special handling for rematerialization value
        if (this.VRToSpillLoc[VR] < 1) {
            //System.out.println("now restore " + curOpRecord + ", it's spill loc is " + this.VRToSpillLoc[VR]);
            OpRecord loadIRecord = new OpRecord(-1, Opcode.loadI, 
                                                new Operand(-this.VRToSpillLoc[VR], -this.VRToSpillLoc[VR], -this.VRToSpillLoc[VR], null, false), 
                                                null, 
                                                new Operand(null, VR, PR, curOpRecordIndex, false));
            preOpRecord.setNext(loadIRecord);

            loadIRecord.setPrev(preOpRecord);
            loadIRecord.setNext(curOpRecord);

            curOpRecord.setPrev(loadIRecord);
        } else if (this.VRToSpillLoc[VR] > 32767 && this.VRToSpillLoc[VR] != Integer.MAX_VALUE) {
            //general case
            OpRecord loadIRecord = new OpRecord(-1, Opcode.loadI, 
                                                new Operand(this.VRToSpillLoc[VR], this.VRToSpillLoc[VR], this.VRToSpillLoc[VR], null, false), 
                                                null, 
                                                new Operand(null, null, this.reservedRegister, curOpRecordIndex, false));
            OpRecord loadRecord = new OpRecord(-1, Opcode.load, 
                                                new Operand(null, null, this.reservedRegister, null, false), 
                                                null, 
                                                new Operand(null, VR, PR, curOpRecordIndex, false));
            
            preOpRecord.setNext(loadIRecord);

            loadIRecord.setPrev(preOpRecord);
            loadIRecord.setNext(loadRecord);

            loadRecord.setPrev(loadIRecord);
            loadRecord.setNext(curOpRecord);

            curOpRecord.setPrev(loadRecord);
        }
        //update spilledAddr and VRToSpilledLoc
        this.VRToSpillLoc[VR] = Integer.MAX_VALUE;
    }
}
