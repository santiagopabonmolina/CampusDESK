import { API } from './api.js';
import { setSession, isLoggedIn } from './auth.js';
import { showToast, setButtonLoading } from './ui.js';

// Redirect if already logged in
if (isLoggedIn()) {
    window.location.href = 'dashboard.html';
}

// Elements
const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const authTabsBar = document.getElementById('auth-tabs-bar');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');
const regGeneralError = document.getElementById('reg-general-error');
const loginGeneralError = document.getElementById('login-general-error');

// Tab switching
export function switchToLogin() {
    if (tabLogin) tabLogin.className = 'auth-tab active';
    if (tabRegister) tabRegister.className = 'auth-tab';
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (loginForm) loginForm.classList.remove('hidden');
    if (registerForm) registerForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

export function switchToRegister() {
    if (tabRegister) tabRegister.className = 'auth-tab active';
    if (tabLogin) tabLogin.className = 'auth-tab';
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (registerForm) registerForm.classList.remove('hidden');
    if (loginForm) loginForm.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

window.switchToLogin = switchToLogin;
window.switchToRegister = switchToRegister;

if (tabLogin) tabLogin.addEventListener('click', switchToLogin);
if (tabRegister) tabRegister.addEventListener('click', switchToRegister);

// Check query param (mode=register)
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('mode') === 'register') {
    switchToRegister();
}

// Login Submit
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
            if (loginGeneralError) loginGeneralError.textContent = error.message;
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Acceder');
        }
    });
}

// Register Submit
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
            setButtonLoading(btn, true, 'Registrando...');
            await API.register({ firstName, lastName, email, password });
            
            showToast('Cuenta creada exitosamente. Ahora puedes iniciar sesión.', 'success');
            registerForm.reset();
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
            setButtonLoading(btn, false, 'Registrarse');
        }
    });
}
