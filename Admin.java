import java.time.LocalDate;
import java.util.List;

/**
 * Admin account: manages the items, accepts or rejects transactions and
 * confirms returns (with late fee calculation).
 */

public class Admin extends Akun {   // inheritance
    /** Items of the shop. */
    private final DaftarBarang daftarBarang;

    /** All transactions of the shop. */
    private final DaftarTransaksi daftarTransaksi;

    /**
     * Creates an admin account.
     * @param id              account id
     * @param username        login name
     * @param password        login password
     * @param daftarBarang    items of the shop
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
        t.getPembayaran().prosesPembayaran(t.hitungTotal());   // polymorphism
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
     * @param t       the rented transaction
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
                case 1: DaftarBarang.tampilkan(daftarBarang.getSemua()); break;
                case 2: tambahBarang(in); break;
                case 3: editBarang(in); break;
                case 4: hapusBarang(in); break;
                default: aktif = false;
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
            LocalDate kembali = in.bacaTanggal(
                    "  Return date dd-MM-yyyy (Enter = today): ");
            konfirmasiKembali(t, kembali);
        } catch (RentalException e) {
            System.out.println("  [!] " + e.getMessage());
        }
    }

    @Override
    public void jalankanMenu(InputCLI in) {   // polymorphism: admin menu
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
                case 1: kelolaBarang(in); break;
                case 2:
                    if (daftarTransaksi.getSemua().isEmpty()) {
                        System.out.println("  No transactions yet.");
                    } else {
                        DaftarTransaksi.tampilkan(daftarTransaksi.getSemua());
                    }
                    break;
                case 3: menuTransaksiMasuk(in); break;
                case 4: menuPengembalian(in); break;
                default:
                    System.out.println("  Logged out.");
                    aktif = false;
            }
        }
    }
}
