package ecg.wireframe.model;

import ecg.wireframe.model.math.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Uniform cubic B-spline.
 * Given K control points, produces K-3 segments, each approximated by N sub-segments.
 */
public class BSpline {

    // B-spline basis matrix (multiplied by 1/6)
    private static final double[][] MS = {
        {-1.0/6,  3.0/6, -3.0/6, 1.0/6},
        { 3.0/6, -6.0/6,  3.0/6, 0.0  },
        {-3.0/6,  0.0,    3.0/6, 0.0  },
        { 1.0/6,  4.0/6,  1.0/6, 0.0  }
    };

    /**
     * Compute all spline points from the given control points.
     * Result has N*(K-3)+1 points.
     *
     * @param controlPoints at least 4 control points
     * @param N             number of sub-segments per spline section
     * @return list of 2D points along the spline
     */
    public static List<Point2D> computeSplinePoints(List<Point2D> controlPoints, int N) {
        int K = controlPoints.size();
        List<Point2D> result = new ArrayList<>();

        int numSegments = K - 3;
        for (int seg = 0; seg < numSegments; seg++) {
            // 4 control points for this segment: P_{seg}, P_{seg+1}, P_{seg+2}, P_{seg+3}
            // index i in formula means segment index (1-based in TZ, 0-based here)
            // G^i_s = { P_{i-1}, P_i, P_{i+1}, P_{i+2} } where i starts at 1
            double[] gu = {
                controlPoints.get(seg).u,
                controlPoints.get(seg + 1).u,
                controlPoints.get(seg + 2).u,
                controlPoints.get(seg + 3).u
            };
            double[] gv = {
                controlPoints.get(seg).v,
                controlPoints.get(seg + 1).v,
                controlPoints.get(seg + 2).v,
                controlPoints.get(seg + 3).v
            };

            int startJ = (seg == 0) ? 0 : 1; // avoid duplicate junction points
            for (int j = startJ; j <= N; j++) {
                double t = (double) j / N;
                double[] T = {t * t * t, t * t, t, 1.0};
                double u = evaluateScalar(T, MS, gu);
                double v = evaluateScalar(T, MS, gv);
                result.add(new Point2D(u, v));
            }
        }
        return result;
    }

    private static double evaluateScalar(double[] T, double[][] Ms, double[] G) {
        // result = T * Ms * G
        double[] MsG = new double[4];
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                MsG[row] += Ms[row][col] * G[col];
            }
        }
        double res = 0;
        for (int i = 0; i < 4; i++) res += T[i] * MsG[i];
        return res;
    }

    /**
     * Indices of spline points that correspond to ends of segments (= junction of B-spline sections).
     * These are the points where circles are drawn.
     * For K control points and N sub-segments, junction indices are: 0, N, 2N, ..., (K-3)*N
     */
    public static List<Integer> getJunctionIndices(int K, int N) {
        List<Integer> indices = new ArrayList<>();
        int numSegments = K - 3;
        for (int seg = 0; seg <= numSegments; seg++) {
            indices.add(seg * N);
        }
        return indices;
    }
}
