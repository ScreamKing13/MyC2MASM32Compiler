import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SyntaxAnalyzerC {
    private static final ArrayList<String> beginTypeLexems = new ArrayList<>(Arrays.asList("CONDITIONAL_IF", "CYCLE_WHL", "CYCLE_FOR", "TYPE", "NAME"));
    private static int absIndex;
    private static ArrayList<Token> nameTable;
    private static ArrayList<Token> statementPostfix;
    private static HashMap<String, Integer> typeValue;
    private static ArrayList<ArrayList<Token>> DeclarationSequence;
    private static ArrayList<ArrayList<Token>> StatementSequence;
    private static ArrayList<ArrayList<Token>> ConstSequence;
    private static int varIndex;

    public static ArrayList<ArrayList<Token>> checkSyntax(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        typeValue = new HashMap<>();
        nameTable = new ArrayList<>();
        typeValue.put("POINTER", -1);
        typeValue.put("CHAR", 0);
        typeValue.put("UNSIGNED CHAR", 0);
        typeValue.put("SIGNED CHAR", 0);
        typeValue.put("SHORT", 0);
        typeValue.put("UNSIGNED SHORT", 0);
        typeValue.put("INT", 1);
        typeValue.put("UNSIGNED", 2);
        typeValue.put("UNSIGNED INT", 2);
        typeValue.put("LONG", 3);
        typeValue.put("UNSIGNED LONG", 4);
        typeValue.put("LONG LONG", 5);
        typeValue.put("UNSIGNED LONG LONG", 6);
        typeValue.put("FLOAT", 7);
        typeValue.put("DOUBLE", 8);
        typeValue.put("LONG DOUBLE", 9);
        absIndex = 0;
        varIndex = 0;
        DeclarationSequence = new ArrayList<>();
        StatementSequence = new ArrayList<>();
        ConstSequence = new ArrayList<>();
        startParse(lexems);
        DeclarationSequence.addAll(StatementSequence);
        ConstSequence.addAll(DeclarationSequence);
        return ConstSequence;
    }

    private static void startParse(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        nameTable.add(new Token("scope", "CATGUARD"));
        if (!checkStart(lexems.get(0))) {
            throw new InvalidSyntaxEcxeption(
                    "Not appropriate start of the statement!",
                    lexems.get(absIndex).getIndex()
            );
        } else {
            checkStatementSequence(lexems);
        }
        if (nameTable.size() > 0) {
            nameTable.subList(0, nameTable.size()).clear();
        }
    }

    private static void checkStatementSequence(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        while (absIndex < lexems.size()){
            checkStatement(lexems);
        }
    }

    private static void checkStatement(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        switch (lexems.get(absIndex).type) {
            case "TYPE":
                StringBuilder builder = new StringBuilder();
                while (checkTypeEq(lexems.get(absIndex).type, "TYPE")) {
                    builder.append(lexems.get(absIndex).name);
                    builder.append(" ");
                    absIndex ++;
                }
                if (!typeValue.containsKey(builder.toString().strip().toUpperCase())) {
                    throw new InvalidSyntaxEcxeption(
                            "Invalid type!",
                            lexems.get(absIndex).getIndex()
                    );
                }
                checkDeclStatement(lexems, builder.toString().strip().toUpperCase(), 1);
                break;
            default:
                checkNameStatement(lexems);
        }
    }

    private static void checkDeclStatement(ArrayList<Token> lexems, String type, int varCounter) throws
            InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        boolean pointer = false;
        boolean array = false;
        if (checkTypeEq(lexems.get(absIndex).type, "OPERATION_MULTIPLY")) {
            absIndex++;
            pointer = true;
        }
        if (!checkTypeEq(lexems.get(absIndex).type, "NAME")) {
            throw new InvalidSyntaxEcxeption(
                    "NAME is required!",
                    lexems.get(absIndex).getIndex()
            );
        }
        Token variable = lexems.get(absIndex);
        absIndex++;
        if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_LEFT")) {
            absIndex++;
            if (!checkTypeEq(lexems.get(absIndex).type, "INT_NUM")) {
                throw new InvalidSyntaxEcxeption(
                        "Index must be of integer type and a number!",
                        lexems.get(absIndex).getIndex()
                );
            }
            variable.type += "." + lexems.get(absIndex).name;
            absIndex++;
            if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_RIGHT")) {
                absIndex++;
                array = true;
            } else {
                throw new InvalidSyntaxEcxeption(
                        "']' is required",
                        lexems.get(absIndex).getIndex()
                );
            }
        }
        if (isDeclared(variable.name)) {
            throw new InvalidSyntaxEcxeption(
                    "This variable " + variable.name.replace("_", "") + " was already defined in this scope!",
                    lexems.get(absIndex).getIndex()
            );
        } else {
            Token var, typeToken;
            if (pointer) {
                var = new Token(variable.name, "POINTER_" + type);
            } else if (array) {
                String arrayType = variable.type.replace("NAME", type);
                var = new Token(variable.name, arrayType);
            }
            else {
                var = new Token(variable.name, type);
            }
            typeToken = new Token(var.type, "TYPE");
            nameTable.add(var);
            DeclarationSequence.add(new ArrayList<>(Arrays.asList(var, typeToken)));
        }
        if (checkTypeEq(lexems.get(absIndex).type, "DELIMITER_COMMA")) {
            absIndex++;
            checkDeclStatement(lexems, type, ++varCounter);
        } else if (checkTypeEq(lexems.get(absIndex).type, "DELIMITER_SEMICOLON")) {
            absIndex++;
        } else {
            throw new InvalidSyntaxEcxeption(
                    "Invalid operation!",
                    lexems.get(absIndex).getIndex()
            );
        }
    }

    private static boolean isDeclared(String variableName) {
        for (int i = nameTable.size() - 1; i >= 0; i--) {
            Token object = nameTable.get(i);
            if (object.name.equals(variableName)) {
                return true;
            } else if(object.type.equals("CATGUARD")) {
                return false;
            }
        }
        return false;
    }

    private static Token findVariableByName(String name) {
        for (int i = nameTable.size() - 1; i >= 0; i--) {
            if (nameTable.get(i).name.equals(name)) {
                return nameTable.get(i);
            }
        }
        return new Token("none", "none");
    }

    private static void checkNameStatement(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        boolean pointer = false;
        boolean array = false;
        boolean array_pointer = false;
        statementPostfix = new ArrayList<>();

        if (checkTypeEq(lexems.get(absIndex).type, "OPERATION_MULTIPLY")) {
            pointer = true;
            absIndex++;
        }
        if (!checkTypeEq(lexems.get(absIndex).type, "NAME")){
            throw new InvalidSyntaxEcxeption(
                    "Inappropriate start of the statement!",
                    lexems.get(absIndex).getIndex());
        }

        Token variable = findVariableByName(lexems.get(absIndex).name);
        if (variable.name.equals("none")) {
            throw new InvalidSyntaxEcxeption(
                    "variable used without declaration",
                    lexems.get(absIndex).getIndex());
        }
        absIndex++;
        statementPostfix.add(variable);

        if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_LEFT")) {
            if (!variable.type.contains(".")) {
                throw new InvalidSyntaxEcxeption(
                        "Can't use indexing in non-array variable!",
                        lexems.get(absIndex).getIndex()
                );
            }
            absIndex ++;
            String indexType  = expr(lexems);
            if (typeValue.get(indexType) > 1) {
                throw new InvalidSyntaxEcxeption(
                        "Index must be of integer type!",
                        lexems.get(absIndex).getIndex()
                );
            }
            if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_RIGHT")) {
                absIndex++;
                array = true;
            } else {
                throw new InvalidSyntaxEcxeption(
                        "']' is required",
                        lexems.get(absIndex).getIndex()
                );
            }
        } else if (variable.type.contains(".") && !checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_LEFT")) {
            array_pointer = true;
        }

        if (!lexems.get(absIndex).type.split("_")[1].equals("ASSIGN")) {
            throw new InvalidSyntaxEcxeption(
                    "Assignment is required!",
                    lexems.get(absIndex).getIndex());
        }
        Token assign = lexems.get(absIndex);
        int assignIndex = assign.getIndex();
        absIndex++;

        int variableTypeValue;
        String variableTypeName;
        if (pointer) {
            variableTypeName = variable.type.split("_")[1];
            statementPostfix.add(new Token("ADDR", "CONTROL_ADDR"));
        } else if (array || array_pointer) {
            variableTypeName = variable.type.split("\\.")[0];
            statementPostfix.add(new Token("PTR", "CONTROL_PTR"));
        } else {
            variableTypeName = variable.type.split("_")[0];
        }
        variableTypeValue = typeValue.get(variableTypeName);

        if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_CLY_LEFT")) {
            if (array_pointer) {
                checkArrayAssignment(lexems, variable);
            } else {
                throw new InvalidSyntaxEcxeption(
                        "Cannot assign array of elements to non-array variable!",
                        assignIndex
                );
            }
        } else {
            String resultType = expr(lexems);
            if (checkTypeEq(lexems.get(absIndex).type, "CONDITIONAL_QMARK")) {
                resultType = checkConditionalShort(lexems);
            }
            if (variableTypeValue < typeValue.get(resultType)) {
                throw new InvalidSyntaxEcxeption(
                        String.format("Cannot assign %s to %s!", resultType, variableTypeName),
                        assignIndex
                );
            }
            statementPostfix.add(assign);
            StatementSequence.add(new ArrayList<>(statementPostfix));
        }
        checkSemicolon(lexems);
    }

    private static void checkArrayAssignment(ArrayList<Token> lexems, Token variable) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        int arrayLength = Integer.parseInt(variable.type.split("\\.")[1]);
        String type = variable.type.split("\\.")[0];
        int pointer = 0;
        do {
            absIndex++;
            if (checkTypeEq(lexems.get(absIndex).type, "INT_NUM") ||
                    checkTypeEq(lexems.get(absIndex).type, "FLOAT_NUM") ||
                    checkTypeEq(lexems.get(absIndex).type, "DOUBLE_NUM")) {
                if (checkTypeEq(lexems.get(absIndex).type.split("_")[0], type)) {
                    statementPostfix.clear();
                    statementPostfix.add(variable);
                    statementPostfix.add(new Token(String.valueOf(pointer), "INT"));
                    statementPostfix.add(new Token("PTR", "CONTROL_PTR"));
                    Token constant = declareConstant(lexems.get(absIndex));
                    statementPostfix.add(constant);
                    statementPostfix.add(new Token("=", "OPERATION_ASSIGN"));
                    absIndex++;
                    pointer++;
                    StatementSequence.add(new ArrayList<>(statementPostfix));
                } else {
                    throw new InvalidSyntaxEcxeption(
                            String.format("Cannot assign %s to %s!", lexems.get(absIndex).type, type),
                            lexems.get(absIndex).getIndex()
                    );
                }
            } else {
                throw new InvalidSyntaxEcxeption(
                        "Number expected!",
                        lexems.get(absIndex).getIndex()
                );
            }
        } while (checkTypeEq(lexems.get(absIndex).type, "DELIMITER_COMMA"));
        if (pointer != arrayLength) {
            throw new InvalidSyntaxEcxeption(
                    "Size of array and number of operands do not match!",
                    lexems.get(absIndex).getIndex()
            );
        }
        if (!checkTypeEq(lexems.get(absIndex).type, "BRACKET_CLY_RIGHT")) {
            throw new InvalidSyntaxEcxeption(
                    "'}' is needed!",
                    lexems.get(absIndex).getIndex()
            );
        }
        absIndex++;
    }

    private static Token declareConstant(Token token) {
        ArrayList<Token> constantDeclaration = new ArrayList<>();
        Token constant = new Token("var" + varIndex, token.type);
        constantDeclaration.add(constant);
        constantDeclaration.add(new Token(constant.type.split("_")[0], "TYPE"));
        constantDeclaration.add(token);
        ConstSequence.add(constantDeclaration);
        varIndex++;
        return constant;
    }

    private static String expr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException{
        String type = andExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "LOGICAL_OR")) {
            absIndex++;
            andExpr(lexems);
            statementPostfix.add(new Token("||", "LOGICAL_OR"));
            type = "INT";
        }
        return type;
    }

    private static String andExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException{
        String type = equalityExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "LOGICAL_AND")) {
            absIndex++;
            equalityExpr(lexems);
            statementPostfix.add(new Token("&&", "LOGICAL_AND"));
            type = "INT";
        }
        return type;
    }

    private static String equalityExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException{
        String type = relationalExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "COMPARE_EQ") ||
                checkTypeEq(lexems.get(absIndex).type, "COMPARE_NEQ")) {
            Token token = lexems.get(absIndex);
            absIndex++;
            relationalExpr(lexems);
            statementPostfix.add(token);
            type = "INT";
        }
        return type;
    }

    private static String relationalExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException{
        String type = additionExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "COMPARE_LESS_OR_EQ") ||
                checkTypeEq(lexems.get(absIndex).type, "COMPARE_LESS") ||
                checkTypeEq(lexems.get(absIndex).type, "COMPARE_MORE_OR_EQ") ||
                checkTypeEq(lexems.get(absIndex).type, "COMPARE_MORE")) {
            Token token = lexems.get(absIndex);
            absIndex++;
            additionExpr(lexems);
            statementPostfix.add(token);
            type = "INT";
        }
        return type;
    }

    private static String additionExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption {
        String type1 = multiplicationExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "OPERATION_ADD") ||
                checkTypeEq(lexems.get(absIndex).type, "OPERATION_SUBTRACT")) {
            Token token = lexems.get(absIndex);
            absIndex++;
            String type2 = multiplicationExpr(lexems);
            statementPostfix.add(token);
            if (typeValue.get(type1) < typeValue.get(type2)) {
                type1 = type2;
            }
        }
        return type1;
    }

    private static String multiplicationExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption {
        String type1 = primaryExpr(lexems);
        while (checkTypeEq(lexems.get(absIndex).type, "OPERATION_MULTIPLY") ||
                checkTypeEq(lexems.get(absIndex).type, "OPERATION_DIVIDE") ||
                checkTypeEq(lexems.get(absIndex).type, "OPERATION_MOD")) {
            Token token = lexems.get(absIndex);
            absIndex++;
            String type2 = primaryExpr(lexems);
            statementPostfix.add(token);
            if (typeValue.get(type1) < typeValue.get(type2)) {
                type1 = type2;
            }
        }
        return type1;
    }

    private static String primaryExpr(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException{
        String type1;
        if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_RD_LEFT")) {
            absIndex++;
            type1 = expr(lexems);
            if (!checkTypeEq(lexems.get(absIndex).type, "BRACKET_RD_RIGHT")) {
                throw new InvalidSyntaxEcxeption(
                        "')' not found!",
                        lexems.get(absIndex).getIndex()
                );
            } else {
                absIndex++;
            }
        } else if (checkTypeEq(lexems.get(absIndex).type, "OPERATION_INC") ||
                checkTypeEq(lexems.get(absIndex).type, "OPERATION_DEC")) {
            Token op = lexems.get(absIndex);
            absIndex ++;
            if (!checkTypeEq(lexems.get(absIndex).type, "NAME")) {
                throw new InvalidSyntaxEcxeption(
                        "NAME is required!",
                        lexems.get(absIndex).getIndex()
                );
            } else {
                Token variable = findVariableByName(lexems.get(absIndex).name);
                if (!variable.name.equals("none")) {
                    if (checkAssigned(variable.name)) {
                        type1 = variable.type;
                        statementPostfix.add(variable);
                        statementPostfix.add(op);
                        absIndex++;
                    } else {
                        throw new InvalidSyntaxEcxeption(
                                String.format("Variable %s is not assigned!", variable.name),
                                lexems.get(absIndex).getIndex()
                        );
                    }
                } else {
                    throw new InvalidSyntaxEcxeption(
                            String.format("Variable %s is not defined!", lexems.get(absIndex).name),
                            lexems.get(absIndex).getIndex()
                    );
                }
            }
        } else if (checkTypeEq(lexems.get(absIndex).type, "LOGICAL_NOT")) {
            type1 = "INT";
            absIndex ++;
            if (!checkTypeEq(lexems.get(absIndex).type, "NAME")) {
                throw new InvalidSyntaxEcxeption(
                        "NAME is required!",
                        lexems.get(absIndex).getIndex()
                );
            } else {
                Token variable = findVariableByName(lexems.get(absIndex).name);
                if (!variable.name.equals("none")) {
                    if (checkAssigned(variable.name)) {
                        statementPostfix.add(variable);
                        statementPostfix.add(new Token("0", "INT_NUM"));
                        statementPostfix.add(new Token("==", "COMPARE_EQ"));
                        absIndex++;
                    } else {
                        throw new InvalidSyntaxEcxeption(
                                String.format("Variable %s is not assigned!", variable.name),
                                lexems.get(absIndex).getIndex()
                        );
                    }
                } else {
                    throw new InvalidSyntaxEcxeption(
                            String.format("Variable %s is not defined!", lexems.get(absIndex).name),
                            lexems.get(absIndex).getIndex()
                    );
                }
            }
        } else if (checkTypeEq(lexems.get(absIndex).type, "OPERATION_SUBTRACT")) {
            absIndex++;
            if (!checkTypeEq(lexems.get(absIndex).type, "NAME")) {
                throw new InvalidSyntaxEcxeption(
                        "NAME is required!",
                        lexems.get(absIndex).getIndex()
                );
            } else {
                Token variable = findVariableByName(lexems.get(absIndex).name);
                if (!variable.name.equals("none")) {
                    if (checkAssigned(variable.name)) {
                        type1 = variable.type;
                        statementPostfix.add(variable);
                        statementPostfix.add(new Token("INV", "INVERT"));
                        absIndex++;
                    } else {
                        throw new InvalidSyntaxEcxeption(
                                String.format("Variable %s is not assigned!", variable.name),
                                lexems.get(absIndex).getIndex()
                        );
                    }
                } else {
                    throw new InvalidSyntaxEcxeption(
                            String.format("Variable %s is not defined!", lexems.get(absIndex).name),
                            lexems.get(absIndex).getIndex()
                    );
                }
            }
        } else if (checkTypeEq(lexems.get(absIndex).type, "NAME")) {
            Token variable = findVariableByName(lexems.get(absIndex).name);
            if (!variable.name.equals("none")) {
                if (checkAssigned(variable.name)) {
                    type1 = variable.type;
                    statementPostfix.add(variable);
                    absIndex++;
                    if (checkTypeEq(lexems.get(absIndex).type, "OPERATION_INC") ||
                            checkTypeEq(lexems.get(absIndex).type, "OPERATION_DEC")) {
                        Token op = lexems.get(absIndex);
                        statementPostfix.add(op);
                        absIndex++;
                    } else if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_LEFT")) {
                        if (!variable.type.contains(".")) {
                            throw new InvalidSyntaxEcxeption(
                                    "Can't use indexing in non-array variable!",
                                    lexems.get(absIndex).getIndex()
                            );
                        }
                        absIndex++;
                        String indexType  = expr(lexems);
                        if (typeValue.get(indexType) > 1) {
                            throw new InvalidSyntaxEcxeption(
                                    "Index must be of integer type!",
                                    lexems.get(absIndex).getIndex()
                            );
                        }
                        if (checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_RIGHT")) {
                            absIndex++;
                            statementPostfix.add(new Token("PTR", "CONTROL_PTR"));
                        } else {
                            throw new InvalidSyntaxEcxeption(
                                    "']' is required",
                                    lexems.get(absIndex).getIndex()
                            );
                        }
                    } else if (variable.type.contains(".") && !checkTypeEq(lexems.get(absIndex).type, "BRACKET_SQ_LEFT")) {
                        throw new InvalidSyntaxEcxeption(
                                "Can't use array pointer as a variable!",
                                lexems.get(absIndex).getIndex()
                        );
                    }
                } else {
                    throw new InvalidSyntaxEcxeption(
                            String.format("Variable %s is not assigned!", variable.name),
                            lexems.get(absIndex).getIndex()
                    );
                }

            } else {
                throw new InvalidSyntaxEcxeption(
                        String.format("Variable %s is not defined!", lexems.get(absIndex).name),
                        lexems.get(absIndex).getIndex()
                );
            }
        } else if (checkTypeEq(lexems.get(absIndex).type, "INT_NUM") ||
                checkTypeEq(lexems.get(absIndex).type, "FLOAT_NUM") ||
                checkTypeEq(lexems.get(absIndex).type, "DOUBLE_NUM")) {
            Token number = declareConstant(lexems.get(absIndex));
            type1 = number.type;
            statementPostfix.add(number);
            absIndex++;
        } else {
            throw new InvalidSyntaxEcxeption(
                    String.format("Variable or number expected at %s!", lexems.get(absIndex).name),
                    lexems.get(absIndex).getIndex()
            );
        }
        if (type1.contains(".")) {
            return type1.split("\\.")[0];
        } else if (type1.contains("_")) {
            return type1.split("_")[0];
        } else {
            return type1;
        }
    }

    private static boolean checkAssigned(String name) {
        for (int i = StatementSequence.size() - 1; i >= 0; i--) {
            if (StatementSequence.get(i).get(0).name.equals(name)) {
                return true;
            }
        }
        return false;
    }


    private static String checkConditionalShort(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        absIndex++;
        statementPostfix.add(new Token("?", "CONDITIONAL_QMARK"));
        String type1 = expr(lexems);
        if (!checkTypeEq(lexems.get(absIndex).type, "CONDITIONAL_COLON")) {
            throw new InvalidSyntaxEcxeption(
                    "':' is required!",
                    lexems.get(absIndex).getIndex()
            );
        } else {
            absIndex++;
            statementPostfix.add(new Token(":", "CONDITIONAL_COLON"));
        }
        String type2 = expr(lexems);
        statementPostfix.add(new Token("MOVE_CONDITIONAL", "MOVE_CONDITIONAL"));
        if (typeValue.get(type1) < typeValue.get(type2)) {
            return type2;
        } else {
            return type1;
        }
    }

    private static boolean checkTypeEq(String typeLexem, String type) {
        return typeLexem.equals(type);
    }


    private static boolean checkStart(Token token) {
        return beginTypeLexems.contains(token.type);
    }

    private static void checkSemicolon(ArrayList<Token> lexems) throws InvalidSyntaxEcxeption,
            IndexOutOfBoundsException {
        if (checkTypeEq(lexems.get(absIndex).type, "DELIMITER_SEMICOLON")) {
            absIndex ++;
        } else {
            throw new InvalidSyntaxEcxeption(
                    "';' is required!",
                    lexems.get(absIndex).getIndex()
            );
        }
    }
}
