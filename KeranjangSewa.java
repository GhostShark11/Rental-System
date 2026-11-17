import java.util.ArrayList;
import java.util.List;

/**
 * The renter's rental list (shopping cart). One or more items can be put here
 * before the transaction is made.
 */
public class KeranjangSewa {
    /** Items in the list. */
    private final ArrayList<Barang> barang = new ArrayList<>();

    /** Creates an empty rental list. */
    public KeranjangSewa() {
        // starts empty
    }

    /**
     * Adds an available item to the list.
     *
     * @param b the item
     * @throws BarangTidakTersediaException if the item is rented or already in the list
     */
    public void tambahBarang(Barang b) throws BarangTidakTersediaException {
        if (!b.isTersedia()) {
            throw new BarangTidakTersediaException(
                    b.getNama() + " is currently rented and cannot be added.");
        }
        if (barang.contains(b)) {
            throw new BarangTidakTersediaException(b.getNama() + " is already in your list.");
        }
        barang.add(b);
    }

    /**
     * Removes an item from the list.
     *
     * @param id code of the item
     * @throws DataTidakDitemukanException if the item is not in the list
     */
    public void hapusBarang(String id) throws DataTidakDitemukanException {
        for (Barang b : barang) {
            if (b.getId().equalsIgnoreCase(id)) {
                barang.remove(b);
                return;
            }
        }
        throw new DataTidakDitemukanException("Item '" + id + "' is not in your list.");
    }

    /** Empties the list. */
    public void kosongkan() {
        barang.clear();
    }

    /**
     * Gets the value: true if there is no item in the list.
     *
     * @return true if there is no item in the list
     */
    public boolean isKosong() {
        return barang.isEmpty();
    }

    /**
     * Gets the value: a copy of the items in the list.
     *
     * @return a copy of the items in the list
     */
    public List<Barang> getBarang() {
        return new ArrayList<>(barang);
    }
}
