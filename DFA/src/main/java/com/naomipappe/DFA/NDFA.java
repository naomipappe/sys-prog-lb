package com.naomipappe.DFA;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NDFA
{
    private Set<Character> NDFAAlphabet;
    private Set<String> NDFAStates;
    private String initialState;
    private Set<String> NDFAfinalStates;
    private Map<String,Map<Character,Set<String>>> NDFAtransitionMap;

    private void transitionMapLoader(String fileLine){
        String [] transition = fileLine.split("\\s+");
        if( !NDFAtransitionMap.containsKey(transition[0])){
            NDFAtransitionMap.put(transition[0],new TreeMap<>());
        }
        Map<Character,Set<String>> curEdges = NDFAtransitionMap.get(transition[0]);
        if(!curEdges.containsKey(transition[1].charAt(0))){
            curEdges.put(transition[1].charAt(0),new TreeSet<>());
        }
        curEdges.get(transition[1].charAt(0)).add(transition[2]);
    }


    public NDFA(){
        NDFAAlphabet = new TreeSet<>();
        NDFAStates = new TreeSet<>();
        NDFAfinalStates = new TreeSet<>();
        NDFAtransitionMap = new TreeMap<>();
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
        for(int i = 97; i < 97 + alphabetLength; ++i){
            NDFAAlphabet.add((char)i);
        }
        int statesAmount = Integer.parseInt(lines.poll());
        for(int i = 0; i < statesAmount; ++i){
            NDFAStates.add(String.valueOf(i));
        }
        initialState = lines.poll();
        try {
            NDFAfinalStates.addAll(Arrays.asList(lines.poll().split("\\s+")));
        }
        catch (NullPointerException unexpected){
            unexpected.printStackTrace();
        }
        lines.forEach(s-> transitionMapLoader(s));
//        System.out.println();
//        NDFAAlphabet.forEach(s-> System.out.print(String.valueOf(s)+" "));
//        System.out.println();
//        NDFAStates.forEach(s-> System.out.print(s+' '));
//        System.out.println();
//        System.out.println(initialState);
//        System.out.println();
//        NDFAfinalStates.forEach(s-> System.out.print(s));
//        System.out.println();
//        NDFAtransitionMap.forEach((s,k)-> System.out.println(NDFAtransitionMap.get(s)));

        return true;
    }



    public static void main( String[] args )
    {
        Scanner in = new Scanner(System.in);
        String filename = in.nextLine();
        NDFA automaton = new NDFA();
        automaton.initFromFile(filename);
    }
}
