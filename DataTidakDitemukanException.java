/**
 * Thrown when an item, transaction or cart entry cannot be found.
 */
public class DataTidakDitemukanException extends RentalException {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     *
     * @param message description of the problem
     */
    public DataTidakDitemukanException(String message) {
        super(message);
    }
}
