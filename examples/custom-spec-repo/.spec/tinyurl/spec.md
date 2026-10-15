---
status: approved
---
# tinyurl

## Problem
No way to turn long URLs into short, shareable links within this repo.

## Goal
A Spring Boot backend API service that shortens a long URL into a 6-character code and redirects that code back to the original URL.

## Non-goals
- Custom aliases
- Analytics / click tracking
- Auth / rate limiting
- Persistence across restarts (in-memory store only for v1)
- Expiration of short links

## Acceptance criteria
- `POST /api/shorten` with JSON body `{ "url": "<long url>" }` returns `201` with JSON `{ "shortCode": "<6-char base62>", "shortUrl": "<base>/<code>" }`.
- `POST /api/shorten` with a malformed/invalid URL (not a valid absolute HTTP/HTTPS URL) returns `400` with a JSON error body.
- `GET /{shortCode}` for a known code responds `302 Found` with `Location` header set to the original long URL.
- `GET /{shortCode}` for an unknown code returns `404` with a JSON error body.
- Short codes are 6 characters, base62 (`[A-Za-z0-9]`), generated randomly; on collision with an existing stored code, regenerate until unique.
- Shortening the same long URL twice produces two independent short codes (no dedup requirement for v1).
- Data is stored in-memory only; restarting the service clears all mappings.
