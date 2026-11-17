/**
 * Thrown when data entered by the user is invalid (empty name, negative price, wrong duration, wrong date ...).
 */
public class InputTidakValidException extends RentalException {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;
    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public InputTidakValidException(String message) {
        super(message);
    }
}
