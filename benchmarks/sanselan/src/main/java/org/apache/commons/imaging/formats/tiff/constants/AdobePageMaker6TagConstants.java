package org.apache.commons.imaging.formats.tiff.constants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoByte;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoLong;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoShort;

/**
 * TIFF specification supplement 1
 * <BR>
 * Enhancements for Adobe PageMaker(R) 6.0 software
 * <BR>
 * http://partners.adobe.com/public/developer/en/tiff/TIFFPM6.pdf
 */
public interface AdobePageMaker6TagConstants extends TiffFieldTypeConstants {
    public static final TagInfoLong TIFF_TAG_SUB_IFD = new TagInfoLong(
            "Sub IFD",  0x014a, -1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN, true);

    public static final TagInfoByte TIFF_TAG_CLIP_PATH = new TagInfoByte(
            "Clip Path",  0x0157, -1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
    
    public static final TagInfoLong TIFF_TAG_XCLIP_PATH_UNITS = new TagInfoLong(
            "XClip Path Units", 0x0158, 1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
    
    public static final TagInfoLong TIFF_TAG_YCLIP_PATH_UNITS = new TagInfoLong(
            "YClip Path Units", 0x0159, 1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);

    public static final TagInfoShort TIFF_TAG_INDEXED = new TagInfoShort(
            "Indexed", 0x015a, 1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
    public static final int INDEXED_VALUE_NOT_INDEXED = 0;
    public static final int INDEXED_VALUE_INDEXED = 1;

    public static final TagInfoShort TIFF_TAG_OPIPROXY = new TagInfoShort(
            "OPIProxy", 0x015f, 1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
    public static final int OPIPROXY_VALUE_HIGHER_RESOLUTION_IMAGE_DOES_NOT_EXIST = 0;
    public static final int OPIPROXY_VALUE_HIGHER_RESOLUTION_IMAGE_EXISTS = 1;

    public static final TagInfoAscii TIFF_TAG_IMAGE_ID = new TagInfoAscii(
            "Image ID", 0x800d, -1,
            TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN);
    
    public static final List<TagInfo> ALL_ADOBE_PAGEMAKER_6_TAGS =
            Collections.unmodifiableList(Arrays.asList(
                    TIFF_TAG_SUB_IFD,
                    TIFF_TAG_CLIP_PATH,
                    TIFF_TAG_XCLIP_PATH_UNITS,
                    TIFF_TAG_YCLIP_PATH_UNITS,
                    TIFF_TAG_INDEXED,
                    TIFF_TAG_OPIPROXY,
                    TIFF_TAG_IMAGE_ID));
}
