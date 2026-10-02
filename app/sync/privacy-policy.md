**Privacy Policy — Simple Music Player**

Simple Music Player ("the app") is an Android music player developed by Anrimian, an individual developer. This policy explains what data the app accesses, how it is used, and how it is protected.

**Summary**

*   The app has no backend server. Nothing you store in the app or in your cloud storage is ever sent to the developer.
*   No analytics, no advertising, no tracking.
*   Cloud sync is optional. When you enable it, the app talks directly from your device to your own cloud storage account.

**Data stored on your device**

Your music library, playlists, settings and sync records are stored in the app's private storage on your device. Android isolates this storage from other apps. Some settings, including which cloud storage is connected, may be included in Android's own device backup, which is stored in your Google account by Google. The developer has no access to it.

**Log data**

When an error occurs, the app may save diagnostic information on your device (device model, Android version, error details). It is never sent automatically. It leaves your device only if you choose to share it with the developer yourself, for example by email.

**Cloud synchronization (Google Drive, Dropbox)**

Cloud sync keeps your music library in sync between your devices through a folder in your own cloud storage. It works only after you connect a storage account and grant access through the provider's official OAuth 2.0 consent screen.

*Google user data the app accesses*

When you connect Google Drive, the app accesses:

*   Your Google account email address, to show which account is connected and to request access for that account.
*   The names and folder structure of your Google Drive files, so you can choose an existing folder as the sync folder and the app can find it.
*   The contents of audio files inside the sync folder you chose.
*   Your Google Drive storage usage, to warn you when the storage is running out of space.

*How the app uses this data*

The data is used only to provide the sync feature you turned on:

*   Audio files in the sync folder are downloaded to your device and added to your library.
*   Music you add, rename, move or delete on your device is uploaded, renamed, moved or deleted in the sync folder. Deleting a synced track in the app deletes the corresponding file in your cloud storage.
*   The app keeps its sync records in a `.smp-metadata` folder inside the sync folder.

The app does not read or change the contents of files outside the sync folder.

*Sharing*

Google user data is transferred only between your device and Google's servers. It is not sent to the developer, sold, shared with third parties, or used for advertising. It is not used to develop, improve or train generalized AI or machine learning models.

*Limited Use*

Simple Music Player's use and transfer to any other app of information received from Google APIs will adhere to the [Google API Services User Data Policy](https://developers.google.com/terms/api-services-user-data-policy), including the Limited Use requirements.

**Data protection**

*   All communication with Google Drive and Dropbox goes directly from your device to the provider's official API over HTTPS (TLS encryption). No intermediate server is involved.
*   Google access tokens are issued and managed by Google Play services on your device. The app stores only your account email address. Dropbox access credentials are stored in the app's private storage on your device.
*   No data from your cloud storage is stored anywhere except on your device and in your own cloud storage account.

**Retention and deletion**

*   Data the app keeps on your device stays there while you use the sync feature. Uninstalling the app or clearing its data in Android settings deletes everything the app stored on the device.
*   Files the app wrote to your cloud storage, including the `.smp-metadata` folder, remain in your account under your control. You can delete them at any time.
*   You can revoke the app's access to your Google account at any time at https://myaccount.google.com/permissions, and to Dropbox at https://www.dropbox.com/account/connected_apps. After that the app can no longer access your cloud storage.

**Subscriptions and payments**

Google Drive sync and other paid cloud storages require a subscription. Subscriptions are purchased and managed entirely through Google Play:

*   The app uses the Google Play Billing library to ask the store whether a subscription is active, and to open the store's own purchase screen.
*   Payment details are handled by Google Play. The app never receives, processes, or stores card numbers, billing addresses, or any other payment information.
*   The result of the check (whether a subscription is active) is stored locally on your device only. It is not sent to the developer or to any third party, and it is not included in device backups.
*   No account identifier is attached to purchases, and there is no developer-operated server involved in verifying them.

Google Play's own handling of your purchase is covered by Google's privacy policy.

**Links to other sites**

The app may contain links to other sites, such as your cloud provider's pages. These sites are not operated by the developer, and their own privacy policies apply.

**Changes to this policy**

This policy may be updated from time to time. Changes are published on this page.

This policy is effective as of 2026-10-03.

**Contact**

If you have any questions about this policy, contact smusicplayer.feedback@gmail.com.
