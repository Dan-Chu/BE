package com.likelion.danchu.global.util;

public class DistanceUtils {

  private static final double EARTH_RADIUS_METERS = 6371000.0; // 지구 반지름 (m)

  /** 위도, 경도로 두 지점 간 거리를 계산 (meter 단위). */
  public static double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);

    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_METERS * c;
  }

  /** 위도, 경도로 두 지점 간 거리를 km 단위로 계산. */
  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    return haversineMeters(lat1, lon1, lat2, lon2) / 1000.0;
  }

  /** 소수점 1자리 반올림 */
  public static double round1(double value) {
    return Math.round(value * 10.0) / 10.0;
  }

  /** 소수점 2자리 반올림 */
  public static double round2(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
