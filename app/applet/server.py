import http.server
import socketserver
import os

PORT = 3000
DIRECTORY = "/app/applet/public"

class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=DIRECTORY, **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        if self.path.endswith(".apk"):
            self.send_header("Content-Type", "application/vnd.android.package-archive")
            self.send_header("Content-Disposition", 'attachment; filename="app-debug.apk"')
        super().end_headers()

if __name__ == "__main__":
    os.makedirs(DIRECTORY, exist_ok=True)
    os.chdir(DIRECTORY)
    with socketserver.TCPServer(("", PORT), Handler) as httpd:
        print(f"Serving HTTP on 0.0.0.0 port {PORT} from {DIRECTORY} ...")
        httpd.serve_forever()
