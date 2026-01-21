#pragma once
#include <string>

// Command coming from Phase 1–4

namespace cef_ui {
    namespace ui {
        struct CefUiCommand {
            std::string type;      // "open", "navigate", "reload", etc.
            std::string windowId;
            std::string payload;   // JSON (string for now)
        };

        // Phase 1–4 talks ONLY to this
        class ICefUiBridge {
        public:
            virtual ~ICefUiBridge() = default;

            // fire-and-forget
            virtual void PostCommand(const CefUiCommand& cmd) = 0;

            // health check for ProcessController
            virtual bool IsUiAlive() const = 0;
        };
    }
}