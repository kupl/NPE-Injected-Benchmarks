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

package org.apache.pdfbox.jbig2.image;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assume.assumeTrue;

import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.jbig2.Bitmap;
import org.apache.pdfbox.jbig2.JBIG2DocumentFacade;
import org.apache.pdfbox.jbig2.err.JBIG2Exception;
import org.apache.pdfbox.jbig2.image.Bitmaps;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.apache.pdfbox.jbig2.util.CombinationOperator;
import org.junit.Test;

public class BitmapsBlitTest
{

    @Test
    public void testCompleteBitmapTransfer() throws IOException, JBIG2Exception
    {

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        final InputStream inputStream = new FileInputStream(inputFile);

        final DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        final ImageInputStream iis = disf.getInputStream(inputStream);

        final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);

        final Bitmap src = doc.getPageBitmap(1);
        final Bitmap dst = new Bitmap(src.getWidth(), src.getHeight());
        Bitmaps.blit(src, dst, 0, 0, CombinationOperator.REPLACE);

        final byte[] srcData = src.getByteArray();
        final byte[] dstData = dst.getByteArray();

        assertArrayEquals(srcData, dstData);
    }

    @Test
    public void test() throws IOException, JBIG2Exception
    {

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        final InputStream inputStream = new FileInputStream(inputFile);

        final DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        final ImageInputStream iis = disf.getInputStream(inputStream);

        final JBIG2DocumentFacade doc = new JBIG2DocumentFacade(iis);

        final Bitmap dst = doc.getPageBitmap(1);

        final Rectangle roi = new Rectangle(100, 100, 100, 100);
        final Bitmap src = new Bitmap(roi.width, roi.height);
        Bitmaps.blit(src, dst, roi.x, roi.y, CombinationOperator.REPLACE);

        final Bitmap dstRegionBitmap = Bitmaps.extract(roi, dst);

        final byte[] srcData = src.getByteArray();
        final byte[] dstRegionData = dstRegionBitmap.getByteArray();

        assertArrayEquals(srcData, dstRegionData);
    }

}
