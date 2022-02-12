/*
 * This file is part of Noise, licensed under the MIT License (MIT).
 *
 * Copyright (c) Flow Powered <https://github.com/flow>
 * Copyright (c) SpongePowered <https://github.com/SpongePowered>
 * Copyright (c) contributors
 *
 * Original libnoise C++ library by Jason Bevins <http://libnoise.sourceforge.net>
 * jlibnoise Java port by Garrett Fleenor <https://github.com/RoyAwesome/jlibnoise>
 * Noise is re-licensed with permission from jlibnoise author.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.noise;

public final class Noise {
    private static final int X_NOISE_GEN = 1619;
    private static final int Y_NOISE_GEN = 31337;
    private static final int Z_NOISE_GEN = 6971;
    private static final int SEED_NOISE_GEN = 1013;
    private static final int SHIFT_NOISE_GEN = 8;

    private Noise() {
    }

    /**
     * Generates a simplex-style gradient coherent noise value from the
     * coordinates of a three-dimensional input value.
     *
     * <p>Does not use the classic Simplex noise algorithm, but an alternative.
     * Adapted from the following URLs:
     * https://github.com/KdotJPG/New-Simplex-Style-Gradient-Noise/blob/master/java/FastSimplexStyleNoise.java
     * https://github.com/KdotJPG/New-Simplex-Style-Gradient-Noise/blob/master/java/SuperSimplexNoise.java</p>
     *
     * <p>The return value ranges from 0 to 1.</p>
     *
     * @param x The {@code x} coordinate of the input value.
     * @param y The {@code y} coordinate of the input value.
     * @param z The {@code z} coordinate of the input value.
     * @param seed The random number seed.
     * @param orientation The lattice orientation of the simplex-style coherent noise. See documentation for {@link LatticeOrientation}.
     * @param quality The quality of the simplex-style coherent noise.
     * @return The generated gradient-coherent-noise value.
     */
    public static double simplexStyleGradientCoherentNoise3D(final double x, final double y, final double z, final int seed, final LatticeOrientation orientation, final NoiseQualitySimplex quality) {
        final double squaredRadius = quality.kernelSquaredRadius();
        final double[] randomVectors = quality.randomVectors();
        final Utils.LatticePointBCC[] lookup = quality.lookup();

        // Re-orient the cubic lattices via rotation. These are orthonormal rotations, not skew transforms.
        final double xr, yr, zr;
        if (orientation == LatticeOrientation.CLASSIC) {
            final double r = (2.0 / 3.0) * (x + y + z);
            xr = r - x; yr = r - y; zr = r - z;
        } else if (orientation == LatticeOrientation.XY_BEFORE_Z) {
            final double xy = x + y;
            final double s2 = xy * -0.211324865405187;
            final double zz = z * 0.577350269189626;
            xr = x + s2 - zz; yr = y + s2 - zz;
            zr = xy * 0.577350269189626 + zz;
        } else { // XZ_BEFORE_Y
            final double xz = x + z;
            final double s2 = xz * -0.211324865405187;
            final double yy = y * 0.577350269189626;
            xr = x + s2 - yy; zr = z + s2 - yy;
            yr = xz * 0.577350269189626 + yy;
        }

        // Get base and offsets inside cube of first lattice.
        final int xrb = ((xr > 0.0) ? (int) xr : (int) xr - 1);
        final int yrb = ((yr > 0.0) ? (int) yr : (int) yr - 1);
        final int zrb = ((zr > 0.0) ? (int) zr : (int) zr - 1);
        final double xri = xr - xrb;
        final double yri = yr - yrb;
        final double zri = zr - zrb;

        // Identify which octant of the cube we're in. This determines which cell
        // in the other cubic lattice we're in, and also narrows down one point on each.
        final int xht = (int)(xri + 0.5);
        final int yht = (int)(yri + 0.5);
        final int zht = (int)(zri + 0.5);
        final int index = (xht << 0) | (yht << 1) | (zht << 2);

        // Point contributions
        double value = 0.5;
        Utils.LatticePointBCC c = lookup[index];
        do {
            final double dxr = xri + c.dxr;
            final double dyr = yri + c.dyr;
            final double dzr = zri + c.dzr;
            double attn = squaredRadius - dxr * dxr - dyr * dyr - dzr * dzr;
            if (attn < 0) {
                c = c.nextOnFailure;
            } else {
                final int ix = xrb + c.xrv;
                final int iy = yrb + c.yrv;
                final int iz = zrb + c.zrv;
                int vectorIndex = (Noise.X_NOISE_GEN * ix + Noise.Y_NOISE_GEN * iy + Noise.Z_NOISE_GEN * iz + Noise.SEED_NOISE_GEN * seed);
                vectorIndex ^= (vectorIndex >> Noise.SHIFT_NOISE_GEN);
                vectorIndex &= 0xff;
                final double xvGradient = randomVectors[(vectorIndex << 2)];
                final double yvGradient = randomVectors[(vectorIndex << 2) + 1];
                final double zvGradient = randomVectors[(vectorIndex << 2) + 2];
                final double ramped = ((xvGradient * dxr) + (yvGradient * dyr) + (zvGradient * dzr));

                attn *= attn;
                value += attn * attn * ramped;
                c = c.nextOnSuccess;
            }
        } while (c != null);
        return value;
    }

    /**
     * Generates a gradient-coherent-noise value from the coordinates of a
     * three-dimensional input value.
     *
     * <p>The return value ranges from 0 to 1.</p>
     *
     * <p>For an explanation of the difference between <i>gradient</i> noise
     * and <i>value</i> noise, see the comments for the
     * {@link #gradientNoise3D(double, double, double, int, int, int, int)} function.</p>
     *
     * @param x The {@code x} coordinate of the input value.
     * @param y The {@code y} coordinate of the input value.
     * @param z The {@code z} coordinate of the input value.
     * @param seed The random number seed.
     * @param quality The quality of the coherent-noise.
     * @return The generated gradient-coherent-noise value.
     */
    public static double gradientCoherentNoise3D(final double x, final double y, final double z, final int seed, final NoiseQuality quality) {

        // Create a unit-length cube aligned along an integer boundary.  This cube
        // surrounds the input point.

        final int x0 = ((x > 0.0) ? (int) x : (int) x - 1);
        final int x1 = x0 + 1;

        final int y0 = ((y > 0.0) ? (int) y : (int) y - 1);
        final int y1 = y0 + 1;

        final int z0 = ((z > 0.0) ? (int) z : (int) z - 1);
        final int z1 = z0 + 1;

        // Map the difference between the coordinates of the input value and the
        // coordinates of the cube's outer-lower-left vertex onto an S-curve.
        final double xs, ys, zs;
        if (quality == NoiseQuality.FAST) {
            xs = (x - (double) x0);
            ys = (y - (double) y0);
            zs = (z - (double) z0);
        } else if (quality == NoiseQuality.STANDARD) {
            xs = Utils.sCurve3(x - (double) x0);
            ys = Utils.sCurve3(y - (double) y0);
            zs = Utils.sCurve3(z - (double) z0);
        } else {
            xs = Utils.sCurve5(x - (double) x0);
            ys = Utils.sCurve5(y - (double) y0);
            zs = Utils.sCurve5(z - (double) z0);
        }

        // Now calculate the noise values at each vertex of the cube.  To generate
        // the coherent-noise value at the input point, interpolate these eight
        // noise values using the S-curve value as the interpolant (trilinear
        // interpolation.)
        double n0, n1, ix0, ix1, iy0, iy1;
        n0 = Noise.gradientNoise3D(x, y, z, x0, y0, z0, seed);
        n1 = Noise.gradientNoise3D(x, y, z, x1, y0, z0, seed);
        ix0 = Utils.linearInterp(n0, n1, xs);

        n0 = Noise.gradientNoise3D(x, y, z, x0, y1, z0, seed);
        n1 = Noise.gradientNoise3D(x, y, z, x1, y1, z0, seed);
        ix1 = Utils.linearInterp(n0, n1, xs);
        iy0 = Utils.linearInterp(ix0, ix1, ys);
        n0 = Noise.gradientNoise3D(x, y, z, x0, y0, z1, seed);
        n1 = Noise.gradientNoise3D(x, y, z, x1, y0, z1, seed);
        ix0 = Utils.linearInterp(n0, n1, xs);
        n0 = Noise.gradientNoise3D(x, y, z, x0, y1, z1, seed);
        n1 = Noise.gradientNoise3D(x, y, z, x1, y1, z1, seed);
        ix1 = Utils.linearInterp(n0, n1, xs);
        iy1 = Utils.linearInterp(ix0, ix1, ys);
        return Utils.linearInterp(iy0, iy1, zs);
    }

    /**
     * Generates a gradient-noise value from the coordinates of a
     * three-dimensional input value and the integer coordinates of a
     * nearby three-dimensional value.
     *
     * <p>The difference between fx and ix must be less than or equal to one.
     * The difference between {@code fy} and {@code iy} must be less than or equal to one.
     * The difference between {@code fz} and {@code iz} must be less than
     * or equal to one.</p>
     *
     * <p>A <i>gradient</i>-noise function generates better-quality noise than a
     * <i>value</i>-noise function. Most noise modules use gradient noise for
     * this reason, although it takes much longer to calculate.</p>
     *
     * <p>The return value ranges from 0 to 1.</p>
     *
     * <p>This function generates a gradient-noise value by performing the
     * following steps:</p>
     * <ol>
     * <li>It first calculates a random normalized vector based on the nearby
     * integer value passed to this function.</li>
     * <li>It then calculates a new value by adding this vector to the nearby
     * integer value passed to this function.</li>
     * <li>It then calculates the dot product of the above-generated value and
     * the floating-point input value passed to this function.</li>
     * </ol>
     *
     * <p>A noise function differs from a random-number generator because it
     * always returns the same output value if the same input value is passed
     * to it.</p>
     *
     * @param fx The floating-point {@code x} coordinate of the input value.
     * @param fy The floating-point {@code y} coordinate of the input value.
     * @param fz The floating-point {@code z} coordinate of the input value.
     * @param ix The integer {@code x} coordinate of a nearby value.
     * @param iy The integer {@code y} coordinate of a nearby value.
     * @param iz The integer {@code z} coordinate of a nearby value.
     * @param seed The random number seed.
     * @return The generated gradient-noise value.
     */
    public static double gradientNoise3D(final double fx, final double fy, final double fz, final int ix, final int iy, final int iz, final int seed) {
        // Randomly generate a gradient vector given the integer coordinates of the
        // input value.  This implementation generates a random number and uses it
        // as an index into a normalized-vector lookup table.
        int vectorIndex = (Noise.X_NOISE_GEN * ix + Noise.Y_NOISE_GEN * iy + Noise.Z_NOISE_GEN * iz + Noise.SEED_NOISE_GEN * seed);
        vectorIndex ^= (vectorIndex >> Noise.SHIFT_NOISE_GEN);
        vectorIndex &= 0xff;

        final double xvGradient = Utils.RANDOM_VECTORS_PERLIN[(vectorIndex << 2)];
        final double yvGradient = Utils.RANDOM_VECTORS_PERLIN[(vectorIndex << 2) + 1];
        final double zvGradient = Utils.RANDOM_VECTORS_PERLIN[(vectorIndex << 2) + 2];

        // Set up us another vector equal to the distance between the two vectors
        // passed to this function.
        final double xvPoint = (fx - ix);
        final double yvPoint = (fy - iy);
        final double zvPoint = (fz - iz);

        // Now compute the dot product of the gradient vector with the distance
        // vector.  The resulting value is gradient noise.  Apply a scaling and
        // offset value so that this noise value ranges from 0 to 1.
        return ((xvGradient * xvPoint) + (yvGradient * yvPoint) + (zvGradient * zvPoint)) + 0.5;
    }

    /**
     * Generates an integer-noise value from the coordinates of a
     * three-dimensional input value.
     *
     * <p>The return value ranges from {@code 0} to {@code 2147483647}.</p>
     *
     * <p>A noise function differs from a random-number generator because it
     * always returns the same output value if the same input value is passed
     * to it.</p>
     *
     * @param x The integer {@code x} coordinate of the input value.
     * @param y The integer {@code y} coordinate of the input value.
     * @param z The integer {@code z} coordinate of the input value.
     * @param seed A random number seed.
     * @return The generated integer-noise value.
     */
    public static int intValueNoise3D(final int x, final int y, final int z, final int seed) {
        // All constants are primes and must remain prime in order for this noise
        // function to work correctly.
        int n = (Noise.X_NOISE_GEN * x + Noise.Y_NOISE_GEN * y + Noise.Z_NOISE_GEN * z + Noise.SEED_NOISE_GEN * seed) & 0x7fffffff;
        n = (n >> 13) ^ n;
        return (n * (n * n * 60493 + 19990303) + 1376312589) & 0x7fffffff;
    }

    /**
     * Generates a value-coherent-noise value from the coordinates of a
     * three-dimensional input value.
     *
     * @param x The {@code x} coordinate of the input value.
     * @param y The {@code y} coordinate of the input value.
     * @param z The {@code z} coordinate of the input value.
     * @param seed The random number seed.
     * @param quality The quality of the coherent-noise.
     * @return The generated value-coherent-noise value.
     *
     * <p>The return value ranges from 0 to 1.</p>
     * <p>For an explanation of the difference between <i>gradient</i> noise and
     * <i>value</i> noise, see the comments for
     * {@link #gradientNoise3D(double, double, double, int, int, int, int)}.</p>
     */
    public static double valueCoherentNoise3D(final double x, final double y, final double z, final int seed, final NoiseQuality quality) {
        // Create a unit-length cube aligned along an integer boundary.  This cube
        // surrounds the input point.
        final int x0 = (x > 0.0 ? (int) x : (int) x - 1);
        final int x1 = x0 + 1;
        final int y0 = (y > 0.0 ? (int) y : (int) y - 1);
        final int y1 = y0 + 1;
        final int z0 = (z > 0.0 ? (int) z : (int) z - 1);
        final int z1 = z0 + 1;

        // Map the difference between the coordinates of the input value and the
        // coordinates of the cube's outer-lower-left vertex onto an S-curve.
        final double xs, ys, zs;
        if (quality == NoiseQuality.FAST) {
            xs = (x - x0);
            ys = (y - y0);
            zs = (z - z0);
        } else if (quality == NoiseQuality.STANDARD) {
            xs = Utils.sCurve3(x - x0);
            ys = Utils.sCurve3(y - y0);
            zs = Utils.sCurve3(z - z0);
        } else {
            xs = Utils.sCurve5(x - x0);
            ys = Utils.sCurve5(y - y0);
            zs = Utils.sCurve5(z - z0);
        }

        // Now calculate the noise values at each vertex of the cube.  To generate
        // the coherent-noise value at the input point, interpolate these eight
        // noise values using the S-curve value as the interpolant (trilinear
        // interpolation.)
        double n0, n1, ix0, ix1, iy0, iy1;
        n0 = Noise.valueNoise3D(x0, y0, z0, seed);
        n1 = Noise.valueNoise3D(x1, y0, z0, seed);
        ix0 = Utils.linearInterp(n0, n1, xs);
        n0 = Noise.valueNoise3D(x0, y1, z0, seed);
        n1 = Noise.valueNoise3D(x1, y1, z0, seed);
        ix1 = Utils.linearInterp(n0, n1, xs);
        iy0 = Utils.linearInterp(ix0, ix1, ys);

        n0 = Noise.valueNoise3D(x0, y0, z1, seed);
        n1 = Noise.valueNoise3D(x1, y0, z1, seed);
        ix0 = Utils.linearInterp(n0, n1, xs);
        n0 = Noise.valueNoise3D(x0, y1, z1, seed);
        n1 = Noise.valueNoise3D(x1, y1, z1, seed);
        ix1 = Utils.linearInterp(n0, n1, xs);
        iy1 = Utils.linearInterp(ix0, ix1, ys);

        return Utils.linearInterp(iy0, iy1, zs);
    }

    /**
     * Generates a value-noise value from the coordinates of a three-dimensional
     * input value.
     *
     * <p>The return value ranges from 0 to 1.</p>
     *
     * <p>A noise function differs from a random-number generator because it
     * always returns the same output value if the same input value is passed
     * to it.</p>
     *
     * @param x The {@code x} coordinate of the input value.
     * @param y The {@code y} coordinate of the input value.
     * @param z The {@code z} coordinate of the input value.
     * @param seed A random number seed.
     * @return The generated value-noise value.
     */
    public static double valueNoise3D(final int x, final int y, final int z, final int seed) {
        return Noise.intValueNoise3D(x, y, z, seed) / 2147483647.0;
    }

}
