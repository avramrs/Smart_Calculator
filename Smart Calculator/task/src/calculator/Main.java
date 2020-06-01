package calculator;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        Calculator calculator = new Calculator();
        while (input.compareTo("/exit") != 0) {
            if (input.compareTo("/help") == 0) {
                System.out.println("The program calculates the sum of numbers");
            } else if (input.matches("/.*")) {
                System.out.println("Unknown command");
            } else if (!input.isEmpty()) {
                try {
                    calculator.compute(input);
                } catch (InvalidAssignmentException e) {
                    System.out.println("Invalid assignment");
                } catch (InvalidIdentifierException e) {
                    System.out.println("Invalid identifier");
                } catch (UnknownVariableException e) {
                    System.out.println("Unknown variable");
                } catch (SyntaxException | InvalidOperationException e) {
                    System.out.println("Invalid expression");
                }
            }
            input = scanner.nextLine().trim();
        }
        System.out.println("Bye!");
    }
}
