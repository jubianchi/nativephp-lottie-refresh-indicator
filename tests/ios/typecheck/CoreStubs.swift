// Stand-ins for what the core app links from PHP's C API and ZIPFoundation,
// so the app, mobile-ui and this plugin can be typechecked together.
import Foundation

final class AppUpdateManager {
    static let shared = AppUpdateManager()
    func getAppPath() -> String { "" }
    func ensureAppExists() -> Bool { true }
}

struct zend_file_handle {}

func php_embed_init<A, B>(_ argc: A, _ argv: B) {}
func zend_stream_init_filename<T>(_ handle: inout zend_file_handle, _ path: T) {}
func php_execute_script(_ handle: inout zend_file_handle) {}
func php_embed_shutdown() {}
