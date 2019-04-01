package com.sysprog.university;

import java.io.IOException;
import java.util.stream.Stream;

public interface Parser {
    void parse(Stream<String> fileStream);
    void createHTML(String outputFileName)throws IOException;
}
