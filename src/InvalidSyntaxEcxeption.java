public class InvalidSyntaxEcxeption extends Exception{
    public InvalidSyntaxEcxeption(String error, int index) {
        String test = "-".repeat(index);
        System.out.println(test + "^");
        System.out.printf("Syntax error at index %d\n", index);
        System.out.println(error);
    }
}
