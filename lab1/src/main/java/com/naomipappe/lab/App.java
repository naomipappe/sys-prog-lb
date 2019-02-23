package com.naomipappe.lab;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class App
{   private static Set<String> results = new LinkedHashSet<>();
    private static String DEFAULT_FLAG = "-v";
    final private static String VOWELS_PATTERN = "[aeiouyAEIOUY]+$";

    private static boolean noConsonantLettersPredicate(String word){
        return word.matches(VOWELS_PATTERN);
    }

    private static boolean isUniqueCharsPredicate (String word) {
        if (word.isEmpty()){
            return false;
        }
        char[] charactersFromWord = word.toLowerCase().toCharArray();
        Arrays.sort(charactersFromWord);
        for (int i = 0; i<charactersFromWord.length-1; ++i){
            if(charactersFromWord[i]==charactersFromWord[i+1]) {
                return false;
            }
        }
        return true;
    }

    private static void processLine(String line, Function<String, Boolean> predicate){
        Stream<String> words = Stream.of(line.split("\\PL+"));
        Stream<String> filtered = words.filter(s-> (s.length() <= 30) && predicate.apply(s));
        filtered.forEach(s->results.add(s));
        words.close();
        filtered.close();
    }

    private static void processFile(String filename, String flag){
        Function<String,Boolean> predicate;
        if (flag.equals("-u")){
            predicate = App::isUniqueCharsPredicate;
        }
        else if (flag.equals("-v")){
            predicate = App::noConsonantLettersPredicate;
        }
        else{
            predicate = App::noConsonantLettersPredicate;
        }
        try(Stream<String> lines = Files.lines(Paths.get(filename),StandardCharsets.UTF_8))
        {
            lines.forEach(s->processLine(s,predicate));
        }
        catch(IOException e)
        {
            System.out.println("An exception occurred while reading a file");
            e.printStackTrace();
            results = Collections.emptySet();
        }
    }

    private static Set<String> processText(String filename, String flag){
        processFile(filename,flag);
        return results;
    }

    public static void main( String[] args ){
        System.out.println("Please enter a file name(path to a file) and a processing flag");
        System.out.println("\"-u\" stands for getting all the words with non-repeating characters");
        System.out.println("\"-v\" stands for getting all the words that consist only of vowels");
        System.out.println("\"Word\" is defined as a sequence of alphabetical characters(excluding numbers)");
        Scanner scanner = new Scanner(System.in);
        String [] input = scanner.nextLine().split("\\s+");
        String filename = input[0];
        String flag;
        if (input.length==1){
            flag = DEFAULT_FLAG;
        }
        else{
            flag = input[1];
        }
        long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        long startTime = Calendar.getInstance().getTime().getTime();

        Set<String> results = processText(filename,flag);
        if (!results.equals(Collections.emptySet())){
        results.forEach(System.out::println);
        }
        else{
            System.out.println("No such words found");
        }

        long endTime = Calendar.getInstance().getTime().getTime();
        long afterUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();

        System.out.println("Memory used:" + ((afterUsedMem-beforeUsedMem)*1e-6));
        System.out.println("Execution time in milliseconds: " + (endTime-startTime));
    }
}
