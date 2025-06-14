package handler.portal

import db.withPostgresRole
import dbContextKey
import kotlin.use
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK


fun reseedDbHandler(req: Request): Response {

  // Luckily, it is easy to drop down to jdbc.
  val dbctx = dbContextKey(req)
  dbctx.withPostgresRole(req) { pgdbctx ->
    pgdbctx.database.connection.use { jdbc ->
      jdbc.prepareStatement(
        """
create table if not exists Organizations
(
  organizationId serial primary key,
  name           text not null,
  managersUserId uuid not null,
  createdAt      timestamptz default now(),
  updatedAt      timestamptz default now()
);

-- First drop the policies (they depend on the table)
DROP POLICY IF EXISTS "Allow updates by managing user in same org" ON Organizations;
DROP POLICY IF EXISTS "Allow select by managing user in same org" ON Organizations;
DROP POLICY IF EXISTS "Allow insert for self-managing user in own org" ON Organizations;

-- Drop the function
DROP FUNCTION IF EXISTS portal_permissions_manage_organizations(uuid);

-- Drop the tables (order matters due to foreign key relationships)
DROP TABLE IF EXISTS OrganizationMemberships;
DROP TABLE IF EXISTS Organizations;
DROP TABLE IF EXISTS PortalPermissions;
DROP TABLE IF EXISTS AdminPermissions;


CREATE TABLE PortalPermissions
(
  userId              uuid    not null,
  manageOrganizations boolean not null,
  viewOrganizations   boolean not null
);

CREATE TABLE AdminPermissions
(
  userId              uuid    not null,
  manageOrganizations boolean not null,
  viewOrganizations   boolean not null
);



create or replace function portal_permissions_manage_organizations(_userId uuid)
  returns boolean as
$$
select exists(select 1 from PortalPermissions
where _userId = auth.uid() and PortalPermissions.manageOrganizations)
$$ language sql stable;


-- 1. Create the table with managersUserId
create table if not exists Organizations
(
  organizationId serial primary key,
  name           text not null,
  managersUserId uuid not null,
  createdAt      timestamptz default now(),
  updatedAt      timestamptz default now()
);

create table OrganizationMemberships
(
  userId         uuid not null,
  organizationId int  not null,
  primary key (userId, organizationId)
);


-- 2. Enable RLS
alter table Organizations
  enable row level security;


-- 3. Policy: allow update if user manages this org and belongs to it
create policy "Allow updates by managing user in same org"
  on Organizations
  for update
  using (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
--         and
    exists(select 1 from PortalPermissions
    where userId = auth.uid() and PortalPermissions.manageOrganizations)
  );

-- Optional: Select policy (same logic)
create policy "Allow select by managing user in same org"
  on Organizations
  for select
  using (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
    managersUserId = auth.uid()
  );

-- Optional: Insert policy (user can only insert with matching org and self as manager)
create policy "Allow insert for self-managing user in own org"
  on Organizations
  for insert
  with check (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
--         and
    managersUserId = auth.uid()
  );


insert into Organizations (organizationId, Name, managersUserId)
values (1, 'Acme Corp', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),  -- Alice
       (2, 'Beta Ltd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),   -- Bob
       (3, 'Skunkworks', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'), -- Eve, different manager in same org
       (4, 'Gamma GmbH', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'); -- Alice managing in another org
"""
      ).execute()
    }
  }
  return Response(OK).body("reseeded.")
}
