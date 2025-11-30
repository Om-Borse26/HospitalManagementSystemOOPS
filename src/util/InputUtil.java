package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputUtil {
    private final Scanner scanner;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public InputUtil() {
        this.scanner = new Scanner(System.in);
    }

    // Read integer with validation and range checking
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("❌ Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }

    // Read integer without range checking
    public int readInt(String prompt) {
        return readInt(prompt, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    // Read non-empty string
    public String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("❌ Input cannot be empty. Please try again.");
        }
    }

    // Read string (allows empty)
    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    // Read date with validation
    public LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                return LocalDate.parse(input, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Invalid date format. Please use YYYY-MM-DD (e.g., 2024-12-31)");
            }
        }
    }

    // Read date with minimum date validation
    public LocalDate readDateWithMinimum(String prompt, LocalDate minDate) {
        while (true) {
            LocalDate date = readDate(prompt);

            if (date.isBefore(minDate)) {
                System.out.println("❌ Date cannot be before " + minDate.format(DATE_FORMATTER));
                continue;
            }

            return date;
        }
    }

    // Read yes/no confirmation
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("y") || input.equals("yes")) {
                return true;
            } else if (input.equals("n") || input.equals("no")) {
                return false;
            }
            System.out.println("❌ Please enter 'y' or 'n'");
        }
    }

    // Read double with validation
    public double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("❌ Please enter a number between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
    }

    // Read phone number with basic validation
    public String readPhoneNumber(String prompt) {
        while (true) {
            String phone = readNonEmpty(prompt);

            if (ValidationUtil.isValidPhoneNumber(phone)) {
                return phone;
            }
            System.out.println("❌ Invalid phone number. Please enter 10 digits.");
        }
    }

    // Read email with validation
    public String readEmail(String prompt) {
        while (true) {
            String email = readNonEmpty(prompt);

            if (ValidationUtil.isValidEmail(email)) {
                return email;
            }
            System.out.println("❌ Invalid email format. Please try again.");
        }
    }

    // Press enter to continue
    public void pressEnterToContinue() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // Clear console (works on most terminals)
    public void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // If clearing fails, just print newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    // Close scanner (call this on application exit)
    public void close() {
        scanner.close();
    }
}
