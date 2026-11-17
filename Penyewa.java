import java.util.ArrayList;
import java.util.List;

/**
 * Renter account: can see available items, fill the rental list, make a
 * transaction, choose a payment method and see the rental history.
 */
public class Penyewa extends Akun {   // inheritance
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
    private final Pembayaran[] metodePembayaran = {   // polymorphism: one type, many classes
        new TransferBank(), new QRIS(), new Tunai()
    };

    /**
     * Creates a renter account.
     *
     * @param id              account id
     * @param username        login name
     * @param password        login password
     * @param daftarBarang    items of the shop
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
     *
     * @param lamaSewa   rental duration in days
     * @param pembayaran chosen payment method
     * @return the new transaction
     * @throws RentalException if the list is empty, the duration is wrong or an item is not available
     */
    public Transaksi buatTransaksi(int lamaSewa, Pembayaran pembayaran)
            throws RentalException {
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
     *
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
    public void jalankanMenu(InputCLI in) {   // polymorphism: renter menu
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
                case 1: lihatBarang(); break;
                case 2: menuTambah(in); break;
                case 3: menuKeranjang(in); break;
                case 4: menuTransaksi(in); break;
                case 5: menuRiwayat(in); break;
                case 6: menuPengajuan(); break;
                default:
                    System.out.println("  Logged out.");
                    aktif = false;
            }
        }
    }

    /**
     * Screen: add an item to the rental list.
     *
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
     *
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
     *
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
            double biaya = b.hitungBiaya(hari);   // polymorphism
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
     *
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
     *
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
