#pragma once
#include "CefUiBridge.h"

#include "include/cef_browser.h"
#include "include/base/cef_callback.h"

namespace cef_ui {
    namespace ui {
        class CefUiBridgeImpl : public ICefUiBridge {
        public:
            CefUiBridgeImpl();

            void PostCommand(const CefUiCommand& cmd) override;
            bool IsUiAlive() const override;

            // Called when browser is created/destroyed
            void SetBrowser(CefRefPtr<CefBrowser> browser);
            void ClearBrowser();

        private:
            void HandleCommandOnUiThread(CefUiCommand cmd);

            CefRefPtr<CefBrowser> browser_;
            
            friend class CefUiCommandTask;
        };
    }
}