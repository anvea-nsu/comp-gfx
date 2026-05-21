package ecg.wireframe.model;

import ecg.wireframe.model.math.Point2D;
import ecg.wireframe.model.math.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the 3-D wireframe of a revolution body.
 *
 * Axis of revolution: Z (corresponds to the U-axis in 2D).
 *   R_x = F_v * cos(phi_j)
 *   R_y = F_v * sin(phi_j)
 *   R_z = F_u
 */
public class RevolutionBody {

    /**
     * Generate all 3-D segments (generatrices + circles).
     *
     * @param splinePoints   2-D generatrix points
     * @param K              number of control points (needed to find junction indices)
     * @param N              sub-segments per B-spline section
     * @param M              number of generatrices
     * @param M1             segments between adjacent generatrices on circles
     * @return combined list of 3-D segments
     */
    public static List<Segment3D> generate(List<Point2D> splinePoints,
                                           int K, int N, int M, int M1) {
        List<Segment3D> segments = new ArrayList<>();

        // Build revolution surface points: surface[j][i] = 3D point
        int numSplinePoints = splinePoints.size();
        Point3D[][] surface = new Point3D[M][numSplinePoints];

        for (int j = 0; j < M; j++) {
            double phi = Math.toRadians(j * 360.0 / M);
            double cosPhi = Math.cos(phi);
            double sinPhi = Math.sin(phi);
            for (int i = 0; i < numSplinePoints; i++) {
                double u = splinePoints.get(i).u;
                double v = splinePoints.get(i).v;
                surface[j][i] = new Point3D(v * cosPhi, v * sinPhi, u);
            }
        }

        // 1. Generatrix segments (M polylines along the surface)
        for (int j = 0; j < M; j++) {
            for (int i = 0; i < numSplinePoints - 1; i++) {
                segments.add(new Segment3D(surface[j][i], surface[j][i + 1]));
            }
        }

        // 2. Circle segments at junction points (K-2 circles)
        List<Integer> junctions = BSpline.getJunctionIndices(K, N);
        for (int idx : junctions) {
            if (idx >= numSplinePoints) continue;
            // Build circle through surface[0..M-1][idx]
            for (int j = 0; j < M; j++) {
                int jNext = (j + 1) % M;
                if (M1 == 1) {
                    segments.add(new Segment3D(surface[j][idx], surface[jNext][idx]));
                } else {
                    // Interpolate M1 sub-segments along the circle arc
                    double phiA = Math.toRadians(j * 360.0 / M);
                    // When jNext wraps to 0, use 360° so arc goes forward, not backward
                    double phiB = Math.toRadians((jNext == 0 ? M : jNext) * 360.0 / M);
                    double v = splinePoints.get(idx).v;
                    double u = splinePoints.get(idx).u;

                    Point3D prev = surface[j][idx];
                    for (int k = 1; k <= M1; k++) {
                        double phi = phiA + (phiB - phiA) * k / M1;
                        Point3D cur = new Point3D(v * Math.cos(phi), v * Math.sin(phi), u);
                        segments.add(new Segment3D(prev, cur));
                        prev = cur;
                    }
                }
            }
        }

        return segments;
    }

    /**
     * Compute bounding box and return [minX, minY, minZ, maxX, maxY, maxZ].
     */
    public static double[] boundingBox(List<Segment3D> segments) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Segment3D s : segments) {
            for (Point3D p : new Point3D[]{s.a, s.b}) {
                if (p.x < minX) minX = p.x; if (p.x > maxX) maxX = p.x;
                if (p.y < minY) minY = p.y; if (p.y > maxY) maxY = p.y;
                if (p.z < minZ) minZ = p.z; if (p.z > maxZ) maxZ = p.z;
            }
        }
        return new double[]{minX, minY, minZ, maxX, maxY, maxZ};
    }
}
