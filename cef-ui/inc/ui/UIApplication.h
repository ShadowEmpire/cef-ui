#pragma once

#include <memory>

namespace cef_ui {
    namespace ui {

        class NativeWindow;
        class CefBrowserManager;
        class ShutdownCoordinator;

        /// Application-level root (Phase 5+)
        /// Owns UI objects, NOT CEF kernel lifecycle.
        class UIApplication {
        public:
            UIApplication();
            ~UIApplication();

            // Non-copyable / non-movable
            UIApplication(const UIApplication&) = delete;
            UIApplication& operator=(const UIApplication&) = delete;
            UIApplication(UIApplication&&) = delete;
            UIApplication& operator=(UIApplication&&) = delete;

            /// Called AFTER CefInitialize()
            /// Must not call any CEF global init/shutdown APIs.
            void Start();

        private:
            std::unique_ptr<NativeWindow> window_;
            std::unique_ptr<CefBrowserManager> browser_;
            std::unique_ptr<ShutdownCoordinator> shutdown_;
        };

    } // namespace ui
} // namespace cef_ui
