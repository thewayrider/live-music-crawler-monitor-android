# Live Music Crawler Monitor — Android (F-Droid Ready)

A modern, privacy-respecting, open-source Android application (built with Jetpack Compose & Material 3) that provides a live health and performance dashboard for the 10 music crawlers in the `live-music-search-agent` pipeline.

---

## Features

- **Live Telemetry & Diagnostics**: Real-time status, schedules, and last run times across all 10 music crawlers.
- **Discovery Metrics**: Daily, past 7 days, and all-time new song discoveries and run counts.
- **Actionable Health Alerts**: Automated warning banners flagging non-performing crawlers (e.g. crawlers with consecutive zero-discovery streaks) so you know which crawlers to modify or eliminate.
- **GitHub Gist Cloud Bridge**: Seamless, conflict-free sync from your always-on Mini PC to your mobile device via a lightweight GitHub Gist payload.
- **100% F-Droid & FOSS Compliant**: Built strictly with open-source AndroidX and Jetpack Compose libraries. Zero Google Play Services, zero proprietary trackers, zero analytics blobs.

---

## Architecture Overview

```
[ Always-On Mini PC ]
   │
   ├─ Scheduled runs execute (Bandcamp, Triple J, ListenBrainz, etc.)
   ├─ metricsAggregator.js computes daily/weekly/total stats
   └─ gistSync.js uploads crawler_metrics.json to your GitHub Gist
         │
         ▼
[ GitHub Gist (Cloud Telemetry) ]
         │
         ▼
[ Android Phone / F-Droid App ]
   └─ Fetches JSON via GistApiClient and renders live status & alerts
```

---

## Setup & Configuration

### 1. Create your GitHub Personal Access Token (PAT)
1. Go to [GitHub Settings → Developer Settings → Personal Access Tokens](https://github.com/settings/tokens?type=beta).
2. Generate a fine-grained token with:
   - **Gists**: Read and Write permissions.

### 2. Create the Telemetry Gist
1. Go to [gist.github.com](https://gist.github.com).
2. Create a Gist with a file named `crawler_metrics.json` and paste `{}` as initial content.
3. Save the Gist and copy the alphanumeric Gist ID from the URL (e.g., `https://gist.github.com/thewayrider/8f4a2b9...` → ID is `8f4a2b9...`).

### 3. Configure the Crawler (`live-music-search-agent`)
Add the following keys to `configs/secrets.json` on both your desktop and Mini PC:
```json
{
  "githubGistId": "your_gist_id_here",
  "githubToken": "your_personal_access_token_here"
}
```
*Note: `configs/secrets.json` is already gitignored and will never be committed to your repository.*

### 4. Configure the Android App
Open the app, tap the **Settings (⚙)** icon in the top right, and enter your Gist ID.

---

## Building the APK

### Build Requirements
- Android SDK 35
- JDK 17+
- Gradle 8.5+

### Build Commands
```bash
# Debug APK
./gradlew assembleDebug

# Release APK (F-Droid eligible)
./gradlew assembleRelease
```
The output APK will be located at:
`app/build/outputs/apk/release/app-release-unsigned.apk`

---

## F-Droid Distribution

### Option A: Custom F-Droid Repository (Recommended)
You can host your own private or public F-Droid repository via GitHub Pages:
1. Create a GitHub repository named `fdroid-repo`.
2. Install `fdroidserver`: `pip install fdroidserver`
3. Run `fdroid init` and copy your signed APK into the `repo/` directory.
4. Run `fdroid update --create-metadata`.
5. Push to GitHub Pages.
6. In the F-Droid client on your phone, go to **Settings → Repositories → Add Repository** and paste your GitHub Pages URL.

### Option B: Official F-Droid Repository
Submit `metadata/com.wayrider.musiccrawlermonitor.yml` to the official [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repository via a merge request.
