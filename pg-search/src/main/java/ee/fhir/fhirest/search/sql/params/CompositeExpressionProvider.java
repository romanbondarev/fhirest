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
package ee.fhir.fhirest.search.sql.params;

import ee.fhir.fhirest.core.exception.FhirException;
import ee.fhir.fhirest.core.exception.FhirestIssue;
import ee.fhir.fhirest.core.model.search.QueryParam;
import ee.fhir.fhirest.core.service.conformance.ConformanceHolder;
import ee.fhir.fhirest.search.sql.ExpressionProvider;
import ee.fhir.fhirest.search.sql.SearchSqlUtil;
import ee.fhir.fhirest.util.sql.SqlBuilder;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r5.model.SearchParameter;

import static java.util.stream.Collectors.toList;

public class CompositeExpressionProvider extends ExpressionProvider {


  @Override
  public SqlBuilder makeExpression(QueryParam param, String alias) {
    SearchParameter sp = ConformanceHolder.getSearchParam(param.getResourceType(), param.getKey());
    List<SqlBuilder> ors = param.getValues().stream().filter(v -> !StringUtils.isEmpty(v)).map(v -> {
      String[] parts = StringUtils.split(v, "$");
      if (parts.length != sp.getComponent().size()) {
        throw new FhirException(FhirestIssue.FEST_030);
      }
      List<SqlBuilder> ands = sp.getComponent().stream().map(c -> {
        String p = parts[sp.getComponent().indexOf(c)];
        String subParamId = StringUtils.substringAfterLast(c.getDefinition(), "/");
        SearchParameter subParam = ConformanceHolder.getSearchParams().get(subParamId);
        // XXX: c.getExpression() is ignored.
        // looks like fhir tries to narrow-down search parameter expressions when using composite params.
        // we can't do that, however, because expressions are done on 'SearchParameter' level
        QueryParam qp = new QueryParam(subParam.getCode(), null, subParam.getType(), param.getResourceType());
        qp.setValues(List.of(p));
        return SearchSqlUtil.condition(qp, alias);
      }).collect(toList());
      return new SqlBuilder().append(ands, "AND");
    }).collect(toList());
    return new SqlBuilder().append(ors, "OR");
  }

  @Override
  public SqlBuilder order(String resourceType, String key, String alias, String direction) {
    throw new FhirException(FhirestIssue.FEST_031);
  }
}
