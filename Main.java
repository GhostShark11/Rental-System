import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point of the "Lensa Rental" system: a photography equipment rental
 * (cameras, lenses and drones) with a command line interface.
 */
public class Main {
    /** The account that is logged in (null when nobody is logged in). */
    private Akun akunAktif;

    /** All accounts, found by username (uses a {@link HashMap}). */
    private final Map<String, Akun> daftarAkun = new HashMap<>();

    /** Items of the shop. */
    private final DaftarBarang daftarBarang = new DaftarBarang();

    /** Transactions of the shop. */
    private final DaftarTransaksi daftarTransaksi = new DaftarTransaksi();

    /** Keyboard input reader. */
    private final InputCLI input = new InputCLI(new Scanner(System.in));

    /** Creates the application object. */
    public Main() {
        // fields are initialised above
    }

    /**
     * Program start.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Main aplikasi = new Main();
        aplikasi.siapkanData();
        aplikasi.jalankan();
    }

    /** Creates the starting items and the two accounts (admin and renter). */
    private void siapkanData() {
        try {
            daftarBarang.tambahBarang(new Kamera("K01", "Canon EOS 90D", 250_000));
            daftarBarang.tambahBarang(new Kamera("K02", "Sony A7 III", 350_000));
            daftarBarang.tambahBarang(new Lensa("L01", "Sigma 50mm f/1.4", 100_000));
            daftarBarang.tambahBarang(new Lensa("L02", "Canon 24-70mm f/2.8", 150_000));
            daftarBarang.tambahBarang(new Drone("D01", "DJI Mini 3", 300_000));
            daftarBarang.tambahBarang(new Drone("D02", "DJI Air 2S", 400_000));
        } catch (InputTidakValidException e) {
            throw new IllegalStateException("Invalid starting data: " + e.getMessage(), e);
        }
        Admin admin = new Admin("A01", "admin", "admin123", daftarBarang, daftarTransaksi);
        Penyewa penyewa = new Penyewa("P01", "penyewa", "penyewa123",
                daftarBarang, daftarTransaksi);
        daftarAkun.put(admin.getUsername(), admin);
        daftarAkun.put(penyewa.getUsername(), penyewa);
    }

    /** Main loop: login screen until the user chooses Exit. */
    private void jalankan() {
        System.out.println("==============================================");
        System.out.println("        LENSA RENTAL - Photography Gear");
        System.out.println("==============================================");
        boolean berjalan = true;
        while (berjalan) {
            System.out.println();
            System.out.println("  ===== MAIN MENU =====");
            System.out.println("  1. Login");
            System.out.println("  2. Exit");
            if (input.bacaInt("  Choose (1-2): ", 1, 2) == 1) {
                login();
                if (akunAktif != null) {
                    akunAktif.jalankanMenu(input);   // polymorphism: Admin or Penyewa menu
                    akunAktif = null;
                }
            } else {
                System.out.println("  Thank you. Goodbye!");
                berjalan = false;
            }
        }
    }

    /** Asks for username and password and sets the active account if correct. */
    public void login() {
        System.out.println();
        System.out.println("  ----- LOGIN -----");
        System.out.println("  (demo accounts: admin / admin123  and  penyewa / penyewa123)");
        String username = input.bacaTeks("  Username: ");
        String password = input.bacaTeks("  Password: ");
        try {
            akunAktif = autentikasi(username, password);
            System.out.println("  Welcome, " + akunAktif.getUsername()
                    + " (" + akunAktif.getPeran() + ")!");
        } catch (LoginGagalException e) {
            akunAktif = null;
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Checks the login data.
     *
     * @param username typed username
     * @param password typed password
     * @return the matching account
     * @throws LoginGagalException if the username or password is wrong
     */
    private Akun autentikasi(String username, String password) throws LoginGagalException {
        Akun akun = daftarAkun.get(username);
        if (akun == null || !akun.login(username, password)) {
            throw new LoginGagalException("Wrong username or password.");
        }
        return akun;
    }
}
