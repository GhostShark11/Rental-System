/**
 * Payment by bank transfer.
 */
public class TransferBank implements Pembayaran {
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
