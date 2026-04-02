## 2024-05-20 - [View Controllers Bypassing ViewModels]
**Learning:** Found a pattern where JavaFX Controllers inject and directly call Micronaut domain services (like `IGrammarLoaderService`), bypassing the ViewModel layer. This breaks MVVM separation of concerns, as the View should only communicate with the ViewModel.
**Action:** When inspecting controllers, ensure they only inject their respective ViewModels (and context for initial loading if strictly necessary) and delegate all business logic requests and domain service calls to the ViewModel instead.

## 2026-04-01 - Prevented ViewModels from handling styling services
**Learning:** Found an MVVM violation where `MainViewModel.loadGrammarAsync` accepted `TokenHighlightMappingService` (a CSS styling service) as a parameter. The ViewModel used it to rebuild the vocabulary mappings. This forced the ViewModel to coordinate UI-specific styling logic, breaking the rule that "CSS and visual states belong purely to the View."
**Action:** Removed the `TokenHighlightMappingService` parameter from `MainViewModel.loadGrammarAsync`. Moved the `buildVocabularyMapping` call into `MainViewController`'s JavaFX Task success handler (`WorkerStateEvent.WORKER_STATE_SUCCEEDED`), keeping styling coordination strictly in the View layer.
