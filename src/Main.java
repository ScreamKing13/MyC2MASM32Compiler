import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String expression1 = "float a, c[3], d; a = 1.1; c = {3.3, 2.2, 1.1}; d = 4.4; int b;";
        LexemAnalyzer lexemAnalyzer = new LexemAnalyzer();
        try {
            System.out.println(expression1);
            ArrayList<Token> lexems = lexemAnalyzer.getTokenList(expression1);
            try {
                ArrayList<ArrayList<Token>> resultTokens = SyntaxAnalyzerC.checkSyntax(lexems);
                try {
                    TokenTranslator.translate(resultTokens);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IndexOutOfBoundsException e) {
                    System.out.println("Something went wrong in code generation!");
                }
            }
            catch (InvalidSyntaxEcxeption e) {}
            catch (IndexOutOfBoundsException e) {
                System.out.println("Unfinished statement!");
            }
        } catch (InvalidLexemException e) {}
    }
}
