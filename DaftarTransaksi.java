import java.util.ArrayList;
import java.util.List;

/**
 * Collection of all transactions of the shop, shared by renters and admin
 * (uses an {@link ArrayList}).
 */
public class DaftarTransaksi {
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
