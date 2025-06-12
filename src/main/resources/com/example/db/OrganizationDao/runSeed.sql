create table if not exists organizations
(
  organization_id serial primary key,
  name            text not null,
  manager_user_id uuid not null,
  created_at      timestamptz default now(),
  updated_at      timestamptz default now()
);

-- First drop the policies (they depend on the table)
DROP POLICY IF EXISTS "Allow updates by managing user in same org" ON organizations;
DROP POLICY IF EXISTS "Allow select by managing user in same org" ON organizations;
DROP POLICY IF EXISTS "Allow insert for self-managing user in own org" ON organizations;

-- Drop the function
DROP FUNCTION IF EXISTS portal_permissions_manage_organizations(uuid);

-- Drop the tables (order matters due to foreign key relationships)
DROP TABLE IF EXISTS organization_memberships;
DROP TABLE IF EXISTS organizations;
DROP TABLE IF EXISTS portal_permissions;
DROP TABLE IF EXISTS admin_permissions;


CREATE TABLE portal_permissions
(
  user_id              uuid    not null,
  manage_organizations boolean not null,
  view_organizations   boolean not null
);

CREATE TABLE admin_permissions
(
  user_id              uuid    not null,
  manage_organizations boolean not null,
  view_organizations   boolean not null
);



create or replace function portal_permissions_manage_organizations(_user_id uuid)
  returns boolean as
$$
select exists(select 1 from portal_permissions
where user_id = auth.uid() and portal_permissions.manage_organizations)
$$ language sql stable;


-- 1. Create the table with manager_user_id
create table if not exists organizations
(
  organization_id serial primary key,
  name            text not null,
  manager_user_id uuid not null,
  created_at      timestamptz default now(),
  updated_at      timestamptz default now()
);

create table organization_memberships
(
  user_id         uuid not null,
  organization_id int  not null,
  primary key (user_id, organization_id)
);


-- 2. Enable RLS
alter table organizations
  enable row level security;


-- 3. Policy: allow update if user manages this org and belongs to it
create policy "Allow updates by managing user in same org"
  on organizations
  for update
  using (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
--         and
    exists(select 1 from portal_permissions
    where user_id = auth.uid() and portal_permissions.manage_organizations)
  );

-- Optional: Select policy (same logic)
create policy "Allow select by managing user in same org"
  on organizations
  for select
  using (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
  manager_user_id = auth.uid()
  );

-- Optional: Insert policy (user can only insert with matching org and self as manager)
create policy "Allow insert for self-managing user in own org"
  on organizations
  for insert
  with check (
--     organization_id = current_setting('request.jwt.claim.organization_id', true)::integer
--         and
  manager_user_id = auth.uid()
  );


insert into organizations (organization_id, name, manager_user_id) values
  (1, 'Acme Corp', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'),  -- Alice
  (2, 'Beta Ltd', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'),   -- Bob
  (3, 'Skunkworks', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'), -- Eve, different manager in same org
  (4, 'Gamma GmbH', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'); -- Alice managing in another org

