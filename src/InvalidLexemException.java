public class InvalidLexemException extends Exception{
    public InvalidLexemException(String expr, int index) {
        System.out.println("-".repeat(index) + "^");
        System.out.printf("Invalid lexem %s at index %d!", expr, index);

    }
    public InvalidLexemException(char ch) {
        System.out.printf("Unknown character %s", ch);
    }
}
