package calculator;

import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Calculator {
    final private Pattern variableAssignPattern = Pattern.compile("(\\w+)\\s*=(.*)");
    final private Pattern operationPattern = Pattern.compile("^\\s*[)(]*\\s*((\\+*|-*)\\s*\\d+|[a-zA-Z]+)(\\s*(-+|\\++|\\*|/)\\s*(\\d+|[a-zA-Z]+|[()]+-*\\s*(\\d+|[a-zA-Z]+))\\s*[)(]*)*$");
    final private Pattern tokenPattern = Pattern.compile("\\s*(?<operand>\\d+|[a-zA-Z]+)|\\s*(?<sign>\\++|-+|\\*|/|\\(|\\))");
    final private Pattern numberPattern = Pattern.compile("\\d+");
    final private Pattern variablePattern = Pattern.compile("[a-zA-Z]+");
    final private Pattern operatorPattern = Pattern.compile("(?<add>\\+)|(?<substract>-)|(?<multiply>\\*)|(?<divide>/)|(?<raise>^)");
    private Deque<String> postfixStack;
    final private Map<String, BigInteger> variables;
    final private Map<String, Integer> opPrecedence;

    public Calculator() {
        variables = new HashMap<>();
        opPrecedence = Map.of("+", 1, "-", 1, "*", 2, "/", 2, "^", 3, "(", 4);
    }


    private void parseOperation(String input) {
        Deque<String> operatorStack = new ArrayDeque<>();
        postfixStack = new ArrayDeque<>();
        Matcher tokenMatcher = tokenPattern.matcher(input);
        while (tokenMatcher.find()) {
            if (tokenMatcher.group("operand") != null) {
                postfixStack.offerLast(tokenMatcher.group("operand"));
            } else if (tokenMatcher.group("sign") != null) {
                String sign = tokenMatcher.group("sign");
                if (sign.matches("\\+{2,}")) {
                    sign = "+";
                } else if (sign.matches("-{2,}")) {
                    sign = sign.length() % 2 == 0 ? "+" : "-";
                }
                if (operatorStack.isEmpty()) {
                    operatorStack.offerLast(sign);
                } else if (operatorStack.peekLast().equals("(")) {
                    operatorStack.offerLast(sign);
                } else if (sign.equals("(")) {
                    operatorStack.offerLast(sign);
                } else if (sign.equals(")")) {
                    while (!operatorStack.isEmpty() && !operatorStack.peekLast().equals("(")) {
                        postfixStack.offerLast(operatorStack.pollLast());
                    }
                    if (operatorStack.isEmpty()) {
                        throw new SyntaxException();
                    } else {
                        operatorStack.pollLast();
                    }
                } else if (opPrecedence.get(sign) > opPrecedence.get(operatorStack.peekLast())) {
                    operatorStack.offerLast(sign);
                } else if (opPrecedence.get(sign) <= opPrecedence.get(operatorStack.peekLast())) {
                    while (!operatorStack.isEmpty() && !operatorStack.peekLast().equals("(") && opPrecedence.get(operatorStack.peekLast()) >= opPrecedence.get(sign)) {
                        postfixStack.offerLast(operatorStack.pollLast());
                    }
                    operatorStack.offerLast(sign);
                }
            }
        }
        while (!operatorStack.isEmpty()) {
            if (operatorStack.peekLast().equals("(")) {
                throw new SyntaxException();
            }
            postfixStack.offerLast(operatorStack.pollLast());
        }
    }

    private void parseAssignment(Matcher assignMatcher) {
        String key, value;
        key = assignMatcher.group(1);
        value = assignMatcher.group(2).trim();
        if (!key.matches("[a-zA-Z]+")) {
            throw new InvalidIdentifierException();
        }
        if (value.matches("[a-zA-Z]+")) {
            if (variables.containsKey(value)) {
                variables.put(key, variables.get(value));
            } else {
                throw new UnknownVariableException();
            }
        } else if (value.matches("\\+*\\d+")) {
            value = value.replaceAll("\\+", "");
            variables.put(key, new BigInteger(value));
        } else if (value.matches("-*\\d+")) {
            value = value.replaceAll("--", "");
            variables.put(key, new BigInteger(value));
        } else {
            throw new InvalidAssignmentException();
        }
    }

    private int parse(String input) {
        Matcher assignMatcher = variableAssignPattern.matcher(input);
        Matcher operationMatcher = operationPattern.matcher(input);
        if (assignMatcher.matches()) {
            parseAssignment(assignMatcher);
            return 1;
        } else if (operationMatcher.matches()) {
            parseOperation(input);
            return 0;
        } else {
            throw new InvalidOperationException();
        }
    }

    private BigInteger calculate() {
        Deque<BigInteger> resultStack = new ArrayDeque<>();
        Matcher matcher;
        for (String element : postfixStack) {
            matcher = numberPattern.matcher(element);
            if (matcher.matches()) {
                resultStack.offerLast(new BigInteger(element));
                continue;
            }
            matcher = variablePattern.matcher(element);
            if (matcher.matches()) {
                if (variables.containsKey(element)) {
                    resultStack.offerLast(variables.get(element));
                } else {
                    throw new UnknownVariableException();
                }
                continue;
            }
            matcher = operatorPattern.matcher(element);
            if (matcher.matches()) {
                BigInteger nr1, nr2;
                String sign = matcher.group();
                assert !resultStack.isEmpty();
                nr2 = resultStack.pollLast();
                assert !resultStack.isEmpty();
                nr1 = resultStack.pollLast();
                switch (sign) {
                    case "+":
                        resultStack.offerLast(nr1.add(nr2));
                        break;
                    case "-":
                        resultStack.offerLast(nr1.subtract(nr2));
                        break;
                    case "*":
                        resultStack.offerLast(nr1.multiply(nr2));
                        break;
                    case "/":
                        resultStack.offerLast(nr1.divide(nr2));
                        break;
                }
            }
        }
        assert !resultStack.isEmpty();
        return resultStack.pollLast();
    }

    public void compute(String input) {
        int parseResult = parse(input);
        if (parseResult == 0) {
            System.out.println(calculate());
        }
    }

    public void print() {
        for (String number : postfixStack) {
            System.out.print(number + " ");
        }
        System.out.println();
    }
}

class InvalidIdentifierException extends RuntimeException {
}

class InvalidAssignmentException extends RuntimeException {
}

class UnknownVariableException extends RuntimeException {
}

class InvalidOperationException extends RuntimeException {
}

class SyntaxException extends RuntimeException {
}