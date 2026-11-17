/**
 * Payment in cash at the counter.
 */
public class Tunai implements Pembayaran {

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
