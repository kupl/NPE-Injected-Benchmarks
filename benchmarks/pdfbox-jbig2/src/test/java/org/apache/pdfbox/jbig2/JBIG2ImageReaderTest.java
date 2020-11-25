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

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import junit.framework.Assert;

import org.apache.pdfbox.jbig2.JBIG2ImageReader;
import org.apache.pdfbox.jbig2.JBIG2ImageReaderSpi;
import org.apache.pdfbox.jbig2.err.IntegerMaxValueException;
import org.apache.pdfbox.jbig2.err.InvalidHeaderValueException;
import org.apache.pdfbox.jbig2.io.DefaultInputStreamFactory;
import org.junit.Test;

public class JBIG2ImageReaderTest
{

    @Test
    public void testGetDefaultReadParams() throws Exception
    {
        ImageReader reader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        ImageReadParam param = reader.getDefaultReadParam();
        Assert.assertNotNull(param);

        Assert.assertNull(param.getSourceRegion());
        Assert.assertNull(param.getSourceRenderSize());

        Assert.assertEquals(1, param.getSourceXSubsampling());
        Assert.assertEquals(1, param.getSourceYSubsampling());
        Assert.assertEquals(0, param.getSubsamplingXOffset());
        Assert.assertEquals(0, param.getSubsamplingYOffset());
    }

    @Test
    public void testRead() throws IOException, InvalidHeaderValueException, IntegerMaxValueException
    {

        int imageIndex = 0;

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        InputStream inputStream = new FileInputStream(inputFile);

        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream imageInputStream = disf.getInputStream(inputStream);

        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        imageReader.setInput(imageInputStream);

        // long timeStamp = System.currentTimeMillis();
        BufferedImage bufferedImage = imageReader.read(imageIndex,
                imageReader.getDefaultReadParam());
        // long duration = System.currentTimeMillis() - timeStamp;
        // System.out.println(filepath + " decoding took " + duration + " ms");

        Assert.assertNotNull(bufferedImage);
    }

    @Test
    public void testReadRaster()
            throws IOException, InvalidHeaderValueException, IntegerMaxValueException
    {

        int imageIndex = 0;

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        InputStream inputStream = new FileInputStream(inputFile);

        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream imageInputStream = disf.getInputStream(inputStream);

        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        imageReader.setInput(imageInputStream);
        Raster raster = imageReader.readRaster(imageIndex, imageReader.getDefaultReadParam());

        Assert.assertNotNull(raster);
    }

    @Test
    public void testReadImageReadParamNull()
            throws IOException, InvalidHeaderValueException, IntegerMaxValueException
    {

        int imageIndex = 0;

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        InputStream inputStream = new FileInputStream(inputFile);

        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream imageInputStream = disf.getInputStream(inputStream);
        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        imageReader.setInput(imageInputStream);
        BufferedImage bufferedImage = imageReader.read(imageIndex, null);

        Assert.assertNotNull(bufferedImage);
    }

    @Test
    public void testReadRasterImageReadParamNull()
            throws IOException, InvalidHeaderValueException, IntegerMaxValueException
    {

        int imageIndex = 0;

        final File inputFile = new File("target/images/042_1.jb2");
        // skip test if input stream isn't available
        assumeTrue(inputFile.exists());

        InputStream inputStream = new FileInputStream(inputFile);

        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream imageInputStream = disf.getInputStream(inputStream);
        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        imageReader.setInput(imageInputStream);
        Raster raster = imageReader.readRaster(imageIndex, null);

        Assert.assertNotNull(raster);
    }

    @Test
    public void testGetNumImages()
            throws IOException, InvalidHeaderValueException, IntegerMaxValueException
    {
        String filepath = "/images/002.jb2";

        InputStream inputStream = getClass().getResourceAsStream(filepath);
        DefaultInputStreamFactory disf = new DefaultInputStreamFactory();
        ImageInputStream imageInputStream = disf.getInputStream(inputStream);
        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        imageReader.setInput(imageInputStream);
        int numImages = imageReader.getNumImages(true);
        Assert.assertEquals(17, numImages);
    }

    @Test
    public void testCanReadRaster() throws IOException
    {
        JBIG2ImageReader imageReader = new JBIG2ImageReader(new JBIG2ImageReaderSpi());
        Assert.assertTrue(imageReader.canReadRaster());
    }

}
