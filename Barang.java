/**
 * Abstract rental item. Every kind of item calculates its rental cost in a
 * different way, so {@link #hitungBiaya(int)} is abstract (abstraction,
 * inheritance and polymorphism).
 */
public abstract class Barang {
    /** Late fee per day as a fraction of the daily rate (50%). */
    public static final double RASIO_DENDA = 0.5;

    /** Unique code of the item, e.g. K01. */
    private final String id;

    /** Name of the item. */
    private String nama;

    /** Normal rental price per day. */
    private double tarifHarian;

    /** True when the item can be rented, false while it is being rented. */
    private boolean tersedia = true;

    /**
     * Creates an item.
     *
     * @param id          unique code
     * @param nama        item name (not empty)
     * @param tarifHarian daily rate (greater than 0)
     * @throws InputTidakValidException if the name or the rate is invalid
     */
    public Barang(String id, String nama, double tarifHarian)
            throws InputTidakValidException {
        this.id = id;
        setNama(nama);
        setTarifHarian(tarifHarian);
    }

    /**
     * Gets the value: the item code.
     *
     * @return the item code
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the value: the item name.
     *
     * @return the item name
     */
    public String getNama() {
        return nama;
    }

    /**
     * Changes the name.
     *
     * @param nama new name (not empty)
     * @throws InputTidakValidException if the name is empty
     */
    public final void setNama(String nama) throws InputTidakValidException {
        if (nama == null || nama.trim().isEmpty()) {
            throw new InputTidakValidException("Item name cannot be empty.");
        }
        this.nama = nama.trim();
    }

    /**
     * Gets the value: the daily rate.
     *
     * @return the daily rate
     */
    public double getTarifHarian() {
        return tarifHarian;
    }

    /**
     * Changes the daily rate.
     *
     * @param tarifHarian new rate (greater than 0)
     * @throws InputTidakValidException if the rate is not positive
     */
    public final void setTarifHarian(double tarifHarian) throws InputTidakValidException {
        if (tarifHarian <= 0) {
            throw new InputTidakValidException("Daily rate must be greater than 0.");
        }
        this.tarifHarian = tarifHarian;
    }

    /**
     * Gets the value: true if the item is available for rent.
     *
     * @return true if the item is available for rent
     */
    public boolean isTersedia() {
        return tersedia;
    }

    /**
     * Sets the availability of the item.
     *
     * @param tersedia true = available, false = being rented
     */
    public void setTersedia(boolean tersedia) {
        this.tersedia = tersedia;
    }

    /**
     * Gets the value: the late fee for one day of delay.
     *
     * @return the late fee for one day of delay
     */
    public double getDendaPerHari() {
        return tarifHarian * RASIO_DENDA;
    }

    /**
     * Calculates the rental cost. Each category has its own formula.
     *
     * @param hari number of rental days (greater than 0)
     * @return total cost in Rupiah
     */
    public abstract double hitungBiaya(int hari);

    /**
     * Gets the value: the category name, e.g. "Camera".
     *
     * @return the category name, e.g. "Camera"
     */
    public abstract String getKategori();

    /**
     * Gets the value: short text that explains how the cost is calculated.
     *
     * @return short text that explains how the cost is calculated
     */
    public abstract String getKeteranganBiaya();

    /**
     * Checks that the number of days is valid.
     *
     * @param hari number of days
     * @throws IllegalArgumentException if the number of days is not positive
     */
    protected void periksaHari(int hari) {
        if (hari <= 0) {
            throw new IllegalArgumentException("Rental days must be greater than 0.");
        }
    }
}
