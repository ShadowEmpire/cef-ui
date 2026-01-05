#include "../../inc/ui/UIApplication.h"
#include "../../inc/ui/NativeWindow.h"
#include "../../inc/ui/CefBrowserManager.h"
#include "../../inc/ui/ShutdownCoordinator.h"

namespace cef_ui {
    namespace ui {

        UIApplication::UIApplication() = default;

        UIApplication::~UIApplication() = default;

        void UIApplication::Start() {
            // Phase-5 rule:
            // - No CefInitialize
            // - No CefRunMessageLoop
            // - No CefShutdown

            // 1. Create native window (hidden initially)
            window_ = std::make_unique<NativeWindow>("CEF UI");

            // 2. Create browser and load UI
            const std::string ui_url = "https://www.google.com/"; // keep as-is for now
            browser_ = std::make_unique<CefBrowserManager>(*window_, ui_url);

            // 3. Setup shutdown coordination
            shutdown_ = std::make_unique<ShutdownCoordinator>();

            // NOTE:
            // Showing window, IPC, crash handling -> Phase 6/7
        }

    } // namespace ui
} // namespace cef_ui
