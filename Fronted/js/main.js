import { API } from './api.js';
import { setSession, isLoggedIn } from './auth.js';
import { showToast, setButtonLoading } from './ui.js';

// Redirect if already logged in
if (isLoggedIn()) {
    window.location.href = 'dashboard.html';
}

// Tab navigation
export function switchToLogin() {
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    if (tabLogin) tabLogin.className = 'auth-tab active';
    if (tabRegister) tabRegister.className = 'auth-tab';
    if (loginForm) loginForm.classList.remove('hidden');
    if (registerForm) registerForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

export function switchToRegister() {
    const tabLogin = document.getElementById('tab-login');
    const tabRegister = document.getElementById('tab-register');
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    if (tabRegister) tabRegister.className = 'auth-tab active';
    if (tabLogin) tabLogin.className = 'auth-tab';
    if (registerForm) registerForm.classList.remove('hidden');
    if (loginForm) loginForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

window.switchToLogin = switchToLogin;
window.switchToRegister = switchToRegister;

// Bind submit handlers
const loginForm = document.getElementById('login-form');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        document.querySelectorAll('.form-error').forEach(el => el.textContent = '');

        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const btn = document.getElementById('btn-login');

        try {
            setButtonLoading(btn, true, 'Acceder');
            const data = await API.login({ email, password });
            if (data && data.token) {
                setSession(data);
                showToast('¡Bienvenido a CampusDESK!', 'success');
                setTimeout(() => window.location.href = 'dashboard.html', 1000);
            }
        } catch (error) {
            // Manejo de error real contra el servidor (sin modo offline/demo)
            const errEl = document.getElementById('login-general-error');
            if (errEl) errEl.textContent = error.message;
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Acceder');
        }
    });
}

const registerForm = document.getElementById('register-form');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        document.querySelectorAll('.form-error').forEach(el => el.textContent = '');

        const firstName = document.getElementById('reg-firstname').value.trim();
        const lastName = document.getElementById('reg-lastname').value.trim();
        const email = document.getElementById('reg-email').value.trim();
        const password = document.getElementById('reg-password').value;
        const btn = document.getElementById('btn-register');

        if (password.length < 6) {
            const passErr = document.getElementById('reg-password-error');
            if (passErr) passErr.textContent = 'La contraseña debe tener al menos 6 caracteres';
            return;
        }

        try {
            setButtonLoading(btn, true, 'Registrando...');
            await API.register({ firstName, lastName, email, password });
            showToast('Cuenta creada exitosamente. Ya puedes iniciar sesión.', 'success');
            registerForm.reset();
            switchToLogin();
            const loginEmail = document.getElementById('login-email');
            if (loginEmail) loginEmail.value = email;
            const loginPass = document.getElementById('login-password');
            if (loginPass) loginPass.focus();
        } catch (error) {
            // Manejo de error real contra el servidor (sin modo offline/demo)
            const errEl = document.getElementById('reg-general-error');
            if (errEl) errEl.textContent = error.message;
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Registrarse');
        }
    });
}
