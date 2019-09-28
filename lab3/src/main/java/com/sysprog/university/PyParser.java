package com.sysprog.university;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PyParser implements Parser {
    private static LexemContainer container;
    private StringBuilder finalContents;

    public PyParser() {
        finalContents = new StringBuilder();
        container = new LexemContainer();
    }

    private static String wrapToHTML(String line) {
        return "<div class=\"line\">" + line + "</div>";
    }

    private static String wrap(String line, String type) {
        if (type.equals("Whitespace")) {
            if ((int) line.charAt(0) == 9)
                type = "Tab";
            else
                type = "Space";
        }
        return "<div class='" + type + "'>" + line + "</div>";
    }

    public void parse(Stream<String> fileStream) {
        LinkedList<String> filecontents = fileStream.collect(Collectors.toCollection(LinkedList::new));
        for (String line : filecontents) {
            String finalLine = "";
            List<LineSegment> lineLexems = container.parse(line);
            for (LineSegment segment : lineLexems) {
                String lexem = line.substring(segment.getFrom(), segment.getTo());
                finalLine += (wrap(lexem, segment.getType()));
            }
            finalContents.append(wrapToHTML(finalLine));
        }
    }

    public void createHTML(String outputFileName) throws IOException {
        FileWriter fileWriter = new FileWriter(outputFileName);
        String styles = "<link rel=\"stylesheet\" href=\"stylesheet.css\">";
        fileWriter.write(styles + "<main>" + "<meta charset=\"UTF-8\">" + finalContents.toString() + "</main>");
        fileWriter.close();
    }

}

class Lexem<Pattern, Type> {
    private final Pattern pattern;
    private final Type type;

    Lexem(Pattern pattern, Type type) {
        this.pattern = pattern;
        this.type = type;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Lexem)) {
            return false;
        }
        Lexem right = (Lexem) obj;
        return this.pattern.equals(right.getPattern()) && this.type.equals(right.getType());
    }

    @Override
    public int hashCode() {
        return pattern.hashCode() ^ type.hashCode();
    }
}

class LineSegment {
    private int from, to;
    private String type;

    public LineSegment(int from, int to, String type) {
        this.from = from;
        this.to = to;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}

class LexemContainer {
    private static final ArrayList<String> types = new ArrayList<>(Arrays.asList("Whitespace", "Comment",
            "Property", "Imaginary number", "Floating point number", "String", "Function", "Operator", "Keyword",
            "Integer", "Boolean", "None", "Variable", "Undefined"));

    private static final ArrayList<String> pythonKeywords = new ArrayList<>(Arrays.asList("and", "as", "assert",
            "break", "class", "continue", "def", "del", "elif", "else", "except", "finally", "for", "from", "global",
            "if", "import", "in", "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while",
            "with", "yield"));

    private static final ArrayList<String> ordinaryOperators = new ArrayList<>(Arrays.asList(">>=", "<<=", "%-", "!=",
            "<<", ">>", "<=", ">=", "-=", "&=", "%="));
    private static final ArrayList<String> specialOperators = new ArrayList<>(Arrays.asList("+=", "*=", "/=", "|=",
            "^=", "+", "/", "|", "^", "*", "(", ")", "{", "}", "[", "]", ".", ":"));
    private static final ArrayList<String> ordinaryShortOperators = new ArrayList<>(Arrays.asList("<", ">", "=", "-",
            "&", "~", ",", ";", "@"));

    private static final String[] specialSymbols = {"+", "*", "/", "|", "^", "(", ")", "{", "}", "[", "]", ".", ":"};

    private static Map<String, Integer> lexemTable = new HashMap<>();
    private static List<Lexem<String, Integer>> lexemList = new ArrayList<>();

    public LexemContainer() {
        for (String type : types) {
            lexemTable.put(type, types.indexOf(type));
        }

        for (String keyword : pythonKeywords) {
            insertLexem(new Lexem<>(buildSingleWordPattern(keyword), lexemTable.get("Keyword")));
        }

        insertLexem(new Lexem<>("\\/\\/", lexemTable.get("Operator")));
        insertLexem(new Lexem<>("\\/\\/=", lexemTable.get("Operator")));
        insertLexem(new Lexem<>("\\*\\*", lexemTable.get("Operator")));
        insertLexem(new Lexem<>("\\*\\*=", lexemTable.get("Operator")));

        for (String ordinaryOperator : ordinaryOperators) {
            insertLexem(new Lexem<>(ordinaryOperator, lexemTable.get("Operator")));
        }

        insertLexem(new Lexem<>("(?<=\\W|^)@[a-zA-Z_]+(?=\\W|$)", lexemTable.get("Property")));

        for (String specialOperator : specialOperators) {
            insertLexem(new Lexem<>(buildSingleOperatorPattern(specialOperator), lexemTable.get("Operator")));
        }

        for (String ordinaryShorOperator : ordinaryShortOperators) {
            insertLexem(new Lexem<>(ordinaryShorOperator, lexemTable.get("Operator")));
        }

        insertLexem(new Lexem<>("#.*", lexemTable.get("Comment")));

        insertLexem(new Lexem<>("(?<=[^\\\\]|^)\\\".*?[^\\\\]\\\"", lexemTable.get("String")));
        insertLexem(new Lexem<>("(?<=[^\\\\]|^)\\\'.*?[^\\\\]\\\'", lexemTable.get("String")));

        insertLexem(new Lexem<>("(?<=[^\\d\\w\\.]|^)\\d+\\.\\d+j(?=[^\\d\\w\\.]|$)",
                lexemTable.get("Imaginary number")));
        insertLexem(new Lexem<>("(?<=[^\\d\\w\\.]|^)\\d*\\.+\\d+(?=[^\\d\\w\\.]|$)",
                lexemTable.get("Floating point number")));
        insertLexem(new Lexem<>("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?",
                lexemTable.get("Floating point number")));
        insertLexem(new Lexem<>("(?<=[^\\d\\w\\.]|^)\\d*(?=[^\\d\\w\\.]|$)",
                lexemTable.get("Integer")));
        insertLexem(new Lexem<>("(?<=[\\W]|^)(True|False)((?=[\\W])|$)",
                lexemTable.get("Boolean")));
        insertLexem(new Lexem<>("(?<=[\\W]|^)(None)((?=[\\W])|$)",

                lexemTable.get("None")));
        insertLexem(new Lexem<>("(?<=\\s|^)[a-zA-Z_]+[\\s]*(?=\\()",

                lexemTable.get("Function")));
        insertLexem(new Lexem<>("(?<=[\\W]|^)[a-zA-Z_]\\w*(?=\\W|$)",

                lexemTable.get("Variable")));
        insertLexem(new Lexem<>("\\s", lexemTable.get("Whitespace")));

        insertLexem(new Lexem<>("(?<=\\s|^)[^\\s]+(?=\\s|$)", lexemTable.get("Undefined")));

    }

    public static List<LineSegment> parse(String line) {
        List<LineSegment> result = new ArrayList<>();
        for (Lexem<String, Integer> i : lexemList) {
            Pattern pattern = Pattern.compile(i.getPattern());
            Matcher matcher = pattern.matcher(line);

            int type = i.getType();
            while (matcher.find()) {
                int from = matcher.start();
                int to = matcher.end();

                if (to - from == 0)
                    continue;

                insertLineSegment(result, new LineSegment(from, to, types.get(type)));

                if (types.get(type).equals("Whitespace"))
                    continue;

                line = line.substring(0, from) + filler(to - from) + line.substring(to);
            }
        }
        return result;
    }

    private static void insertLexem(Lexem<String, Integer> lexem) {
        boolean added = false;
        for (int i = 0; i < lexemList.size(); ++i) {
            if (lexem.getType() < (int) lexemList.get(i).getType()) {
                lexemList.add(i, lexem);
                added = true;
                break;
            }
        }
        if (!added) {
            lexemList.add(lexem);
        }
    }

    private static String filler(int spacesAmount) {
        char[] fillerArray = new char[spacesAmount];
        for (int i = 0; i < fillerArray.length; ++i) {
            fillerArray[i] = ' ';
        }
        return String.valueOf(fillerArray);
    }

    private static String escapeSpecial(char symbol) {
        String s = String.valueOf(symbol);
        if (Arrays.asList(specialSymbols).contains(s))
            return "\\" + s;
        if (Character.isWhitespace(symbol))
            return "\\s";
        return s;
    }

    private static void insertLineSegment(List<LineSegment> l, LineSegment segment) {
        for (int i = 0; i < l.size(); ++i) {
            if (segment.getFrom() < l.get(i).getFrom()) {
                l.add(i, segment);
                return;
            }
        }
        l.add(segment);
    }

    public void printLexemPatternDict() {
        for (Lexem<String, Integer> lexem : lexemList) {
            System.out.println(lexem.getPattern() + ": " + lexem.getType());
        }
    }

    private String buildSingleWordPattern(String word) {
        return "(?<=[\\W]|^)" + word + "((?=[\\W])|$)";
    }

    private String buildSingleOperatorPattern(String operator) {
        return "\\" + operator;
    }
}