/**
 * Thrown when a transaction is in a status that does not allow the requested action.
 */
public class TransaksiException extends RentalException {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;
    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public TransaksiException(String message) {
        super(message);
    }
}
