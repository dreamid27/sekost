# Deployment

Sekost test builds ship through **Firebase App Distribution**. Nothing goes to the Play Store yet.

| | |
| --- | --- |
| Firebase project | `sekost-app` |
| Release app ID | `id.my.selo.sekost` (Firebase app `1:929635600442:android:57f5e20b18bce56b0a8d03`) |
| Debug app ID | `id.my.selo.sekost.debug` (installs next to the release build) |
| Tester group | `alpha` ("Alpha Testers") |
| Console | https://console.firebase.google.com/project/sekost-app/appdistribution |

## Installing a test build (testers)

1. Open the Firebase App Distribution invite email on your Android phone and accept it.
2. Sign in with the Google account the invite was sent to.
3. Download the latest release in the browser or in the Firebase App Tester app.
4. When Android asks, allow installing apps from that source.

New releases arrive by email. Installing one updates the app in place and keeps your data.

## Shipping a new build

Prerequisites (one time):

- Firebase CLI installed, then `firebase login` with an account that has access to `sekost-app`
- `keystore/sekost-release.jks` and `keystore.properties` in the project root (see [Signing](#signing))

Steps:

1. Bump `versionCode` (required) and `versionName` in `app/build.gradle.kts`.
2. Build and upload:

   ```sh
   ./gradlew testDebugUnitTest assembleRelease
   firebase appdistribution:distribute app/build/outputs/apk/release/app-release.apk \
     --app 1:929635600442:android:57f5e20b18bce56b0a8d03 \
     --groups alpha \
     --release-notes "What changed in this build"
   ```

Testers in `alpha` get an email right away.

## Managing testers

```sh
firebase appdistribution:testers:add someone@example.com --group-alias alpha
firebase appdistribution:testers:remove someone@example.com
```

## Signing

Release builds are signed with `keystore/sekost-release.jks`. `keystore.properties` holds its
passwords, and both files are gitignored.

- **Back up both files outside the repo.** If you lose the key, new builds can't install as
  updates, and every tester has to uninstall first and lose their data.
- If `keystore.properties` is missing, `assembleRelease` still runs but produces an unsigned APK
  that won't install.
- Changing the key? Add its SHA-1/SHA-256 to the release app in Firebase project settings.

## Crashes and analytics

Release builds report crashes to **Crashlytics** and usage to **Analytics**. Look in the Firebase
console under the release app. A crash usually shows up within minutes of the app's next launch.

## Troubleshooting

| Problem | Fix |
| --- | --- |
| Upload says "re-uploaded already existing release" | You didn't bump `versionCode`. Bump it and rebuild. |
| "App not installed" on the phone | The installed copy has a different signature. Uninstall it, then install again. |
| Distribute fails with 404 on a group | The group alias doesn't exist. Create it with `firebase appdistribution:group:create "Alpha Testers" alpha`. |
| Build fails about `google-services.json` | Make sure `app/google-services.json` exists. Re-download it with `firebase apps:sdkconfig ANDROID <app-id> -o app/google-services.json`. |
