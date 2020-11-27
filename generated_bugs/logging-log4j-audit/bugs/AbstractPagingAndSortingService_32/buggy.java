/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.logging.log4j.catalog.jpa.service;

import java.util.Locale;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class AbstractPagingAndSortingService {

protected org.springframework.data.domain.Pageable createPageRequest(int startPage, int itemsPerPage, java.lang.String sortColumn, java.lang.String direction) {
    org.springframework.data.domain.PageRequest pageRequest;
    if ((sortColumn == null) || (sortColumn.length() == 0)) {
        pageRequest = new org.springframework.data.domain.PageRequest(startPage, itemsPerPage);
    } else {
        org.springframework.data.domain.Sort.Direction sortDirection;
        {
            sortDirection = org.springframework.data.domain.Sort.Direction.fromStringOrNull(/* NPEX_NULL_EXP */
            direction.toUpperCase(java.util.Locale.US));
            if (sortDirection == null) {
                sortDirection = org.springframework.data.domain.Sort.Direction.ASC;
            }
        }
        pageRequest = new org.springframework.data.domain.PageRequest(startPage, itemsPerPage, new org.springframework.data.domain.Sort(sortDirection, sortColumn));
    }
    return pageRequest;
}
}
