/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.jbig2.util;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class Utils
{

    /**
     * Create a rectangle with the same area as the given input rectangle but with all of its edges snapped (rounded) to
     * the integer grid. The resulting rectangle is guaranteed to cover <em>all</em> of the input rectangle's area, so
     * that <code>enlargeToGrid(r).contains(r) == true</code> holds. This can be depicted as the edges being stretched
     * in an outward direction.
     * 
     * @param r the given rectangle
     * @return the resulting rectangle
     */
    public static Rectangle enlargeRectToGrid(Rectangle2D r)
    {
        final int x0 = floor(r.getMinX());
        final int y0 = floor(r.getMinY());
        final int x1 = ceil(r.getMaxX());
        final int y1 = ceil(r.getMaxY());
        return new Rectangle(x0, y0, x1 - x0, y1 - y0);
    }

    /**
     * Return a new rectangle which covers the area of the given rectangle with an additional margin on the sides.
     * 
     * @param r the given rectangle
     * @param marginX horizontal value of the additional margin
     * @param marginY vertical value of the additional margin
     * @return the resulting rectangle
     */
    public static Rectangle2D dilateRect(Rectangle2D r, double marginX, double marginY)
    {
        return new Rectangle2D.Double(r.getX() - marginX, r.getY() - marginY,
                r.getWidth() + 2 * marginX, r.getHeight() + 2 * marginY);
    }

    /**
     * Clamp the value into the range [min..max].
     * 
     * @param value input value
     * @param min minimal value
     * @param max maximal value
     * @return the clamped value
     */
    public static double clamp(double value, double min, double max)
    {
        return Math.min(max, Math.max(value, min));
    }

    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5;

    /**
     * A fast implementation of {@link Math#floor(double)}.
     * 
     * @param x the argument
     * @return resulting floor value
     */
    public static int floor(double x)
    {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    /**
     * A fast implementation of {@link Math#round(double)}.
     * 
     * @param x the argument
     * @return rounded value
     */
    public static int round(double x)
    {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    /**
     * A fast implementation of {@link Math#ceil(double)}.
     * 
     * @param x the argument
     * @return resulting ceil value
     */
    public static int ceil(double x)
    {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - x);
    }

}
