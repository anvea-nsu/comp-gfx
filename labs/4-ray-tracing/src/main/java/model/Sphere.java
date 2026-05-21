package model;

import java.util.List;

public record Sphere(List<Point3D> center, double radius, OpticalChar opticalChar) {
}
