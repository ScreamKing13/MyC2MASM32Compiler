import java.awt.image.AreaAveragingScaleFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TokenTranslator {
    private static int absIndex;
    private static ArrayList<ArrayList<Token>> statements;
    private static ArrayList<Token> statement;
    private static int index;
    private static boolean firstArray;


    public static void translate(ArrayList<ArrayList<Token>> resultTokens) throws IOException,
            IndexOutOfBoundsException {
        absIndex = 0;
        statements = resultTokens;
        firstArray = false;
        File file = new File("result.asm");
        file.delete();
        file.createNewFile();
        FileWriter writer = new FileWriter(file, true);
        generateStartOfTheFile(writer);
        generateConstants(writer);
        generateVariables(writer);
        generateCode(writer);
        writer.close();
    }

    private static void generateCode(FileWriter writer) throws IOException {
        writer.write(".code\nmain:\n");
        while (absIndex < statements.size()) {
            generatePostfix(writer, statements.get(absIndex));
            writer.write("\n");
            absIndex++;
        }
        writer.write("invoke ExitProcess, 0\n");
        writer.write("end main\n");
        writer.flush();
    }

    private static void generatePostfix(FileWriter writer, ArrayList<Token> tokens) throws IOException {
        index = 0;
        statement = new ArrayList<>(tokens);
        while (!statement.get(index).type.contains("OPERATION_ASSIGN")) {
            if (statement.get(index).type.equals("OPERATION_INC") ||
                    statement.get(index).type.equals("OPERATION_DEC")) {
                doIncDec(writer);
            } else if (statement.get(index).type.split("_")[0].equals("OPERATION")) {
                generateSomeOperation(writer);
            }
            index++;
        }
        if (!statement.get(index-1).name.equals("RES")) {
            index--;
            putValue(writer);
        }
        getResult(statement.get(0), writer);
    }

    private static void generateSomeOperation(FileWriter writer) throws IOException {
        Token op = statement.get(index);
        index--;
        if (statement.get(index).type.equals("INVERT")) {
            index -= 2;
            putValue(writer);
            index += 2;
        } else if(statement.get(index).type.equals("CONTROL_PTR")) {
            index -= 3;
            putValue(writer);
            index += 3;
        } else {
            index--;
            putValue(writer);
            index++;
        }

        putValue(writer);
        index = statement.indexOf(op);
        putOperation(writer);

        statement.remove(index);
        index--;
        statement.remove(index);
        index--;
        statement.remove(index);
        statement.add(index, new Token("RES", "STACK"));
    }

    private static void getResult(Token var1, FileWriter writer) throws IOException {
        Token var = new Token(var1.name, var1.type);
        if (var.type.contains(".")) {
            var.name = "[" + var.name + " + 4 * " + statement.get(1).name + "]";
            var.type = var.type.split("\\.")[0];
        }
        if (var.type.split("_")[0].equals("INT")) {
            writer.write("fistp " + var.name + "\n");
        } else if (var.type.split("_")[0].equals("FLOAT")) {
            writer.write("fstp " + var.name + "\n");
        }
        writer.write("\n");
        writer.flush();
    }

    private static void putValue(FileWriter writer) throws IOException {
        Token token = statement.get(index);
        if (token.type.equals("INVERT")) {
            statement.remove(index);
            index--;
            putVariable(writer);
            writer.write("fchs\n");
        } else if (token.type.equals("CONTROL_PTR")) {
            checkArrayOperand(writer);
        } else {
            putVariable(writer);
        }
        writer.flush();
    }

    private static void putArrayElement(FileWriter writer) throws IOException {
        statement.remove(index);
        index--;
        if (!statement.get(index).name.equals("RES")) {
            putVariable(writer);
        }
        writer.write("fistp _addr_\n");
        writer.write("mov ecx, _addr_\n");
        writer.flush();
        statement.remove(index);
        index--;

        String name =  "[" + statement.get(index).name + " + 4 * ecx]";
        String type = statement.get(index).type;
        statement.remove(index);
        statement.add(index, new Token(name, type));
        putVariable(writer);
    }

    private static void checkArrayOperand(FileWriter writer) throws IOException {
        if (!firstArray) {
            firstArray = true;
            putArrayElement(writer);
        } else {
            getResult(statement.get(index + 1), writer);
            putArrayElement(writer);
            index++;
            putVariable(writer);
        }
    }

    private static void putVariable(FileWriter writer) throws IOException {
        Token token = statement.get(index);
        if (token.type.contains("INT")) {
            writer.write("fild " + token.name + "\n");
        } else if (token.type.contains("FLOAT")) {
            writer.write("fld " + token.name + "\n");
        }
        writer.flush();
    }

    private static void putOperation(FileWriter writer)
            throws IOException {
        String operationType = statement.get(index).type;
        String second = statement.get(index-1).name;
        String first = statement.get(index-2).name;
        firstArray = false;
        switch (operationType) {
            case "OPERATION_ADD":
                writer.write("faddp\n");
                break;
            case "OPERATION_SUBTRACT":
                if (second.equals("RES") && !first.equals("RES")) {
                    writer.write("fsubrp\n");
                } else {
                    writer.write("fsubp\n");
                }
                break;
            case "OPERATION_MULTIPLY":
                writer.write("fmulp\n");
                break;
            case "OPERATION_DIVIDE":
                if (second.equals("RES") && !first.equals("RES")) {
                    writer.write("fdivrp\n");
                } else {
                    writer.write("fdivp\n");
                }
                break;
        }
        writer.flush();
    }

    private static void doIncDec(FileWriter writer) throws IOException {
        String operation = statement.get(index).type;
        statement.remove(index);
        index--;
        putVariable(writer);
        if (operation.equals("OPERATION_INC")) {
            writer.write("fld1\n");
            writer.write("faddp\n");
        } else {
            writer.write("fld1\n");
            writer.write("fsubp\n");
        }
        writer.flush();
        getResult(statement.get(index), writer);
    }

    private static void generateVariables(FileWriter writer) throws IOException {
        writer.write(".data\n");
        while (statements.get(absIndex).get(1).type.equals("TYPE") &&
                statements.get(absIndex).size() == 2) {
            ArrayList<Token> statement = statements.get(absIndex);
            if (statement.get(0).type.contains(".")) {
                writer.write(statement.get(0).name + " DWORD " + statement.get(0).type.split("\\.")[1] + " dup(?)" + "\n");
            } else {
                writer.write(statement.get(0).name + " DWORD " + "?" + "\n");
            }
            writer.flush();
            absIndex++;
        }
        writer.write("_addr_ DWORD ?\n");
        writer.write("_temp_ DWORD ?\n");
    }

    private static void generateConstants(FileWriter writer) throws IOException {
        writer.write(".const\n");
        while (statements.get(absIndex).get(1).type.equals("TYPE") &&
                statements.get(absIndex).size() == 3) {
            ArrayList<Token> statement = statements.get(absIndex);
            writer.write(statement.get(0).name + " DWORD " + statement.get(2).name + "\n");
            writer.flush();
            absIndex++;
        }
    }

    private static void generateStartOfTheFile(FileWriter writer) throws IOException {
        writer.write(".586\n.model flat, stdcall\noption casemap : none\n" +
                "include C:\\masm32\\include\\windows.inc\n" +
                "include C:\\masm32\\include\\kernel32.inc\n" +
                "include C:\\masm32\\include\\user32.inc\n" +
                "include module.inc\n" +
                "include longop.inc\n" +
                "includelib C:\\masm32\\lib\\kernel32.lib\n" +
                "includelib C:\\masm32\\lib\\user32.lib\n");
        writer.flush();
    }
}
