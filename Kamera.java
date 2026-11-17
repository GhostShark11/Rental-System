/**
 * Camera: daily rate plus 10% insurance.
 */
public class Kamera extends Barang {
    /** Insurance as a fraction of the rental price (10%). */
    public static final double ASURANSI = 0.10;
    /**
     * Creates a camera.
     * @param id          unique code
     * @param nama        name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Kamera(String id, String nama, double tarifHarian)
            throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }
    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return getTarifHarian() * hari * (1 + ASURANSI);
    }
    @Override
    public String getKategori() {
        return "Camera";
    }
    @Override
    public String getKeteranganBiaya() {
        return "daily rate + 10% insurance";
    }
}
