const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const EDITOR_UID = "editor_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function buildValidSavedCase(userId, now) {
  return {
    userId,
    title: "Andhra Pradesh Class 10 Classroom Chainsaw Attack",
    summary: "A 21-year-old man breached school security and attacked a Class 10 girl student inside her classroom.",
    fullReport: "Detailed permanent investigative record of the Andhra Pradesh school security breach and attack on a Class 10 student.",
    stateName: "Andhra Pradesh",
    districtName: "Anakapalli",
    cityName: "Elamanchili",
    mlaName: "Sundarapu Vijay Kumar (Elamanchili)",
    mpName: "C. M. Ramesh (Anakapalli)",
    victimCategory: "Teenage Student (13-19)",
    crimeCategory: "School/Campus Attack",
    caseStage: "CHARGESHEET_FILED",
    incidentDateLabel: "14 Aug 2026",
    personalNotes: "Monitor fast-track POCSO and attempt-to-murder trial progress.",
    imageUrl: "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Andhra_Pradesh_High_Court.jpg/960px-Andhra_Pradesh_High_Court.jpg",
    videoUrl: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    mediaCaption: "Verified incident photo & video broadcast (POCSO Section 23 compliant).",
    createdAt: now,
    updatedAt: now,
  };
}

function buildValidPublicDispatch(userId, now) {
  const payload = buildValidSavedCase(userId, now);
  delete payload.personalNotes;
  return {
    ...payload,
    visibility: "PUBLIC",
  };
}

function buildValidFollowUp(userId, now) {
  return {
    userId,
    caseId: "case_ap_school_01",
    caseTitle: "Andhra Pradesh Class 10 Classroom Chainsaw Attack",
    updateDateLabel: "Month 2 Follow-Up",
    stageBadge: "CHARGESHEET_FILED",
    headline: "Forensic report submitted and school security audit ordered",
    details: "Police filed the forensic examination report and local representatives were questioned on campus safety protocols.",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user cannot read or create saved_cases", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("saved_cases").get()
  );
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_cases")
      .doc("case_1")
      .set(buildValidSavedCase(ALICE_UID, new Date()))
  );
});

test("Authenticated owner can create, read, list, update, and delete their own saved_case", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("saved_cases")
    .doc("case_ap_01");

  const now = new Date();
  await assertSucceeds(docRef.set(buildValidSavedCase(ALICE_UID, now)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("saved_cases").get()
  );
  await assertSucceeds(
    docRef.update({
      caseStage: "TRIAL_ONGOING",
      personalNotes: "Trial hearing scheduled next week.",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Cross-user access to saved_cases is rejected", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_cases")
      .doc("case_ap_01")
      .set(buildValidSavedCase(ALICE_UID, now));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("saved_cases")
      .doc("case_ap_01")
      .get()
  );
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("saved_cases").get()
  );
});

test("Shadow field injection and userId spoofing are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("saved_cases")
    .doc("case_spoof");

  const now = new Date();
  // Spoofed userId
  await assertFails(docRef.set({ ...buildValidSavedCase(BOB_UID, now) }));
  // Ghost field injection
  await assertFails(
    docRef.set({ ...buildValidSavedCase(ALICE_UID, now), isAdmin: true })
  );
});

test("Authenticated owner can create and list case_updates while cross-user is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const updRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("case_updates")
    .doc("upd_01");

  const now = new Date();
  await assertSucceeds(updRef.set(buildValidFollowUp(ALICE_UID, now)));
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("case_updates").get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("case_updates")
      .doc("upd_01")
      .get()
  );
});

test("Authenticated owner can create and read correction_requests while cross-user is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const corrRef = aliceDb
    .collection("users")
    .doc(ALICE_UID)
    .collection("correction_requests")
    .doc("corr_01");

  const now = new Date();
  await assertSucceeds(
    corrRef.set({
      userId: ALICE_UID,
      caseId: "case_ap_school_chainsaw_01",
      issueType: "Court / Stage Update",
      details: "High court hearing adjourned to next month per official cause list.",
      status: "PENDING_EDITORIAL_REVIEW",
      createdAt: now,
      updatedAt: now,
    })
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("correction_requests").get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("correction_requests")
      .doc("corr_01")
      .get()
  );
});

test("Public dispatches: regular users CANNOT publish directly, only verified editors can publish without personalNotes", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const editorDb = testEnv.authenticatedContext(EDITOR_UID, { editor: true }).firestore();
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date();

  // Regular authenticated client (Alice) is DENIED direct write to /public_dispatches
  await assertFails(
    aliceDb.collection("public_dispatches").doc("case_unauth_pub").set(
      buildValidPublicDispatch(ALICE_UID, now)
    )
  );

  // Even a verified editor is DENIED if personalNotes is included in a public dispatch
  await assertFails(
    editorDb.collection("public_dispatches").doc("case_leak_notes").set({
      ...buildValidPublicDispatch(EDITOR_UID, now),
      personalNotes: "Private survivor note that must never be public",
    })
  );

  // Verified editor CAN publish a clean public dispatch without personalNotes
  const editorDispatchRef = editorDb.collection("public_dispatches").doc("case_public_01");
  await assertSucceeds(
    editorDispatchRef.set(buildValidPublicDispatch(EDITOR_UID, now))
  );

  // Bob and Alice can read and list PUBLIC dispatches
  await assertSucceeds(
    bobDb
      .collection("public_dispatches")
      .where("visibility", "==", "PUBLIC")
      .get()
  );
  await assertSucceeds(
    bobDb.collection("public_dispatches").doc("case_public_01").get()
  );

  // Bob cannot modify or delete the public dispatch
  await assertFails(
    bobDb.collection("public_dispatches").doc("case_public_01").update({
      title: "Tampered headline by Bob",
      updatedAt: new Date(),
    })
  );
  await assertFails(
    bobDb.collection("public_dispatches").doc("case_public_01").delete()
  );

  // Unauthenticated client cannot read or write public_dispatches
  await assertFails(
    unauthDb
      .collection("public_dispatches")
      .where("visibility", "==", "PUBLIC")
      .get()
  );
});
