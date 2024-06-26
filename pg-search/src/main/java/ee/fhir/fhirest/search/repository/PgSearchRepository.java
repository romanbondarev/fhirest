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
package ee.fhir.fhirest.search.repository;

import ee.fhir.fhirest.core.model.ResourceId;
import ee.fhir.fhirest.core.model.search.QueryParam;
import ee.fhir.fhirest.core.model.search.SearchCriterion;
import ee.fhir.fhirest.search.api.PgResourceSearchFilter;
import ee.fhir.fhirest.search.sql.SearchSqlUtil;
import ee.fhir.fhirest.util.sql.SqlBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PgSearchRepository {
  @Inject
  @Named("searchAppJdbcTemplate")
  private JdbcTemplate jdbcTemplate;
  @Inject
  private Optional<PgResourceSearchFilter> pgResourceSearchFilter;

  public Integer count(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder("SELECT count(1) FROM search.resource base ");
    sb.append(joins(criteria));
    sb.append(" WHERE base.resource_type = ?", ResourceStructureRepository.getTypeId(criteria.getType()));
    sb.append(criteria(criteria));
    pgResourceSearchFilter.ifPresent(f -> f.filter(sb, "base"));
    log.debug(sb.getPretty());
    return jdbcTemplate.queryForObject(sb.getSql(), Integer.class, sb.getParams());
  }

  public List<ResourceId> search(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder("SELECT base.resource_id, rt.type resource_type FROM search.resource base ");
    sb.append(" INNER JOIN search.resource_type rt on rt.id = base.resource_type ");
    sb.append(joins(criteria));
    sb.append(" WHERE base.resource_type = ?", ResourceStructureRepository.getTypeId(criteria.getType()));
    sb.append(criteria(criteria));
    pgResourceSearchFilter.ifPresent(f -> f.filter(sb, "base"));
    sb.append(order(criteria));
    sb.append(limit(criteria));
    log.debug(sb.getPretty());
    return jdbcTemplate.query(sb.getSql(), (rs, x) -> new ResourceId(rs.getString("resource_type"), rs.getString("resource_id")), sb.getParams());
  }

  private SqlBuilder limit(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    Integer limit = criteria.getCount();
    Integer page = criteria.getPage();
    Integer offset = limit * (page - 1);
    return sb.append(" LIMIT ? OFFSET ?", limit, offset);
  }

  private SqlBuilder joins(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    sb.append(SearchSqlUtil.chain(criteria.getChains(), "base"));
    return sb;
  }

  private SqlBuilder criteria(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    for (QueryParam param : criteria.getConditions()) {
      SqlBuilder peanut = SearchSqlUtil.condition(param, "base");
      if (peanut != null) {
        sb.and("(").append(peanut).append(")");
      }
    }
    sb.and("base.active = true");
    return sb;
  }

  private SqlBuilder order(SearchCriterion criteria) {
    SqlBuilder sb = new SqlBuilder();
    boolean first = true;
    for (QueryParam param : criteria.getResultParams(SearchCriterion._SORT)) {
      SqlBuilder sql = SearchSqlUtil.order(param, "base");
      if (sql == null) {
        continue;
      }
      sb.append(first ? " ORDER BY " : ",");
      sb.append(sql);
      first = false;
    }
    return sb;
  }

}
