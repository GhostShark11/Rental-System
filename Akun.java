/**
 * Abstract user account. Admin and renter accounts extend this class and each
 * shows its own menu (abstraction, inheritance and polymorphism).
 */

public abstract class Akun {
    /** Unique account id. */
    private final String id;

    /** Login name. */
    private final String username;

    /** Login password. */
    private final String password;

    /**
     * Creates an account.
     * @param id       account id
     * @param username login name
     * @param password login password
     */
    
    public Akun(String id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    /**
     * Gets the value: the account id.
     * @return the account id
     */
    
    public String getId() {
        return id;
    }

    /**
     * Gets the value: the login name.
     * @return the login name
     */
    
    public String getUsername() {
        return username;
    }
    
    /**
     * Checks a username and password.
     * @param usernameInput typed username
     * @param passwordInput typed password
     * @return true if both are correct
     */
    
    public boolean login(String usernameInput, String passwordInput) {
        return username.equals(usernameInput) && password.equals(passwordInput);
    }
    /**
     * Gets the value: the role name ("Admin" or "Renter").
     * @return the role name ("Admin" or "Renter")
     */
    public abstract String getPeran();
    /**
     * Shows the menu of this kind of account and handles the choices until logout.
     * @param in keyboard input reader
     */
    public abstract void jalankanMenu(InputCLI in);
}
