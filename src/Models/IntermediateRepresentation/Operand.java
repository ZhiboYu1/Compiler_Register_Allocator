package IntermediateRepresentation;

public class Operand {
    String SR;
    String VR;
    String PR;
    String NU;
    public Operand(String SR, String VR, String PR, String NU) {
        this.SR = SR;
        this.VR = VR;
        this.PR = PR;
        this.NU = NU;
    }
    public String toString() {
        return String.format("SR: %s", this.SR);
    }
}
