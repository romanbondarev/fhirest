/*
 * MIT License
 *
 * Copyright (c) 2024 FHIRest community
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ee.fhir.fhirest.core.api.resource;

import ee.fhir.fhirest.core.model.search.SearchCriterion;
import ee.fhir.fhirest.core.model.search.SearchResult;

/**
 * Main interface for implementing FHIR resource search.
 * There can be only on implementation for every resource type.
 */
public interface ResourceSearchHandler {
  String DEFAULT = "default";

  /**
   * <p>A FHIR resource type this implementation will only be applied for.</p>
   * <p>Use value "<b>default</b>" to apply this to all other resources</p>
   */
  default String getResourceType() {
    return DEFAULT;
  }

  /**
   * @param criteria Parsed query parameters
   * @return Found resources and meta
   */
  SearchResult search(SearchCriterion criteria);
}
