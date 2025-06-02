SET LOCAL request.jwt.claim.sub = :userUuid;
SET LOCAL request.jwt.claim.email = :userEmail;
SET LOCAL request.jwt.claim.org = :orgId;
SET LOCAL request.jwt.claim.iss = :issuer;
SET LOCAL request.jwt.claim.iat = :issuedAt;
SET LOCAL request.jwt.claim.role = 'authenticated';
SET LOCAL role = 'authenticated';

