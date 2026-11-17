/**
 * Payment with a QRIS code.
 */
public class QRIS implements Pembayaran {

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
