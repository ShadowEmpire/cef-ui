// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "../../cef-parallel/inc/ui/CefNativeAppl.h"

#include <string>

#include "include/cef_browser.h"
#include "include/cef_command_line.h"
#include "include/views/cef_browser_view.h"
#include "include/views/cef_window.h"
#include "include/wrapper/cef_helpers.h"
#include "../../cef-parallel/inc/ui/CefHandler.h"


namespace cef_ui {
    namespace ui {

        // When using the Views framework this object provides the delegate
        // implementation for the CefWindow that hosts the Views-based browser.
        class CefWinDelegate : public CefWindowDelegate {
        public:
            CefWinDelegate(CefRefPtr<CefBrowserView> browser_view,
                cef_runtime_style_t runtime_style,
                cef_show_state_t initial_show_state)
                : browser_view_(browser_view),
                runtime_style_(runtime_style),
                initial_show_state_(initial_show_state) {
            }

            void OnWindowCreated(CefRefPtr<CefWindow> window) override {
                // Add the browser view and show the window.
                window->AddChildView(browser_view_);

                if (initial_show_state_ != CEF_SHOW_STATE_HIDDEN) {
                    window->Show();
                }
            }

            void OnWindowDestroyed(CefRefPtr<CefWindow> window) override {
                browser_view_ = nullptr;
            }

            bool CanClose(CefRefPtr<CefWindow> window) override {
                // Allow the window to close if the browser says it's OK.
                CefRefPtr<CefBrowser> browser = browser_view_->GetBrowser();
                if (browser) {
                    return browser->GetHost()->TryCloseBrowser();
                }
                return true;
            }

            CefSize GetPreferredSize(CefRefPtr<CefView> view) override {
                return CefSize(800, 600);
            }

            cef_show_state_t GetInitialShowState(CefRefPtr<CefWindow> window) override {
                return initial_show_state_;
            }

            cef_runtime_style_t GetWindowRuntimeStyle() override {
                return runtime_style_;
            }

        private:
            CefRefPtr<CefBrowserView> browser_view_;
            const cef_runtime_style_t runtime_style_;
            const cef_show_state_t initial_show_state_;

            IMPLEMENT_REFCOUNTING(CefWinDelegate);
            DISALLOW_COPY_AND_ASSIGN(CefWinDelegate);
        };

        class BrowserViewDelegate : public CefBrowserViewDelegate {
        public:
            explicit BrowserViewDelegate(cef_runtime_style_t runtime_style)
                : runtime_style_(runtime_style) {
            }

            bool OnPopupBrowserViewCreated(CefRefPtr<CefBrowserView> browser_view,
                CefRefPtr<CefBrowserView> popup_browser_view,
                bool is_devtools) override {
                // Create a new top-level Window for the popup. It will show itself after
                // creation.
                CefWindow::CreateTopLevelWindow(new CefWinDelegate(
                    popup_browser_view, runtime_style_, CEF_SHOW_STATE_NORMAL));

                // We created the Window.
                return true;
            }

            cef_runtime_style_t GetBrowserRuntimeStyle() override {
                return runtime_style_;
            }

        private:
            const cef_runtime_style_t runtime_style_;

            IMPLEMENT_REFCOUNTING(BrowserViewDelegate);
            DISALLOW_COPY_AND_ASSIGN(BrowserViewDelegate);
        };

        CefNativeAppl::CefNativeAppl() = default;

        void CefNativeAppl::OnContextInitialized() {
            CEF_REQUIRE_UI_THREAD();

            CefRefPtr<CefCommandLine> command_line =
                CefCommandLine::GetGlobalCommandLine();

            static ui::CefUiBridgeImpl g_uiBridge;

            // Check if Alloy style will be used.
            cef_runtime_style_t runtime_style = CEF_RUNTIME_STYLE_DEFAULT;
            bool use_alloy_style = command_line->HasSwitch("use-alloy-style");
            if (use_alloy_style) {
                runtime_style = CEF_RUNTIME_STYLE_ALLOY;
            }

            // CefHandler implements browser-level callbacks.
            CefRefPtr<CefHandler> handler(new CefHandler(use_alloy_style, &g_uiBridge));

            // Specify CEF browser settings here.
            CefBrowserSettings browser_settings;

            std::string url;

            // Check if a "--url=" value was provided via the command-line. If so, use
            // that instead of the default URL.
            url = command_line->GetSwitchValue("url");
            if (url.empty()) {
                url = "http://10.1.29.18:8080/";
            }

            // Views is enabled by default (add `--use-native` to disable).
            const bool use_views = !command_line->HasSwitch("use-native");

            // If using Views create the browser using the Views framework, otherwise
            // create the browser using the native platform framework.
            if (use_views) {
                // Create the BrowserView.
                CefRefPtr<CefBrowserView> browser_view = CefBrowserView::CreateBrowserView(
                    handler, url, browser_settings, nullptr, nullptr,
                    new BrowserViewDelegate(runtime_style));

                // Optionally configure the initial show state.
                cef_show_state_t initial_show_state = CEF_SHOW_STATE_NORMAL;
                const std::string& show_state_value =
                    command_line->GetSwitchValue("initial-show-state");
                if (show_state_value == "minimized") {
                    initial_show_state = CEF_SHOW_STATE_MINIMIZED;
                }
                else if (show_state_value == "maximized") {
                    initial_show_state = CEF_SHOW_STATE_MAXIMIZED;
                }
#if defined(OS_MAC)
                // Hidden show state is only supported on MacOS.
                else if (show_state_value == "hidden") {
                    initial_show_state = CEF_SHOW_STATE_HIDDEN;
                }
#endif

                // Create the Window. It will show itself after creation.
                CefWindow::CreateTopLevelWindow(new CefWinDelegate(
                    browser_view, runtime_style, initial_show_state));
            }
            else {
                // Information used when creating the native window.
                CefWindowInfo window_info;

#if defined(OS_WIN)
                // On Windows we need to specify certain flags that will be passed to
                // CreateWindowEx().
                window_info.SetAsPopup(nullptr, "cefsimple");
#endif

                // Alloy style will create a basic native window. Chrome style will create a
                // fully styled Chrome UI window.
                window_info.runtime_style = runtime_style;

                // Create the first browser window.
                CefBrowserHost::CreateBrowser(window_info, handler, url, browser_settings,
                    nullptr, nullptr);
            }
        }

        CefRefPtr<CefClient> CefNativeAppl::GetDefaultClient() {
            // Called when a new browser window is created via Chrome style UI.
            return CefHandler::GetInstance();
        }
    }
}
