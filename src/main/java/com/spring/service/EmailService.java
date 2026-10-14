package com.spring.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${spring.mail.username:no-reply@campusdesk.com}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String code) {
        boolean isDev = "dev".equalsIgnoreCase(activeProfile) || (activeProfile != null && activeProfile.contains("dev"));
        boolean isSmtpConfigured = mailSender != null && mailHost != null && !mailHost.trim().isEmpty();

        // En perfil dev o si no hay SMTP configurado, registrar en consola para pruebas sin bloquear la app
        if (isDev) {
            log.info("===============================================================================");
            log.info("[CAMPUSDESK DEV] Código de verificación generado para {}: {}", toEmail, code);
            log.info("===============================================================================");
        }

        if (!isSmtpConfigured) {
            if (!isDev) {
                log.warn("Servicio SMTP no configurado en entorno de producción para destinatario {}", toEmail);
            }
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail, "CampusDESK");
            helper.setTo(toEmail);
            helper.setSubject("Tu código de verificación de CampusDESK: " + code);
            helper.setText(buildHtmlTemplate(code), true);

            mailSender.send(mimeMessage);
            log.info("Correo de verificación enviado exitosamente a {}", toEmail);
        } catch (Exception e) {
            log.error("No se pudo enviar el correo de verificación a {}: {}", toEmail, e.getMessage());
            // No propagamos la excepción para evitar que un fallo de red o credenciales SMTP rompa la app
        }
    }

    private String buildHtmlTemplate(String code) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #0f172a; margin: 0; padding: 20px; color: #f8fafc; }
                    .card { max-width: 500px; margin: 0 auto; background: #1e293b; border-radius: 12px; padding: 32px; border: 1px solid #334155; text-align: center; }
                    .logo { font-size: 24px; font-weight: 700; color: #818cf8; margin-bottom: 20px; }
                    .title { font-size: 20px; font-weight: 600; margin-bottom: 12px; color: #ffffff; }
                    .text { font-size: 14px; line-height: 1.6; color: #94a3b8; margin-bottom: 24px; }
                    .code-box { display: inline-block; font-size: 32px; font-weight: 700; letter-spacing: 8px; color: #ffffff; background: #4f46e5; padding: 14px 28px; border-radius: 8px; margin-bottom: 24px; }
                    .footer { font-size: 12px; color: #64748b; margin-top: 24px; border-top: 1px solid #334155; padding-top: 16px; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="logo">CampusDESK</div>
                    <div class="title">Verificación de Cuenta</div>
                    <p class="text">Gracias por registrarte en CampusDESK. Utiliza el siguiente código de 6 dígitos para completar la verificación de tu correo electrónico:</p>
                    <div class="code-box">""" + code + """
                    </div>
                    <p class="text">Este código es válido por <strong>10 minutos</strong>. Nunca compartas este código con nadie.</p>
                    <div class="footer">
                        Si tú no solicitaste este registro, puedes ignorar este mensaje de forma segura.<br>
                        © 2026 CampusDESK - Gestión de Incidencias Tecnológicas
                    </div>
                </div>
            </body>
            </html>
            """;
    }
}
