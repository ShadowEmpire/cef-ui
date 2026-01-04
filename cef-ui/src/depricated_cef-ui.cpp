#include <windows.h>
#include "include/cef_app.h"

#include "../inc/ui/CefBootstrap.h"
#include "../inc/ui/NativeWindow.h"
#include "../inc/ui/CefBrowserManager.h"
#include "../inc/ui/ShutdownCoordinator.h"

using namespace cef_ui::ui;

int main(int argc, char* argv[]) {
    CefMainArgs main_args(GetModuleHandle(nullptr));

    // REQUIRED: handle CEF sub-process logic
    int exit_code = CefExecuteProcess(main_args, nullptr, nullptr);
    if (exit_code >= 0) {
        return exit_code;
    }

    try {
        CefBootstrap cef;
        NativeWindow window("CEF UI");

        const std::string ui_url = "https://localhost";
        CefBrowserManager browser(window, ui_url);

        ShutdownCoordinator shutdown;

        cef.Run();
        return 0;
    }
    catch (const std::exception& ex) {
        return 1;
    }
}
