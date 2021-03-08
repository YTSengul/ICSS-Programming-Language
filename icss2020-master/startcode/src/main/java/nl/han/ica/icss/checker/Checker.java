package nl.han.ica.icss.checker;

import java.util.*;

import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.*;

public class Checker {

    private LinkedList<HashMap<String,ExpressionType>> variableTypes;
    private int currentScope = 0;

    public void check(AST ast) {
        currentScope = 0;
        variableTypes = new LinkedList<>();
        variableTypes.add(new HashMap<>());

        for (ASTNode node : ast.root.getChildren()) {
            ControlUnusedVariables(node);
            controlOperandsOfOperations(node);
            controlNoColorInOperation(node);
        }
    }

    //CH01
    public void ControlUnusedVariables(ASTNode node) {
        if (node instanceof Stylerule) {
            currentScope++;
            variableTypes.add(new HashMap<>());
        }
        if (node instanceof VariableAssignment) {
            variableTypes.get(currentScope).put(((VariableAssignment) node).name.name, getExpressionType(((VariableAssignment) node).expression));
        }
        if (node instanceof VariableReference) {
            int GLOBALSCOPE = 0;
            if (!variableTypes.get(currentScope).containsKey(((VariableReference) node).name) &&
                    !variableTypes.get(GLOBALSCOPE).containsKey(((VariableReference) node).name)) {
                node.setError("Variabelen worden gebruikt die niet gedefinieerd zijn.");
            }
        }

        for (ASTNode nodes : node.getChildren()) {
            ControlUnusedVariables(nodes);
        }
    }

    //CH02
    public void controlOperandsOfOperations(ASTNode node) {
        Set<ExpressionType> literalsInOperation = new HashSet<>();
        if (node instanceof Operation) {
            getVariabelsFromOperation((Operation) node, literalsInOperation);
        }

        if (node instanceof MultiplyOperation) {
            if (!checkIfOnlyScalar(literalsInOperation)) {
                node.setError("Vermenigvuldigen gebeurt niet enkel met scalaire waarden.");
            }
        }

        if (node instanceof AddOperation || node instanceof SubtractOperation) {
            if (!checkIfOnlyOneTypeOfLiteral(literalsInOperation)) {
                node.setError("Operanden van de operaties plus en min zijn niet van gelijke type.");
            }
        }

        for (ASTNode nodes : node.getChildren()) {
            controlOperandsOfOperations(nodes);
        }

    }

    //CH03
    public void controlNoColorInOperation(ASTNode node) {
        if (node instanceof Operation) {
            if (((Operation) node).lhs instanceof ColorLiteral || ((Operation) node).rhs instanceof ColorLiteral) {
                node.setError("Er worden kleuren gebruikt in operaties.");
            }
        }
        for (ASTNode nodes : node.getChildren()) {
            controlNoColorInOperation(nodes);
        }
    }

    private ExpressionType getExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        }
        return null;
    }

    private boolean checkIfOnlyOneTypeOfLiteral(Set<ExpressionType> literals) {
        literals.remove(null);
        return literals.size() <= 1;
    }

    private boolean checkIfOnlyScalar(Set<ExpressionType> literals) {
        literals.remove(null);

        for (ExpressionType type : literals) {
            if (type != ExpressionType.SCALAR) {
                return false;
            }
        }
        return true;
    }

    private void getVariabelsFromOperation(Operation operation, Set<ExpressionType> list) {
        if (operation.lhs instanceof Literal) {
            list.add(getExpressionType(operation.lhs));
        } else if (operation.lhs instanceof VariableReference) {
            addExpressionTypeReferenceToList((VariableReference) operation.lhs, list);
        } else {
            getVariabelsFromOperation((Operation) operation.lhs, list);
        }

        if (operation.rhs instanceof Literal) {
            list.add(getExpressionType(operation.rhs));
        } else if (operation.rhs instanceof VariableReference) {
            addExpressionTypeReferenceToList((VariableReference) operation.rhs, list);
        } else {
            getVariabelsFromOperation((Operation) operation.rhs, list);
        }

        list.remove(null);
    }

    private void addExpressionTypeReferenceToList(VariableReference ref, Set<ExpressionType> list) {
        for (HashMap<String, ExpressionType> maps : variableTypes) {
            if (maps.containsKey(ref.name)) {
                ExpressionType type = maps.get(ref.name);
                list.add(type);
            }
        }
    }

}