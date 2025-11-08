[33mcommit 42cadb9557301005db679aa905b4b02f364ca4ec[m[33m ([m[1;36mHEAD[m[33m -> [m[1;32mConnorUdal-branch[m[33m)[m
Merge: df82ff2 e57089e
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Fri Nov 7 11:07:33 2025 -0700

    Merge branch 'ConnorUdal-branch' of https://github.com/Matrix-F25/matrix-events into ConnorUdal-branch

[33mcommit e57089e86078eedcfc443ede591ab6a2ba4ffb2b[m[33m ([m[1;31morigin/ConnorUdal-branch[m[33m)[m
Merge: 4d7dcb6 a4bc317
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Fri Nov 7 00:45:00 2025 -0700

    Merge branch 'main' into ConnorUdal-branch

[33mcommit 4d7dcb6e1ce468bdb44af5716971b4226a64530c[m
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Fri Nov 7 00:44:22 2025 -0700

    set up file for US tests

[33mcommit 921ab2200d03da4273a763ab26d3274c9096d92f[m
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Fri Nov 7 00:20:14 2025 -0700

    eventmanager

[33mcommit a4bc31754a6b5b17e10192017040a8a8d0233d44[m
Merge: 515c2be 3fa6f61
Author: Connor McRae <144768491+connorless@users.noreply.github.com>
Date:   Thu Nov 6 23:14:05 2025 -0700

    Merge pull request #71 from Matrix-F25/nikolai-branch
    
    cloud functions, updates selection process, more event stuff as always

[33mcommit 3fa6f619e1780f17d4a93b28b1126aab7c739e63[m
Author: Nikolai Philipenko <nikolai.philipenko@gmail.com>
Date:   Thu Nov 6 22:56:53 2025 -0700

    google cloud functions v1

[33mcommit 8c72e1de41937d9674eac4acd24611ebd28a3e22[m
Author: Nikolai Philipenko <nikolai.philipenko@gmail.com>
Date:   Thu Nov 6 22:56:10 2025 -0700

    entrant accept / decline yagagaggagag

[33mcommit 8c766e8829be408a178dfced70f8e6b557421112[m
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Thu Nov 6 22:07:51 2025 -0700

    event testing

[33mcommit df82ff2e253e34ca0e4b31d7425999b7d998490e[m
Merge: fb92078 515c2be
Author: ConnorUdal0 <udalizzer@gmail.com>
Date:   Thu Nov 6 17:34:19 2025 -0700

    Merge branch 'main' into ConnorUdal-branch

[33mcommit 515c2be3c498ce477ef8fffed12b49e5889ccc2c[m[33m ([m[1;31morigin/albert-branch[m[33m)[m
Merge: f222e50 628671e
Author: Connor McRae <144768491+connorless@users.noreply.github.com>
Date:   Thu Nov 6 17:20:07 2025 -0700

    Merge pull request #70 from Matrix-F25/albert-branch
    
    Notifications added

[33mcommit 9d5159d52865f9929d45b7f7a9b68fc2732923fa[m
Author: Nikolai Philipenko <nikolai.philipenko@gmail.com>
Date:   Thu Nov 6 16:57:51 2025 -0700

    my events entrant UI rework

[33mcommit f222e50c321889c90e4bb3fa57351fff48d07f6f[m
Merge: e49853f 8619a74
Author: Connor McRae <144768491+connorless@users.noreply.github.com>
Date:   Thu Nov 6 16:04:48 2025 -0700

    Merge pull request #68 from Matrix-F25/nikolai-branch
    
    Event details, Fleshed out Event entity, Fixed SearchActivity

[33mcommit 8619a74768d06f2b5c7a0a9ced7bcfa744ac9b90[m
Author: Nikolai Philipenko <nikolai.philipenko@gmail.com>
Date:   Thu Nov 6 15:59:34 2025 -0700

    my events entrant view basic functionality

[33mcommit 628671e4bcb085aa39eeccc4df4e0b37a8e856e2[m
Merge: e49853f 99b93a7
Author: albertganut <albert.ganut@gmail.com>
Date:   Thu Nov 6 15:05:31 2025 -0700

    Merge branch 'albert-branch' of https://github.com/Matrix-F25/matrix-events into albert-branch

[33mcommit 2215831406e66076742795aff41af4abc1a9063b[m
Author: Nikolai Philipenko <nikolai.philipenko@gmail.com>
Date:   Thu Nov 6 14:52:44 2025 -0700

    working towards accepting an event

[33mcommit 99b93a74d5dae94633330eade4c8c889ea26a0d7[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:58:59 2025 -0700

    Refactored notification message layout
    
    Adjusted constraints for the title and message text to accommodate the delete button. Removed redundant margin and bias attributes from the delete button for a cleaner layout.

[33mcommit dde53a742c56bf806ba50981c3a2aae4b7afacca[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:58:19 2025 -0700

    Fix: Removed "(Organizer)" from notification title
    
    The hardcoded "(Organizer)" suffix has been removed from the sender's name in the "New message from" title within the `NotificationSeeMore` fragment.

[33mcommit 2093da6bcd25a46fc81e1c3c9368ec27eef3723d[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:55:03 2025 -0700

    Refactor: Remove unused import
    
    Removed `android.util.Log` import from `NotificationArrayAdapter.java`.

[33mcommit 6a84fdb302e4561ac3651882a64ae465cc53fbc2[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:54:40 2025 -0700

    Refactor: Remove unused import
    
    Removed `android.util.Log` import from `NotificationArrayAdapter.java`.

[33mcommit e2f38f7bec6743572a6b92fdfe90a840b2cdc301[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:53:42 2025 -0700

    Refactored NotificationActivity to filter notifications by device ID
    
    - Fetched the unique Android device ID.
    - Updated the `update()` method to retrieve and display only the notifications intended for the specific device.
    - Changed class member access modifiers to private.

[33mcommit 3fa1f90dc14c7b1edb4aaa3e5d3f533d0b5240dc[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:12:43 2025 -0700

    feat: Implement NotificationArrayAdapter
    
    Adds a new `NotificationArrayAdapter` to display notifications in a list view.
    
    Key features include:
    - Inflating the layout for each notification item.
    - Displaying the notification title and a message preview.
    - Implementing a "See More" button to open a detailed view of the notification.
    - Adding a "Delete" button to remove a notification.

[33mcommit 6fef60abe9cd45a6efa70ca360d78288f1f7187e[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:09:12 2025 -0700

    feat: Implement NotificationArrayAdapter
    
    Adds a new `NotificationArrayAdapter` to display notifications in a list view.
    
    Key features include:
    - Inflating the layout for each notification item.
    - Displaying the notification title and a message preview.
    - Implementing a "See More" button to open a detailed view of the notification.
    - Adding a "Delete" button to remove a notification.

[33mcommit 332780f08d1798e8a399d51e1f62d02fc59c0548[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 02:03:35 2025 -0700

    Refactor: Renamed button_close to button_delete

[33mcommit 389a662fc1d23e5db7e44ff3c8d788f6e76ae69c[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 01:57:51 2025 -0700

    feat: Implement notification list view
    
    - Add ListView to display notifications in `NotificationActivity`.
    - Create `NotificationArrayAdapter` to populate the list.
    - Implement `View` interface to update the UI when notification data changes.
    - Integrate `NotificationManager` to fetch and display notification data.

[33mcommit d1202fd8e3a375633510d6c5145efa601296e001[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 01:57:21 2025 -0700

    using Nikolai's Notification entity implementation

[33mcommit bba3998ab8719823c429509993a2a968acc72d35[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 01:54:28 2025 -0700

    feat: Updated notification page layout
    
    - Changed the page title from "notification page" to "Notifications".
    - Added a `ListView` to display a list of notifications.
    - Styled the `ListView` to be clickable, have transparent dividers, and margins.

[33mcommit 6ab827ec918736fb531869a9496defb952d2c2c5[m
Author: Albert <albert.ganut@gmail.com>
Date:   Thu Nov 6 01:53:15 2025 -0700

    Updated the Notification See More Fragment layout
    
    This commit updates the layout for the "See More" notification screen.
    
    Key changes:
    - The root layout now fills the entire parent and has a semi-transparent background.
    - A close button has been added to the notification card.
    - The date and time chips have been moved inside the `MaterialCardView`.
    - The layout has been refactored to a more centered, modal-like appearance.
    - Removed the `contentDescription` from the info icon.

[33mcommit e4835a0c4f25705391