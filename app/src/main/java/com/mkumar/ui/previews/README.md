# Android Studio preview catalog

- `DashboardPreviews.kt` — recent-order dashboard section and populated order cards.
- `CustomerAndOrderPreviews.kt` — recently added customers and customer order history.
- `PreferencePreviews.kt` — expanded settings containers, preference rows, and backup restore cards.
- `ProductFormPreviews.kt` — populated frame and contact-lens forms.
- `InputAndSortPreviews.kt` — shared text fields, pricing inputs, and sorting/filter controls.
- `PreviewData.kt` — reusable realistic sample records shared by the previews.

Every visual catalog includes light and dark variants. Open a preview file and use Android
Studio's **Split** or **Design** view to render and interact with its `@Preview` functions.

Screens that require Hilt, navigation, Room, WorkManager, or live Android permissions are
represented through their reusable stateless UI sections so previews remain deterministic.
