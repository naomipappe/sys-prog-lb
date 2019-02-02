package com.naomipappe.lab;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class App
{   private static Set<String> results = new LinkedHashSet<>();
    final private static String VOWELS_PATTERN = "[aeiouyаэуояеёюиAEIOUАЭУОЯЕЁЮИ]+$";
//    private static boolean vowelsPredicate(String word)
//    {
//        return !word.contains(consonants);
//    }
    private static void processLines(String line){
        Stream<String> words = Stream.of(line.split("\\PL+"));
        Stream<String> filtered = words.filter(s->s.matches(VOWELS_PATTERN) && s.length()<=30);
        filtered.forEach(s->results.add(s));
        words.close();
        filtered.close();
    }
    private static Set<String> processFile(String filename)
    {
        try(Stream<String> lines = Files.lines(Paths.get(filename),StandardCharsets.UTF_8))
        {
            lines.forEach(App::processLines);
        }
        catch(IOException e)
        {
            System.out.println("An exception occurred while reading a file");
            e.printStackTrace();
            results = Collections.emptySet();
        }
        return results;
    }


    private static Set<String> onlyVowels(String filename)
    {
        processFile(filename);
        return results;
    }

    public static void main( String[] args )

    {
        Scanner scanner = new Scanner(System.in);
        String filename = scanner.nextLine();
        long beforeUsedMem=Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
        long startTime = Calendar.getInstance().getTime().getTime();
        Set<String> noConsonants = onlyVowels(filename);
        if (!noConsonants.equals(Collections.emptySet())){
        noConsonants.forEach(System.out::println);
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
