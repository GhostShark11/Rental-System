/**
 * Lens: normal daily rate only.
 */
public class Lensa extends Barang {
    /**
     * Creates a lens.
     * @param id          unique code
     * @param nama        name
     * @param tarifHarian daily rate
     * @throws InputTidakValidException if the data is invalid
     */
    public Lensa(String id, String nama, double tarifHarian)
            throws InputTidakValidException {
        super(id, nama, tarifHarian);
    }
    @Override
    public double hitungBiaya(int hari) {
        periksaHari(hari);
        return getTarifHarian() * hari;
    }
    @Override
    public String getKategori() {
        return "Lens";
    }
    @Override
    public String getKeteranganBiaya() {
        return "normal daily rate";
    }
}
