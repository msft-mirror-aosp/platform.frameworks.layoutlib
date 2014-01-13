/*
 * Copyright (C) 2014 The Android Open Source Project
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

#ifndef ANDROID_HWUI_SPOT_SHADOW_H
#define ANDROID_HWUI_SPOT_SHADOW_H

#include "Debug.h"
#include "Vector.h"
#include "VertexBuffer.h"

namespace android {
namespace uirenderer {

class SpotShadow {
public:
    static void createSpotShadow(const Vector3* poly, int polyLength,
            const Vector3& lightCenter, float lightSize, int lightVertexCount,
            int rays, int layers, float strength, VertexBuffer& retStrips);

private:
    static void computeSpotShadow(const Vector3* lightPoly, int lightPolyLength,
            const Vector3& lightCenter, const Vector3* poly, int polyLength,
            int rays, int layers, float strength, VertexBuffer& retstrips);

    static void computeLightPolygon(int points, const Vector3& lightCenter,
            float size, Vector3* ret);

    static int  getStripSize(int rays, int layers);
    static void smoothPolygon(int level, int rays, float* rayDist);
    static float calculateOpacity(float jf, float deltaDist);
    static float rayIntersectPoly(const Vector2* poly, int polyLength,
            const Vector2& point, float dx, float dy);

    static Vector2 centroid2d(const Vector2* poly, int polyLength);

    static void xsort(Vector2* points, int pointsLength);
    static int hull(Vector2* points, int pointsLength, Vector2* retPoly);
    static bool rightTurn(double ax, double ay, double bx, double by, double cx, double cy);
    static int intersection(Vector2* poly1, int poly1length, Vector2* poly2, int poly2length);
    static void sort(Vector2* poly, int polyLength, const Vector2& center);

    static float angle(const Vector2& point, const Vector2& center);
    static void swap(Vector2* points, int i, int j);
    static void quicksortCirc(Vector2* points, int low, int high, const Vector2& center);
    static void quicksortX(Vector2* points, int low, int high);

    static bool testPointInsidePolygon(const Vector2 testPoint, const Vector2* poly, int len);
    static void makeClockwise(Vector2* polygon, int len);
    static bool isClockwise(Vector2* polygon, int len);
    static void reverse(Vector2* polygon, int len);
    static inline bool lineIntersection(double x1, double y1, double x2, double y2,
            double x3, double y3, double x4, double y4, Vector2& ret);

    static void generateTriangleStrip(const Vector2* penumbra, int penumbraLength,
            const Vector2* umbra, int umbraLength, int rays, int layers,
            float strength, VertexBuffer& retstrips);

    static const double EPSILON = 1e-7;
}; // SpotShadow

}; // namespace uirenderer
}; // namespace android

#endif // ANDROID_HWUI_SPOT_SHADOW_H