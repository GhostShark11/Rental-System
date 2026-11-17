import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

/**
 * Entry point of the "Lensa Rental" system: a photography equipment rental
 * (cameras, lenses and drones) with a command line interface.
 */
public class Main {
    /** The account that is logged in (null when nobody is logged in). */
    private Akun akunAktif;

    /** All accounts, found by username (uses a HashMap). */
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
        Penyewa penyewa = new Penyewa("P01", "penyewa", "penyewa123", daftarBarang, daftarTransaksi);
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
                    akunAktif.jalankanMenu(input);
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

/**
 * Abstract user account. Admin and renter accounts extend this class and each
 * shows its own menu (abstraction, inheritance and polymorphism).
 */
abstract class Akun {
    /** Unique account id. */
    private final String id;

    /** Login name. */
    private final String username;

    /** Login password. */
    private final String password;

    /**
     * Creates an account.
     * @param id account id
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

/**
 * Admin account: manages the items, accepts or rejects transactions and
 * confirms returns (with late fee calculation).
 */
class Admin extends Akun {
    /** Items of the shop. */
    private final DaftarBarang daftarBarang;

    /** All transactions of the shop. */
    private final DaftarTransaksi daftarTransaksi;

    /**
     * Creates an admin account.
     * @param id account id
     * @param username login name
     * @param password login password
     * @param daftarBarang items of the shop
     * @param daftarTransaksi transactions of the shop
     */
    public Admin(String id, String username, String password,
                DaftarBarang daftarBarang, DaftarTransaksi daftarTransaksi) {
        super(id, username, password);
        this.daftarBarang = daftarBarang;
        this.daftarTransaksi = daftarTransaksi;
    }

    @Override
    public String getPeran() {
        return "Admin";
    }

    /**
     * Accepts a waiting transaction: the items become rented, the payment is
     * processed, the invoice is printed and the transaction goes to the
     * renter's history. Nothing changes if an item is no longer available.
     * @param t the transaction to accept
     * @throws RentalException if the transaction is not waiting or an item is not available
     */
    public void terimaTransaksi(Transaksi t) throws RentalException {
        if (t.getStatus() != StatusTransaksi.MENUNGGU) {
            throw new TransaksiException("Only waiting transactions can be accepted.");
        }
        for (Barang b : t.getBarang()) {
            if (!daftarBarang.masihAda(b)) {
                throw new BarangTidakTersediaException(b.getNama() + " (" + b.getId()
                        + ") no longer exists in the system.");
            }
            if (!b.isTersedia()) {
                throw new BarangTidakTersediaException(b.getNama() + " (" + b.getId()
                        + ") is already rented by another renter.");
            }
        }
        t.mulaiSewa(LocalDate.now());
        for (Barang b : t.getBarang()) {
            b.setTersedia(false);
        }
        t.ubahStatus(StatusTransaksi.DISEWA);
        t.getPembayaran().prosesPembayaran(t.hitungTotal());
        t.getPenyewa().tambahRiwayat(t);
        new Invoice(t).cetakInvoice();
    }

    /**
     * Rejects a waiting transaction.
     * @param t the transaction to reject
     * @throws TransaksiException if the transaction is not waiting
     */
    public void tolakTransaksi(Transaksi t) throws TransaksiException {
        if (t.getStatus() != StatusTransaksi.MENUNGGU) {
            throw new TransaksiException("Only waiting transactions can be rejected.");
        }
        t.ubahStatus(StatusTransaksi.DITOLAK);
    }

    /**
     * Confirms that the items were returned, calculates the late fee (per day
     * of delay), makes the items available again and prints the final invoice.
     * @param t the rented transaction
     * @param kembali the date the items came back
     * @throws RentalException if the transaction is not being rented or the date is invalid
     */
    public void konfirmasiKembali(Transaksi t, LocalDate kembali) throws RentalException {
        if (t.getStatus() != StatusTransaksi.DISEWA) {
            throw new TransaksiException("Only transactions that are being rented can be returned.");
        }
        t.catatPengembalian(kembali);
        for (Barang b : t.getBarang()) {
            b.setTersedia(true);
        }
        t.ubahStatus(StatusTransaksi.SELESAI);
        double denda = t.hitungDenda();
        if (denda > 0) {
            System.out.println("  [!] Late by " + t.hitungHariTerlambat() + " day(s). Late fee: "
                    + Util.rupiah(denda));
            t.getPembayaran().prosesPembayaran(denda);
        } else {
            System.out.println("  Returned on time. No late fee.");
        }
        new Invoice(t).cetakInvoice();
    }

    /**
     * Item management screen: list, add, edit and delete items.
     * @param in keyboard input reader
     */
    public void kelolaBarang(InputCLI in) {
        boolean aktif = true;
        while (aktif) {
            System.out.println();
            System.out.println("  ----- MANAGE ITEMS -----");
            System.out.println("  1. List all items");
            System.out.println("  2. Add item");
            System.out.println("  3. Edit item");
            System.out.println("  4. Delete item");
            System.out.println("  5. Back");
            switch (in.bacaInt("  Choose (1-5): ", 1, 5)) {
                case 1:
                    DaftarBarang.tampilkan(daftarBarang.getSemua());
                    break;
                case 2:
                    tambahBarang(in);
                    break;
                case 3:
                    editBarang(in);
                    break;
                case 4:
                    hapusBarang(in);
                    break;
                default:
                    aktif = false;
            }
        }
    }

    /**
     * Screen: add a new item.
     * @param in keyboard input reader
     */
    private void tambahBarang(InputCLI in) {
        System.out.println("  Category: 1. Camera  2. Lens  3. Drone");
        int kategori = in.bacaInt("  Choose (1-3): ", 1, 3);
        String nama = in.bacaTeks("  Item name: ");
        double tarif = in.bacaUang("  Daily rate (Rp): ");
        try {
            Barang baru;
            if (kategori == 1) {
                baru = new Kamera(daftarBarang.buatId("K"), nama, tarif);
            } else if (kategori == 2) {
                baru = new Lensa(daftarBarang.buatId("L"), nama, tarif);
            } else {
                baru = new Drone(daftarBarang.buatId("D"), nama, tarif);
            }
            daftarBarang.tambahBarang(baru);
            System.out.println("  [OK] " + baru.getKategori() + " added with code " + baru.getId() + ".");
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: edit an item.
     * @param in keyboard input reader
     */
    private void editBarang(InputCLI in) {
        DaftarBarang.tampilkan(daftarBarang.getSemua());
        String id = in.bacaTeks("  Item code to edit (Enter to cancel): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            Barang b = daftarBarang.cariBarang(id);
            String nama = in.bacaTeks("  New name [" + b.getNama() + "] (Enter to keep): ");
            Double tarif = in.bacaUangOpsional("  New daily rate [" + Util.rupiah(b.getTarifHarian())
                    + "] (Enter to keep): ");
            daftarBarang.editBarang(id, nama.isEmpty() ? null : nama, tarif);
            System.out.println("  [OK] Item " + b.getId() + " updated.");
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: delete an item.
     * @param in keyboard input reader
     */
    private void hapusBarang(InputCLI in) {
        DaftarBarang.tampilkan(daftarBarang.getSemua());
        String id = in.bacaTeks("  Item code to delete (Enter to cancel): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            Barang b = daftarBarang.cariBarang(id);
            if (in.konfirmasi("  Delete " + b.getNama())) {
                daftarBarang.hapusBarang(id);
                System.out.println("  [OK] Item deleted.");
            }
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: view the waiting transactions and accept or reject one.
     * @param in keyboard input reader
     */
    private void menuTransaksiMasuk(InputCLI in) {
        List<Transaksi> menunggu = daftarTransaksi.getByStatus(StatusTransaksi.MENUNGGU);
        if (menunggu.isEmpty()) {
            System.out.println("  No waiting transactions.");
            return;
        }
        DaftarTransaksi.tampilkan(menunggu);
        String id = in.bacaTeks("  Transaction ID to process (Enter to cancel): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            Transaksi t = daftarTransaksi.cari(id);
            System.out.println("  1. Accept   2. Reject   3. Cancel");
            int pilihan = in.bacaInt("  Choose (1-3): ", 1, 3);
            if (pilihan == 1) {
                terimaTransaksi(t);
            } else if (pilihan == 2) {
                tolakTransaksi(t);
                System.out.println("  [OK] Transaction " + t.getId() + " rejected.");
            }
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: confirm the return of a rented transaction.
     * @param in keyboard input reader
     */
    private void menuPengembalian(InputCLI in) {
        List<Transaksi> disewa = daftarTransaksi.getByStatus(StatusTransaksi.DISEWA);
        if (disewa.isEmpty()) {
            System.out.println("  No items are being rented.");
            return;
        }
        DaftarTransaksi.tampilkan(disewa);
        String id = in.bacaTeks("  Transaction ID that is returned (Enter to cancel): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            Transaksi t = daftarTransaksi.cari(id);
            LocalDate kembali = in.bacaTanggal("  Return date dd-MM-yyyy (Enter = today): ");
            konfirmasiKembali(t, kembali);
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    @Override
    public void jalankanMenu(InputCLI in) {
        boolean aktif = true;
        while (aktif) {
            System.out.println();
            System.out.println("  ===== ADMIN MENU =====");
            System.out.println("  1. Manage items (add / edit / delete)");
            System.out.println("  2. View all transactions");
            System.out.println("  3. Incoming transactions (accept / reject)");
            System.out.println("  4. Confirm item return");
            System.out.println("  5. Logout");
            switch (in.bacaInt("  Choose (1-5): ", 1, 5)) {
                case 1:
                    kelolaBarang(in);
                    break;
                case 2:
                    if (daftarTransaksi.getSemua().isEmpty()) {
                        System.out.println("  No transactions yet.");
                    } else {
                        DaftarTransaksi.tampilkan(daftarTransaksi.getSemua());
                    }
                    break;
                case 3:
                    menuTransaksiMasuk(in);
                    break;
                case 4:
                    menuPengembalian(in);
                    break;
                default:
                    System.out.println("  Logged out.");
                    aktif = false;
            }
        }
    }
}

/**
 * Abstract rental item. Every kind of item calculates its rental cost in a
 * different way, so hitungBiaya(int) is abstract (abstraction, inheritance and polymorphism).
 */
abstract class Barang {
    /** Late fee per day as a fraction of the daily rate (50%). */
    public static final double RASIO_DENDA = 0.5;

    /** Unique code of the item, e.g. K01. */
    private final String id;

    /** Name of the item. */
    private String nama;

    /** Normal rental price per day. */
    private double tarifHarian;

    /** True when the item can be rented, false while it is being rented. */
    private boolean tersedia = true;

    /**
     * Creates an item.
     * @param id unique code
     * @param nama item name (not empty)
     * @param tarifHarian daily rate (greater than 0)
     * @throws InputTidakValidException if the name or the rate is invalid
     */
    public Barang(String id, String nama, double tarifHarian) throws InputTidakValidException {
        this.id = id;
        setNama(nama);
        setTarifHarian(tarifHarian);
    }

    /**
     * Gets the value: the item code.
     * @return the item code
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value: the item name.
     * @return the item name
     */
    public String getNama() {
        return nama;
    }

    /**
     * Changes the name.
     * @param nama new name (not empty)
     * @throws InputTidakValidException if the name is empty
     */
    public final void setNama(String nama) throws InputTidakValidException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new InputTidakValidException("Item name cannot be empty.");
        }
        this.nama = nama.trim();
    }

    /**
     * Gets the value: the daily rate.
     * @return the daily rate
     */
    public double getTarifHarian() {
        return tarifHarian;
    }

    /**
     * Changes the daily rate.
     * @param tarifHarian new rate (greater than 0)
     * @throws InputTidakValidException if the rate is not positive
     */
    public final void setTarifHarian(double tarifHarian) throws InputTidakValidException {
        if (tarifHarian <= 0) {
            throw new InputTidakValidException("Daily rate must be greater than 0.");
        }
        this.tarifHarian = tarifHarian;
    }

    /**
     * Gets the value: true if the item is available for rent.
     * @return true if the item is available for rent
     */
    public boolean isTersedia() {
        return tersedia;
    }

    /**
     * Sets the availability of the item.
     * @param tersedia true = available, false = being rented
     */
    public void setTersedia(boolean tersedia) {
        this.tersedia = tersedia;
    }

    /**
     * Gets the value: the late fee for one day of delay.
     * @return the late fee for one day of delay
     */
    public double getDendaPerHari() {
        return tarifHarian * RASIO_DENDA;
    }

    /**
     * Calculates the rental cost. Each category has its own formula.
     * @param hari number of rental days (greater than 0)
     * @return total cost in Rupiah
     */
    public abstract double hitungBiaya(int hari);

    /**
     * Gets the value: the category name, e.g. "Camera".
     * @return the category name, e.g. "Camera"
     */
    public abstract String getKategori();

    /**
     * Gets the value: short text that explains how the cost is calculated.
     * @return short text that explains how the cost is calculated
     */
    public abstract String getKeteranganBiaya();

    /**
     * Checks that the number of days is valid.
     * @param hari number of days
     * @throws IllegalArgumentException if the number of days is not positive
     */
    protected void periksaHari(int hari) {
        if (hari <= 0) {
            throw new IllegalArgumentException("Rental days must be greater than 0.");
        }
    }
}

/**
 * Camera: daily rate plus 10% insurance.
 */
class Kamera extends Barang {
    /** Insurance as a fraction of the rental price (10%). */
    public static final double ASURANSI = 0.10;

    /**
     * Creates a camera.
     * @param id unique code
     * @param nama name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Kamera(String id, String nama, double tarifHarian) throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }

    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return getTarifHarian() * hari * (1 + ASURANSI);
    }

    @Override
    public String getKategori() {
        return "Camera";
    }

    @Override
    public String getKeteranganBiaya() {
        return "daily rate + 10% insurance";
    }
}

/**
 * Lens: normal daily rate only.
 */
class Lensa extends Barang {
    /**
     * Creates a lens.
     * @param id unique code
     * @param nama name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Lensa(String id, String nama, double tarifHarian) throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }

    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return getTarifHarian() * hari;
    }

    @Override
    public String getKategori() {
        return "Lens";
    }

    @Override
    public String getKeteranganBiaya() {
        return "normal daily rate";
    }
}

/**
 * Drone: daily rate plus a fixed operator fee for every day.
 */
class Drone extends Barang {
    /** Operator fee per rental day. */
    public static final double BIAYA_OPERATOR = 100_000;

    /**
     * Creates a drone.
     * @param id unique code
     * @param nama name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Drone(String id, String nama, double tarifHarian) throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }

    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return (getTarifHarian() + BIAYA_OPERATOR) * hari;
    }

    @Override
    public String getKategori() {
        return "Drone";
    }

    @Override
    public String getKeteranganBiaya() {
        return "daily rate + operator fee " + Util.rupiah(BIAYA_OPERATOR) + "/day";
    }
}

/**
 * Collection of all items of the rental shop (uses an ArrayList).
 * Provides add, edit, delete and search operations.
 */
class DaftarBarang {
    /** All items. */
    private final ArrayList<Barang> barang = new ArrayList<>();

    /** Creates an empty collection. */
    public DaftarBarang() {
        // starts empty
    }

    /**
     * Adds an item.
     * @param b the new item
     * @throws InputTidakValidException if an item with the same code exists
     */
    public void tambahBarang(Barang b) throws InputTidakValidException {
        if (adaId(b.getId())) {
            throw new InputTidakValidException("Item code " + b.getId() + " already exists.");
        }
        barang.add(b);
    }

    /**
     * Edits the name and/or the daily rate of an item that is not being rented.
     * @param id code of the item
     * @param namaBaru new name, or null to keep the old one
     * @param tarifBaru new daily rate, or null to keep the old one
     * @throws RentalException if the item does not exist, is being rented or the data is invalid
     */
    public void editBarang(String id, String namaBaru, Double tarifBaru) throws RentalException {
        Barang b = cariBarang(id);
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is being rented and cannot be edited.");
        }
        if (namaBaru != null) {
            b.setNama(namaBaru);
        }
        if (tarifBaru != null) {
            b.setTarifHarian(tarifBaru);
        }
    }

    /**
     * Deletes an item that is not being rented.
     * @param id code of the item
     * @throws RentalException if the item does not exist or is being rented
     */
    public void hapusBarang(String id) throws RentalException {
        Barang b = cariBarang(id);
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is being rented and cannot be deleted.");
        }
        barang.remove(b);
    }

    /**
     * Finds an item by its code (not case sensitive).
     * @param id code of the item
     * @return the item
     * @throws DataTidakDitemukanException if there is no such item
     */
    public Barang cariBarang(String id) throws DataTidakDitemukanException {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                return b;
            }
        }
        throw new DataTidakDitemukanException("Item with code '" + id + "' not found.");
    }

    /**
     * Tells whether an item code is already used.
     * @param id code to test
     * @return true if used
     */
    public boolean adaId(String id) {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether this exact item object is still registered.
     * @param b the item
     * @return true if it is still in the list
     */
    public boolean masihAda(Barang b) {
        return barang.contains(b);
    }

    /**
     * Creates the next free code for a category, e.g. K03.
     * @param prefix first letter of the category (K, L or D)
     * @return a code that is not used yet
     */
    public String buatId(String prefix) {
        int nomor = 1;
        while (adaId(prefix + String.format("%02d", nomor))) {
            nomor++;
        }
        return prefix + String.format("%02d", nomor);
    }

    /**
     * Gets the value: a copy of the list of all items.
     * @return a copy of the list of all items
     */
    public List<Barang> getSemua() {
        return new ArrayList<>(barang);
    }

    /**
     * Gets the value: the items that can be rented now.
     * @return the items that can be rented now
     */
    public List<Barang> getTersedia() {
        List<Barang> hasil = new ArrayList<>();
        for (Barang b : barang) {
            if (b.isTersedia()) {
                hasil.add(b);
            }
        }
        return hasil;
    }

    /**
     * Prints a table of items.
     * @param daftar items to print
     */
    public static void tampilkan(List<Barang> daftar) {
        String format = "  %-5s %-8s %-26s %12s  %-42s %-9s%n";
        System.out.println("  " + Util.garis('-', 108));
        System.out.printf(format, "Code", "Category", "Name", "Rate/day", "Cost rule", "Status");
        System.out.println("  " + Util.garis('-', 108));
        for (Barang b : daftar) {
            System.out.printf(format, b.getId(), b.getKategori(), b.getNama(),
                    Util.rupiah(b.getTarifHarian()), b.getKeteranganBiaya(),
                    b.isTersedia() ? "Available" : "Rented");
        }
        System.out.println("  " + Util.garis('-', 108));
    }
}

/**
 * Collection of all transactions of the shop, shared by renters and admin
 * (uses an ArrayList).
 */
class DaftarTransaksi {
    /** All transactions. */
    private final ArrayList<Transaksi> transaksi = new ArrayList<>();

    /** Creates an empty collection. */
    public DaftarTransaksi() {
        // starts empty
    }

    /**
     * Adds a new transaction.
     * @param t the transaction
     */
    public void tambah(Transaksi t) {
        transaksi.add(t);
    }

    /**
     * Finds a transaction by its code (not case sensitive).
     * @param id transaction code
     * @return the transaction
     * @throws DataTidakDitemukanException if there is no such transaction
     */
    public Transaksi cari(String id) throws DataTidakDitemukanException {
        for (Transaksi t : transaksi) {
            if (t.getId().equalsIgnoreCase(id)) {
                return t;
            }
        }
        throw new DataTidakDitemukanException("Transaction '" + id + "' not found.");
    }

    /**
     * Gets the value: a copy of all transactions.
     * @return a copy of all transactions
     */
    public List<Transaksi> getSemua() {
        return new ArrayList<>(transaksi);
    }

    /**
     * Gives the transactions that have a status.
     * @param status wanted status
     * @return matching transactions
     */
    public List<Transaksi> getByStatus(StatusTransaksi status) {
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : transaksi) {
            if (t.getStatus() == status) {
                hasil.add(t);
            }
        }
        return hasil;
    }

    /**
     * Gives the transactions of one renter.
     * @param penyewa the renter
     * @return matching transactions
     */
    public List<Transaksi> getByPenyewa(Penyewa penyewa) {
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : transaksi) {
            if (t.getPenyewa() == penyewa) {
                hasil.add(t);
            }
        }
        return hasil;
    }

    /**
     * Prints a table of transactions.
     * @param daftar transactions to print
     */
    public static void tampilkan(List<Transaksi> daftar) {
        String format = "  %-8s %-10s %-14s %4s %14s %-11s %-11s %-9s%n";
        System.out.println("  " + Util.garis('-', 92));
        System.out.printf(format, "ID", "Renter", "Items", "Days", "Total",
                "Start", "Due", "Status");
        System.out.println("  " + Util.garis('-', 92));
        for (Transaksi t : daftar) {
            System.out.printf(format, t.getId(), t.getPenyewa().getUsername(),
                    t.ringkasBarang(), t.getLamaSewa(), Util.rupiah(t.hitungTotal()),
                    Util.tanggal(t.getTanggalSewa()), Util.tanggal(t.getTanggalJatuhTempo()),
                    t.getStatus().getLabel());
        }
        System.out.println("  " + Util.garis('-', 92));
    }
}

/**
 * Thrown when data entered by the user is invalid (empty name, negative price, wrong duration, wrong date ...).
 */
class InputTidakValidException extends RentalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public InputTidakValidException(String message) {
        super(message);
    }
}

/**
 * Thrown when an item cannot be rented or changed because it is already rented, already in the list, or no longer exists.
 */
class BarangTidakTersediaException extends RentalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public BarangTidakTersediaException(String message) {
        super(message);
    }
}

/**
 * Base class of all checked exceptions of the rental system.
 * Subclasses describe the specific problem (inheritance of exceptions).
 */
class RentalException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message that can be shown to the user.
     * @param message description of the problem
     */
    public RentalException(String message) {
        super(message);
    }
}

/**
 * Thrown when a transaction is in a status that does not allow the requested action.
 */
class TransaksiException extends RentalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public TransaksiException(String message) {
        super(message);
    }
}

/**
 * Thrown when the username or password is wrong.
 */
class LoginGagalException extends RentalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public LoginGagalException(String message) {
        super(message);
    }
}

/**
 * Thrown when an item, transaction or cart entry cannot be found.
 */
class DataTidakDitemukanException extends RentalException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates the exception.
     * @param message description of the problem
     */
    public DataTidakDitemukanException(String message) {
        super(message);
    }
}

/**
 * Status of a rental transaction.
 */
enum StatusTransaksi {
    /** Created by the renter, waiting for the admin. */
    MENUNGGU("Waiting"),
    /** Accepted by the admin; the items are being rented. */
    DISEWA("Rented"),
    /** Items returned and confirmed by the admin. */
    SELESAI("Completed"),
    /** Rejected by the admin. */
    DITOLAK("Rejected");

    /** Text shown to the user. */
    private final String label;

    /**
     * Creates a status.
     * @param label text shown to the user
     */
    StatusTransaksi(String label) {
        this.label = label;
    }

    /**
     * Gets the value: the text shown to the user.
     * @return the text shown to the user
     */
    public String getLabel() {
        return label;
    }
}

/**
 * A rental transaction made by a renter. The prices are calculated when the
 * transaction is created, so later price changes do not affect it.
 */
class Transaksi {
    /** Counter used to create transaction codes. */
    private static int penghitung = 0;

    /** Transaction code, e.g. TRX-001. */
    private final String id;

    /** Renter who made the transaction. */
    private final Penyewa penyewa;

    /** Items in this transaction. */
    private final ArrayList<Barang> barang;

    /** Cost of every item (same order as barang). */
    private final ArrayList<Double> biayaItem = new ArrayList<>();

    /** Late fee per day for all items together. */
    private final double dendaPerHari;

    /** Number of rental days. */
    private final int lamaSewa;

    /** Chosen payment method. */
    private final Pembayaran pembayaran;

    /** Date the rental started. */
    private LocalDate tanggalSewa;

    /** Date the items must be returned. */
    private LocalDate tanggalJatuhTempo;

    /** Date the items were really returned (null while not returned). */
    private LocalDate tanggalKembali;

    /** Current status. */
    private StatusTransaksi status = StatusTransaksi.MENUNGGU;

    /**
     * Creates a transaction with status "waiting".
     * @param penyewa the renter
     * @param daftar the items to rent (at least one)
     * @param lamaSewa rental duration in days (greater than 0)
     * @param pembayaran chosen payment method
     * @throws InputTidakValidException if there is no item or the duration is invalid
     */
    public Transaksi(Penyewa penyewa, List<Barang> daftar, int lamaSewa,
                     Pembayaran pembayaran) throws InputTidakValidException {
        if (daftar.isEmpty()) {
            throw new InputTidakValidException("A transaction needs at least one item.");
        }
        if (lamaSewa <= 0) {
            throw new InputTidakValidException("Rental duration must be at least 1 day.");
        }
        penghitung++;
        this.id = String.format("TRX-%03d", penghitung);
        this.penyewa = penyewa;
        this.barang = new ArrayList<>(daftar);
        this.lamaSewa = lamaSewa;
        this.pembayaran = pembayaran;
        double denda = 0;
        for (Barang b : this.barang) {
            biayaItem.add(b.hitungBiaya(lamaSewa));
            denda += b.getDendaPerHari();
        }
        this.dendaPerHari = denda;
        this.tanggalSewa = LocalDate.now();
        this.tanggalJatuhTempo = tanggalSewa.plusDays(lamaSewa);
    }

    /**
     * Gets the value: the transaction code.
     * @return the transaction code
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value: the renter.
     * @return the renter
     */
    public Penyewa getPenyewa() {
        return penyewa;
    }

    /**
     * Gets the value: a copy of the list of items.
     * @return a copy of the list of items
     */
    public List<Barang> getBarang() {
        return new ArrayList<>(barang);
    }

    /**
     * Gives the cost of one item.
     * @param indeks position of the item in the transaction
     * @return its cost for the whole rental duration
     */
    public double getBiayaItem(int indeks) {
        return biayaItem.get(indeks);
    }

    /**
     * Gets the value: the number of rental days.
     * @return the number of rental days
     */
    public int getLamaSewa() {
        return lamaSewa;
    }

    /**
     * Gets the value: the payment method.
     * @return the payment method
     */
    public Pembayaran getPembayaran() {
        return pembayaran;
    }

    /**
     * Gets the value: the start date of the rental.
     * @return the start date of the rental
     */
    public LocalDate getTanggalSewa() {
        return tanggalSewa;
    }

    /**
     * Gets the value: the date the items must be returned.
     * @return the date the items must be returned
     */
    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    /**
     * Gets the value: the real return date, or null if not returned yet.
     * @return the real return date, or null if not returned yet
     */
    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    /**
     * Gets the value: the current status.
     * @return the current status
     */
    public StatusTransaksi getStatus() {
        return status;
    }

    /**
     * Changes the status.
     * @param status the new status
     */
    public void ubahStatus(StatusTransaksi status) {
        this.status = status;
    }

    /**
     * Gets the value: the total rental cost of all items (without late fee).
     * @return the total rental cost of all items (without late fee)
     */
    public double hitungTotal() {
        double total = 0;
        for (double biaya : biayaItem) {
            total += biaya;
        }
        return total;
    }

    /**
     * Gets the value: the number of days the return was late (0 if on time or not returned).
     * @return the number of days the return was late (0 if on time or not returned)
     */
    public long hitungHariTerlambat() {
        if (tanggalKembali == null) {
            return 0;
        }
        return Math.max(0, ChronoUnit.DAYS.between(tanggalJatuhTempo, tanggalKembali));
    }

    /**
     * Gets the value: the late fee = late days x late fee per day.
     * @return the late fee = late days x late fee per day
     */
    public double hitungDenda() {
        return hitungHariTerlambat() * dendaPerHari;
    }

    /**
     * Starts the rental: sets the start date and the due date.
     * @param mulai the start date
     */
    public void mulaiSewa(LocalDate mulai) {
        this.tanggalSewa = mulai;
        this.tanggalJatuhTempo = mulai.plusDays(lamaSewa);
    }

    /**
     * Records the return date.
     * @param kembali the date the items came back
     * @throws InputTidakValidException if the date is before the rental start
     */
    public void catatPengembalian(LocalDate kembali) throws InputTidakValidException {
        if (kembali.isBefore(tanggalSewa)) {
            throw new InputTidakValidException("Return date cannot be before the rental date ("
                    + Util.tanggal(tanggalSewa) + ").");
        }
        this.tanggalKembali = kembali;
    }

    /**
     * Gets the value: the item codes separated by commas, e.g. "K01,D01".
     * @return the item codes separated by commas, e.g. "K01,D01"
     */
    public String ringkasBarang() {
        StringBuilder sb = new StringBuilder();
        for (Barang b : barang) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(b.getId());
        }
        return sb.toString();
    }
}

/**
 * Interface for payment methods (abstraction). Every method of payment
 * processes the money in its own way.
 */
interface Pembayaran {
    /**
     * Gives the name of the payment method.
     * @return name shown in menus and invoices
     */
    String getNama();

    /**
     * Processes a payment and prints the payment instruction/confirmation.
     * @param jumlah amount to pay in Rupiah
     */
    void prosesPembayaran(double jumlah);
}

/**
 * Payment by bank transfer.
 */
class TransferBank implements Pembayaran {
    /** Account number of the rental shop. */
    private static final String REKENING = "BCA 123-456-7890 a.n. Lensa Rental";

    /** Creates the payment method. */
    public TransferBank() {
        // nothing to initialise
    }

    @Override
    public String getNama() {
        return "Bank Transfer";
    }

    @Override
    public void prosesPembayaran(double jumlah) {
        System.out.println("  [Payment] Bank transfer of " + Util.rupiah(jumlah)
                + " received on " + REKENING + ".");
    }
}

/**
 * Payment with a QRIS code.
 */
class QRIS implements Pembayaran {
    /** Creates the payment method. */
    public QRIS() {
        // nothing to initialise
    }

    @Override
    public String getNama() {
        return "QRIS";
    }

    @Override
    public void prosesPembayaran(double jumlah) {
        System.out.println("  [Payment] QRIS scanned, " + Util.rupiah(jumlah)
                + " paid successfully.");
    }
}

/**
 * Payment in cash at the counter.
 */
class Tunai implements Pembayaran {
    /** Creates the payment method. */
    public Tunai() {
        // nothing to initialise
    }

    @Override
    public String getNama() {
        return "Cash";
    }

    @Override
    public void prosesPembayaran(double jumlah) {
        System.out.println("  [Payment] Cash " + Util.rupiah(jumlah)
                + " received at the counter.");
    }
}

/**
 * The renter's rental list (shopping cart). One or more items can be put here
 * before the transaction is made.
 */
class KeranjangSewa {
    /** Items in the list. */
    private final ArrayList<Barang> barang = new ArrayList<>();

    /** Creates an empty rental list. */
    public KeranjangSewa() {
        // starts empty
    }

    /**
     * Adds an available item to the list.
     * @param b the item
     * @throws BarangTidakTersediaException if the item is rented or already in the list
     */
    public void tambahBarang(Barang b) throws BarangTidakTersediaException {
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is currently rented and cannot be added.");
        }
        if (barang.contains(b)) {
            throw new BarangTidakTersediaException(b.getNama() + " is already in your list.");
        }
        barang.add(b);
    }

    /**
     * Removes an item from the list.
     * @param id code of the item
     * @throws DataTidakDitemukanException if the item is not in the list
     */
    public void hapusBarang(String id) throws DataTidakDitemukanException {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                barang.remove(b);
                return;
            }
        }
        throw new DataTidakDitemukanException("Item '" + id + "' is not in your list.");
    }

    /** Empties the list. */
    public void kosongkan() {
        barang.clear();
    }

    /**
     * Gets the value: true if there is no item in the list.
     * @return true if there is no item in the list
     */
    public boolean isKosong() {
        return barang.isEmpty();
    }

    /**
     * Gets the value: a copy of the items in the list.
     * @return a copy of the items in the list
     */
    public List<Barang> getBarang() {
        return new ArrayList<>(barang);
    }
}

/**
 * Renter account: can see available items, fill the rental list, make a
 * transaction, choose a payment method and see the rental history.
 */
class Penyewa extends Akun {
    /** Minimum rental duration in days. */
    private static final int MIN_HARI = 1;

    /** Maximum rental duration in days. */
    private static final int MAX_HARI = 30;

    /** The renter's rental list. */
    private final KeranjangSewa keranjang = new KeranjangSewa();

    /** Accepted transactions of this renter (history). */
    private final ArrayList<Transaksi> riwayat = new ArrayList<>();

    /** Items of the shop. */
    private final DaftarBarang daftarBarang;

    /** All transactions of the shop. */
    private final DaftarTransaksi daftarTransaksi;

    /** Payment methods the renter can choose from. */
    private final Pembayaran[] metodePembayaran = {
        new TransferBank(), new QRIS(), new Tunai()
    };

    /**
     * Creates a renter account.
     * @param id account id
     * @param username login name
     * @param password login password
     * @param daftarBarang items of the shop
     * @param daftarTransaksi transactions of the shop
     */
    public Penyewa(String id, String username, String password,
                  DaftarBarang daftarBarang, DaftarTransaksi daftarTransaksi) {
        super(id, username, password);
        this.daftarBarang = daftarBarang;
        this.daftarTransaksi = daftarTransaksi;
    }

    @Override
    public String getPeran() {
        return "Renter";
    }

    /** Prints the items that are available for rent. */
    public void lihatBarang() {
        List<Barang> tersedia = daftarBarang.getTersedia();
        if (tersedia.isEmpty()) {
            System.out.println("  No item is available right now.");
        } else {
            DaftarBarang.tampilkan(tersedia);
        }
    }

    /**
     * Makes a transaction from the items in the rental list. The transaction
     * waits for the admin. The rental list is emptied afterwards.
     * @param lamaSewa rental duration in days
     * @param pembayaran chosen payment method
     * @return the new transaction
     * @throws RentalException if the list is empty, the duration is wrong or an item is not available
     */
    public Transaksi buatTransaksi(int lamaSewa, Pembayaran pembayaran) throws RentalException {
        if (keranjang.isKosong()) {
            throw new InputTidakValidException("Your rental list is empty.");
        }
        if (lamaSewa < MIN_HARI || lamaSewa > MAX_HARI) {
            throw new InputTidakValidException("Rental duration must be between "
                    + MIN_HARI + " and " + MAX_HARI + " days.");
        }
        for (Barang b : keranjang.getBarang()) {
            if (!b.isTersedia()) {
                throw new BarangTidakTersediaException(
                        b.getNama() + " has just been rented by someone else.");
            }
        }
        Transaksi t = new Transaksi(this, keranjang.getBarang(), lamaSewa, pembayaran);
        daftarTransaksi.tambah(t);
        keranjang.kosongkan();
        return t;
    }

    /**
     * Adds an accepted transaction to the history.
     * @param t the accepted transaction
     */
    public void tambahRiwayat(Transaksi t) {
        riwayat.add(t);
    }

    /** Prints the history of accepted transactions. */
    public void lihatRiwayat() {
        if (riwayat.isEmpty()) {
            System.out.println("  No rental history yet.");
        } else {
            DaftarTransaksi.tampilkan(riwayat);
        }
    }

    @Override
    public void jalankanMenu(InputCLI in) {
        boolean aktif = true;
        while (aktif) {
            System.out.println();
            System.out.println("  ===== RENTER MENU =====");
            System.out.println("  1. View available items");
            System.out.println("  2. Add item to my rental list");
            System.out.println("  3. View / edit my rental list");
            System.out.println("  4. Make rental transaction");
            System.out.println("  5. Rental history");
            System.out.println("  6. My pending / rejected requests");
            System.out.println("  7. Logout");
            switch (in.bacaInt("  Choose (1-7): ", 1, 7)) {
                case 1:
                    lihatBarang();
                    break;
                case 2:
                    menuTambah(in);
                    break;
                case 3:
                    menuKeranjang(in);
                    break;
                case 4:
                    menuTransaksi(in);
                    break;
                case 5:
                    menuRiwayat(in);
                    break;
                case 6:
                    menuPengajuan();
                    break;
                default:
                    System.out.println("  Logged out.");
                    aktif = false;
            }
        }
    }

    /**
     * Screen: add an item to the rental list.
     * @param in keyboard input reader
     */
    private void menuTambah(InputCLI in) {
        lihatBarang();
        String id = in.bacaTeks("  Item code to add (Enter to cancel): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            Barang b = daftarBarang.cariBarang(id);
            keranjang.tambahBarang(b);
            System.out.println("  [OK] " + b.getNama() + " added to your rental list.");
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: show the rental list and optionally remove an item.
     * @param in keyboard input reader
     */
    private void menuKeranjang(InputCLI in) {
        if (keranjang.isKosong()) {
            System.out.println("  Your rental list is empty.");
            return;
        }
        DaftarBarang.tampilkan(keranjang.getBarang());
        String id = in.bacaTeks("  Item code to remove (Enter to keep all): ");
        if (id.isEmpty()) {
            return;
        }
        try {
            keranjang.hapusBarang(id);
            System.out.println("  [OK] Item removed from your list.");
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Screen: ask duration and payment method, then create the transaction.
     * @param in keyboard input reader
     */
    private void menuTransaksi(InputCLI in) {
        if (keranjang.isKosong()) {
            System.out.println("  [!] Your rental list is empty. Add items first (menu 2).");
            return;
        }
        List<Barang> daftar = keranjang.getBarang();
        DaftarBarang.tampilkan(daftar);
        int hari = in.bacaInt("  Rental duration in days (" + MIN_HARI + "-" + MAX_HARI + "): ",
                MIN_HARI, MAX_HARI);
        Pembayaran pembayaran = pilihPembayaran(in);
        double total = 0;
        System.out.println("  Cost estimate for " + hari + " day(s):");
        for (Barang b : daftar) {
            double biaya = b.hitungBiaya(hari);
            total += biaya;
            System.out.printf("    %-5s %-26s %16s%n", b.getId(), b.getNama(), Util.rupiah(biaya));
        }
        System.out.println("    TOTAL " + Util.rupiah(total) + " | Payment: " + pembayaran.getNama());
        if (!in.konfirmasi("  Submit this transaction")) {
            System.out.println("  Cancelled.");
            return;
        }
        try {
            Transaksi t = buatTransaksi(hari, pembayaran);
            System.out.println("  [OK] Transaction " + t.getId()
                    + " submitted. Waiting for admin approval.");
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    /**
     * Lets the renter choose one of the payment methods.
     * @param in keyboard input reader
     * @return the chosen payment method
     */
    private Pembayaran pilihPembayaran(InputCLI in) {
        System.out.println("  Payment method:");
        for (int i = 0; i < metodePembayaran.length; i++) {
            System.out.println("    " + (i + 1) + ". " + metodePembayaran[i].getNama());
        }
        int pilihan = in.bacaInt("  Choose (1-" + metodePembayaran.length + "): ",
                1, metodePembayaran.length);
        return metodePembayaran[pilihan - 1];
    }

    /**
     * Screen: rental history, with the possibility to print an invoice.
     * @param in keyboard input reader
     */
    private void menuRiwayat(InputCLI in) {
        lihatRiwayat();
        if (riwayat.isEmpty()) {
            return;
        }
        String id = in.bacaTeks("  Transaction ID to view its invoice (Enter to go back): ");
        if (id.isEmpty()) {
            return;
        }
        for (Transaksi t : riwayat) {
            if (t.getId().equalsIgnoreCase(id)) {
                new Invoice(t).cetakInvoice();
                return;
            }
        }
        System.out.println("  [!] Transaction '" + id + "' is not in your history.");
    }

    /** Screen: transactions that are waiting or were rejected. */
    private void menuPengajuan() {
        List<Transaksi> hasil = new ArrayList<>();
        for (Transaksi t : daftarTransaksi.getByPenyewa(this)) {
            if (t.getStatus() == StatusTransaksi.MENUNGGU
                    || t.getStatus() == StatusTransaksi.DITOLAK) {
                hasil.add(t);
            }
        }
        if (hasil.isEmpty()) {
            System.out.println("  No pending or rejected requests.");
        } else {
            DaftarTransaksi.tampilkan(hasil);
        }
    }
}

/**
 * Invoice of a transaction. It keeps the total and the late fee at the moment
 * it is created and prints them.
 */
class Invoice {
    /** The transaction this invoice belongs to. */
    private final Transaksi transaksi;

    /** Rental total (without late fee). */
    private final double total;

    /** Late fee (0 if the items are not returned late). */
    private final double denda;

    /**
     * Creates an invoice for a transaction.
     * @param transaksi the transaction
     */
    public Invoice(Transaksi transaksi) {
        this.transaksi = transaksi;
        this.total = transaksi.hitungTotal();
        this.denda = transaksi.hitungDenda();
    }

    /** Prints the invoice on the screen. */
    public void cetakInvoice() {
        System.out.println();
        System.out.println("  " + Util.garis('=', 62));
        System.out.println("                       RENTAL INVOICE");
        System.out.println("  " + Util.garis('=', 62));
        System.out.println("  Transaction : " + transaksi.getId());
        System.out.println("  Renter      : " + transaksi.getPenyewa().getUsername());
        System.out.println("  Rent date   : " + Util.tanggal(transaksi.getTanggalSewa())
                + "  (" + transaksi.getLamaSewa() + " days)");
        System.out.println("  Due date    : " + Util.tanggal(transaksi.getTanggalJatuhTempo()));
        System.out.println("  Returned on : " + Util.tanggal(transaksi.getTanggalKembali()));
        System.out.println("  Payment     : " + transaksi.getPembayaran().getNama());
        System.out.println("  Status      : " + transaksi.getStatus().getLabel());
        System.out.println("  " + Util.garis('-', 62));
        List<Barang> daftar = transaksi.getBarang();
        for (int i = 0; i < daftar.size(); i++) {
            Barang b = daftar.get(i);
            System.out.printf("  %-5s %-28s %24s%n", b.getId(), b.getNama(),
                    Util.rupiah(transaksi.getBiayaItem(i)));
            System.out.println("        (" + b.getKategori() + ": " + b.getKeteranganBiaya() + ")");
        }
        System.out.println("  " + Util.garis('-', 62));
        System.out.printf("  %-34s %24s%n", "Rental total", Util.rupiah(total));
        System.out.printf("  %-34s %24s%n",
                "Late fee (" + transaksi.hitungHariTerlambat() + " day(s) late)",
                Util.rupiah(denda));
        System.out.printf("  %-34s %24s%n", "TOTAL TO PAY", Util.rupiah(total + denda));
        System.out.println("  " + Util.garis('=', 62));
    }
}

/**
 * Reads and validates keyboard input for the command line interface.
 * Every read method repeats the question until the answer is valid.
 */
class InputCLI {
    /** Scanner that reads from the keyboard. */
    private final Scanner scanner;

    /**
     * Creates an input reader.
     * @param scanner scanner connected to the keyboard
     */
    public InputCLI(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads one trimmed line. If the input has ended the program stops cleanly.
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
     * @param prompt text shown before reading
     * @param min lowest accepted value
     * @param max highest accepted value
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
     * @param prompt text shown before reading
     * @return the amount, or null if the answer is empty
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

/**
 * Small helper methods for formatting output (money, dates, lines).
 */
final class Util {
    /** Date format used everywhere in the program: dd-MM-yyyy. */
    public static final DateTimeFormatter FORMAT_TANGGAL =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    /** Private constructor: this utility class has no objects. */
    private Util() {
        // utility class: no objects
    }

    /**
     * Formats an amount of money, e.g. Rp 1,250,000.
     * @param jumlah amount in Rupiah
     * @return formatted text
     */
    public static String rupiah(double jumlah) {
        return "Rp " + String.format(Locale.US, "%,.0f", jumlah);
    }

    /**
     * Formats a date as dd-MM-yyyy.
     * @param tanggal the date (may be null)
     * @return formatted date, or "-" when the date is null
     */
    public static String tanggal(LocalDate tanggal) {
        return tanggal == null ? "-" : tanggal.format(FORMAT_TANGGAL);
    }

    /**
     * Builds a line made of the same character.
     * @param karakter character to repeat
     * @param panjang number of characters
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
