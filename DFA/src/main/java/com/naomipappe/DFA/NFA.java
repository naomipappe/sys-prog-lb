package com.naomipappe.DFA;


import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class NFA
{   /*
    All sets are made HashSet in terms of performance
    All sets are made final so attempting to change current NFA config results in failure
    Transition function is implemented as map of maps as we have NFA
    Implemented in such way, transition function can hold such triplets as (s, a, s'): (1 a 2) and (1 a 3)
    */
    private final Set<Character> NFAAlphabet;
    private final Set<Integer> NFAStates;
    private Integer initialState;
    private final Set<Integer> NFAFinalStates;
    private final Map<Integer, Map<Character, Set<Integer>>> NFATransitionMap;
    private final StringBuilder ACCEPTED_WORDS_BUILDER;
    private Set<String> NFAAcceptedWords;

    public NFA(){
        NFAAlphabet = new HashSet<>();
        NFAStates = new HashSet<>();
        NFAFinalStates = new HashSet<>();
        NFATransitionMap = new HashMap<>();
        ACCEPTED_WORDS_BUILDER = new StringBuilder();
        NFAAcceptedWords = new HashSet<>();
    }

    public NFA(String file){
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
        IntStream.range(97, 97 + alphabetLength).forEach(i -> NFAAlphabet.add((char) i));

        int statesAmount = Integer.parseInt(Objects.requireNonNull(lines.poll(),"Specify NFA states amount"));

        IntStream.range(0, statesAmount).forEach(i -> NFAStates.add(i));

        initialState = Integer.parseInt(Objects.requireNonNull(lines.poll(),"Specify initial NFA state"));
        String [] finalStates;
        finalStates = Objects.requireNonNull(lines.poll(),"Specify NFA final states").split("\\s+");
        Stream.of(finalStates).forEach(s->NFAFinalStates.add(Integer.parseInt(s)));

        lines.forEach(s-> transitionMapLoader(s));
        return true;
    }

    public void printNFAConfiguration() {
        System.out.println();
        NFAAlphabet.forEach(s-> System.out.print(s + " "));
        System.out.println();
        NFAStates.forEach(s-> System.out.print(s + " "));
        System.out.println();
        System.out.println(initialState);
        NFAFinalStates.forEach(s-> System.out.print(s + " "));
        System.out.println();
        NFATransitionMap.forEach((s, k)-> System.out.println(s + ": " + NFATransitionMap.get(s)));
    }

    public boolean isAccepted(String w1, String w2){
        makeAcceptedWords();
        ArrayList<Boolean> results = new ArrayList<>();
        for(String accepted: NFAAcceptedWords){
            results.add(accepts(w1 + accepted + w2));
        }
        return results.stream().anyMatch(s-> !s.equals(Boolean.FALSE));
    }

    private boolean accepts(String tested) {
        Set<Integer> reachable = new HashSet<>();
        reachable.add(initialState);
        for(Character signal : tested.toCharArray()){
            reachable.addAll(nextStates(signal,reachable));
        }
        return reachable.stream().anyMatch(s->NFAFinalStates.contains(s));
    }

    public Set<String> getAcceptedWord(){
        return NFAAcceptedWords;
    }

    private Set<Integer> nextStates(Character signal,Set<Integer> current){
        Set<Integer> nextStates = new HashSet<>();
        for(Integer state: current){
            Map<Character, Set<Integer>> possibleWays = NFATransitionMap.getOrDefault(state,Collections.emptyMap());
            if(possibleWays.equals(Collections.emptyMap())){
                continue;
            }
            Set<Integer> possibleStates = possibleWays.getOrDefault(signal,Collections.emptySet());
            if(possibleStates.equals(Collections.emptySet()) || !NFAAlphabet.contains(signal)){
                continue;
            }
            nextStates.addAll(possibleStates);
        }
        return nextStates;
    }

    private void makeAcceptedWords(){
        DFS(initialState, new TreeSet<>());
        Stream<String> accepted = Stream.of(ACCEPTED_WORDS_BUILDER.toString().split("\\|"));
        NFAAcceptedWords = accepted.collect(Collectors.toCollection(HashSet::new));
    }

    private void DFS(Integer current, Set<Integer> marked) {
        if(NFAFinalStates.contains(current)){
            ACCEPTED_WORDS_BUILDER.append('|'); //got to a final state, accepted word is build, delimit it
            return;
        }
        if(marked.contains(current)){
            return;
        }
        if(!NFATransitionMap.keySet().contains(current)){
            return;
        }
        marked.add(current);
        for(Character key: NFATransitionMap.get(current).keySet()){
            for(Integer state: NFATransitionMap.get(current).get(key)){
                if(!marked.contains(state)){
                    ACCEPTED_WORDS_BUILDER.append(key);
                    DFS(state, marked);
                }
            }
        }
    }

    private void transitionMapLoader(String fileLine){
        //transition goes like this (s, a, s')
        String [] transition = fileLine.split("\\s+");
        if(!NFATransitionMap.containsKey(Integer.parseInt(transition[0]))){
            NFATransitionMap.put(Integer.parseInt(transition[0]), new TreeMap<>());
        }
        Map<Character,Set<Integer>> curEdges = NFATransitionMap.get(Integer.parseInt(transition[0]));
        if(!curEdges.containsKey(transition[1].charAt(0))){
            curEdges.put(transition[1].charAt(0), new TreeSet<>());
        }
        curEdges.get(transition[1].charAt(0)).add(Integer.parseInt(transition[2]));
    }
}
