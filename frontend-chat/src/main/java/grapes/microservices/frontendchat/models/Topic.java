package grapes.microservices.frontendchat.models;

// topicId could be the multicast address or a unique ID from the API
public record Topic(int id, String name, String lastMessage) {
    @Override
    public String toString() {
        return name; // For simple display in ListView
    }

    /**
     * Generates a multicast address based on the ID using modulo => 253 +1 -> 4
     * @return The generated multicast address.
     */
    public String getMulticastGroup() {
        int multicast_base_address = 224 * 256 * 256 * 256; // 224.0.0.3
        int multicast_group = this.id % 253 + 3; // so it will never be .0 .1 .2
        int full_address = multicast_base_address + multicast_group;
        return convertIntToIP(full_address);
    }

    private String convertIntToIP(int intValue) {
        int octet1 = (intValue >> 24) & 0xFF; // Extract the first (highest) 8 bits
        int octet2 = (intValue >> 16) & 0xFF; // Extract the second 8 bits
        int octet3 = (intValue >> 8) & 0xFF;  // Extract the third 8 bits
        int octet4 = intValue & 0xFF;         // Extract the last (lowest) 8 bits

        return octet1 + "." + octet2 + "." + octet3 + "." + octet4;
    }
}