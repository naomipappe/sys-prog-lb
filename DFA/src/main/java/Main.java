import com.naomipappe.DFA.*;
import java.util.Scanner;


public class Main {
    public static void main(String [] args){
        Scanner in = new Scanner(System.in);
        String filePath = in.nextLine();
        String[] pathParts = filePath.split("\\\\");
        String filename = pathParts[pathParts.length-1];
        NFA automaton = new NFA();

        if(!automaton.initFromFile(filePath)){
            System.out.println("Invalid automaton configuration");
        }
        else{
            automaton.printNFAConfiguration();
            String w2, w1;
            w2 = in.nextLine();
            w1 = in.nextLine();
            if(automaton.accepts(w2, w1)){
                System.out.println("Automaton " + filename + " accepts word w0: " + automaton.getAcceptedWord());
                System.out.print("Automaton " + filename);
                System.out.print(" accepts word: " + w2 + automaton.getAcceptedWord() + w1);
            }
            else{
                System.out.println("Automaton " + filename + " accepts word w0: " + automaton.getAcceptedWord());
                System.out.print("Automaton " + filename);
                System.out.print(" does not accept word: " + w2 + automaton.getAcceptedWord() + w1);
            }
        }
    }
}
