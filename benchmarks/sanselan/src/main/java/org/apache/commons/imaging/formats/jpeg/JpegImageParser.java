/*
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
package org.apache.commons.imaging.formats.jpeg;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageParser;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.common.bytesource.ByteSource;
import org.apache.commons.imaging.formats.jpeg.decoder.JpegDecoder;
import org.apache.commons.imaging.formats.jpeg.iptc.IptcParser;
import org.apache.commons.imaging.formats.jpeg.iptc.PhotoshopApp13Data;
import org.apache.commons.imaging.formats.jpeg.segments.App13Segment;
import org.apache.commons.imaging.formats.jpeg.segments.App2Segment;
import org.apache.commons.imaging.formats.jpeg.segments.ComSegment;
import org.apache.commons.imaging.formats.jpeg.segments.DqtSegment;
import org.apache.commons.imaging.formats.jpeg.segments.GenericSegment;
import org.apache.commons.imaging.formats.jpeg.segments.JfifSegment;
import org.apache.commons.imaging.formats.jpeg.segments.Segment;
import org.apache.commons.imaging.formats.jpeg.segments.SofnSegment;
import org.apache.commons.imaging.formats.jpeg.segments.UnknownSegment;
import org.apache.commons.imaging.formats.jpeg.xmp.JpegXmpParser;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageParser;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.util.Debug;

public class JpegImageParser extends ImageParser implements JpegConstants
{
    public JpegImageParser()
    {
        setByteOrder(BYTE_ORDER_NETWORK);
        // setDebug(true);
    }

    @Override
    protected ImageFormat[] getAcceptedTypes()
    {
        return new ImageFormat[] { ImageFormat.IMAGE_FORMAT_JPEG, //
        };
    }

    @Override
    public String getName()
    {
        return "Jpeg-Custom";
    }

    @Override
    public String getDefaultExtension()
    {
        return DEFAULT_EXTENSION;
    }

    private static final String DEFAULT_EXTENSION = ".jpg";

    private static final String ACCEPTED_EXTENSIONS[] = { ".jpg", ".jpeg", };

    @Override
    protected String[] getAcceptedExtensions()
    {
        return ACCEPTED_EXTENSIONS;
    }

    @Override
    public final BufferedImage getBufferedImage(ByteSource byteSource,
            Map params) throws ImageReadException, IOException
    {
        JpegDecoder jpegDecoder = new JpegDecoder();
        return jpegDecoder.decode(byteSource);
    }

    private boolean keepMarker(int marker, int markers[])
    {
        if (markers == null)
            return true;

        for (int i = 0; i < markers.length; i++)
        {
            if (markers[i] == marker)
                return true;
        }

        return false;
    }

    public List<Segment> readSegments(ByteSource byteSource, final int markers[],
            final boolean returnAfterFirst, boolean readEverything)
            throws ImageReadException, IOException
    {
        final List<Segment> result = new ArrayList<Segment>();
        final JpegImageParser parser = this;
        final int[] sofnSegments = {
                // kJFIFMarker,
                SOF0Marker,

                SOF1Marker, SOF2Marker, SOF3Marker, SOF5Marker, SOF6Marker,
                SOF7Marker, SOF9Marker, SOF10Marker, SOF11Marker, SOF13Marker,
                SOF14Marker, SOF15Marker,
        };

        JpegUtils.Visitor visitor = new JpegUtils.Visitor() {
            // return false to exit before reading image data.
            public boolean beginSOS()
            {
                return false;
            }

            public void visitSOS(int marker, byte markerBytes[],
                    byte imageData[])
            {
            }

            // return false to exit traversal.
            public boolean visitSegment(int marker, byte markerBytes[],
                    int markerLength, byte markerLengthBytes[],
                    byte segmentData[]) throws ImageReadException, IOException
            {
                if (marker == EOIMarker)
                    return false;

                // Debug.debug("visitSegment marker", marker);
                // // Debug.debug("visitSegment keepMarker(marker, markers)",
                // keepMarker(marker, markers));
                // Debug.debug("visitSegment keepMarker(marker, markers)",
                // keepMarker(marker, markers));

                if (!keepMarker(marker, markers))
                    return true;

                if (marker == JPEG_APP13_Marker)
                {
                    // Debug.debug("app 13 segment data", segmentData.length);
                    result.add(new App13Segment(parser, marker, segmentData));
                } else if (marker == JPEG_APP2_Marker)
                {
                    result.add(new App2Segment(marker, segmentData));
                } else if (marker == JFIFMarker)
                {
                    result.add(new JfifSegment(marker, segmentData));
                } else if (Arrays.binarySearch(sofnSegments, marker) >= 0)
                {
                    result.add(new SofnSegment(marker, segmentData));
                } else if (marker == DQTMarker)
                {
                    result.add(new DqtSegment(marker, segmentData));
                } else if ((marker >= JPEG_APP1_Marker)
                        && (marker <= JPEG_APP15_Marker))
                {
                    result.add(new UnknownSegment(marker, segmentData));
                } else if (marker == COMMarker)
                {
                    result.add(new ComSegment(marker, segmentData));
                }

                if (returnAfterFirst)
                    return false;

                return true;
            }
        };

        new JpegUtils().traverseJFIF(byteSource, visitor);

        return result;
    }

    public static final boolean permissive = true;

    private byte[] assembleSegments(List<App2Segment> v) throws ImageReadException
    {
        try
        {
            return assembleSegments(v, false);
        } catch (ImageReadException e)
        {
            return assembleSegments(v, true);
        }
    }

    private byte[] assembleSegments(List<App2Segment> v, boolean start_with_zero)
            throws ImageReadException
    {
        if (v.size() < 1)
            throw new ImageReadException("No App2 Segments Found.");

        int markerCount = v.get(0).num_markers;

        // if (permissive && (markerCount == 0))
        // markerCount = v.size();

        if (v.size() != markerCount)
            throw new ImageReadException("App2 Segments Missing.  Found: "
                    + v.size() + ", Expected: " + markerCount + ".");

        Collections.sort(v);

        int offset = start_with_zero ? 0 : 1;

        int total = 0;
        for (int i = 0; i < v.size(); i++)
        {
            App2Segment segment = v.get(i);

            if ((i + offset) != segment.cur_marker)
            {
                dumpSegments(v);
                throw new ImageReadException(
                        "Incoherent App2 Segment Ordering.  i: " + i
                                + ", segment[" + i + "].cur_marker: "
                                + segment.cur_marker + ".");
            }

            if (markerCount != segment.num_markers)
            {
                dumpSegments(v);
                throw new ImageReadException(
                        "Inconsistent App2 Segment Count info.  markerCount: "
                                + markerCount + ", segment[" + i
                                + "].num_markers: " + segment.num_markers + ".");
            }

            total += segment.icc_bytes.length;
        }

        byte result[] = new byte[total];
        int progress = 0;

        for (int i = 0; i < v.size(); i++)
        {
            App2Segment segment = v.get(i);

            System.arraycopy(segment.icc_bytes, 0, result, progress,
                    segment.icc_bytes.length);
            progress += segment.icc_bytes.length;
        }

        return result;
    }

    private void dumpSegments(List<? extends Segment> v)
    {
        Debug.debug();
        Debug.debug("dumpSegments", v.size());

        for (int i = 0; i < v.size(); i++)
        {
            App2Segment segment = (App2Segment) v.get(i);

            Debug.debug((i) + ": " + segment.cur_marker + " / "
                    + segment.num_markers);
        }
        Debug.debug();
    }

    public List<Segment> readSegments(ByteSource byteSource, int markers[],
            boolean returnAfterFirst) throws ImageReadException, IOException
    {
        return readSegments(byteSource, markers, returnAfterFirst, false);
    }

    @Override
    public byte[] getICCProfileBytes(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {
        List<Segment> segments = readSegments(byteSource,
                new int[] { JPEG_APP2_Marker, }, false);

        List<App2Segment> filtered = new ArrayList<App2Segment>();
        if (segments != null)
        {
            // throw away non-icc profile app2 segments.
            for (int i = 0; i < segments.size(); i++)
            {
                App2Segment segment = (App2Segment) segments.get(i);
                if (segment.icc_bytes != null)
                    filtered.add(segment);
            }
        }

        if ((filtered == null) || (filtered.size() < 1))
            return null;

        byte bytes[] = assembleSegments(filtered);

        if (debug)
            System.out.println("bytes" + ": "
                    + ((bytes == null) ? null : "" + bytes.length));

        if (debug)
            System.out.println("");

        return (bytes);
    }

    @Override
    public IImageMetadata getMetadata(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {
        TiffImageMetadata exif = getExifMetadata(byteSource, params);

        JpegPhotoshopMetadata photoshop = getPhotoshopMetadata(byteSource,
                params);

        if (null == exif && null == photoshop)
            return null;

        JpegImageMetadata result = new JpegImageMetadata(photoshop, exif);

        return result;
    }

    public static boolean isExifAPP1Segment(GenericSegment segment)
    {
        return byteArrayHasPrefix(segment.bytes, EXIF_IDENTIFIER_CODE);
    }

    private List<Segment> filterAPP1Segments(List<Segment> v)
    {
        List<Segment> result = new ArrayList<Segment>();

        for (int i = 0; i < v.size(); i++)
        {
            GenericSegment segment = (GenericSegment) v.get(i);
            if (isExifAPP1Segment(segment))
                result.add(segment);
        }

        return result;
    }

    public TiffImageMetadata getExifMetadata(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {
        byte bytes[] = getExifRawData(byteSource);
        if (null == bytes)
            return null;

        if (params == null)
            params = new HashMap();
        if (!params.containsKey(PARAM_KEY_READ_THUMBNAILS))
            params.put(PARAM_KEY_READ_THUMBNAILS, Boolean.TRUE);

        return (TiffImageMetadata) new TiffImageParser().getMetadata(bytes,
                params);
    }

    public byte[] getExifRawData(ByteSource byteSource)
            throws ImageReadException, IOException
    {
        List<Segment> segments = readSegments(byteSource,
                new int[] { JPEG_APP1_Marker, }, false);

        if ((segments == null) || (segments.size() < 1))
            return null;

        List<Segment> exifSegments = filterAPP1Segments(segments);
        if (debug)
            System.out.println("exif_segments.size" + ": "
                    + exifSegments.size());

        // Debug.debug("segments", segments);
        // Debug.debug("exifSegments", exifSegments);

        // TODO: concatenate if multiple segments, need example.
        if (exifSegments.size() < 1)
            return null;
        if (exifSegments.size() > 1)
            throw new ImageReadException(
                    "Sanselan currently can't parse EXIF metadata split across multiple APP1 segments.  "
                            + "Please send this image to the Sanselan project.");

        GenericSegment segment = (GenericSegment) exifSegments.get(0);
        byte bytes[] = segment.bytes;

        // byte head[] = readBytearray("exif head", bytes, 0, 6);
        //
        // Debug.debug("head", head);

        return getByteArrayTail("trimmed exif bytes", bytes, 6);
    }

    public boolean hasExifSegment(ByteSource byteSource)
            throws ImageReadException, IOException
    {
        final boolean result[] = { false, };

        JpegUtils.Visitor visitor = new JpegUtils.Visitor() {
            // return false to exit before reading image data.
            public boolean beginSOS()
            {
                return false;
            }

            public void visitSOS(int marker, byte markerBytes[],
                    byte imageData[])
            {
            }

            // return false to exit traversal.
            public boolean visitSegment(int marker, byte markerBytes[],
                    int markerLength, byte markerLengthBytes[],
                    byte segmentData[]) throws ImageReadException, IOException
            {
                if (marker == 0xffd9)
                    return false;

                if (marker == JPEG_APP1_Marker)
                {
                    if (byteArrayHasPrefix(segmentData, EXIF_IDENTIFIER_CODE))
                    {
                        result[0] = true;
                        return false;
                    }
                }

                return true;
            }
        };

        new JpegUtils().traverseJFIF(byteSource, visitor);

        return result[0];
    }

    public boolean hasIptcSegment(ByteSource byteSource)
            throws ImageReadException, IOException
    {
        final boolean result[] = { false, };

        JpegUtils.Visitor visitor = new JpegUtils.Visitor() {
            // return false to exit before reading image data.
            public boolean beginSOS()
            {
                return false;
            }

            public void visitSOS(int marker, byte markerBytes[],
                    byte imageData[])
            {
            }

            // return false to exit traversal.
            public boolean visitSegment(int marker, byte markerBytes[],
                    int markerLength, byte markerLengthBytes[],
                    byte segmentData[]) throws ImageReadException, IOException
            {
                if (marker == 0xffd9)
                    return false;

                if (marker == JPEG_APP13_Marker)
                {
                    if (new IptcParser().isPhotoshopJpegSegment(segmentData))
                    {
                        result[0] = true;
                        return false;
                    }
                }

                return true;
            }
        };

        new JpegUtils().traverseJFIF(byteSource, visitor);

        return result[0];
    }

    public boolean hasXmpSegment(ByteSource byteSource)
            throws ImageReadException, IOException
    {
        final boolean result[] = { false, };

        JpegUtils.Visitor visitor = new JpegUtils.Visitor() {
            // return false to exit before reading image data.
            public boolean beginSOS()
            {
                return false;
            }

            public void visitSOS(int marker, byte markerBytes[],
                    byte imageData[])
            {
            }

            // return false to exit traversal.
            public boolean visitSegment(int marker, byte markerBytes[],
                    int markerLength, byte markerLengthBytes[],
                    byte segmentData[]) throws ImageReadException, IOException
            {
                if (marker == 0xffd9)
                    return false;

                if (marker == JPEG_APP1_Marker)
                {
                    if (new JpegXmpParser().isXmpJpegSegment(segmentData))
                    {
                        result[0] = true;
                        return false;
                    }
                }

                return true;
            }
        };
        new JpegUtils().traverseJFIF(byteSource, visitor);

        return result[0];
    }

    /**
     * Extracts embedded XML metadata as XML string.
     * <p>
     *
     * @param byteSource
     *            File containing image data.
     * @param params
     *            Map of optional parameters, defined in SanselanConstants.
     * @return Xmp Xml as String, if present. Otherwise, returns null.
     */
    @Override
    public String getXmpXml(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {

        final List<String> result = new ArrayList<String>();

        JpegUtils.Visitor visitor = new JpegUtils.Visitor() {
            // return false to exit before reading image data.
            public boolean beginSOS()
            {
                return false;
            }

            public void visitSOS(int marker, byte markerBytes[],
                    byte imageData[])
            {
            }

            // return false to exit traversal.
            public boolean visitSegment(int marker, byte markerBytes[],
                    int markerLength, byte markerLengthBytes[],
                    byte segmentData[]) throws ImageReadException, IOException
            {
                if (marker == 0xffd9)
                    return false;

                if (marker == JPEG_APP1_Marker)
                {
                    if (new JpegXmpParser().isXmpJpegSegment(segmentData))
                    {
                        result.add(new JpegXmpParser()
                                .parseXmpJpegSegment(segmentData));
                        return false;
                    }
                }

                return true;
            }
        };
        new JpegUtils().traverseJFIF(byteSource, visitor);

        if (result.size() < 1)
            return null;
        if (result.size() > 1)
            throw new ImageReadException(
                    "Jpeg file contains more than one XMP segment.");
        return result.get(0);
    }

    public JpegPhotoshopMetadata getPhotoshopMetadata(ByteSource byteSource,
            Map params) throws ImageReadException, IOException
    {
        List<Segment> segments = readSegments(byteSource,
                new int[] { JPEG_APP13_Marker, }, false);

        if ((segments == null) || (segments.size() < 1))
            return null;

        PhotoshopApp13Data photoshopApp13Data = null;

        for (int i = 0; i < segments.size(); i++)
        {
            App13Segment segment = (App13Segment) segments.get(i);

            PhotoshopApp13Data data = segment.parsePhotoshopSegment(params);
            if (data != null && photoshopApp13Data != null)
                throw new ImageReadException(
                        "Jpeg contains more than one Photoshop App13 segment.");

            photoshopApp13Data = data;
        }

        if(null==photoshopApp13Data)
            return null;
        return new JpegPhotoshopMetadata(photoshopApp13Data);
    }

    @Override
    public Dimension getImageSize(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {
        List<Segment> segments = readSegments(byteSource, new int[] {
                // kJFIFMarker,
                SOF0Marker,

                SOF1Marker, SOF2Marker, SOF3Marker, SOF5Marker, SOF6Marker,
                SOF7Marker, SOF9Marker, SOF10Marker, SOF11Marker, SOF13Marker,
                SOF14Marker, SOF15Marker,

        }, true);

        if ((segments == null) || (segments.size() < 1))
            throw new ImageReadException("No JFIF Data Found.");

        if (segments.size() > 1)
            throw new ImageReadException("Redundant JFIF Data Found.");

        SofnSegment fSOFNSegment = (SofnSegment) segments.get(0);

        return new Dimension(fSOFNSegment.width, fSOFNSegment.height);
    }

    public byte[] embedICCProfile(byte image[], byte profile[])
    {
        return null;
    }

    @Override
    public boolean embedICCProfile(File src, File dst, byte profile[])
    {
        return false;
    }

    @Override
    public ImageInfo getImageInfo(ByteSource byteSource, Map params)
            throws ImageReadException, IOException
    {
        // List allSegments = readSegments(byteSource, null, false);

        List<Segment> SOF_segments = readSegments(byteSource, new int[] {
                // kJFIFMarker,

                SOF0Marker, SOF1Marker, SOF2Marker, SOF3Marker, SOF5Marker,
                SOF6Marker, SOF7Marker, SOF9Marker, SOF10Marker, SOF11Marker,
                SOF13Marker, SOF14Marker, SOF15Marker,

        }, false);

        if (SOF_segments == null)
            throw new ImageReadException("No SOFN Data Found.");

        // if (SOF_segments.size() != 1)
        // System.out.println("Incoherent SOFN Data Found: "
        // + SOF_segments.size());

        List<Segment> jfifSegments = readSegments(byteSource,
                new int[] { JFIFMarker, }, true);

        SofnSegment fSOFNSegment = (SofnSegment) SOF_segments.get(0);
        // SofnSegment fSOFNSegment = (SofnSegment) findSegment(segments,
        // SOFNmarkers);

        if (fSOFNSegment == null)
            throw new ImageReadException("No SOFN Data Found.");

        int Width = fSOFNSegment.width;
        int Height = fSOFNSegment.height;

        JfifSegment jfifSegment = null;

        if ((jfifSegments != null) && (jfifSegments.size() > 0))
            jfifSegment = (JfifSegment) jfifSegments.get(0);

        // JfifSegment fTheJFIFSegment = (JfifSegment) findSegment(segments,
        // kJFIFMarker);

        double x_density = -1.0;
        double y_density = -1.0;
        double units_per_inch = -1.0;
        // int JFIF_major_version;
        // int JFIF_minor_version;
        String FormatDetails;

        if (jfifSegment != null)
        {
            x_density = jfifSegment.xDensity;
            y_density = jfifSegment.yDensity;
            int density_units = jfifSegment.densityUnits;
            // JFIF_major_version = fTheJFIFSegment.JFIF_major_version;
            // JFIF_minor_version = fTheJFIFSegment.JFIF_minor_version;

            FormatDetails = "Jpeg/JFIF v." + jfifSegment.jfifMajorVersion + "."
                    + jfifSegment.jfifMinorVersion;

            switch (density_units)
            {
            case 0:
                break;
            case 1: // inches
                units_per_inch = 1.0;
                break;
            case 2: // cms
                units_per_inch = 2.54;
                break;
            default:
                break;
            }
        } else
        {
            JpegImageMetadata metadata = (JpegImageMetadata) getMetadata(
                    byteSource, params);

            if (metadata != null)
            {
                {
                    TiffField field = metadata
                            .findEXIFValue(TiffTagConstants.TIFF_TAG_XRESOLUTION);
                    if (field != null)
                        x_density = ((Number) field.getValue()).doubleValue();
                }
                {
                    TiffField field = metadata
                            .findEXIFValue(TiffTagConstants.TIFF_TAG_YRESOLUTION);
                    if (field != null)
                        y_density = ((Number) field.getValue()).doubleValue();
                }
                {
                    TiffField field = metadata
                            .findEXIFValue(TiffTagConstants.TIFF_TAG_RESOLUTION_UNIT);
                    if (field != null)
                    {
                        int density_units = ((Number) field.getValue())
                                .intValue();

                        switch (density_units)
                        {
                        case 1:
                            break;
                        case 2: // inches
                            units_per_inch = 1.0;
                            break;
                        case 3: // cms
                            units_per_inch = 2.54;
                            break;
                        default:
                            break;
                        }
                    }

                }
            }

            FormatDetails = "Jpeg/DCM";

        }

        int PhysicalHeightDpi = -1;
        float PhysicalHeightInch = -1;
        int PhysicalWidthDpi = -1;
        float PhysicalWidthInch = -1;

        if (units_per_inch > 0)
        {
            PhysicalWidthDpi = (int) Math.round(x_density * units_per_inch);
            PhysicalWidthInch = (float) (Width / (x_density * units_per_inch));
            PhysicalHeightDpi = (int) Math.round(y_density     * units_per_inch);
            PhysicalHeightInch = (float) (Height / (y_density * units_per_inch));
        }

        List<String> Comments = new ArrayList<String>();
        List<Segment> commentSegments = readSegments(byteSource,
                new int[] { COMMarker }, false);
        for (int i = 0; i < commentSegments.size(); i++)
        {
            ComSegment comSegment = (ComSegment) commentSegments.get(i);
            String comment = "";
            try {
                comment = new String(comSegment.comment, "UTF-8");
            } catch (UnsupportedEncodingException cannotHappen) {
            }
            Comments.add(comment);
        }

        int Number_of_components = fSOFNSegment.numberOfComponents;
        int Precision = fSOFNSegment.precision;

        int BitsPerPixel = Number_of_components * Precision;
        ImageFormat Format = ImageFormat.IMAGE_FORMAT_JPEG;
        String FormatName = "JPEG (Joint Photographic Experts Group) Format";
        String MimeType = "image/jpeg";
        // we ought to count images, but don't yet.
        int NumberOfImages = 1;
        // not accurate ... only reflects first
        boolean isProgressive = fSOFNSegment.marker == SOF2Marker;

        boolean isTransparent = false; // TODO: inaccurate.
        boolean usesPalette = false; // TODO: inaccurate.
        int ColorType;
        if (Number_of_components == 1)
            ColorType = ImageInfo.COLOR_TYPE_BW;
        else if (Number_of_components == 3)
            ColorType = ImageInfo.COLOR_TYPE_RGB;
        else if (Number_of_components == 4)
            ColorType = ImageInfo.COLOR_TYPE_CMYK;
        else
            ColorType = ImageInfo.COLOR_TYPE_UNKNOWN;

        String compressionAlgorithm = ImageInfo.COMPRESSION_ALGORITHM_JPEG;

        ImageInfo result = new ImageInfo(FormatDetails, BitsPerPixel, Comments,
                Format, FormatName, Height, MimeType, NumberOfImages,
                PhysicalHeightDpi, PhysicalHeightInch, PhysicalWidthDpi,
                PhysicalWidthInch, Width, isProgressive, isTransparent,
                usesPalette, ColorType, compressionAlgorithm);

        return result;
    }

    // public ImageInfo getImageInfo(ByteSource byteSource, Map params)
    // throws ImageReadException, IOException
    // {
    //
    // List allSegments = readSegments(byteSource, null, false);
    //
    // final int SOF_MARKERS[] = new int[]{
    // SOF0Marker, SOF1Marker, SOF2Marker, SOF3Marker, SOF5Marker,
    // SOF6Marker, SOF7Marker, SOF9Marker, SOF10Marker, SOF11Marker,
    // SOF13Marker, SOF14Marker, SOF15Marker,
    // };
    //
    // List sofMarkers = new ArrayList();
    // for(int i=0;i<SOF_MARKERS.length;i++)
    // sofMarkers.add(new Integer(SOF_MARKERS[i]));
    // List SOFSegments = filterSegments(allSegments, sofMarkers);
    // if (SOFSegments == null || SOFSegments.size()<1)
    // throw new ImageReadException("No SOFN Data Found.");
    //
    // List jfifMarkers = new ArrayList();
    // jfifMarkers.add(new Integer(JFIFMarker));
    // List jfifSegments = filterSegments(allSegments, jfifMarkers);
    //
    // SofnSegment firstSOFNSegment = (SofnSegment) SOFSegments.get(0);
    //
    // int Width = firstSOFNSegment.width;
    // int Height = firstSOFNSegment.height;
    //
    // JfifSegment jfifSegment = null;
    //
    // if (jfifSegments != null && jfifSegments.size() > 0)
    // jfifSegment = (JfifSegment) jfifSegments.get(0);
    //
    // double x_density = -1.0;
    // double y_density = -1.0;
    // double units_per_inch = -1.0;
    // // int JFIF_major_version;
    // // int JFIF_minor_version;
    // String FormatDetails;
    //
    // if (jfifSegment != null)
    // {
    // x_density = jfifSegment.xDensity;
    // y_density = jfifSegment.yDensity;
    // int density_units = jfifSegment.densityUnits;
    // // JFIF_major_version = fTheJFIFSegment.JFIF_major_version;
    // // JFIF_minor_version = fTheJFIFSegment.JFIF_minor_version;
    //
    // FormatDetails = "Jpeg/JFIF v." + jfifSegment.jfifMajorVersion
    // + "." + jfifSegment.jfifMinorVersion;
    //
    // switch (density_units)
    // {
    // case 0 :
    // break;
    // case 1 : // inches
    // units_per_inch = 1.0;
    // break;
    // case 2 : // cms
    // units_per_inch = 2.54;
    // break;
    // default :
    // break;
    // }
    // }
    // else
    // {
    // JpegImageMetadata metadata = (JpegImageMetadata) getMetadata(byteSource,
    // params);
    //
    // {
    // TiffField field = metadata
    // .findEXIFValue(TiffField.TIFF_TAG_XRESOLUTION);
    // if (field == null)
    // throw new ImageReadException("No XResolution");
    //
    // x_density = ((Number) field.getValue()).doubleValue();
    // }
    // {
    // TiffField field = metadata
    // .findEXIFValue(TiffField.TIFF_TAG_YRESOLUTION);
    // if (field == null)
    // throw new ImageReadException("No YResolution");
    //
    // y_density = ((Number) field.getValue()).doubleValue();
    // }
    // {
    // TiffField field = metadata
    // .findEXIFValue(TiffField.TIFF_TAG_RESOLUTION_UNIT);
    // if (field == null)
    // throw new ImageReadException("No ResolutionUnits");
    //
    // int density_units = ((Number) field.getValue()).intValue();
    //
    // switch (density_units)
    // {
    // case 1 :
    // break;
    // case 2 : // inches
    // units_per_inch = 1.0;
    // break;
    // case 3 : // cms
    // units_per_inch = 2.54;
    // break;
    // default :
    // break;
    // }
    //
    // }
    //
    // FormatDetails = "Jpeg/DCM";
    //
    // }
    //
    // int PhysicalHeightDpi = -1;
    // float PhysicalHeightInch = -1;
    // int PhysicalWidthDpi = -1;
    // float PhysicalWidthInch = -1;
    //
    // if (units_per_inch > 0)
    // {
    // PhysicalWidthDpi = (int) Math.round((double) x_density
    // / units_per_inch);
    // PhysicalWidthInch = (float) ((double) Width / (x_density *
    // units_per_inch));
    // PhysicalHeightDpi = (int) Math.round((double) y_density
    // * units_per_inch);
    // PhysicalHeightInch = (float) ((double) Height / (y_density *
    // units_per_inch));
    // }
    //
    // List Comments = new ArrayList();
    // // TODO: comments...
    //
    // int Number_of_components = firstSOFNSegment.numberOfComponents;
    // int Precision = firstSOFNSegment.precision;
    //
    // int BitsPerPixel = Number_of_components * Precision;
    // ImageFormat Format = ImageFormat.IMAGE_FORMAT_JPEG;
    // String FormatName = "JPEG (Joint Photographic Experts Group) Format";
    // String MimeType = "image/jpeg";
    // // we ought to count images, but don't yet.
    // int NumberOfImages = -1;
    // // not accurate ... only reflects first
    // boolean isProgressive = firstSOFNSegment.marker == SOF2Marker;
    //
    // boolean isTransparent = false; // TODO: inaccurate.
    // boolean usesPalette = false; // TODO: inaccurate.
    // int ColorType;
    // if (Number_of_components == 1)
    // ColorType = ImageInfo.COLOR_TYPE_BW;
    // else if (Number_of_components == 3)
    // ColorType = ImageInfo.COLOR_TYPE_RGB;
    // else if (Number_of_components == 4)
    // ColorType = ImageInfo.COLOR_TYPE_CMYK;
    // else
    // ColorType = ImageInfo.COLOR_TYPE_UNKNOWN;
    //
    // String compressionAlgorithm = ImageInfo.COMPRESSION_ALGORITHM_JPEG;
    //
    // ImageInfo result = new ImageInfo(FormatDetails, BitsPerPixel, Comments,
    // Format, FormatName, Height, MimeType, NumberOfImages,
    // PhysicalHeightDpi, PhysicalHeightInch, PhysicalWidthDpi,
    // PhysicalWidthInch, Width, isProgressive, isTransparent,
    // usesPalette, ColorType, compressionAlgorithm);
    //
    // return result;
    // }

    @Override
    public boolean dumpImageFile(PrintWriter pw, ByteSource byteSource)
            throws ImageReadException, IOException
    {
        pw.println("tiff.dumpImageFile");

        {
            ImageInfo imageInfo = getImageInfo(byteSource);
            if (imageInfo == null)
                return false;

            imageInfo.toString(pw, "");
        }

        pw.println("");

        {
            List<Segment> segments = readSegments(byteSource, null, false);

            if (segments == null)
                throw new ImageReadException("No Segments Found.");

            for (int d = 0; d < segments.size(); d++)
            {

                Segment segment = segments.get(d);

                NumberFormat nf = NumberFormat.getIntegerInstance();
                // this.debugNumber("found, marker: ", marker, 4);
                pw.println(d + ": marker: "
                        + Integer.toHexString(segment.marker) + ", "
                        + segment.getDescription() + " (length: "
                        + nf.format(segment.length) + ")");
                segment.dump(pw);
            }

            pw.println("");
        }

        return true;
    }

}
