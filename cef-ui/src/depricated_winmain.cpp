//#include <windows.h>
//#include <shellapi.h>   // CommandLineToArgvW
//#include <vector>
//#include <string>
//
//// Forward declaration of your existing main()
//int main(int argc, char* argv[]);
//
//int WINAPI WinMain(
//    HINSTANCE,
//    HINSTANCE,
//    LPSTR,
//    int
//) {
//    int argc = 0;
//    LPWSTR* argvW = CommandLineToArgvW(GetCommandLineW(), &argc);
//
//    std::vector<std::string> argvUtf8;
//    argvUtf8.reserve(argc);
//
//    std::vector<char*> argv;
//    argv.reserve(argc);
//
//    for (int i = 0; i < argc; ++i) {
//        int len = WideCharToMultiByte(
//            CP_UTF8, 0, argvW[i], -1, nullptr, 0, nullptr, nullptr
//        );
//        std::string utf8(len, '\0');
//        WideCharToMultiByte(
//            CP_UTF8, 0, argvW[i], -1, utf8.data(), len, nullptr, nullptr
//        );
//        argvUtf8.push_back(std::move(utf8));
//    }
//
//    for (auto& s : argvUtf8) {
//        argv.push_back(s.data());
//    }
//
//    LocalFree(argvW);
//
//    return main(argc, argv.data());
//}


#include <windows.h>

// Forward declaration
int main(int argc, char* argv[]);

int WINAPI WinMain(
    HINSTANCE,
    HINSTANCE,
    LPSTR,
    int
) {
    // IMPORTANT:
    // Do NOT forward command-line arguments.
    // CEF spawns and manages subprocesses itself.

    char* argv[] = {
        const_cast<char*>("cef-ui.exe")
    };

    return main(1, argv);
}