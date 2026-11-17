/**
 * Status of a rental transaction.
 */
public enum StatusTransaksi {
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
