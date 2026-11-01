# Security Specification — Suraksha Chronicle

## 1. Data Invariants
1. **Strict User Ownership & Isolation**:
   - Every document in `/users/{userId}/saved_cases/{caseId}` and `/users/{userId}/case_updates/{updateId}` belongs strictly to `userId`.
   - Only an authenticated user where `request.auth != null && request.auth.uid == userId` may `get`, `list`, `create`, `update`, or `delete` documents under `/users/{userId}/...`.
   - `incoming().userId` must strictly equal `request.auth.uid` and `userId`.
2. **Schema & Boundary Integrity**:
   - All document IDs (`userId`, `caseId`, `updateId`) must match `^[a-zA-Z0-9_\-]+$` and be `<= 128` characters.
   - All string fields must satisfy explicit minimum and maximum `.size()` bounds.
   - `createdAt` and `updatedAt` must be valid Firestore `timestamp` values `<= request.time`.
   - On `update`, `userId` and `createdAt` are strictly immutable (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Read**: `unauthDb.collection("users/alice/saved_cases").get()` -> Reject (`PERMISSION_DENIED`).
2. **Cross-User Read**: Bob (`uid = bob`) reading `users/alice/saved_cases/case_1` -> Reject (`PERMISSION_DENIED`).
3. **Identity Spoofing on Create**: Alice creating `users/alice/saved_cases/case_1` with `userId: "bob"` -> Reject (`PERMISSION_DENIED`).
4. **Shadow/Ghost Field Injection on Create**: Creating `saved_cases` with extra field `isAdmin: true` -> Reject via `.keys().hasOnly(...)`.
5. **Shadow/Ghost Field Injection on Update**: Updating `saved_cases` with extra field `verified: true` -> Reject via `affectedKeys().hasOnly(...)`.
6. **Immutable `createdAt` Tampering**: Updating `saved_cases` with a modified `createdAt` timestamp -> Reject.
7. **Immutable `userId` Tampering**: Updating `saved_cases` to transfer `userId` to another user -> Reject.
8. **Oversized String Resource Poisoning**: Creating `saved_cases` with a 500-char `title` (limit 300) -> Reject.
9. **Empty Required String**: Creating `saved_cases` with `stateName: ""` -> Reject.
10. **Future Timestamp Spoofing**: Creating `saved_cases` with `createdAt` set in the future (`> request.time`) -> Reject.
11. **Malformed Document ID Poisoning**: Creating document with ID exceeding 128 chars or containing invalid symbols -> Reject via `isValidId()`.
12. **Cross-User Follow-Up Injection**: Bob attempting to write to `users/alice/case_updates/upd_1` -> Reject.
