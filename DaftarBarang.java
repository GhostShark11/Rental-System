import java.util.ArrayList;
import java.util.List;

/**
 * Collection of all items of the rental shop (uses an {@link ArrayList}).
 * Provides add, edit, delete and search operations.
 */
public class DaftarBarang {
    /** All items. */
    private final ArrayList<Barang> barang = new ArrayList<>();

    /** Creates an empty collection. */
    public DaftarBarang() {
        // starts empty
    }

    /**
     * Adds an item.
     *
     * @param b the new item
     * @throws InputTidakValidException if an item with the same code exists
     */
    public void tambahBarang(Barang b) throws InputTidakValidException {
        if (adaId(b.getId())) {
            throw new InputTidakValidException("Item code " + b.getId() + " already exists.");
        }
        barang.add(b);
    }

    /**
     * Edits the name and/or the daily rate of an item that is not being rented.
     *
     * @param id         code of the item
     * @param namaBaru   new name, or {@code null} to keep the old one
     * @param tarifBaru  new daily rate, or {@code null} to keep the old one
     * @throws RentalException if the item does not exist, is being rented or the data is invalid
     */
    public void editBarang(String id, String namaBaru, Double tarifBaru)
            throws RentalException {
        Barang b = cariBarang(id);
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is being rented and cannot be edited.");
        }
        if (namaBaru != null) {
            b.setNama(namaBaru);
        }
        if (tarifBaru != null) {
            b.setTarifHarian(tarifBaru);
        }
    }

    /**
     * Deletes an item that is not being rented.
     *
     * @param id code of the item
     * @throws RentalException if the item does not exist or is being rented
     */
    public void hapusBarang(String id) throws RentalException {
        Barang b = cariBarang(id);
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is being rented and cannot be deleted.");
        }
        barang.remove(b);
    }

    /**
     * Finds an item by its code (not case sensitive).
     *
     * @param id code of the item
     * @return the item
     * @throws DataTidakDitemukanException if there is no such item
     */
    public Barang cariBarang(String id) throws DataTidakDitemukanException {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                return b;
            }
        }
        throw new DataTidakDitemukanException("Item with code '" + id + "' not found.");
    }

    /**
     * Tells whether an item code is already used.
     *
     * @param id code to test
     * @return true if used
     */
    public boolean adaId(String id) {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether this exact item object is still registered.
     *
     * @param b the item
     * @return true if it is still in the list
     */
    public boolean masihAda(Barang b) {
        return barang.contains(b);
    }

    /**
     * Creates the next free code for a category, e.g. K03.
     *
     * @param prefix first letter of the category (K, L or D)
     * @return a code that is not used yet
     */
    public String buatId(String prefix) {
        int nomor = 1;
        while (adaId(prefix + String.format("%02d", nomor))) {
            nomor++;
        }
        return prefix + String.format("%02d", nomor);
    }

    /**
     * Gets the value: a copy of the list of all items.
     *
     * @return a copy of the list of all items
     */
    public List<Barang> getSemua() {
        return new ArrayList<>(barang);
    }

    /**
     * Gets the value: the items that can be rented now.
     *
     * @return the items that can be rented now
     */
    public List<Barang> getTersedia() {
        List<Barang> hasil = new ArrayList<>();
        for (Barang b : barang) {
            if (b.isTersedia()) {
                hasil.add(b);
            }
        }
        return hasil;
    }

    /**
     * Prints a table of items.
     *
     * @param daftar items to print
     */
    public static void tampilkan(List<Barang> daftar) {
        String format = "  %-5s %-8s %-26s %12s  %-42s %-9s%n";
        System.out.println("  " + Util.garis('-', 108));
        System.out.printf(format, "Code", "Category", "Name", "Rate/day", "Cost rule", "Status");
        System.out.println("  " + Util.garis('-', 108));
        for (Barang b : daftar) {
            System.out.printf(format, b.getId(), b.getKategori(), b.getNama(),
                    Util.rupiah(b.getTarifHarian()), b.getKeteranganBiaya(),
                    b.isTersedia() ? "Available" : "Rented");
        }
        System.out.println("  " + Util.garis('-', 108));
    }
}
