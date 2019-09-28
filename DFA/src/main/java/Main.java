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
            w1 = in.nextLine();
            w2 = in.nextLine();
            if(automaton.isAccepted(w1, w2)){
                System.out.println("Automaton " + filename + " accepts words w0: " + automaton.getAcceptedWord());
                System.out.print("Automaton " + filename);
                System.out.print(" accepts word: " + w1 + automaton.getAcceptedWord() + w2);
            }
            else{
                System.out.println("Automaton " + filename + " accepts word w0s: " + automaton.getAcceptedWord());
                System.out.print("Automaton " + filename);
                System.out.print(" does not accept word: " + w1 + automaton.getAcceptedWord() + w2);
            }
        }
    }
}
