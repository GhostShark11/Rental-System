/**
 * Thrown when an item cannot be rented or changed because it is already rented, already in the list, or no longer exists.
 */
public class BarangTidakTersediaException extends RentalException {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;
    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public BarangTidakTersediaException(String message) {
        super(message);
    }
}
