// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#include "../../cef-parallel/inc/ui/CefHandler.h"

#include <sstream>
#include <string>

#include "include/base/cef_callback.h"
#include "include/cef_app.h"
#include "include/cef_parser.h"
#include "include/views/cef_browser_view.h"
#include "include/views/cef_window.h"
#include "include/wrapper/cef_closure_task.h"
#include "include/wrapper/cef_helpers.h"


namespace cef_ui {
    namespace ui {

        CefHandler* g_instance = nullptr;

        // Returns a data: URI with the specified contents.
        std::string GetDataURI(const std::string& data, const std::string& mime_type) {
            return "data:" + mime_type + ";base64," +
                CefURIEncode(CefBase64Encode(data.data(), data.size()), false)
                .ToString();
        }

        CefHandler::CefHandler(bool is_alloy_style, CefUiBridgeImpl* bridge)
            : is_alloy_style_(is_alloy_style), uiBridge_(bridge) {
            DCHECK(!g_instance);
            g_instance = this;
        }

        CefHandler::~CefHandler() {
            g_instance = nullptr;
        }

        // static
        CefHandler* CefHandler::GetInstance() {
            return g_instance;
        }

        void CefHandler::OnTitleChange(CefRefPtr<CefBrowser> browser,
            const CefString& title) {
            CEF_REQUIRE_UI_THREAD();

            if (auto browser_view = CefBrowserView::GetForBrowser(browser)) {
                // Set the title of the window using the Views framework.
                CefRefPtr<CefWindow> window = browser_view->GetWindow();
                if (window) {
                    window->SetTitle(title);
                }
            }
            else if (is_alloy_style_) {
                // Set the title of the window using platform APIs.
                PlatformTitleChange(browser, title);
            }
        }

        void CefHandler::OnAfterCreated(CefRefPtr<CefBrowser> browser) {
            CEF_REQUIRE_UI_THREAD();
            uiBridge_->SetBrowser(browser);

            // Sanity-check the configured runtime style.
            CHECK_EQ(is_alloy_style_ ? CEF_RUNTIME_STYLE_ALLOY : CEF_RUNTIME_STYLE_CHROME,
                browser->GetHost()->GetRuntimeStyle());

            // Add to the list of existing browsers.
            browser_list_.push_back(browser);
        }

        bool CefHandler::DoClose(CefRefPtr<CefBrowser> browser) {
            CEF_REQUIRE_UI_THREAD();

            // Closing the main window requires special handling. See the DoClose()
            // documentation in the CEF header for a detailed destription of this
            // process.
            if (browser_list_.size() == 1) {
                // Set a flag to indicate that the window close should be allowed.
                is_closing_ = true;
            }

            // Allow the close. For windowed browsers this will result in the OS close
            // event being sent.
            return false;
        }

        void CefHandler::OnBeforeClose(CefRefPtr<CefBrowser> browser) {
            CEF_REQUIRE_UI_THREAD();
            uiBridge_->ClearBrowser();

            // Remove from the list of existing browsers.
            BrowserList::iterator bit = browser_list_.begin();
            for (; bit != browser_list_.end(); ++bit) {
                if ((*bit)->IsSame(browser)) {
                    browser_list_.erase(bit);
                    break;
                }
            }

            if (browser_list_.empty()) {
                // All browser windows have closed. Quit the application message loop.
                CefQuitMessageLoop();
            }
        }

        void CefHandler::OnLoadError(CefRefPtr<CefBrowser> browser,
            CefRefPtr<CefFrame> frame,
            ErrorCode errorCode,
            const CefString& errorText,
            const CefString& failedUrl) {
            CEF_REQUIRE_UI_THREAD();

            // Allow Chrome to show the error page.
            if (!is_alloy_style_) {
                return;
            }

            // Don't display an error for downloaded files.
            if (errorCode == ERR_ABORTED) {
                return;
            }

            // Display a load error message using a data: URI.
            std::stringstream ss;
            ss << "<html><body bgcolor=\"white\">"
                "<h2>Failed to load URL "
                << std::string(failedUrl) << " with error " << std::string(errorText)
                << " (" << errorCode << ").</h2></body></html>";

            frame->LoadURL(GetDataURI(ss.str(), "text/html"));
        }

        void CefHandler::ShowMainWindow() {
            if (!CefCurrentlyOn(TID_UI)) {
                // Execute on the UI thread.
                CefPostTask(TID_UI, base::BindOnce(&CefHandler::ShowMainWindow, this));
                return;
            }

            if (browser_list_.empty()) {
                return;
            }

            auto main_browser = browser_list_.front();

            if (auto browser_view = CefBrowserView::GetForBrowser(main_browser)) {
                // Show the window using the Views framework.
                if (auto window = browser_view->GetWindow()) {
                    window->Show();
                }
            }
            else if (is_alloy_style_) {
                PlatformShowWindow(main_browser);
            }
        }

        void CefHandler::CloseAllBrowsers(bool force_close) {
            if (!CefCurrentlyOn(TID_UI)) {
                // Execute on the UI thread.
                CefPostTask(TID_UI, base::BindOnce(&CefHandler::CloseAllBrowsers, this,
                    force_close));
                return;
            }

            if (browser_list_.empty()) {
                return;
            }

            BrowserList::const_iterator it = browser_list_.begin();
            for (; it != browser_list_.end(); ++it) {
                (*it)->GetHost()->CloseBrowser(force_close);
            }
        }

#if !defined(OS_MAC)
        void CefHandler::PlatformShowWindow(CefRefPtr<CefBrowser> browser) {
            NOTIMPLEMENTED();
        }
#endif
    }
}