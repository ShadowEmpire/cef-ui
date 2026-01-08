// Copyright (c) 2013 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

#ifndef CEF_NATIVE_APPL_H_
#define CEF_NATIVE_APPL_H_

#include "include/cef_app.h"


namespace cef_ui {
    namespace ui {

        // Implement application-level callbacks for the browser process.
        class CefNativeAppl : public CefApp, public CefBrowserProcessHandler {
        public:
            CefNativeAppl();

            // CefApp methods:
            CefRefPtr<CefBrowserProcessHandler> GetBrowserProcessHandler() override {
                return this;
            }

            // CefBrowserProcessHandler methods:
            void OnContextInitialized() override;
            CefRefPtr<CefClient> GetDefaultClient() override;

        private:
            // Include the default reference counting implementation.
            IMPLEMENT_REFCOUNTING(CefNativeAppl);
        };

#endif  // CEF_TESTS_CEFSIMPLE_SIMPLE_APP_H_
    }
}
