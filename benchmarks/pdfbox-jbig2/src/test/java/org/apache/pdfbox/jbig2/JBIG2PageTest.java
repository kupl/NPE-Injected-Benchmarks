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

package org.apache.pdfbox.jbig2;

import static org.junit.Assume.assumeTrue;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.stream.ImageInputStream;

import org.apache.pdfbox.jbig2.Bitmap;
import org.apache.pdfbox.jbig2.JBIG2Document;
import org.apache.pdfbox.jbig2.JBIG2ReadParam;
import org.apache.pdfbox.jbig2.TestImage;
import org.apache.pdfbox.jbig2.err.JBIG2Exception;
import org.apache.pdfbox.jbig2.image.Bitmaps;
import org.apache.pdfbox.jbig2.image.FilterType;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.junit.Ignore;
import org.junit.Test;

public class JBIG2PageTest
{

    // TESTS WITH TESTOUTPUT
    // Ignore in build process

    @Ignore
    @Test
    public void composeDisplayTest() throws IOException, JBIG2Exception
    {

        final File inputFile = new File("target/images/amb_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        int pageNumber = 1;

        InputStream is = new FileInputStream(inputFile);
        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream iis = disf.getInputStream(is);
        JBIG2Document doc = new JBIG2Document(iis);

        Bitmap pageBitmap = doc.getPage(pageNumber).getBitmap();
        BufferedImage b = Bitmaps.asBufferedImage(pageBitmap,
                new JBIG2ReadParam(1, 1, 0, 0, new Rectangle(166, 333, 555, 444), null),
                FilterType.Gaussian);
        new TestImage(b);
    }

    @Ignore
    @Test
    public void composeTestWithDurationCalc() throws IOException, JBIG2Exception
    {
        int runs = 40;
        long avg = 0;

        final File inputFile = new File("target/images/042_8.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        int pageNumber = 1;

        InputStream is = new FileInputStream(inputFile);
        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream iis = disf.getInputStream(is);

        for (int i = 0; i < runs; i++)
        {

            long time = System.currentTimeMillis();
            JBIG2Document doc = new JBIG2Document(iis);
            Bitmap pageBitmap = doc.getPage(pageNumber).getBitmap();
            Bitmaps.asBufferedImage(pageBitmap);
            long duration = System.currentTimeMillis() - time;

            System.out.println((i + 1) + ": " + duration + " ms");
            avg += duration;
        }
        System.out.println("Average: " + avg / runs);
    }

    @Ignore
    @Test
    public void composeTestWithDurationCalcAggregate() throws IOException, JBIG2Exception
    {
        int runs = 40;
        long avg = 0;
        String path = "/images/002.jb2";
        int pages = 17;

        System.out.println("File: " + path);

        InputStream is = getClass().getResourceAsStream(path);
        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream iis = disf.getInputStream(is);

        for (int j = 1; j <= pages; j++)
        {
            avg = 0;

            for (int i = 0; i < runs; i++)
            {
                long time = System.currentTimeMillis();
                JBIG2Document doc = new JBIG2Document(iis);
                Bitmap pageBitmap = doc.getPage(j).getBitmap();
                Bitmaps.asBufferedImage(pageBitmap);
                long duration = System.currentTimeMillis() - time;
                System.out.print((i + 1) + ": " + duration + " ms ");
                avg += duration;
            }
            System.out.println();
            System.out.println("Page " + j + " Average: " + avg / runs);
        }
    }

}
