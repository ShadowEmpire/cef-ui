#include "../../cef-parallel/inc/ui/CefUiBridgeImpl.h"

#include "include/cef_task.h"
#include "include/wrapper/cef_helpers.h"
#include "include/base/cef_logging.h"

namespace cef_ui {
    namespace ui {
        class CefUiCommandTask : public CefTask {
        public:
            CefUiCommandTask(CefUiBridgeImpl* bridge, CefUiCommand cmd)
                : bridge_(bridge), cmd_(std::move(cmd)) {
            }

            void Execute() override {
                bridge_->HandleCommandOnUiThread(std::move(cmd_));
            }

        private:
            CefUiBridgeImpl* bridge_;
            CefUiCommand cmd_;

            IMPLEMENT_REFCOUNTING(CefUiCommandTask);
        };

        CefUiBridgeImpl::CefUiBridgeImpl() = default;

        void CefUiBridgeImpl::SetBrowser(CefRefPtr<CefBrowser> browser) {
            CEF_REQUIRE_UI_THREAD();
            browser_ = browser;
        }

        void CefUiBridgeImpl::ClearBrowser() {
            CEF_REQUIRE_UI_THREAD();
            browser_ = nullptr;
        }

        bool CefUiBridgeImpl::IsUiAlive() const {
            return browser_ != nullptr;
        }

        void CefUiBridgeImpl::PostCommand(const CefUiCommand& cmd) {
            // Thread-agnostic entry point (Phase 1–4 safe)
            CefPostTask(
                TID_UI,
                new CefUiCommandTask(this, cmd)
            );
        }

        void CefUiBridgeImpl::HandleCommandOnUiThread(CefUiCommand cmd) {
            CEF_REQUIRE_UI_THREAD();

            LOG(INFO) << "[CEF UI BRIDGE]"
                << " type=" << cmd.type
                << " windowId=" << cmd.windowId
                << " payload=" << cmd.payload;

            // NO BEHAVIOR YET (by design)
        }
    }
}
