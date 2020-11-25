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
package org.apache.commons.imaging.formats.tiff.constants;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.imaging.common.BinaryConstant;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;

public class TagConstantsUtils implements TiffDirectoryConstants
{
    private static final TiffDirectoryType[] tiffDirectoryTypes = TiffDirectoryType.values();

    public static List<TagInfo> mergeTagLists(List<TagInfo>... tagLists)
    {
        int count = 0;
        for (int i = 0; i < tagLists.length; i++) {
            count += tagLists[i].size();
        }

        ArrayList<TagInfo> result = new ArrayList<TagInfo>(count);

        for (int i = 0; i < tagLists.length; i++) {
            for (int j = 0; j < tagLists[i].size(); j++) {
                result.add(tagLists[i].get(j));
            }
        }

        return result;
    }

    public static TiffDirectoryType getExifDirectoryType(int type)
    {
        
        for (int i = 0; i < tiffDirectoryTypes.length; i++)
            if (tiffDirectoryTypes[i].directoryType == type)
                return tiffDirectoryTypes[i];
        return TiffDirectoryType.EXIF_DIRECTORY_UNKNOWN;
    }
    
    public static BinaryConstant createMicrosoftHdPhotoGuidEndingWith(byte end) {
        return new BinaryConstant(new byte[] {
                (byte)0x24, (byte)0xC3, (byte)0xDD, (byte)0x6F,
                (byte)0x03, (byte)0x4E,
                (byte)0xFE, (byte)0x4B,
                (byte)0xB1, (byte)0x85, (byte)0x3D, (byte)0x77, (byte)0x76, (byte)0x8D, (byte)0xC9, end});
    }

}