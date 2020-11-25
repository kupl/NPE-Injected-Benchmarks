<!---
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--->
# Release notes

## Version 3.0.3 (pending)
- [PDFBOX-4472](https://issues.apache.org/jira/browse/PDFBOX-4472): Thread stuck in SoftReferenceCache.get
- [PDFBOX-4598](https://issues.apache.org/jira/browse/PDFBOX-4598): oversized jbig2 decoded result that causing unnecessary operation


## Version 3.0.2 (2018-09-25)
- [PDFBOX-4290](https://issues.apache.org/jira/browse/PDFBOX-4290): Memory Leak in SoftReferenceCache

## Version 3.0.1 (2018-05-17)
- [PDFBOX-4211](https://issues.apache.org/jira/browse/PDFBOX-4211): Some text is missing in JBIG2 images
- [PDFBOX-4142](https://issues.apache.org/jira/browse/PDFBOX-4142): Don't use md5 checksum due to changes to the release distribuition policy

## Version 3.0.0 (2018-02-27)
- [PDFBOX-4014](https://issues.apache.org/jira/browse/PDFBOX-4014): Malformed/pathological/malicious input can lead to infinite looping
- [PDFBOX-4065](https://issues.apache.org/jira/browse/PDFBOX-4065): Set JBIG2 plugin to jdk6
- [Issue #30](https://github.com/levigo/jbig2-imageio/issues/30): Transition project to Apache PDFBox.
- [Issue #25](https://github.com/levigo/jbig2-imageio/issues/25): Build tests fail if project path has a space.
- [Issue #26](https://github.com/levigo/jbig2-imageio/issues/26): Huffman user tables in text regions.
- [Issue #27](https://github.com/levigo/jbig2-imageio/issues/27): Problems in standard Huffman tables.
- [Issue #32](https://github.com/levigo/jbig2-imageio/issues/32): Newlines printed to stdout.

## Version 2.0.0 (2017-06-02)
- Java 9 support.
- [Issue #10](https://github.com/levigo/jbig2-imageio/issues/10): Use general service loading instead of Image I/O specific. See [merge request #18](https://github.com/levigo/jbig2-imageio/pull/18).
- [Issue #11](https://github.com/levigo/jbig2-imageio/issues/11): Remove @author info from javadocs.
- [Issue #16](https://github.com/levigo/jbig2-imageio/issues/16): Replace deprecated ```STANDARD_INPUT_TYPE``` constant with self-created array.
- [Issue #21](https://github.com/levigo/jbig2-imageio/issues/21): Move repetition of previous code length into correct scope. See [merge request #22](https://github.com/levigo/jbig2-imageio/pull/22). 

## Version 1.6.5 (2015-12-29)
- Issue #5: Deployment to maven central

## Version 1.6.3 (2014-08-04)
- Issue #1: Discard the request of copying the row above the first row.

## Version 1.6.2-RC1 (2014-01-22)
- Googlecode Issue 17: Changed computation of result dimension from ceiling to rounding. 

##Version 1.6.1 (2013-04-18)
- Googlecode Issue 11: Added support for `GRREFERENCE` Release notes

## Version 1.6.2-RC1 (2014-01-22)
- Googlecode Issue 17: Changed computation of result dimension from ceiling to rounding. 

## Version 1.6.1 (2013-04-18)
- Googlecode Issue 11: Added support for `GRREFERENCEDX` in generic refinement region coding. 

## Version 1.6.0 (2013-03-25):
- Googlecode Issue 10: Usability of `CacheFactory` and `LoggerFactory` improved.

## Version 1.5.2 (2012-10-09):
- Googlecode Issue  9: Transfer of bitmap's data into target raster went wrong if bitmap's line ends with a padded byte. 

## Version 1.5.1 (2012-10-02):
- Googlecode Issue  8: The default read parameters changed. There will be no source region, no source render size (no scaling) 
  and subsampling factors of 1 (no subsampling). `Bitmaps.java` can handle this correctly.

## Version 1.5.0 (2012-09-20):
- Moved Exception-classes to dedicated package `com.levigo.jbig2.err`.
- Introduced a new utility class `com.levigo.jbig2.image.Bitmaps`. This class provides a bunch of new features operating 
  on a `Bitmap` instance. For example, extracting a region of interest, scaling with high-quality filters and subsampling
  in either or both horizontal and vertical direction.

## Version 1.4.1 (2012-04-20):
- Fixed race condition when parsing documents with multiple pages.

## Version 1.4 (2012-04-10):
- Googlecode Issue  6 : The returned bitmap was too small in case of only one region. Solution is to check if we have only one
  region that forms the complete page. Only if width and height of region equals width and height of page use region's
  bitmap as the page's bitmap. 
- Googlecode Issue  5: A raster which was too small was created. AWT has thrown a `RasterFormatException`.  
- Googlecode Issue  4: `IndexOutOfBoundsException` indicates the end of the stream in `JBIG2Document#reachedEndOfStream()`
- Googlecode Issue  3: Reader recognizes if a file header is present or not.

## Version 1.3 (2011-10-28):
- Untracked Googlecode Issue : Fixed inverted color model for grayscale images.
- Untracked Googlecode Issue : Fixed `IndexArrayOutOfBoundException` in handling requests with region of interests. The region of
  interest is clipped at image boundary.

## Version 1.2 (2011-10-06):
- Googlecode Issue  1: The default read parameters will return a default image size of 1x1 without claiming the missing input.
- Untracked Googlecode Issue : A black pixel was represented by 1 and a white pixel by 0. For work with image masks the
  convention says, a black pixel is the minimum and the white pixel is maximum. This corresponds to an additive
  colorspace. We turned the representation of white and black pixels for conformity.

## Version 1.1 (2010-12-13):
- raster creation optimized
- potential NPE in cache 

## Version 1.0 (2010-07-29):
- open-source'd  DX in generic refinement region coding. 
