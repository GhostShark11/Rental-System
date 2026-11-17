import java.util.List;

/**
 * Invoice of a transaction. It keeps the total and the late fee at the moment
 * it is created and prints them.
 */
public class Invoice {
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
