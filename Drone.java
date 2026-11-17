/**
 * Drone: daily rate plus a fixed operator fee for every day.
 */
public class Drone extends Barang {
    /** Operator fee per rental day. */
    public static final double BIAYA_OPERATOR = 100_000;
    /**
     * Creates a drone.
     * @param id          unique code
     * @param nama        name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Drone(String id, String nama, double tarifHarian)
            throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }
    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return (getTarifHarian() + BIAYA_OPERATOR) * hari;
    }
    @Override
    public String getKategori() {
        return "Drone";
    }
    @Override
    public String getKeteranganBiaya() {
        return "daily rate + operator fee " + Util.rupiah(BIAYA_OPERATOR) + "/day";
    }
}
