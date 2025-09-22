import java.io.File;

public class Main {
    public static void main(String[] args) {

        for (int i = 0; i < args.length; i++){
            System.out.println(String.format("args[%d]: %s", i, args[i]));
        }

        if (args.length == 1){ //either only have -h flag or only have file path
            if (args[0].equals("-h")){
                printCommandDescription();
            } else {
                scanParseFile(args[0], false, false);
            }
        } else if (args.length == 2){
            String flag = args[0];
            String filePath = args[1];
            if (flag.equals("-s")){
                scanParseFile(filePath, true, false);
            } else if (flag.equals("-p")){
                scanParseFile(filePath, false, false);//doesn't print IR
            } else if (flag.equals("-r")){
                scanParseFile(filePath, false, true);
            }
        } else {
            System.err.println("Please input one command line flag for each operation.");
            printCommandDescription();
        }

    }
    /**
     * helper function to parse the file (and print IR) or scan only 
     * @param filePath
     * @param onlyScan
     * @param printIR
     */
    private static void scanParseFile(String filePath, boolean onlyScan, boolean printIR){
        File toBeParsedFile = new File(filePath);
        if (toBeParsedFile.exists()){
            if (!onlyScan){
                Parser parser = new Parser(toBeParsedFile);
                if (printIR){
                    parser.parseAndPrintIR();
                } else {
                    parser.parse();
                }
            } else {
                Scanner scanner = new Scanner(toBeParsedFile);
                scanner.scanEntireFile();
            }
            
        } else {
            System.err.println("ERROR: File does not exist!");
            printCommandDescription();
        }
    }
    private static void printCommandDescription(){
        System.out.println("Supported command flags:\n" +
                "412fe –h: produce a list of valid command-line arguments that " +
                "includes a description of all command-line arguments.\n" +
                "412fe -s <name>: When the -s flag is present, 412fe reads the file specified by <name>" +
                "and print, to the standard output stream, a list of the tokens that the scanner found." +
                "For each token, it prints the line number, the token’s type (or syntactic category), and its" +
                "spelling (or lexeme).\n" +
                "412fe -p <name> When the -p flag is present, 412fe should read the file specified by " +
                "<name>, scan it and parse it, build the intermediate representation, and report either success or" +
                "report all the errors that it finds in the input file. If the parse succeeds, the front end must" +
                "report “Parse succeeded. Processed k operations.”, where k is the number of operations the" +
                "front end handled, printed without commas. If it finds errors, it must print “Parse found errors.”" +
                "412fe -r <name> When the -r flag is present, 412fe should read the file specified by <name>," +
                "scan it, parse it, build the intermediate representation, and print out the information in the" +
                "intermediate representation (in an appropriately human readable format)."
        );
    }
}