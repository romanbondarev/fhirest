--liquibase formatted sql

--changeset kefhir:resource dbms:postgresql
CREATE SEQUENCE store.resource_key_seq INCREMENT 1 MINVALUE 1;

create table store.resource (
  key           bigint not null default nextval('store.resource_key_seq'),
  type          text not null,
  id            text not null,
  last_version  smallint not null default 1,
  last_updated  timestamptz not null default now(),
  author        jsonb,
  content       jsonb,
  sys_status    char(1) not null default 'A'
) PARTITION BY LIST (type);
--


--changeset kefhir:resource_key_seq  dbms:postgresql
CREATE SEQUENCE store.resource_id_seq INCREMENT 1 MINVALUE 1;
SELECT setval('store.resource_id_seq', nextval('store.resource_key_seq'));
--rollback select 1
