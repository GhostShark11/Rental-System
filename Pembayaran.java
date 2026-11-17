/**
 * Interface for payment methods (abstraction). Every method of payment
 * processes the money in its own way.
 */
public interface Pembayaran {

    /**
     * Gives the name of the payment method.
     *
     * @return name shown in menus and invoices
     */
    String getNama();

    /**
     * Processes a payment and prints the payment instruction/confirmation.
     *
     * @param jumlah amount to pay in Rupiah
     */
    void prosesPembayaran(double jumlah);
}
