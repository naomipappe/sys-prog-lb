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
    private final StringBuilder ACCEPTED_WORD_BUILDER;


    private String getAcceptedWord(){
        DFS(initialState,new TreeSet<>());
        return Stream.of(ACCEPTED_WORD_BUILDER.toString().split("\\|")).findAny().get();
    }
    private void DFS(Integer current, Set<Integer> marked) {
        /*TODO implement DFS in such way that only one word is made, or as a stream
        maybe something like return.any
        */
        if(NDFAfinalStates.contains(current)){
            ACCEPTED_WORD_BUILDER.append('|');
            return;
        }
        if(marked.contains(current)){
            return;
        }
        if(!NDFAtransitionMap.keySet().contains(current)){
            return;
        }
        marked.add(current);
        for(Character key: NDFAtransitionMap.get(current).keySet()){
            for(Integer state: NDFAtransitionMap.get(current).get(key)){
                if(!marked.contains(state)){
                ACCEPTED_WORD_BUILDER.append(key);
                DFS(state, marked);
                }
            }
        }
    }


    private boolean accept(String w2, String w1){
        String acceptedString = w2 + getAcceptedWord() + w1;
        Set<Integer> currentStates = new TreeSet<>();
        currentStates.add(initialState);
        for(int i = 0; i<acceptedString.length(); ++i){
            currentStates = nextStates(currentStates,acceptedString.charAt(i));
        }
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
        ACCEPTED_WORD_BUILDER = new StringBuilder();
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

    public static void main( String[] args )
    {
        Scanner in = new Scanner(System.in);
        String filename = in.nextLine();
        NDFA automaton = new NDFA(filename);
        System.out.println(automaton.accept("",""));
    }
}
