# Jeddy Movies

Android movie Home screen using Jetpack Compose Material 3, Paging 3, StateFlow, SharedFlow, Dagger Hilt, and OMDb.

## Modules

```text
app                 Hilt application, Android entry point, network/use case providers
core/network        Retrofit API, OMDb DTOs, authenticated OkHttp client
core/ui             BaseViewModel, Material 3 theme, and shared UI components
core/local          Empty module reserved for later
domain              Movie models, repository contract, search use case
data                Network datasource, repository/Pager, paging source, Hilt bindings, DTO mapping
feature/list        Home UI components/previews, ViewModel, Intent, State, Effect
feature/detail      Empty module reserved for later
sdk/                Reserved for future ad integrations
```

Home immediately loads an initial movie list with the search field empty. Because OMDb requires a search term, this list uses `Batman` internally (`INITIAL_QUERY` in `HomeViewModel`). Typing searches after a 350 ms debounce; clearing the field restores the initial list. Paging 3 loads OMDb's 10-result pages as the grid scrolls, with initial and next-page loading/error/retry states. A new query replaces the previous paging stream. No popular movie endpoint, persistent movie cache, detail screen, or ads are implemented.

Movie cards show the title, year, and poster returned by OMDb search, keyed by IMDb ID. No separate detail requests are made for ratings or other metadata.

Hilt creates the Home ViewModel and injects its use cases, repository, and network datasource. KSP generates the Hilt code. Domain stays free of Android and DI framework dependencies; its use cases are provided by the app module.

ViewModels extend `BaseViewModel<UIState, Intent, SideEffect>`, exposing a `StateFlow` for state, a `SharedFlow` for effects, and an `onIntent` function. Compose observes both flows with lifecycle-aware collection.

The base ViewModel exposes read-only state/effects and protected mutable flows for implementations. `HomeScreen()` obtains its ViewModel through Hilt internally and passes state, paged items, and callbacks to `ScreenContent()` in the same file.

The repository constructs the `Pager` and paging source in `data`, exposing `Flow<PagingData<Movie>>` through the repository contract and search use case. Domain uses the platform-independent Paging common API. The ViewModel exposes a debounced flow cached in its scope; `HomeScreen()` collects it directly with `collectAsLazyPagingItems()`. `HomeState` contains only the query. The activity only calls `HomeScreen()`.

Data maps failures to `MovieError` types, preserving technical causes for diagnostics. The UI maps these types to string resources and uses Paging load states for full-screen or next-page error/retry presentation. API and exception messages are not displayed directly. UI labels, empty states, fallbacks, and error messages live in the owning module’s `strings.xml`.

## OMDb setup

Add your API key from [OMDb](https://www.omdbapi.com/apikey.aspx) to the ignored `local.properties` file:

```properties
OMDB_API_KEY=your_api_key
```

Alternatively set the `OMDB_API_KEY` environment variable. Requests use `https://www.omdbapi.com/` with `apikey`, `s`, `type=movie`, and `page` query parameters. Rebuild after changing the key. Without a key, searching displays a configuration message.

## Build

The project uses AGP 9.4.1, Gradle 9.6, and JDK 17 or newer. Run:

```shell
./gradlew :app:assembleDebug :core:network:test :data:testDebugUnitTest :feature:list:testDebugUnitTest
```

