/**
 * Base class of all checked exceptions of the rental system.
 * Subclasses describe the specific problem (inheritance of exceptions).
 */
public class RentalException extends Exception {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that can be shown to the user.
     *
     * @param message description of the problem
     */
    public RentalException(String message) {
        super(message);
    }
}
