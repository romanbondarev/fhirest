CREATE OR REPLACE FUNCTION search.extract_quantity(_type text, _content jsonb, _path text)
RETURNS TABLE (range numrange, system text, code text, unit text)
AS $$
  WITH paths as (select p.element, (p.path->>'lower')::text[] lower, (p.path->>'upper')::text[] upper, (p.path->>'system')::text[] sys_path, (p.path->>'code')::text[] code_path, (p.path->>'unit')::text[] unit_path FROM search.subpaths(_type, _path, 'quantity') p),
       data  as (SELECT paths.*, unnest(search.jsonpath(_content, paths.element)) d from paths)
  SELECT numrange(coalesce((data.d#>>lower)::numeric, '-infinity') , coalesce((data.d#>>upper)::numeric, 'infinity'), '[]'),
         (data.d#>>sys_path)::text, (data.d#>>code_path)::text, (data.d#>>unit_path)::text
      FROM data WHERE data.d is not null
$$ LANGUAGE SQL IMMUTABLE;
