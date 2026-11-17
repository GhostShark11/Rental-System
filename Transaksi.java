import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A rental transaction made by a renter. The prices are calculated when the
 * transaction is created, so later price changes do not affect it.
 */
public class Transaksi {
    /** Counter used to create transaction codes. */
    private static int penghitung = 0;

    /** Transaction code, e.g. TRX-001. */
    private final String id;

    /** Renter who made the transaction. */
    private final Penyewa penyewa;

    /** Items in this transaction. */
    private final ArrayList<Barang> barang;

    /** Cost of every item (same order as {@link #barang}). */
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
     *
     * @param penyewa    the renter
     * @param daftar     the items to rent (at least one)
     * @param lamaSewa   rental duration in days (greater than 0)
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
            biayaItem.add(b.hitungBiaya(lamaSewa));   // polymorphism
            denda += b.getDendaPerHari();
        }
        this.dendaPerHari = denda;
        this.tanggalSewa = LocalDate.now();
        this.tanggalJatuhTempo = tanggalSewa.plusDays(lamaSewa);
    }

    /**
     * Gets the value: the transaction code.
     *
     * @return the transaction code
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value: the renter.
     *
     * @return the renter
     */
    public Penyewa getPenyewa() {
        return penyewa;
    }

    /**
     * Gets the value: a copy of the list of items.
     *
     * @return a copy of the list of items
     */
    public List<Barang> getBarang() {
        return new ArrayList<>(barang);
    }

    /**
     * Gives the cost of one item.
     *
     * @param indeks position of the item in the transaction
     * @return its cost for the whole rental duration
     */
    public double getBiayaItem(int indeks) {
        return biayaItem.get(indeks);
    }

    /**
     * Gets the value: the number of rental days.
     *
     * @return the number of rental days
     */
    public int getLamaSewa() {
        return lamaSewa;
    }

    /**
     * Gets the value: the payment method.
     *
     * @return the payment method
     */
    public Pembayaran getPembayaran() {
        return pembayaran;
    }

    /**
     * Gets the value: the start date of the rental.
     *
     * @return the start date of the rental
     */
    public LocalDate getTanggalSewa() {
        return tanggalSewa;
    }

    /**
     * Gets the value: the date the items must be returned.
     *
     * @return the date the items must be returned
     */
    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    /**
     * Gets the value: the real return date, or null if not returned yet.
     *
     * @return the real return date, or null if not returned yet
     */
    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    /**
     * Gets the value: the current status.
     *
     * @return the current status
     */
    public StatusTransaksi getStatus() {
        return status;
    }

    /**
     * Changes the status.
     *
     * @param status the new status
     */
    public void ubahStatus(StatusTransaksi status) {
        this.status = status;
    }

    /**
     * Gets the value: the total rental cost of all items (without late fee).
     *
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
     *
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
     *
     * @return the late fee = late days x late fee per day
     */
    public double hitungDenda() {
        return hitungHariTerlambat() * dendaPerHari;
    }

    /**
     * Starts the rental: sets the start date and the due date.
     *
     * @param mulai the start date
     */
    public void mulaiSewa(LocalDate mulai) {
        this.tanggalSewa = mulai;
        this.tanggalJatuhTempo = mulai.plusDays(lamaSewa);
    }

    /**
     * Records the return date.
     *
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
     *
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
