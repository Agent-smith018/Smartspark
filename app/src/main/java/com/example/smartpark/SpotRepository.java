package com.example.smartpark;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
public class SpotRepository {
    private static SpotRepository instance;
    private List<Spot> spots;

    private SpotRepository() {
        spots = new ArrayList<>();
        loadInitialSpots();
    }

    public static SpotRepository getInstance() {
        if (instance == null) {
            instance = new SpotRepository();
        }
        return instance;
    }

    private void loadInitialSpots() {
        spots.add(new Spot(
                "1", "Spot A - Rue Saint-Denis", "Rue Saint-Denis, Montreal",
                28, "Standard", "Open", "Paid", 4.0, "17:00", "19:00",
                "Undercover parking"
        ));
        spots.add(new Spot(
                "2", "Spot B - Blvd Saint-Laurent", "Blvd Saint-Laurent, Montreal",
                24, "Standard", "Open", "Paid", 4.0, "17:00", "19:00",
                "Undercover parking"
        ));
    }

    public List<Spot> getAllSpots() {
        return new ArrayList<>(spots);
    }

    public void addSpot(Spot spot) {
        spot.setId(UUID.randomUUID().toString());
        spots.add(spot);
    }

    public void updateSpot(Spot updatedSpot) {
        for (int i = 0; i < spots.size(); i++) {
            if (spots.get(i).getId().equals(updatedSpot.getId())) {
                spots.set(i, updatedSpot);
                break;
            }
        }
    }

    public void deleteSpot(String id) {
        spots.removeIf(spot -> spot.getId().equals(id));
    }

    public Spot getSpotById(String id) {
        for (Spot spot : spots) {
            if (spot.getId().equals(id)) {
                return spot;
            }
        }
        return null;
    }
}
