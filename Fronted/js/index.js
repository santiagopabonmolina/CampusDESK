import { API } from './api.js';
import { setSession, isLoggedIn } from './auth.js';
import { showToast, setButtonLoading } from './ui.js';

if (isLoggedIn()) {
    window.location.href = 'dashboard.html';
}

const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const authTabsBar = document.getElementById('auth-tabs-bar');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const regGeneralError = document.getElementById('reg-general-error');
const loginGeneralError = document.getElementById('login-general-error');

/**
 * Switches to login tab.
 */
export function switchToLogin() {
    if (tabLogin) {
        tabLogin.className = 'auth-tab active';
        tabLogin.setAttribute('aria-selected', 'true');
    }
    if (tabRegister) {
        tabRegister.className = 'auth-tab';
        tabRegister.setAttribute('aria-selected', 'false');
    }
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (loginForm) loginForm.classList.remove('hidden');
    if (registerForm) registerForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

/**
 * Switches to registration tab.
 */
export function switchToRegister() {
    if (tabRegister) {
        tabRegister.className = 'auth-tab active';
        tabRegister.setAttribute('aria-selected', 'true');
    }
    if (tabLogin) {
        tabLogin.className = 'auth-tab';
        tabLogin.setAttribute('aria-selected', 'false');
    }
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (registerForm) registerForm.classList.remove('hidden');
    if (loginForm) loginForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

window.switchToLogin = switchToLogin;
window.switchToRegister = switchToRegister;

if (tabLogin) tabLogin.addEventListener('click', switchToLogin);
if (tabRegister) tabRegister.addEventListener('click', switchToRegister);

const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('mode') === 'register') {
    switchToRegister();
}

/**
 * Configures password visibility toggle.
 */
function setupPasswordToggle(btnId, inputId) {
    const btn = document.getElementById(btnId);
    const input = document.getElementById(inputId);
    if (!btn || !input) return;

    btn.addEventListener('click', () => {
        const isPassword = input.type === 'password';
        input.type = isPassword ? 'text' : 'password';

        const eyeOpen = btn.querySelector('.eye-open');
        const eyeClosed = btn.querySelector('.eye-closed');

        if (eyeOpen && eyeClosed) {
            if (isPassword) {
                eyeOpen.classList.add('hidden');
                eyeClosed.classList.remove('hidden');
            } else {
                eyeOpen.classList.remove('hidden');
                eyeClosed.classList.add('hidden');
            }
        }
    });
}

setupPasswordToggle('btn-toggle-login-pwd', 'login-password');
setupPasswordToggle('btn-toggle-reg-pwd', 'reg-password');

const regPasswordInput = document.getElementById('reg-password');
const strengthContainer = document.getElementById('strength-container');
const strengthBar = document.getElementById('strength-bar');
const strengthText = document.getElementById('strength-text');

if (regPasswordInput && strengthContainer && strengthBar && strengthText) {
    regPasswordInput.addEventListener('input', () => {
        const val = regPasswordInput.value;
        if (!val) {
            strengthContainer.style.display = 'none';
            return;
        }
        strengthContainer.style.display = 'flex';

        let score = 0;
        if (val.length >= 6) score++;
        if (val.length >= 10) score++;
        if (/[A-Z]/.test(val)) score++;
        if (/[0-9]/.test(val)) score++;
        if (/[^A-Za-z0-9]/.test(val)) score++;

        strengthBar.className = 'password-strength-bar';
        if (score <= 2) {
            strengthBar.classList.add('strength-weak');
            strengthText.textContent = 'Fortaleza: Débil';
            strengthText.style.color = '#f87171';
        } else if (score <= 4) {
            strengthBar.classList.add('strength-medium');
            strengthText.textContent = 'Fortaleza: Media';
            strengthText.style.color = '#fbbf24';
        } else {
            strengthBar.classList.add('strength-strong');
            strengthText.textContent = 'Fortaleza: Fuerte';
            strengthText.style.color = '#34d399';
        }
    });
}

if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
        
        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;
        const btn = document.getElementById('btn-login');

        try {
            setButtonLoading(btn, true, 'Acceder a la Plataforma');
            const data = await API.login({ email, password });
            
            if (data && data.token) {
                setSession(data);
                showToast('¡Bienvenido a CampusDESK!', 'success');
                setTimeout(() => window.location.href = 'dashboard.html', 800);
            }
        } catch (error) {
            if (loginGeneralError) loginGeneralError.textContent = error.message;
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Acceder a la Plataforma');
        }
    });
}

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
            document.getElementById('reg-password-error').textContent = 'La contraseña debe tener al menos 6 caracteres';
            return;
        }

        try {
            setButtonLoading(btn, true, 'Creando Cuenta...');
            await API.register({ firstName, lastName, email, password });
            
            showToast('Cuenta creada exitosamente. Ahora puedes iniciar sesión.', 'success');
            registerForm.reset();
            if (strengthContainer) strengthContainer.style.display = 'none';
            switchToLogin();
            const loginEmail = document.getElementById('login-email');
            if (loginEmail) loginEmail.value = email;
            const loginPass = document.getElementById('login-password');
            if (loginPass) loginPass.focus();
        } catch (error) {
            let errorText = error.message || 'Error al procesar el registro';
            if (error.data && error.data.errors) {
                const errs = error.data.errors;
                if (errs.firstName) document.getElementById('reg-firstname-error').textContent = errs.firstName;
                if (errs.lastName) document.getElementById('reg-lastname-error').textContent = errs.lastName;
                if (errs.email) document.getElementById('reg-email-error').textContent = errs.email;
                if (errs.password) document.getElementById('reg-password-error').textContent = errs.password;
            } else if (error.message && error.message.toLowerCase().includes('correo')) {
                document.getElementById('reg-email-error').textContent = error.message;
            }
            if (regGeneralError) regGeneralError.textContent = errorText;
            showToast(errorText, 'error');
        } finally {
            setButtonLoading(btn, false, 'Crear Cuenta');
        }
    });
}
