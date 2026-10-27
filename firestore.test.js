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

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
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

test("unauthenticated read is denied", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users").doc(ALICE_UID).get());
});

test("authenticated user can create their own settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const validSettings = {
    userId: ALICE_UID,
    companionName: "Kayara",
    userName: "Alice",
    relationship: "partner",
    gender: "female",
    languageCode: "en-US",
    autoReadReplies: true,
    model: "gemini-2.5-flash",
  };
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validSettings));
});

test("user cannot create or read another user's settings", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      companionName: "Hacker",
      relationship: "friend",
      gender: "female",
      languageCode: "en-US",
      autoReadReplies: false,
      model: "gemini-2.5-flash",
    })
  );
});

test("user can create and read their own messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const msgDoc = aliceDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg1");
  await assertSucceeds(
    msgDoc.set({
      id: "msg1",
      userId: ALICE_UID,
      role: "user",
      text: "Hello Kayara!",
    })
  );
  await assertSucceeds(msgDoc.get());
});

test("cross-user cannot read another user's messages", async () => {
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const msgDoc = bobDb.collection("users").doc(ALICE_UID).collection("messages").doc("msg1");
  await assertFails(msgDoc.get());
});
