package org.btuk.geography;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinateTest {

    @Test
    public void testDistanceToSamePoint() {
        Coordinate london = new Coordinate(51.5074, -0.1278);
        assertEquals(0.0, london.distanceTo(london), 0.001);
    }

    @Test
    public void testDistanceLondonToParis() {
        Coordinate london = new Coordinate(51.5074, -0.1278);
        Coordinate paris = new Coordinate(48.8566, 2.3522);

        double distance = london.distanceTo(paris);
        long roundedDistance = Math.round(distance);
        // London to Paris is approximately 343 km (343556 m)
        assertEquals(343556, roundedDistance, "Distance should be around 343556m, but was " + roundedDistance);
    }

    @Test
    public void testCoordinateConverterDistance() {
        Coordinate c1 = new Coordinate(51.5074, -0.1278);
        Coordinate c2 = new Coordinate(51.5084, -0.1278);

        double distance = CoordinateConverter.distance(c1, c2);
        // 0.001 degree latitude is approximately 111 metres
        long roundedDistance = Math.round(distance);
        assertEquals(111, roundedDistance, "Distance should be 111m, but was " + roundedDistance);
    }
}
