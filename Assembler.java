import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.text.DefaultStyledDocument.ElementSpec;

import java.io.File;

/**
 * Coordinate the translation of SAL assembly code to text-based binary.
 * 
 * @author
 * @version
 */
public class Assembler {
    /**
     * Create an assembler.
     */
    HashMap<String, String> symbols = new HashMap<>();
    HashMap<String, String> variables = new HashMap<>();
    HashMap<String, String> labels = new HashMap<>();
    HashMap<String, String> registers = new HashMap<>();

    public Assembler() {
    }

    /**
     * Translate the SAL asm file.
     * 
     * @param filename The file to be translated.
     * @throws IOException on any input issue.
     */
    public void assemble(String filename)
            throws IOException {
        constructTables();
        File file = new File(filename);
        Scanner scan = new Scanner(file);
        Boolean dataFound = false;
        Boolean codeFound = false;
        int variableCounter = 0;
        int instructionNumber = 0;
        String outputFileName = filename.replace(".sal", ".bin");
        FileWriter fw = new FileWriter(outputFileName);

        while (scan.hasNextLine()) { // First scan for .data and variables

            String line = scan.nextLine();

            int index = line.indexOf("//");
            if (index != -1) { // If line contains "//"
                line = line.substring(0, index); // Take everything before the comment
            }
            line = line.trim();
            String[] vals = line.split("\\s+");

            if (!dataFound) { // If .data was not found, keep searching for .data
                if (vals.length == 1) {
                    if (vals[0].equals(".data")) {
                        // System.out.println(".data found");
                        dataFound = true;
                    }
                }
            } else { // Search the variables under until .code is reached
                if (!codeFound) {
                    // vals contains each variable
                    if (vals[0].equals(".code")) {
                        codeFound = true;
                    } else {
                        if (!vals[0].isEmpty()) {
                            String varValBinary = Integer.toBinaryString(variableCounter);
                            String finalVal = String.format("%6s", varValBinary).replaceAll(" ", "0");
                            variables.put(vals[0], finalVal);
                            variableCounter++;
                        }

                    }

                } else {
                    // System.out.println(".code found");
                    codeFound = false;
                    break;
                }
            }
        }
        scan.close();

        scan = new Scanner(file);
        while (scan.hasNextLine()) {

            String outputLine; // line to be written to a file
            String outputLine2; // If bits > 16
            String line = scan.nextLine();
            int index = line.indexOf("//");
            if (index != -1) { // If line contains "//"
                line = line.substring(0, index); // Take everything before the comment
            }
            if (line.matches("\\s+") || line.isEmpty()) {

            } else {
                line = line.trim().replace(",","");

                String[] vals = line.split("\\s+");

                for (int i = 0; i < vals.length; i++) {
                    vals[i] = vals[i].trim();

                }

                if (vals[0].equals(".code") && vals.length <= 1 && !codeFound) { // If code is found,
                    codeFound = true;
                } else { // Start keeping instruction number and assign labels to their corresponding
                         // instruction number, next instruction

                    if (symbols.containsKey(vals[0])) {
                        instructionNumber++;
                        if(vals.length ==3){
                            if (vals[2].startsWith("#")) {
                                vals[2] = vals[2].substring(1);
                                if(Integer.parseInt(vals[2])>63){
                                instructionNumber++;
                            }
                            }
                            else{
                                if (Character.isDigit(vals[2].charAt(0))) {
                                    if(Integer.parseInt(vals[2])>63){
                                        instructionNumber++;
                                    }
                                }
                                
                            }
                            
                       }

                    } else if (vals[0].endsWith(":") || vals.length > 1 && vals[1].equals(":")) { // check the number of
                                                                                                  // the next
                                                                                                  // instruction

                        while (scan.hasNextLine()) {
                            String line2 = scan.nextLine();

                            int index2 = line2.indexOf("//");

                            if (index2 != -1) { // If line contains "//"
                                line2 = line2.substring(0, index2); // Take everything before the comment
                            }

                            line2 = line2.trim().replace(",", " ");
                            

                            String[] vals2 = line2.split("\\s+");

                            // int test2 = 0;
                            // if (vals2[0].isEmpty()) {
                            // test2++;
                            // }

                            if (codeFound) {
                                if (symbols.containsKey(vals2[0])) { // If next line is an instruction
                                    
                                    if(vals2.length ==3){
                                        if (vals2[2].startsWith("#")) {
                                            vals2[2] = vals2[2].substring(1);
                                            if(Integer.parseInt(vals2[2])>63){
                                            instructionNumber++;
                                        }
                                        }
                                        else{
                                            if (Character.isDigit(vals2[2].charAt(0))) {
                                                if(Integer.parseInt(vals2[2])>63){
                                                    instructionNumber++;
                                                }
                                            }
                                            
                                        }
                                        
                                         
                                    }
                                    String instructionNumberBinary = Integer.toBinaryString(instructionNumber);
                                    String finalInstructionVal = String.format("%6s", instructionNumberBinary)
                                            .replaceAll(" ", "0");
                                    if (vals[0].endsWith(":")) {
                                        labels.put(vals[0].substring(0, vals[0].length() - 1), finalInstructionVal);
                                    } else {
                                        labels.put(vals[0].substring(0, vals[0].length()), finalInstructionVal);
                                    }

                                    // System.out.println("Instruction number: " + instructionNumber);
                                    instructionNumber++;

                                    break; // Leave the loop

                                }

                                else if (vals2[0].endsWith(":")) { // seperate the OR
                                    String instructionNumberBinary = Integer.toBinaryString(instructionNumber);
                                    String finalInstructionVal = String.format("%6s", instructionNumberBinary)
                                            .replaceAll(
                                                    " ",
                                                    "0");
                                    labels.put(vals2[0].substring(0, vals2[0].length() - 1), finalInstructionVal);

                                    // System.out.println(vals2[0]);

                                } else if (vals2.length > 1) {
                                    if (vals2[1].equals(":")) {
                                        String instructionNumberBinary = Integer.toBinaryString(instructionNumber);
                                        String finalInstructionVal = String.format("%6s", instructionNumberBinary)
                                                .replaceAll(
                                                        " ",
                                                        "0");
                                        labels.put(vals2[0].substring(0, vals2[0].length()), finalInstructionVal);
                                    }
                                }
                            }
                        }
                    }
                    // System.out.println(line);
                }
            }

        }
        scan.close();

        System.out.println(variables.entrySet());
        System.out.println(labels.entrySet());
        scan = new Scanner(file);
        codeFound = false;
        while (scan.hasNextLine()) {
            String size = "";
            String operand = "";
            String reg = "";
            String rva = "";
            String outputString1 = "";
            String outputString2 = "";
            String line = scan.nextLine();

            int index = line.indexOf("//");
            if (index != -1) { // If line contains "//"
                line = line.substring(0, index); // Take everything before the comment
            }
            line = line.trim().replace(",", " ");

            String[] vals = line.split("\\s+");
            // for(int i= 0; i< vals.length; i++){
            // System.out.println(vals[i]);
            // }
            if (!codeFound) {
                if (vals[0].equals(".code")) { // Start translating instruction after .code is found
                    codeFound = true;

                }
            } else { // Read lines after .code and check for instruction keywords using HashMap

                if (vals.length > 2) { // If the instruction has 3 words,
                    if (symbols.containsKey(vals[0])) {
                        outputString1 = outputString1 + symbols.get(vals[0]); // colon
                        if (registers.containsKey(vals[1])) {
                            reg = registers.get(vals[1]); // colon
                            
                            if (vals[2].startsWith("#")) {
                                rva = "01"; // colon
                                vals[2] = vals[2].substring(1);
                            }
                            else{
                                rva = "10"; // colon
                            }
                            
                            

                            // Check if 3rd value is a register, 6 bit
                            
                            if (symbols.containsKey(vals[2])) {
                                operand =symbols.get(vals[2]); // colon
                                rva = "00"; // colon
                                size = "0"; // colon

                            } else if (labels.containsKey(vals[2])) { // If 3rd value is a label
                                operand =labels.get(vals[2]);//colon
                                size = "0"; // colon
                            }
                            
                            else if (variables.containsKey(vals[2])){
                                size = "0"; // colon
                                operand = variables.get(vals[2]);
                                //System.out.println("operand: "+ operand);
                            }
                             else { // if 3rd value is a value (e.g #250), or address (e.g. 250)
                                     // if val > 63, add 000000 to str1, add operand to 2nd 16bit line
                                     // If 3rd value starts with #, get its substring, check if result > 63
                                     // if,
                                     // else translate it into binary and add to output line
                                     //
                                     

                                
                                if (Integer.parseInt(vals[2]) > 63) { // Create a new outputline
                                    size = "1"; // colon
                                    String varValBinary = Integer.toBinaryString(Integer.parseInt(vals[2]));
                                    String finalVal = String.format("%16s", varValBinary).replaceAll(" ", "0");
                                    operand = "000000";
                                    outputString2 = finalVal;
                                } else { // Otherwise just add the value to the operand
                                    size = "0"; // colon
                                    String varValBinary = Integer.toBinaryString(Integer.parseInt(vals[2]));
                                    String finalVal = String.format("%6s", varValBinary).replaceAll(" ", "0");
                                    operand = finalVal;
                                }

                            }
                        }

                    }
                    
                } else if (vals.length <= 2 && vals[0].equals("JMP")) { // If the OpCode is JMP
                     // colon
                    reg = "000";    // colon
                    size = "0"; // colon
                    outputString1 += symbols.get("JMP"); // colon
                    if(labels.containsKey(vals[1])) {   // If operand is a label
                        operand = labels.get(vals[1]);
                        rva = "10";
                    }
                    
                } else if (vals.length == 2 && !vals[0].equals("JMP")) { // If the OpCode is INC, DEC or NOT
                    outputString1 += symbols.get(vals[0]); // colon

                    reg = registers.get(vals[1]); // colon
                    size = "0"; // colon
                    rva = "11";// colon
                    operand = "000000";
                    
                }
                
                if(!(outputString1+reg+size+rva+operand).isEmpty()){
                    (outputString1+reg+size+rva+operand).replace(" ", "");
                    System.out.println(outputString1+reg+size+rva+operand);
                    fw.write(outputString1+reg+size+rva+operand+"\n");
                }
                if(!outputString2.isEmpty()){
                    fw.write(outputString2+"\n");
                    System.out.println(outputString2);
                }
                
            }

        }
        fw.close();
        scan.close();
    }

    public void constructTables() {
        symbols.put("ADD", "0000");
        symbols.put("SUB", "0001");
        symbols.put("AND", "0010");
        symbols.put("OR", "0011");
        symbols.put("JMP", "0100");
        symbols.put("JGT", "0101");
        symbols.put("JLT", "0110");
        symbols.put("JEQ", "0111");
        symbols.put("INC", "1001");
        symbols.put("DEC", "1010");
        symbols.put("NOT", "1011");
        symbols.put("LOAD", "1100");
        symbols.put("STORE", "1101");
        symbols.put("r0", "000000");
        symbols.put("r1", "000001");
        symbols.put("r2", "000010");
        symbols.put("r3", "000011");
        symbols.put("r4", "000100");
        symbols.put("r5", "000101");
        symbols.put("r6", "000110");
        symbols.put("r7", "000111");
        registers.put("r0", "000");
        registers.put("r1", "001");
        registers.put("r2", "010");
        registers.put("r3", "011");
        registers.put("r4", "100");
        registers.put("r5", "101");
        registers.put("r6", "110");
        registers.put("r7", "111");

    }
}
