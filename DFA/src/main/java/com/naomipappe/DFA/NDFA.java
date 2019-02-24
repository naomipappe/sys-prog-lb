package com.naomipappe.DFA;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NDFA
{   //all sets are made TreeSets cause we need fast search and fast elements retrieval O(log(n))
    /*transition map(table) is made this way because we have Nondeterministic finite automaton
      so basically transition function should be able to hold such triplets as:
      (1 a 2) and (1 a 3)
    */
    private Set<Character> NDFAAlphabet;
    private Set<Integer> NDFAStates;
    private Integer initialState;
    private Set<Integer> NDFAfinalStates;
    private Integer DEVIL_STATE = -1;
    private Map<Integer,Map<Character,Set<Integer>>> NDFAtransitionMap;
    private final StringBuilder ALLOWED_WORD;

    private void transitionMapLoader(String fileLine){
        //transition goes like this (s, a, s')
        String [] transition = fileLine.split("\\s+");
        if(!NDFAtransitionMap.containsKey(Integer.parseInt(transition[0]))){
            NDFAtransitionMap.put(Integer.parseInt(transition[0]), new TreeMap<>());
        }
        Map<Character,Set<Integer>> curEdges = NDFAtransitionMap.get(Integer.parseInt(transition[0]));
        if(!curEdges.containsKey(transition[1].charAt(0))){
            curEdges.put(transition[1].charAt(0), new TreeSet<>());
        }
        curEdges.get(transition[1].charAt(0)).add(Integer.parseInt(transition[2]));
    }
    private void getAcceptedWord(){
        Set<Integer> used = new TreeSet<>();
        DFS(used,initialState);
        System.out.println(ALLOWED_WORD.toString());
    }

    private void DFS(Set<Integer> used,Integer state){
        used.add(state);
        for(Character key: NDFAtransitionMap.get(state).keySet() ){
            ALLOWED_WORD.append(key);
            for(Integer point: NDFAtransitionMap.get(state).get(key)){
                if(NDFAfinalStates.contains(point)){
                    return;
                }
                if (!used.contains(point)){
                    used.add(point);
                }
            }
        }


    }

    private boolean accept(String w2, String w1){
        getAcceptedWord();
        String acceptedString = w2 + ALLOWED_WORD.toString() + w1;
        Set<Integer> currentStates = new TreeSet<>();
        currentStates.add(initialState);
        for(int i = 0; i<acceptedString.length(); ++i){
            currentStates = nextStates(currentStates,acceptedString.charAt(i));
        }
        currentStates.forEach(s-> System.out.print(s+" "));
        return currentStates.stream().anyMatch(s->NDFAfinalStates.contains(s));
    }

    private Set<Integer> nextStates (Set<Integer> currStates, Character signal){
        Set<Integer> nextStates = new TreeSet<>();
        if(!NDFAAlphabet.contains(signal)){
            currStates.add(DEVIL_STATE);
            return nextStates;
        }
        currStates.forEach(cur->nextStates.addAll(NDFAtransitionMap.get(cur).get(signal)));
        return nextStates;
    }

    public NDFA(){
        NDFAAlphabet = new TreeSet<>();
        NDFAStates = new TreeSet<>();
        NDFAfinalStates = new TreeSet<>();
        NDFAtransitionMap = new TreeMap<>();
        ALLOWED_WORD = new StringBuilder();
    }
    public NDFA (String file){
        this();
        this.initFromFile(file);
    }

    public boolean initFromFile(String file){
        LinkedList<String> lines;

        try(Stream<String> fileStream = Files.lines(Paths.get(file), StandardCharsets.UTF_8)){
            lines = fileStream.collect(Collectors.toCollection(LinkedList::new));
        }
        catch (IOException inputException) {
            inputException.printStackTrace();
            return false;
        }

        if (lines.size()<5){
            return false;
        }

        int alphabetLength = Integer.parseInt(lines.poll());
        IntStream.range(97, 97 + alphabetLength).forEach(i -> NDFAAlphabet.add((char) i));

        int statesAmount;

        try {
            statesAmount = Integer.parseInt(lines.poll());
        }
        catch (NullPointerException unexpected){
            System.out.println("Amount of states is onot specified");
            unexpected.printStackTrace();
            return false;
        }

        IntStream.range(0, statesAmount).forEach(i -> NDFAStates.add(i));

        try {
            initialState = Integer.parseInt(lines.poll());
        }
        catch (NullPointerException unexpected){
            initialState = 0;
            System.out.println("Initial state was not provided, set to 0");
            unexpected.printStackTrace();
            return false;
        }

        try {
            Arrays.asList(lines.poll().split("\\s+")).forEach(s->NDFAfinalStates.add(Integer.parseInt(s)));
        }
        catch (NullPointerException unexpected){
            System.out.println("No final states specified");
            unexpected.printStackTrace();
            return false;
        }

        lines.forEach(s-> transitionMapLoader(s));
        System.out.println();
        NDFAAlphabet.forEach(s-> System.out.print(s + " "));
        System.out.println();
        NDFAStates.forEach(s-> System.out.print(s + " "));
        System.out.println();
        System.out.println(initialState);
        NDFAfinalStates.forEach(s-> System.out.print(s + " "));
        System.out.println();
        NDFAtransitionMap.forEach((s,k)-> System.out.println(NDFAtransitionMap.get(s)));
        return true;
    }


    public static void main( String[] args )
    {
        Scanner in = new Scanner(System.in);
        String filename = in.nextLine();
        NDFA automaton = new NDFA(filename);
        System.out.println(automaton.accept("c","a"));

    }
}
