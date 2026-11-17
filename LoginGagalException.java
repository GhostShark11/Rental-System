/**
 * Thrown when the username or password is wrong.
 */
public class LoginGagalException extends RentalException {
    /** Version number used for serialization. */
    private static final long serialVersionUID = 1L;
    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public LoginGagalException(String message) {
        super(message);
    }
}
