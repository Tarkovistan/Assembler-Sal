import java.io.IOException;

/**
 * Driver program for SAL ASM to text-based binary.
 * @author djb
 * @version 2021.12.01
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
   
        if(args.length != 1) {
            System.err.println("Usage: java Main file.sal");
        }
        else {
            String filename = args[0]; 
            
            if(filename.endsWith(".sal")) {
                
                try {
                    Assembler assem = new Assembler();
                    assem.assemble(filename);
                } catch (IOException ex) {
                    System.err.println("Exception parsing: " + filename);
                    System.err.println(ex);
                }
            }
            else {
                
                System.err.println("Unrecognised file type: " + filename);
            }
        }
    }
}
