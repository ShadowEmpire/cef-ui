#include "../../inc/ui/CefBootstrap.h"

#include <filesystem>
#include <windows.h>   
#include <string>   
#include <stdexcept>

// Include CEF headers
#include <include/cef_app.h>
#include <include/cef_version.h>
//#include "MinimalCefApp.cpp"
//#include <include/cef_command_line.h>
//#include "include/cef_sandbox_win.h"


namespace cef_ui {
    namespace ui {

        std::atomic<bool> CefBootstrap::process_initialized_{ false };

        CefBootstrap::CefBootstrap()
            : initialized_(false) {

            bool expected = false;
            if (!process_initialized_.compare_exchange_strong(expected, true)) {
                throw std::runtime_error("CEF can only be initialized once per process");
            }

            try {
                Initialize();
                initialized_ = true;
            }
            catch (...) {
                throw;
            }
        }

        CefBootstrap::~CefBootstrap() noexcept {
            // Do NOT call CefShutdown() here.
            // CefShutdown() is called by Run() after CefRunMessageLoop() exits.
            // Destructor is only a cleanup marker, CEF lifecycle is managed by Run().
        }

        void CefBootstrap::Run() {
            if (!initialized_) {
                throw std::runtime_error("CEF not initialized - call constructor first");
            }

            if (run_called_) {
                throw std::runtime_error("CefBootstrap::Run() may only be called once");
            }

            run_called_ = true;

            // Enter CEF message loop
            // This blocks until the application exits (e.g., all windows closed)
            CefRunMessageLoop();

            // CefRunMessageLoop() has returned - message loop is done
            // Now perform shutdown
            Shutdown();
        }

        void CefBootstrap::Initialize() {
            //////AllocConsole();
            //////freopen("CONOUT$", "w", stdout);
            //////freopen("CONOUT$", "w", stderr);
            // Prepare main arguments (empty for Windows)
            CefMainArgs main_args(GetModuleHandle(nullptr));
            ////OutputDebugStringA(CEF_VERSION);

            // Prepare minimal CEF settings
            CefSettings settings;
            settings.no_sandbox = true;
            settings.log_severity = LOGSEVERITY_VERBOSE;
            CefString(&settings.log_file) = L"cef.log";
            // Use default settings for Phase 5 Step 1
            // Full configuration deferred to Phase 6+    

            // --- Resolve executable directory ---
            wchar_t exe_path[MAX_PATH];
            GetModuleFileNameW(nullptr, exe_path, MAX_PATH);
            std::filesystem::path exeDir = std::filesystem::path(exe_path).parent_path();

            // --- REQUIRED: resource paths ---
            int __scope_test = 42;
            std::basic_string<wchar_t> resources_path = (exeDir / L"Resources").wstring();
//            \\std::wstring resources_path = (exeDir / L"Resources").wstring();
            std::wstring locales_path = (exeDir / L"Resources" / L"locales").wstring();

            SetCurrentDirectoryW(exeDir.c_str());

            CefString(&settings.resources_dir_path) = resources_path;
            CefString(&settings.locales_dir_path) = locales_path;
            // --- REQUIRED for subprocess ---
            ////////CefString(&settings.browser_subprocess_path) = exe_path;     
            std::wstring icu_path = resources_path + L"\\icudtl.dat";
            ////CefCommandLine::GetGlobalCommandLine()->AppendSwitchWithValue(
            ////    "icu-data-file",
            ////    icu_path
            ////);

            OutputDebugStringW((L"CEF Resources Path: " + resources_path + L"\n").c_str());
            OutputDebugStringW((L"CEF Locales Path: " + locales_path + L"\n").c_str());

            //CefRefPtr<MinimalCefApp> app = new MinimalCefApp();

            // Initialize CEF
            // app=nullptr (no CefApp handler in Phase 5 Step 1)
            // windows_sandbox_info=nullptr (Windows 7+ compatibility, not needed for basic init)
            if (!CefInitialize(main_args, settings, nullptr, nullptr)) {
                throw std::runtime_error("CefInitialize() failed");
            }
        }

        void CefBootstrap::Shutdown() noexcept {
            // Called by Run() after CefRunMessageLoop() exits
            // This is the proper place to call CefShutdown() per CEF documentation
            if (!initialized_) {
                return;
            }

            initialized_ = false;
            CefShutdown();
        }

    }  // namespace ui
}  // namespace cef_ui
