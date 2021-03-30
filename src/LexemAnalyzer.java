import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class LexemAnalyzer {
    private Pattern check1 = Pattern.compile("[а-яА-Я@#%\"\'~`\\\\{]");
    private List<Character> characterList = new ArrayList<>(Arrays.asList(' ', ',', ':', '(', ')', ';', '?','!', '/','[',']', '{', '}', '%','<', '>', '=', '+', '-', '*', '|', '&'));
    private List<Character> numbers = new ArrayList<>(Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9'));
    private final Token[] keywords = {
            new Token("else", "CONDITIONAL_EL"), new Token("if", "CONDITIONAL_IF"),
            new Token("switch", "CONDITIONAL_SW"), new Token("case", "CONDITIONAL_CS"),
            new Token("while", "CYCLE_WHL"), new Token("for", "CYCLE_FOR"),
            new Token("int", "TYPE"), new Token("float", "TYPE"),
            new Token("double", "TYPE"), new Token("bool", "TYPE"),
            new Token("long", "TYPE"), new Token("char", "TYPE"),
            new Token("short", "TYPE"), new Token("unsigned", "TYPE"),
            new Token("signed", "TYPE"),
            new Token("break", "CONTROL_BK"), new Token("default", "CONDITIONAL_DF")
    };
    private int i = 0;
    private String expression = "";
    private char[] charArray;
    private ArrayList<Token> tokenList = new ArrayList<>();


    private boolean checkAndAddKeyword(String lexem, int index) {
        for (Token token : keywords) {
            if (token.name.equals(lexem)){
                tokenList.add(new Token(token.name, token.type, index));
                return true;
            }
        }
        return false;
    }

    private void addVarOrKeyword() throws InvalidLexemException {
        char ch;
        int index = i;
        StringBuilder stringBuilder = new StringBuilder();
        while (i < expression.length() && !characterList.contains(ch = charArray[i])){
            stringBuilder.append(ch);
            i++;
        }
        String string = stringBuilder.toString();
        if (!checkAndAddKeyword(string, index)){
            if (!check1.matcher(string).find()) tokenList.add(new Token("_"+string, "NAME", index));
            else throw new InvalidLexemException("Variable has inappropriate symbols!", i);
        }
    }

    private void Number(String sign) throws InvalidLexemException{
        int index = i;
        String number = checkDigitSeq(sign);
        int digitsAfterPoint;
        String type;
        if (number.contains(".")) {
            digitsAfterPoint = number.split("\\.")[1].length();
            if (digitsAfterPoint > 0 & digitsAfterPoint <= 6) {
                type = "FLOAT_NUM";
            } else {
                type = "DOUBLE_NUM";
            }
        } else {
            type = "INT_NUM";
        }
        tokenList.add(new Token(number, type, index));
    }

    private String checkDigitSeq(String sign) throws InvalidLexemException{
        StringBuilder number = new StringBuilder(sign);
        char ch = ' ';
        int index = i;

        while (i < expression.length()){
            if (numbers.contains(ch = charArray[i])) {
                number.append(ch);
                i++;
            } else if (ch == '.') {
                number.append(ch);
                i++;
                while (i < expression.length() && numbers.contains(ch = charArray[i])) {
                    number.append(ch);
                    i++;
                }
                break;
            } else if (Character.isLetter(ch)) {
                number.append(ch);
                throw new InvalidLexemException(number.toString(), index);
            }
            else {
                break;
            }
        }
        if (i < expression.length() && !characterList.contains(ch)) {
            throw new InvalidLexemException(number.toString(), index);
        }
        return number.toString();
    }

    public ArrayList<Token> getTokenList(String txt) throws InvalidLexemException {
        expression = txt;
        charArray = expression.toCharArray();
        while (i < expression.length()) {
            char ch = charArray[i];
            if (Character.isLetter(ch)){
                addVarOrKeyword();}
            else if (Character.isDigit(ch)){
                Number("");
            }

            else
                switch (ch) {
                    case ' ':
                        i++;
                        break;
                    case ';':
                        tokenList.add(new Token(";", "DELIMITER_SEMICOLON", i));
                        i++;
                        break;
                    case '?':
                        tokenList.add(new Token("?", "CONDITIONAL_QMARK", i));
                        i++;
                        break;
                    case ':':
                        tokenList.add(new Token(":", "CONDITIONAL_COLON", i));
                        i++;
                        break;
                    case '!':
                        if (charArray[i+1] == '='){
                            tokenList.add(new Token("!=", "COMPARE_NEQ", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("!", "LOGICAL_NOT", i));
                            i++;
                        }
                        break;

                    case ',':
                        tokenList.add(new Token(",", "DELIMITER_COMMA", i));
                        i++;
                        break;
                    case '=':
                        if (charArray[i+1] == '='){
                            tokenList.add(new Token("==", "COMPARE_EQ", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("=", "OPERATION_ASSIGN", i));
                            i++;
                        }
                        break;
                    case '%':
                        if (charArray[i+1] == '='){
                            tokenList.add(new Token("%=", "OPERATION_ASSIGN_MOD", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("%", "OPERATION_MOD", i));
                            i++;
                        }
                        break;
                    case '&':
                        if (charArray[i+1] == '&'){
                            tokenList.add(new Token("&&", "LOGICAL_AND", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("&", "BINARY_AND", i));
                            i++;
                        }
                        break;
                    case '|':
                        if (charArray[i+1] == '|'){
                            tokenList.add(new Token("||", "LOGICAL_OR", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("|", "BINARY_OR", i));
                            i++;
                        }
                        break;
                    case '<':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token("<=", "COMPARE_LESS_OR_EQ", i));
                            i+=2;
                        } else {
                            tokenList.add(new Token("<", "COMPARE_LESS", i));
                            i++;
                        }
                        break;
                    case '>':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token(">=", "COMPARE_MORE_OR_EQ", i));
                            i+=2;
                        } else {
                            tokenList.add(new Token(">", "COMPARE_MORE", i));
                            i++;
                        }
                        break;
                    case '{':
                        tokenList.add(new Token("{", "BRACKET_CLY_LEFT", i));
                        i++;
                        break;
                    case '}':
                        tokenList.add(new Token("}", "BRACKET_CLY_RIGHT", i));
                        i++;
                        break;
                    case '[':
                        tokenList.add(new Token("[", "BRACKET_SQ_LEFT", i));
                        i++;
                        break;
                    case ']':
                        tokenList.add(new Token("]", "BRACKET_SQ_RIGHT", i));
                        i++;
                        break;
                    case '(':
                        tokenList.add(new Token("(", "BRACKET_RD_LEFT", i));
                        i++;
                        break;
                    case ')':
                        tokenList.add(new Token(")", "BRACKET_RD_RIGHT", i));
                        i++;
                        break;
                    case '+':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token("+=", "OPERATION_ASSIGN_ADD", i));
                            i+=2;
                        } else if (charArray[i+1] == '+'){
                            tokenList.add(new Token("++", "OPERATION_INC", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("+", "OPERATION_ADD", i));
                            i++;
                        }
                        break;
                    case '-':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token("-=", "OPERATION_ASSIGN_SUB", i));
                            i+=2;
                        } else if (charArray[i+1] == '-'){
                            tokenList.add(new Token("--", "OPERATION_DEC", i));
                            i+=2;
                        } else if (numbers.contains(charArray[i+1])) {
                            i++;
                            Number("-");
                        }
                        else {
                            tokenList.add(new Token("-", "OPERATION_SUBTRACT", i));
                            i++;
                        }
                        break;
                    case '*':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token("*=", "OPERATION_ASSIGN_MULT", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("*", "OPERATION_MULTIPLY", i));
                            i++;
                        }
                        break;
                    case '/':
                        if (charArray[i+1] == '=') {
                            tokenList.add(new Token("/=", "OPERATION_ASSIGN_DIV", i));
                            i+=2;
                        }
                        else {
                            tokenList.add(new Token("/", "OPERATION_DIVIDE", i));
                            i++;
                        }
                        break;
                    default:
                        throw new InvalidLexemException(ch);
                }
        }
        return tokenList;
    }
}