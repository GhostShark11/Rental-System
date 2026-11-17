import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Small helper methods for formatting output (money, dates, lines).
 */
public final class Util {
    /** Date format used everywhere in the program: dd-MM-yyyy. */
    public static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Private constructor: this utility class has no objects. */
    private Util() {
        // utility class: no objects
    }
    /**
     * Formats an amount of money, e.g. {@code Rp 1,250,000}.
     * @param jumlah amount in Rupiah
     * @return formatted text
     */
    
    public static String rupiah(double jumlah) {
        return "Rp " + String.format(Locale.US, "%,.0f", jumlah);
    }
    /**
     * Formats a date as dd-MM-yyyy.
     * @param tanggal the date (may be {@code null})
     * @return formatted date, or "-" when the date is null
     */
    
    public static String tanggal(LocalDate tanggal) {
        return tanggal == null ? "-" : tanggal.format(FORMAT_TANGGAL);
    }
    /**
     * Builds a line made of the same character.
     * @param karakter character to repeat
     * @param panjang  number of characters
     * @return the line
     */
    
    public static String garis(char karakter, int panjang) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < panjang; i++) {
            sb.append(karakter);
        }
        return sb.toString();
    }
}
