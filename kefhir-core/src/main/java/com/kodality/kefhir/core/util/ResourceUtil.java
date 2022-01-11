/* Licensed under the Apache License, Version 2.0 (the "License");
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
 package com.kodality.kefhir.core.util;

import com.kodality.kefhir.core.model.VersionId;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

public final class ResourceUtil {

  private static final String HISTORY = "_history";

  private ResourceUtil() {
    // no init
  }

  public static VersionId parseReference(String uri) {
    if (StringUtils.isEmpty(uri) || uri.startsWith("#")) {
      return null;
    }
    String[] tokens = StringUtils.split(uri, "/");
    VersionId id = new VersionId(tokens[0]);
    if (tokens.length > 1) {
      id.setResourceId(tokens[1]);
    }
    if (tokens.length > 3) {
      Validate.isTrue(HISTORY.equals(tokens[2]));
      id.setVersion(Integer.valueOf(tokens[3]));
    }
    return id;
  }

}
