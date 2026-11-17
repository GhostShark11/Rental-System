import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Reads and validates keyboard input for the command line interface.
 * Every read method repeats the question until the answer is valid.
 */
public class InputCLI {
    /** Scanner that reads from the keyboard. */
    private final Scanner scanner;

    /**
     * Creates an input reader.
     *
     * @param scanner scanner connected to the keyboard
     */
    public InputCLI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads one trimmed line. If the input has ended the program stops cleanly.
     *
     * @param prompt text shown before reading
     * @return the typed text (may be empty)
     */
    public String bacaTeks(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.out.println("Input closed. Goodbye!");
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    /**
     * Reads a whole number inside a range.
     *
     * @param prompt text shown before reading
     * @param min    lowest accepted value
     * @param max    highest accepted value
     * @return the validated number
     */
    public int bacaInt(String prompt, int min, int max) {
        while (true) {
            try {
                int nilai = Integer.parseInt(bacaTeks(prompt));
                if (nilai >= min && nilai <= max) {
                    return nilai;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  [!] Enter a number between " + min + " and " + max + ".");
        }
    }

    /**
     * Reads a money amount (separators "," and "." are ignored).
     *
     * @param prompt text shown before reading
     * @return the amount, always greater than 0
     */
    public double bacaUang(String prompt) {
        while (true) {
            try {
                double nilai = Double.parseDouble(
                        bacaTeks(prompt).replace(",", "").replace(".", ""));
                if (nilai > 0) {
                    return nilai;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  [!] Enter a valid amount greater than 0.");
        }
    }

    /**
     * Reads a date (dd-MM-yyyy). Pressing Enter returns today.
     *
     * @param prompt text shown before reading
     * @return the entered date, or today's date if the answer is empty
     */
    public LocalDate bacaTanggal(String prompt) {
        while (true) {
            String teks = bacaTeks(prompt);
            if (teks.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(teks, Util.FORMAT_TANGGAL);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Use the format dd-MM-yyyy (example 25-11-2026).");
            }
        }
    }

    /**
     * Asks a yes/no question.
     *
     * @param prompt question text (without the "(y/n)" part)
     * @return true for "y", false for "n"
     */
    public boolean konfirmasi(String prompt) {
        while (true) {
            String jawab = bacaTeks(prompt + " (y/n): ");
            if (jawab.equalsIgnoreCase("y")) {
                return true;
            }
            if (jawab.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("  [!] Answer with y or n.");
        }
    }

    /**
     * Reads an optional money amount. Pressing Enter keeps the old value.
     *
     * @param prompt text shown before reading
     * @return the amount, or {@code null} if the answer is empty
     */
    public Double bacaUangOpsional(String prompt) {
        while (true) {
            String teks = bacaTeks(prompt);
            if (teks.isEmpty()) {
                return null;
            }
            try {
                double nilai = Double.parseDouble(teks.replace(",", "").replace(".", ""));
                if (nilai > 0) {
                    return nilai;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  [!] Enter a valid amount greater than 0 (or press Enter).");
        }
    }
}
