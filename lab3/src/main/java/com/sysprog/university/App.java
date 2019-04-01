package com.sysprog.university;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

public class App {
    private static Stream<String> fileStream(String filename) {
        Stream<String> inputStream;
        try {
            inputStream = Files.lines(Paths.get(filename),StandardCharsets.UTF_8);
        }

        catch (IOException inputFailure){
            System.out.println(inputFailure.getMessage());
            inputFailure.printStackTrace();
            return Stream.empty();
        }
        return inputStream;
    }

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        String filepath = inputScanner.nextLine();
        System.out.println(filepath);
        Stream<String> inputStream = fileStream(filepath);
        if (inputStream.equals(Stream.empty())){
            System.out.println("Input failure, shutdown");
            return;
        }
        PyParser parser = new PyParser();
        parser.parse(inputStream);
        try{
            parser.createHTML("result.html");
        }
        catch (IOException outputFailure){
            outputFailure.printStackTrace();
            System.out.println(outputFailure.getMessage());
            System.out.println("Output failure, shutdown");
        }
    }
}
