package com.digitalqueue.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final String frontendUrl;

    public HomeController(@Value("${app.frontend.url:http://127.0.0.1:5173}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> home() {
        String normalizedFrontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;

        String html = """
                <!doctype html>
                <html lang="es">
                  <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Digital Queue API</title>
                    <style>
                      body { font-family: Arial, sans-serif; margin: 0; min-height: 100vh; display: grid; place-items: center; background: #f8fafc; color: #0f172a; }
                      main { width: min(520px, calc(100% - 32px)); padding: 28px; background: white; border: 1px solid #e2e8f0; border-radius: 12px; box-shadow: 0 18px 40px rgba(15, 23, 42, .08); }
                      h1 { margin: 0 0 8px; font-size: 28px; }
                      p { margin: 0 0 18px; color: #475569; line-height: 1.5; }
                      a { display: block; margin-top: 10px; padding: 12px 14px; border-radius: 8px; background: #0f172a; color: white; text-decoration: none; font-weight: 700; text-align: center; }
                      a.secondary { background: #e2e8f0; color: #0f172a; }
                    </style>
                  </head>
                  <body>
                    <main>
                      <h1>Digital Queue API</h1>
                      <p>El backend esta funcionando. Para usar la app visual, entra al frontend.</p>
                      <a href="${frontendUrl}/">Abrir portal cliente</a>
                      <a class="secondary" href="${frontendUrl}/admin/login">Abrir admin</a>
                      <a class="secondary" href="/api/public/filas/starbucks-uade/estado">Probar API publica</a>
                    </main>
                  </body>
                </html>
                """.replace("${frontendUrl}", normalizedFrontendUrl);

        return ResponseEntity.ok(html);
    }
}
