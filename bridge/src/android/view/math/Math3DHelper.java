/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.view.math;

public class Math3DHelper {

    private Math3DHelper() { }

    /**
     * Calculates [p1x+t*(p2x-p1x)=dx*t2+px,p1y+t*(p2y-p1y)=dy*t2+py],[t,t2];
     *
     * @param d - dimension in which the poly is represented (supports 2 or 3D)
     * @return float[]{t2, t, p1} or float[]{Float.NaN}
     */
    public static float[] rayIntersectPoly(float[] poly, int polyLength, float px, float py,
            float dx, float dy, int d) {
        int p1 = polyLength - 1;
        for (int p2 = 0; p2 < polyLength; p2++) {
            float p1x = poly[p1 * d + 0];
            float p1y = poly[p1 * d + 1];
            float p2x = poly[p2 * d + 0];
            float p2y = poly[p2 * d + 1];
            float div = (dx * (p1y - p2y) + dy * (p2x - p1x));
            if (div != 0) {
                float t = (dx * (p1y - py) + dy * (px - p1x)) / div;
                if (t >= 0 && t <= 1) {
                    float t2 = (p1x * (py - p2y)
                            + p2x * (p1y - py)
                            + px * (p2y - p1y))
                            / div;
                    if (t2 > 0) {
                        return new float[]{t2, t, p1};
                    }
                }
            }
            p1 = p2;
        }
        return new float[]{Float.NaN};
    }

    public static void centroid3d(float[] poly, int len, float[] ret) {
        int n = len - 1;
        double area = 0;
        double cx = 0;
        double cy = 0;
        double cz = 0;
        for (int i = 1; i < n; i++) {
            int k = i + 1;
            float a0 = poly[i * 3 + 0] - poly[0 * 3 + 0];
            float a1 = poly[i * 3 + 1] - poly[0 * 3 + 1];
            float a2 = poly[i * 3 + 2] - poly[0 * 3 + 2];
            float b0 = poly[k * 3 + 0] - poly[0 * 3 + 0];
            float b1 = poly[k * 3 + 1] - poly[0 * 3 + 1];
            float b2 = poly[k * 3 + 2] - poly[0 * 3 + 2];
            float c0 = a1 * b2 - b1 * a2;
            float c1 = a2 * b0 - b2 * a0;
            float c2 = a0 * b1 - b0 * a1;
            double areaOfTriangle = Math.sqrt(c0 * c0 + c1 * c1 + c2 * c2);
            area += areaOfTriangle;
            cx += areaOfTriangle * (poly[i * 3 + 0] + poly[k * 3 + 0] + poly[0 * 3 + 0]);
            cy += areaOfTriangle * (poly[i * 3 + 1] + poly[k * 3 + 1] + poly[0 * 3 + 1]);
            cz += areaOfTriangle * (poly[i * 3 + 2] + poly[k * 3 + 2] + poly[0 * 3 + 2]);
        }
        ret[0] = (float) (cx / (3 * area));
        ret[1] = (float) (cy / (3 * area));
        ret[2] = (float) (cz / (3 * area));
    }

    public final static int min(int x1, int x2, int x3) {
        return (x1 > x2) ? ((x2 > x3) ? x3 : x2) : ((x1 > x3) ? x3 : x1);
    }

    public final static int max(int x1, int x2, int x3) {
        return (x1 < x2) ? ((x2 < x3) ? x3 : x2) : ((x1 < x3) ? x3 : x1);
    }

    /**
     * @return Rect bound of flattened (ignoring z). LTRB
     * @param dimension - 2D or 3D
     */
    public static float[] flatBound(float[] poly, int dimension) {
        int polySize = poly.length/dimension;
        float left = poly[0];
        float right = poly[0];
        float top = poly[1];
        float bottom = poly[1];

        for (int i = 0; i < polySize; i++) {
            float x = poly[i * dimension + 0];
            float y = poly[i * dimension + 1];

            if (left > x) {
                left = x;
            } else if (right < x) {
                right = x;
            }

            if (top > y) {
                top = y;
            } else if (bottom < y) {
                bottom = y;
            }
        }
        return new float[]{left, top, right, bottom};
    }

    /**
     * Translate the polygon to x and y
     * @param dimension in what dimension is polygon represented (supports 2 or 3D).
     */
    public static void translate(float[] poly, float translateX, float translateY, int dimension) {
        int polySize = poly.length/dimension;

        for (int i = 0; i < polySize; i++) {
            poly[i * dimension + 0] += translateX;
            poly[i * dimension + 1] += translateY;
        }
    }

}

