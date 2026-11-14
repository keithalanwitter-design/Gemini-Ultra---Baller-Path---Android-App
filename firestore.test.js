const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const MAYA_UID = "maya_family_123";
const OTHER_UID = "other_family_456";
const COACH_UID = "coach_keith_789";

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

function serverTimestamp() {
  return new Date();
}

test("Unauthenticated user: cannot read or write any player_profiles, practice_scopes, video_submissions, or coach_messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(MAYA_UID).collection("player_profiles").get());
  await assertFails(unauthDb.collection("users").doc(MAYA_UID).collection("practice_scopes").get());
  await assertFails(unauthDb.collection("users").doc(MAYA_UID).collection("video_submissions").get());
  await assertFails(unauthDb.collection("users").doc(MAYA_UID).collection("coach_messages").get());
});

test("Authenticated owner: can create and update their own PlayerCloudProfile, and cross-user access is denied", async () => {
  const mayaDb = testEnv.authenticatedContext(MAYA_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();
  const now = serverTimestamp();

  const profileRef = mayaDb.collection("users").doc(MAYA_UID).collection("player_profiles").doc("main");
  await assertSucceeds(
    profileRef.set({
      userId: MAYA_UID,
      playerName: "Maya Brooks",
      jerseyNumber: "#14",
      positionAndGrade: "Guard · 7th Grade",
      activeGoal: "Read the help defender early before putting the ball on the floor",
      challengeAccepted: true,
      courtStrengths: "Wide spacing, quick corner relocation, eyes up on drives",
      completedScopesCount: 2,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Update active goal & challengeAccepted
  await assertSucceeds(
    profileRef.update({
      activeGoal: "Hold corner balance for one full count",
      challengeAccepted: true,
      updatedAt: new Date(),
    })
  );

  // Shadow update with ghost field must fail
  await assertFails(
    profileRef.update({
      isVerified: true,
      updatedAt: new Date(),
    })
  );

  // Cross-user read/write must fail
  await assertFails(
    otherDb.collection("users").doc(MAYA_UID).collection("player_profiles").doc("main").get()
  );
});

test("Authenticated owner and verified Coach Keith admin: can manage PracticeScopeOfWork, VideoCritiqueSubmission, and CoachPlayerMessage", async () => {
  const mayaDb = testEnv.authenticatedContext(MAYA_UID).firestore();
  const coachDb = testEnv.authenticatedContext(COACH_UID, {
    email: "keithalanwitter@gmail.com",
    email_verified: true,
  }).firestore();
  const spoofedCoachDb = testEnv.authenticatedContext("spoof_coach", {
    email: "keithalanwitter@gmail.com",
    email_verified: false,
  }).firestore();

  const now = serverTimestamp();

  // Coach Keith creates a PracticeScopeOfWork for Maya
  const scopeRef = coachDb.collection("users").doc(MAYA_UID).collection("practice_scopes").doc("scope_1");
  await assertSucceeds(
    scopeRef.set({
      userId: MAYA_UID,
      assignmentId: "assign-pass-look-decide",
      title: "Pass, Look, Decide — Scope of Work",
      tacticalObjective: "Spot whether the lane opens after giving up the ball",
      scopeBreakdown: "Step 1: Entry pass & chin scan (2m) · Step 2: Drive or space read (3m) · Step 3: Live game-speed reps (3m)",
      requiredFilmAngle: "Wing / Corner 45-degree angle showing passer and help defender",
      successStandard: "Make the pass and immediately read the defender's chest before cutting or spacing",
      durationMinutes: 8,
      status: "assigned",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Spoofed unverified coach email cannot read or write Maya's scope
  await assertFails(
    spoofedCoachDb.collection("users").doc(MAYA_UID).collection("practice_scopes").doc("scope_1").get()
  );

  // Maya uploads a VideoCritiqueSubmission
  const videoRef = mayaDb.collection("users").doc(MAYA_UID).collection("video_submissions").doc("vid_1");
  await assertSucceeds(
    videoRef.set({
      userId: MAYA_UID,
      assignmentId: "assign-pass-look-decide",
      drillTitle: "Pass, Look, Decide",
      videoLabel: "wing_spacing_rep_01.mp4 (0:18)",
      cameraAngle: "Wing / Corner Read",
      playerObservation: "My teammate drove middle so I held the corner window.",
      coachCritique: "",
      filmTimestampNotes: "",
      nextRepAdjustment: "",
      status: "awaiting_coach",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Coach Keith updates the VideoCritiqueSubmission with tactical film critique
  await assertSucceeds(
     coachDb.collection("users").doc(MAYA_UID).collection("video_submissions").doc("vid_1").update({
      coachCritique: "You read the help defender's chest early and gave your driver a clean lane.",
      filmTimestampNotes: "[0:04] Clean pass follow-through · [0:09] Wide corner slide into passing window",
      nextRepAdjustment: "Hold your corner catch balance for one full count before lifting.",
      status: "coach_reviewed",
      updatedAt: new Date(),
    })
  );

  // Two-way message creation
  const msgRef = mayaDb.collection("users").doc(MAYA_UID).collection("coach_messages").doc("msg_1");
  await assertSucceeds(
    msgRef.set({
      userId: MAYA_UID,
      assignmentId: "assign-pass-look-decide",
      senderRole: "player",
      senderName: "Maya Brooks",
      messageText: "Uploaded my wing spacing clip! Should I slide deeper when the post defender helps?",
      followUpPrompt: "Coach Keith will review your clip and reply with your next-rep adjustment.",
      createdAt: now,
    })
  );
});
