/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.apache.commons.imaging.formats.wbmp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Sanselan;
import org.apache.commons.imaging.common.IImageMetadata;
import org.apache.commons.imaging.util.Debug;

public class WbmpReadTest extends WbmpBaseTest
{

    public void test() throws Exception
    {
        Debug.debug("start");

        List images = getWbmpImages();
        for (int i = 0; i < images.size(); i++)
        {
            if (i % 10 == 0)
                Debug.purgeMemory();

            File imageFile = (File) images.get(i);
            Debug.debug("imageFile", imageFile);

            IImageMetadata metadata = Sanselan.getMetadata(imageFile);
            // assertNotNull(metadata);

            Map params = new HashMap();
            ImageInfo imageInfo = Sanselan.getImageInfo(imageFile, params);
            assertNotNull(imageInfo);

            BufferedImage image = Sanselan.getBufferedImage(imageFile);
            assertNotNull(image);
        }
    }

}
