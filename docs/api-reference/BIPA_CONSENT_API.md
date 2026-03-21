# BIPA Consent API

> Mount Path: `/v1/bipa`
> Auth: None (public endpoints)
> Rate Limiting: Global
> Compliance: Illinois BIPA (740 ILCS 14/)

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/disclosures` | Get required BIPA disclosures |
| POST | `/consent` | Record biometric consent with electronic signature |

## GET /disclosures

Returns all 6 BIPA-required disclosures that must be acknowledged before consent.

**Response (200):**
```json
{
  "success": true,
  "disclosures": [
    { "id": 1, "title": "Retention Policy", "reference": "740 ILCS 14/15(a)" },
    { "id": 2, "title": "Purpose of Collection", "reference": "740 ILCS 14/15(b)(1)" }
  ]
}
```

## POST /consent

Record BIPA consent with electronic signature (per SB 2979 amendment).

**Request:**
```json
{
  "user_uuid": "abc-123-def",
  "acknowledged_disclosures": [1, 2, 3, 4, 5, 6],
  "biometric_types": ["FACE", "FINGERPRINT"],
  "electronic_signature": "John Doe"
}
```

**Privacy:**
- Electronic signature hashed (never stored plaintext)
- IP address anonymized before storage
- Valid biometric types: FACE, FINGERPRINT, VOICE
