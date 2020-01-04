package hotelapp;

/**
 * Stores tourist attraction details namely id, name, rating and address
 */
public class TouristAttraction {
    // FILL IN CODE: add instance variables to store
    // name, rating, address, id
    private String id;
    private String name;
    private double rating;
    private String address;

    /** Constructor for TouristAttraction
     *
     * @param id id of the attraction
     * @param name name of the attraction
     * @param rating overall rating of the attraction
     * @param address address of the attraction
     */
    public TouristAttraction(String id, String name, double rating, String address) {
        // FILL IN CODE
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.address = address;
    }

    // FILL IN CODE: add getters as needed
    
    /**
     * Returns attraction id
     * @return id of the attraction
     */
    public String getId() {
        return id;
    }
    
    /**
     * Returns atrraction name
     * @return name of the attraction
     */
    public String getName() {
        return name;
    }
    
    /**
     * returns rating for the attraction
     * @return attraction's rating
     */
    public double getRating() {
        return rating;
    }
    
    /**
     * Returns address of the attraction
     * @return attraction's address
     */
    public String getAddress() {
        return address;
    }
    
    /**
     * toString() method
     *
     * @return a String representing this TouristAttraction
     */
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.name);
        sb.append(System.lineSeparator());
        sb.append("Rating: " + this.rating);
        sb.append(System.lineSeparator());
        sb.append(this.address);
        sb.append(System.lineSeparator());
        return sb.toString();
        //return this.name;
    }
}
