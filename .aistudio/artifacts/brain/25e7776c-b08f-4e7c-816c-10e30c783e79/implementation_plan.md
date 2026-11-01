# Suraksha Chronicle — Permanent Memory & Civic Accountability Hub for Crimes Against Women & Girls in India

**Suraksha Chronicle** is an investigative journalism, permanent incident archive, and civic accountability Android application built so that heinous crimes against girls, teenage students, and women in India are **never forgotten after one or two months**. It combines a **"Save for Life" Permanent Case Archive** with longitudinal case follow-up timelines, a cascading **State → District → City/Constituency selector** that surfaces the area's **MP (Member of Parliament) and MLA (Member of Legislative Assembly)** for public accountability, live and community-reported dispatches, trauma-informed reading controls, and emergency legal/helpline support.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> This revised blueprint directly incorporates your core requirements:

- **"Never Forget" Permanent Incident Archive & Case Follow-Up Tracker (New Core Pillar)**:
  - Instead of treating crimes as disposable daily news that disappears after a few weeks, every incident is structured as a **Permanent Case Dossier ("Saved for Life")** with a live **"Days Since Incident"** counter and a **Step-by-Step Follow-Up Timeline** (*Incident Reported → FIR Filed → Accused Arrested → Chargesheet Status → Court Trial → Verdict / Justice Status*).
  - Features the **Andhra Pradesh Class 10 Classroom Attack** (*where a 21-year-old man breached school security and attacked a Class 10 teenage girl with a mechanical chainsaw inside her classroom*) alongside other major and regional crimes against girls, teenagers, and women across India, tracking ongoing investigation and trial updates month after month.
- **Hierarchical State → District → City Filter + MP & MLA Accountability (New Core Pillar)**:
  - Provides a cascading 3-level location drill-down (**State → District → City / Town**) across India.
  - Every location selection and every incident dossier explicitly displays the **Local MLA (Assembly Constituency)** and **Local MP (Lok Sabha Constituency)** (along with party/office details and unresolved women/girl safety cases in their jurisdiction) so citizens can hold elected representatives accountable when cases stall.
- **Exclusive Focus on Violence & Problems Facing Girls, Teenagers & Women**:
  - Dedicated strictly to crimes and systemic safety failures affecting schoolgirls, teenage students, and women (School/Campus Security Breaches, Acid & Weapon Attacks, POCSO & Minor Assault, Domestic & Dowry Violence, Cyber Blackmail/Stalking, Missing Girls & Trafficking).
- **Retained Safety & Reading Features**:
  - **Save for Life Vault** (cloud-synced via Firebase Firestore + local Room SQLite offline storage so saved incidents and their follow-up updates are preserved permanently).
  - **Sensitive Content Blur Toggle**, **State/District Statistical Breakdown**, and **Emergency Helpline & Legal Rights Guide** (`112`, `1091`, `1098`, `181`, `1930`, Zero FIR, POCSO Act).

---

## 1. Overview & Core Concept

- **What It Does**: Tracks, archives, and continuously updates incidents of violence against girls, teenage students, and women across India—linking each incident to its exact **State, District, and City**, identifying the responsible **MP and MLA**, and maintaining a permanent chronological record from the day of the crime until justice is served.
- **Target Audience / Persona**: Citizens, parents, students, journalists, women's rights advocates, and voters who refuse to let disturbing crimes fade from public memory and want to track both judicial progress and elected representatives' accountability.
- **Key Value**: Solves the "2-month media amnesia" problem by turning fleeting news articles into **permanent, living case files** with follow-up timelines, permanent personal/cloud archiving, and constituency-level MP/MLA visibility.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Cascading Location & MP/MLA Accountability Bar (`State → District → City`)**:
   - At the top of the Front Page and Case Tracker, users can drill down step-by-step:
     1. **Select State** (e.g., *Andhra Pradesh*, *Uttar Pradesh*, *Maharashtra*, *Delhi NCR*, *West Bengal*, *Bihar*, *Rajasthan*, *Tamil Nadu*, *Karnataka*, etc.)
     2. **Select District** (dynamically filtered by the chosen State, e.g., *Anakapalli / Visakhapatnam / Guntur / Chittoor* in Andhra Pradesh)
     3. **Select City / Town** (dynamically filtered by the chosen District)
   - Once a location is selected (or inside any incident card), a dedicated **Constituency Accountability Card** displays:
     - **MLA (Member of Legislative Assembly)**: Name, Assembly Constituency, Party, and number of tracked cases in the constituency.
     - **MP (Member of Parliament)**: Name, Lok Sabha Constituency, Party, and official statement / action status on local women's safety.
2. **Front Page & "Never Forgotten" Case Dossiers (`Front Page & Case Tracker`)**:
   - Highlights **Featured Long-Term Case Files** (including the **Andhra Pradesh Class 10 School Chainsaw Security Breach & Attack**, campus safety breaches, POCSO cases, and unresolved assaults that are 30+, 60+, or 180+ days old).
   - Each card displays:
     - **Days Since Incident Badge** (e.g., `DAY 47 • JUSTICE PENDING` or `CHARGE SHEET FILED`).
     - **Location Breadcrumb**: `State ▸ District ▸ City`.
     - **Constituency Representatives Badge**: `MLA & MP Tagged`.
     - **Case Stage Progress Bar**: Visual indicator showing where the case currently stands (*Reported → FIR → Arrest → Chargesheet → Trial → Verdict*).
     - **Sensitive Content Shield**: Optional blur overlay for highly disturbing descriptions that unblurs on tap.
3. **Longitudinal Case Dossier & Follow-Up Timeline View**:
   - Tapping any incident opens the full **Case Dossier**:
     - **The Original Incident**: Full factual report of what happened (e.g., the Andhra Pradesh Class 10 classroom chainsaw attack on a teenage student, security lapses, victim condition, and immediate police response).
     - **Chronological Follow-Up Updates ("What Happened After 1 Month / 2 Months...")**: Timestamped timeline of every subsequent development—medical updates, police remand, forensic reports, court hearings, bail objections, and public protests.
     - **Add Case Follow-Up Update**: Authenticated users and citizen reporters can submit verified follow-up updates or court progress notes to keep the case file alive.
     - **MP & MLA Accountability Panel**: Shows the exact MLA and MP of that incident's area, whether public action/compensation/security reform was taken, and a quick action to copy/share an accountability summary.
     - **"Save for Life" Permanent Archive Button**: Pins the incident permanently to the user's **Saved for Life Vault** (stored both offline on-device via Room and synced to the user's cloud account via Firebase Firestore) so the user never loses track of the case and automatically sees every new follow-up update.
4. **"Save for Life" Permanent Archive & Log New Incident (`Permanent Vault`)**:
   - Dedicated tab showing all incidents the user has saved permanently, sorted by *"Needs Follow-Up (1+ Months Old)"*, *"Pending Trial"*, or *"By State/District"*.
   - Includes a **"Record & Preserve New Incident"** form where users can permanently log a crime against a girl, teenager, or woman with full **State → District → City**, **Area MP & MLA names**, **Victim Age Group** (*Schoolgirl / Minor*, *Teenager 13–19*, *Woman*), **Incident Details**, and **Initial Case Status**.
5. **Constituency Data Desk & Emergency Legal Support (`Data & Safety`)**:
   - **State & District Breakdown**: Visual comparison of crimes against women and girls, school/campus safety breaches, and case pendency rates across states and districts.
   - **Emergency Helplines & Legal Rights**: One-tap dialer cards for `112` (Emergency), `1098` (Childline for schoolgirls & teenagers), `1091` (Women Helpline), `181` (Domestic/Women Abuse), `1930` (Cyber Crime), and NCW, plus actionable guides on **Zero FIR**, **POCSO Act**, **School/Campus Safety Liability**, and **BNS Sections for Acid/Weapon/Stalking Attacks**.

### Visual Identity & Theme

- **Aesthetic Direction**: **Serious Broadsheet Investigative Journalism & Civic Archive**—high-contrast, dignified, urgent, and built for long-term documentation.
- **Color Palette & Mood**:
  - *Light Mode (Archival Broadsheet)*:
    - Background (`surface`): Archival Newsprint (`#FAF7F2`)
    - Elevated Dossier Surface (`surfaceContainer`): Warm Parchment (`#F2ECE1`)
    - Primary Ink (`onSurface`): Deep Press Charcoal (`#141413`)
    - Primary Accent (`primary`): Urgent Crimson (`#9E1B24`) for "Save for Life" badges, days-elapsed counters, and critical security breaches
    - Secondary Accent (`secondary`): Civic Navy (`#1E3A5F`) for State/District/City selectors and MP/MLA accountability cards
    - Warning / Trigger Accent (`tertiary`): Deep Amber (`#B45309`) for disturbing content notices and stalled-case alerts
  - *Dark Mode (Night Investigation)*:
    - Background (`surface`): Obsidian Ink (`#111214`)
    - Elevated Dossier Surface (`surfaceContainer`): Slate Press (`#1C1E22`)
    - Primary Ink (`onSurface`): Crisp Ivory (`#EDEAE4`)
    - Primary Accent (`primary`): Crimson Signal (`#E04F56`)
- **Typography & Hierarchy**:
  - *Headline & Case Title Font*: `Playfair Display` (bundled locally in `res/font`) for commanding investigative headlines.
  - *Long-Form Dossier & Timeline Font*: `Source Sans 3` / `Merriweather` for clear, high-legibility reading.
  - *Case IDs, Days-Since Counters & MP/MLA Badges*: `JetBrains Mono` for structured archival metadata.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Case-Centric Longitudinal Data Model (Incident + Follow-Up Timeline)**
  - *Chosen Approach*: Every news story is backed by a structured `CaseDossier` containing both the initial crime report and a chronological list of `CaseFollowUpUpdate` entries, plus a `caseStage` (*FIR Registered*, *Investigation Ongoing*, *Chargesheet Filed*, *Trial in Progress*, *Verdict Delivered*, *Stalled / Unresolved*).
  - *Why*: Directly addresses the problem that people forget crimes after 1–2 months; users can see at a glance how many days have passed and what happened months after the initial headline.
- **Decision 2: Cascading State → District → City Hierarchy with Built-In MP & MLA Directory**
  - *Chosen Approach*: Include a structured Indian geographic hierarchy (States → Districts → Cities/Towns) mapped to constituency **MP (Lok Sabha)** and **MLA (Vidhan Sabha)** records, while also allowing users to enter or update MP/MLA names when logging local incidents.
  - *Why*: Gives citizens immediate visibility into which elected leaders represent the exact district and city where a school security breach or assault occurred.
- **Decision 3: Dual "Save for Life" Persistence (Firebase Firestore + Room SQLite)**
  - *Chosen Approach*: Synchronize user-saved permanent case files and community-logged incidents/updates to **Firebase Firestore** (authenticated via Google Sign-In) while storing full offline copies in **Room SQLite**.
  - *Why*: Ensures that an incident marked **"Save for Life"** is never lost—surviving app restarts, offline usage, and device switches.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                         MainActivity (Edge-to-Edge M3)                       │
│         Google Sign-In Gate (CredentialManager) + Broadsheet Theme           │
└──────────────────────────────────────┬───────────────────────────────────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      SurakshaAppShell (Adaptive Scaffold)                    │
│  Masthead + Cascading Location Bar (State ▸ District ▸ City + MP/MLA Card)   │
├──────────────────┬───────────────────┬───────────────────┬───────────────────┤
│ 1. Case Feed     │ 2. Follow-Up Desk │ 3. Saved for Life │ 4. Data & Safety  │
│ (News & Dossiers)│ (1–6+ Mo Tracker) │ (Permanent Vault) │ (NCRB & Helplines)│
└────────┬─────────┴─────────┬─────────┴─────────┬─────────┴─────────┬─────────┘
         │                   │                   │                   │
         ▼                   ▼                   ▼                   ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      SurakshaViewModel (MVVM StateFlow)                      │
│  • Cascading Filter State: selectedState -> selectedDistrict -> selectedCity │
│  • Active Constituency Representative State (MP & MLA Lookup + Case Count)   │
│  • Combined Case Dossiers (Curated Cases + Live Indian RSS + Cloud Cases)    │
│  • Longitudinal Follow-Up Timeline & "Save for Life" Permanent Sync          │
│  • Sensitive Content Blur Controller (Global + Per-Incident Reveal)          │
└────────────────────┬─────────────────────────────────────┬───────────────────┘
                     │                                     │
                     ▼                                     ▼
┌────────────────────────────────────────┐  ┌──────────────────────────────────┐
│      Local & Network Data Layer        │  │    Firebase Cloud Data Layer     │
│  • IndianHierarchyRepository           │  │  • FirebaseAuth (Google Sign-In) │
│    (State ▸ District ▸ City + MP/MLA)  │  │  • Firestore (Named DB ID):      │
│  • Room Database (`SavedCaseEntity`,   │  │    - /cases/{caseId}             │
│    `CaseUpdateEntity` for Offline Life │  │    - /cases/{caseId}/updates/{id}│
│    Vault)                              │  │    - /users/{uid}/saved_cases    │
│  • Live Indian Crime News RSS Parser   │  │                                  │
└────────────────────────────────────────┘  └──────────────────────────────────┘
```

### Data Model & State

1. **Permanent Case Dossier (`CaseDossier` / `SavedCaseEntity`)**:
   - `id`, `title`, `summary`, `fullReport`, `incidentDateEpoch`, `daysElapsed` (computed dynamically), `victimCategory` (*Schoolgirl / Minor*, *Teenage Student (13–19)*, *Young Woman*, *Woman*), `crimeCategory` (*School/Campus Attack*, *Weapon/Acid Attack*, *POCSO / Minor Assault*, *Domestic/Dowry Violence*, *Cyber Harassment*, *Kidnapping/Missing*), `stateName`, `districtName`, `cityName`, `mlaName`, `mlaConstituency`, `mlaParty`, `mpName`, `mpConstituency`, `mpParty`, `policeJurisdiction`, `caseStage` (*FIR_FILED*, *ACCUSED_ARRESTED*, *CHARGESHEET_FILED*, *TRIAL_ONGOING*, *VERDICT_DELIVERED*, *JUSTICE_PENDING*), `sensitivityLevel` (*HIGH_DISTURBANCE*, *SENSITIVE*, *STANDARD*), `triggerWarningText`, `relevantLaws`, `followUpUpdates` (list of chronological updates), and `isSavedForLife`.
2. **Chronological Case Follow-Up (`CaseFollowUpUpdate`)**:
   - `id`, `caseId`, `updateDateLabel` (e.g., *"Day 1 — Incident & FIR"*, *"Month 1 Follow-Up — Forensic & Charge Sheet"*, *"Month 2 Follow-Up — Fast-Track Court Hearing"*), `stageBadge`, `headline`, `details`, `sourceOrAuthor`, `timestamp`.
3. **Geographic & Representative Hierarchy (`IndiaLocationHierarchy`)**:
   - Maps **Indian States** → **Districts** → **Cities/Towns** along with their corresponding **MLA (Assembly Constituency)** and **MP (Lok Sabha Constituency)** details, pre-populated with major districts/cities across Andhra Pradesh (including the Andhra Pradesh Class 10 school attack region), Uttar Pradesh, Maharashtra, Delhi NCR, West Bengal, Bihar, Rajasthan, Tamil Nadu, Karnataka, Madhya Pradesh, Gujarat, Telangana, Kerala, and Odisha, plus support for custom user-entered district/city/MP/MLA records.

### Interactive Component & State Mapping

- **Cascading State → District → City Selector**:
  - Selecting a **State** populates the **District** dropdown/filter row; selecting a **District** populates the **City/Town** selector and immediately updates the **Area MP & MLA Accountability Banner** as well as filtering all incident dossiers to that jurisdiction.
- **"Save for Life" Permanent Action**:
  - Tapping **"Save for Life"** on the Andhra Pradesh Class 10 school attack dossier or any other incident immediately saves the full report, MP/MLA details, and all follow-up timeline entries into the local **Room `saved_cases` table** and syncs it to **Firestore `/users/{uid}/saved_cases/{caseId}`**, displaying a permanent archival badge (`SAVED FOR LIFE • OFFLINE & CLOUD`).
- **Longitudinal Case Follow-Up Submission**:
  - Inside any Case Dossier, tapping **"Add Follow-Up Update"** opens a modal allowing authenticated users to append a new development (e.g., *Chargesheet filed*, *Bail hearing date*, *Protest or MLA/MP response*), persisting to Firestore `/cases/{caseId}/updates` and updating the case's timeline immediately.
- **Record New Incident for Permanent Tracking**:
  - Tapping **"Archive New Incident"** lets users log a disturbing crime affecting girls or women with cascading **State → District → City**, **Area MLA & MP**, **Incident Description**, and **Case Status**, ensuring local cases are permanently recorded and tracked.
