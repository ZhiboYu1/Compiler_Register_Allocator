package IntermediateRepresentation;

public class Operand {
    private Integer SR;
    private Integer VR;
    private Integer PR;
    private Integer NU;
    private boolean isRegister;
    public Operand(Integer SR, Integer VR, Integer PR, Integer NU, boolean isRegister) {
        this.SR = SR;
        this.VR = VR;
        this.PR = PR;
        this.NU = NU;
        this.isRegister = isRegister;
    }
    public boolean isRegister(){
        return this.isRegister;
    }
    public Integer getSR(){
        return this.SR;
    }
    public Integer getVR(){
        return this.VR;
    }
    public void setVR(int vrNumber) {
        this.VR = vrNumber;
    }
    public Integer getPR() {
        return this.PR;
    }
    public void setPR(int prNumber) {
        this.PR = prNumber;
    }
    public Integer getNU() {
        return this.NU;
    }
    public void setNU(int nu){
        this.NU = nu;
    }
    public String toString() {
        return String.format("[SR: %s, VR: %s, PR: %s]", this.SR, this.VR, this.PR);
    }
}
