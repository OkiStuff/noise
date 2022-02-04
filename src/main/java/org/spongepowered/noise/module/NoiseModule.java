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
package org.spongepowered.noise.module;

import org.spongepowered.noise.exception.NoModuleException;
import org.spongepowered.noise.module.combiner.Add;
import org.spongepowered.noise.module.combiner.Blend;
import org.spongepowered.noise.module.combiner.Max;
import org.spongepowered.noise.module.combiner.Select;
import org.spongepowered.noise.module.modifier.Curve;
import org.spongepowered.noise.module.modifier.Invert;
import org.spongepowered.noise.module.modifier.RotatePoint;
import org.spongepowered.noise.module.modifier.ScalePoint;
import org.spongepowered.noise.module.source.Const;
import org.spongepowered.noise.module.source.Perlin;
import org.spongepowered.noise.module.source.Voronoi;

/**
 * Abstract base class for noise modules.
 *
 * <p>A <em>noise module</em> is an object that calculates and outputs a value
 * given a three-dimensional input value.</p>
 *
 * <p>Each type of noise module uses a specific method to calculate an
 * output value. Some of these methods include:</p>
 * <ul>
 *   <li>Calculating a value using a coherent-noise function or some other
 *   mathematical function.</li>
 *   <li>Mathematically changing the output value from another noise module
 *   in various ways.</li>
 *   <li>Combining the output values from two noise modules in various
 *   ways.</li>
 * </ul>
 *
 * <p>An application can use the output values from these noise modules in
 * the following ways:</p>
 * <ul>
 *   <li>It can be used as an elevation value for a terrain height map</li>
 *   <li>It can be used as a grayscale (or an RGB-channel) value for a
 *   procedural texture</li>
 *   <li>It can be used as a position value for controlling the movement of a
 *   simulated lifeform.</li>
 * </ul>
 *
 * <p>A noise module defines a near-infinite 3-dimensional texture. Each
 * position in this "texture" has a specific value.</p>
 *
 * <h2>Combining noise modules</h2>
 *
 * <p>Noise modules can be combined with other noise modules to generate
 * complex output values. A noise module that is used as a source of
 * output values for another noise module is called a <em>source
 * module</em>. Each of these source modules may be connected to other
 * source modules, and so on.</p>
 *
 * <p>There is no limit to the number of noise modules that can be connected
 * together in this way. However, each connected noise module increases
 * the time required to calculate an output value.</p>
 *
 * <h2>Noise-module categories</h2>
 *
 * <p>The noise module classes that are included in libnoise can be roughly
 * divided into five categories.</p>
 *
 * <h3>Generator Modules</h3>
 *
 * <p>A generator module outputs a value generated by a coherent-noise
 * function or some other mathematical function.</p>
 *
 * <p>Examples of generator modules include:</p>
 * <ul>
 * <li>{@link Const}: Outputs a constant value.</li>
 * <li>{@link Perlin}: Outputs a value generated by a Perlin-noise
 *   function.</li>
 * <li>{@link Voronoi}: Outputs a value generated by a Voronoi-cell
 *   function.</li>
 * </ul>
 *
 * <h3>Modifier Modules</h3>
 *
 * <p>A modifier module mathematically modifies the output value from a
 * source module.</p>
 *
 * <p>Examples of modifier modules include:</p>
 * <ul>
 *   <li>{@link Curve}: Maps the output value from the source module onto an
 * arbitrary function curve.</li>
 *   <li>{@link Invert}: Inverts the output value from the source module.</li>
 * </ul>
 *
 * <h3>Combiner Modules</h3>
 *
 * <p>A combiner module mathematically combines the output values from two
 * or more source modules together.</p>
 *
 * <p>Examples of combiner modules include:</p>
 * <ul>
 *   <li>{@link Add}: Adds the two output values from two source
 *   modules.</li>
 *   <li>{@link Max}: Outputs the larger of the two output values from
 *   two source modules.</li>
 * </ul>
 *
 * <h3>Selector Modules</h3>
 *
 * <p>A selector module uses the output value from a <em>control module</em>
 * to specify how to combine the output values from its source modules.</p>
 *
 * <p>Examples of selector modules include:</p>
 * <ul>
 *   <li>{@link Blend}: Outputs a value that is linearly interpolated
 *   between the output values from two source modules; the interpolation
 *   weight is determined by the output value from the control module.</li>
 *   <li>{@link Select}: Outputs the value selected from one of two
 *   source modules chosen by the output value from a control module.</li>
 * </ul>
 *
 * <h3>Transformer Modules</h3>
 *
 * <p>A transformer module applies a transformation to the coordinates of
 * the input value before retrieving the output value from the source
 * module. A transformer module does not modify the output value.</p>
 *
 * <p>Examples of transformer modules include:</p>
 * <ul>
 *   <li>{@link RotatePoint}: Rotates the coordinates of the input value around
 *   the origin before retrieving the output value from the source module.</li>
 *   <li>{@link ScalePoint}: Multiplies each coordinate of the input value by a
 *   constant value before retrieving the output value from the source
 *   module.</li>
 * </ul>
 *
 * <h2>Connecting source modules to a noise module</h2>
 *
 * <p>An application connects a source module to a noise module by passing
 * the source module to the {@link #setSourceModule(int, NoiseModule)} method.</p>
 *
 * <p>The application must also pass an <em>index value</em> to
 * {@link #setSourceModule(int, NoiseModule)}. An index value is a numeric
 * identifier for that source module. Index values are consecutively numbered
 * starting at zero.</p>
 *
 * <p>To retrieve a reference to a source module, pass its index value to
 * the {@link #sourceModule(int)} method.</p>
 *
 * <p>Each noise module requires the attachment of a certain number of source
 * modules before it can output a value. For example, the {@link Add} module
 * requires two source modules, while the {@link Perlin} module requires none.
 * Call the {@link #sourceModule(int)} method or consult Javadoc to retrieve
 * the number of source modules required by that module.</p>
 *
 * <p>For non-selector modules, it usually does not matter which index value an
 * application assigns to a particular source module, but for selector modules,
 * the purpose of a source module is defined by its index value. For example,
 * consider the {@link Select} noise module, which requires three source
 * modules. The control module is the source module assigned an index value of
 * {@code 2}. The control module determines whether the noise module will output
 * the value from the source module assigned an index value of {@code 0} or the
 * output value from the source module assigned an index value of {@code 1}.</p>
 *
 * <h2>Generating output values with a noise module</h2>
 *
 * <p>Once an application has connected all required source modules to a
 * noise module, the application can now begin to generate output values
 * with that noise module.</p>
 *
 * <p>To generate an output value, pass the {@code (x, y, z)} coordinates
 * of an input value to the {@link #get(double, double, double)} method.</p>
 *
 * <h2>Using a noise module to generate terrain height maps or textures</h2>
 *
 * <p>One way to generate a terrain height map or a texture is to first
 * allocate a 2-dimensional array of floating-point values. For each
 * array element, pass the array subscripts as {@code x} and {@code y} coordinates
 * to the {@link #get(double, double, double)} method (leaving the
 * {@code z} coordinate set to zero) and place the resulting output value into
 * the array element.</p>
 *
 * <h2>Creating your own noise modules</h2>
 *
 * <p>Create a class that extends from {@link NoiseModule}.
 *
 * <p>In the constructor, call the base class' constructor while passing the
 * required number of souce modules to it.</p>
 *
 * <p>Override the {@link #get(double, double, double)} method. For
 * generator modules, calculate and output a value given the coordinates of
 * the input value. For other modules, retrieve the output values from each
 * source module referenced in the {@link #sourceModule} array, mathematically
 * combine those values, and return the combined value.</p>
 *
 * <p>When developing a noise module, you must ensure that your noise module
 * does not modify any source module or control module connected to it; a
 * noise module can only modify the output value from those source modules. You
 * must also ensure that if an application fails to connect
 * all required source modules via the {@link #setSourceModule(int, NoiseModule)}
 * method and then attempts to call the {@link #get(double, double, double)}
 * method, your module will throw an exception.</p>
 *
 * <p>It shouldn't be too difficult to create your own noise module. If you
 * still have some problems, take a look at the source code for {@link Add},
 * which is a very simple noise module.</p>
 */
public abstract class NoiseModule {
    private static final NoiseModule[] EMPTY_MODULE_ARRAY = new NoiseModule[0];

    /**
     * An array containing references to each source module required by this
     * noise module.
     */
    protected NoiseModule[] sourceModule;

    /**
     * Create a new module.
     *
     * @param sourceModuleCount the number of source modules required by
     * this module
     */
    public NoiseModule(final int sourceModuleCount) {
        if (sourceModuleCount > 0) {
            this.sourceModule = new NoiseModule[sourceModuleCount];
        } else {
            this.sourceModule = NoiseModule.EMPTY_MODULE_ARRAY;
        }
    }

    /**
     * Get a source module connected to this noise module.
     *
     * <p>Each noise module requires the attachment of a certain number of
     * source modules before an application can call the
     * {@link #get(double, double, double)} method.</p>
     *
     * @param index the index value assigned to the source module
     * @return the source module if one is set, or else throws {@link NoModuleException}
     * @throws NoModuleException if no module has been set for the specified
     *     index or the index is out of the range from
     *     0 to {@link #sourceModuleCount()}
     */
    public NoiseModule sourceModule(final int index) {
        if (index >= this.sourceModule.length || index < 0 || this.sourceModule[index] == null) {
            throw new NoModuleException(index);
        }
        return this.sourceModule[index];
    }

    /**
     * Connects a source module to this noise module.
     *
     * <p>A noise module mathematically combines the output values from the
     * source modules to generate the value returned by
     * {@link #get(double, double, double)}.</p>
     *
     * <p>The {@code index} value to assign a source module is a unique
     * identifier for that source module. If an index value has already been
     * assigned to a source module, this noise module replaces the old source
     * module with the new source module.</p>
     *
     * <p>Before an application can call the
     * {@link #get(double, double, double)} method, it must first connect
     * all the required source modules. To determine the number of source
     * modules required by the noise module, call the
     * {@link #sourceModuleCount()} method.</p>
     *
     * <p>A noise module does not modify a souce module, it only modifies its
     * output values.</p>
     *
     * @param index an index value to assign to this source module, must be in
     *     the range [0, {@link #sourceModuleCount()})
     * @param sourceModule the source module to attach
     * @throws IllegalArgumentException if the {@code index} is out of bounds
     */
    public void setSourceModule(final int index, final NoiseModule sourceModule) {
        if (this.sourceModule.length == 0) {
            return;
        }
        if (index >= this.sourceModule.length || index < 0) {
            throw new IllegalArgumentException("Index must be between 0 and " + this.sourceModule.length);
        }
        this.sourceModule[index] = sourceModule;
    }

    /**
     * Get the number of source modules required by this noise module.
     *
     * @return the number of source modules required by this noise module
     */
    public final int sourceModuleCount() {
        return this.sourceModule.length;
    }

    /**
     * Generates an output value given the coordinates of the specified input
     * value.
     *
     * <p>All source modules required by this module must have been connected
     * with the {@link #setSourceModule(int, NoiseModule)} method. If these source
     * modules are not connected, this method will throw
     * a {@link NoModuleException}.</p>
     *
     * <p>To determine the number of source modules required by this noise
     * module, call the {@link #sourceModuleCount()} method.</p>
     *
     * @param x the {@code x} coordinate of the input value
     * @param y the {@code y} coordinate of the input value
     * @param z the {@code z} coordinate of the input value
     * @return the output value
     */
    public abstract double get(double x, double y, double z);

}
