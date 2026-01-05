#pragma once
#include "include/cef_app.h"

class MinimalCefApp : public CefApp {
public:
    MinimalCefApp() = default;

    CefRefPtr<CefBrowserProcessHandler> GetBrowserProcessHandler() override {
        return nullptr; // Phase 5: no hooks yet
    }

private:
    IMPLEMENT_REFCOUNTING(MinimalCefApp);
};
