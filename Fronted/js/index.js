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
const verifySection = document.getElementById('verify-section');
const verifyForm = document.getElementById('verify-form');
const verifyEmailDisplay = document.getElementById('verify-email-display');
const verifyCodeError = document.getElementById('verify-code-error');
const btnResend = document.getElementById('btn-resend-code');
const resendTimer = document.getElementById('resend-timer');
const timerCount = document.getElementById('timer-count');
const btnBackFromVerify = document.getElementById('btn-back-login-from-verify');
const regGeneralError = document.getElementById('reg-general-error');
const loginGeneralError = document.getElementById('login-general-error');

let currentPendingEmail = '';

// Tab switching
export function switchToLogin() {
    if (tabLogin) tabLogin.className = 'auth-tab active';
    if (tabRegister) tabRegister.className = 'auth-tab';
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (loginForm) loginForm.classList.remove('hidden');
    if (registerForm) registerForm.classList.add('hidden');
    if (verifySection) verifySection.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

export function switchToRegister() {
    if (tabRegister) tabRegister.className = 'auth-tab active';
    if (tabLogin) tabLogin.className = 'auth-tab';
    if (authTabsBar) authTabsBar.classList.remove('hidden');
    if (registerForm) registerForm.classList.remove('hidden');
    if (loginForm) loginForm.classList.add('hidden');
    if (verifySection) verifySection.classList.add('hidden');
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

export function showVerification(email) {
    currentPendingEmail = email;
    if (verifyEmailDisplay) verifyEmailDisplay.textContent = email;
    if (authTabsBar) authTabsBar.classList.add('hidden');
    if (loginForm) loginForm.classList.add('hidden');
    if (registerForm) registerForm.classList.add('hidden');
    if (verifySection) verifySection.classList.remove('hidden');
    if (verifyCodeError) verifyCodeError.textContent = '';
    
    // Clear inputs and focus first digit
    const digitInputs = document.querySelectorAll('.code-digit');
    digitInputs.forEach(input => input.value = '');
    if (digitInputs[0]) digitInputs[0].focus();
}

window.switchToLogin = switchToLogin;
window.switchToRegister = switchToRegister;
window.showVerification = showVerification;

if (tabLogin) tabLogin.addEventListener('click', switchToLogin);
if (tabRegister) tabRegister.addEventListener('click', switchToRegister);

// Check query param (mode=register)
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('mode') === 'register') {
    switchToRegister();
}

if (btnBackFromVerify) {
    btnBackFromVerify.addEventListener('click', () => {
        switchToLogin();
        const loginEmail = document.getElementById('login-email');
        if (loginEmail && currentPendingEmail) loginEmail.value = currentPendingEmail;
    });
}

// Digits input automation (Jump forward, jump backward, paste full code)
const digitInputs = Array.from(document.querySelectorAll('.code-digit'));
digitInputs.forEach((input, index) => {
    input.addEventListener('input', (e) => {
        const val = e.target.value.replace(/[^0-9]/g, '');
        e.target.value = val ? val[val.length - 1] : '';

        if (e.target.value && index < digitInputs.length - 1) {
            digitInputs[index + 1].focus();
        }
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Backspace' && !input.value && index > 0) {
            digitInputs[index - 1].focus();
        }
    });

    input.addEventListener('paste', (e) => {
        e.preventDefault();
        const pasteData = (e.clipboardData || window.clipboardData).getData('text').trim();
        const cleanNumbers = pasteData.replace(/[^0-9]/g, '').slice(0, 6);
        if (cleanNumbers.length > 0) {
            cleanNumbers.split('').forEach((char, i) => {
                if (digitInputs[i]) digitInputs[i].value = char;
            });
            const nextIdx = Math.min(cleanNumbers.length, digitInputs.length - 1);
            digitInputs[nextIdx].focus();
        }
    });
});

function getEnteredCode() {
    return digitInputs.map(input => input.value.trim()).join('');
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
            // If account is disabled/unverified, switch to verification screen automatically
            if (error.requiresVerification || (error.message && error.message.toLowerCase().includes('verific'))) {
                showVerification(email);
            }
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
            const resp = await API.register({ firstName, lastName, email, password });
            
            showToast('Código de verificación enviado a tu correo', 'success');
            registerForm.reset();
            showVerification(email);
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

// Verification Submit
if (verifyForm) {
    verifyForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (verifyCodeError) verifyCodeError.textContent = '';
        
        const code = getEnteredCode();
        const btn = document.getElementById('btn-verify-submit');

        if (code.length < 6) {
            if (verifyCodeError) verifyCodeError.textContent = 'Por favor ingresa los 6 dígitos del código';
            return;
        }

        try {
            setButtonLoading(btn, true, 'Verificando...');
            const resp = await API.verifyCode({ email: currentPendingEmail, code });
            showToast(resp.message || '¡Cuenta verificada exitosamente!', 'success');
            
            // Switch to login tab and pre-fill email
            setTimeout(() => {
                switchToLogin();
                const loginEmail = document.getElementById('login-email');
                if (loginEmail) loginEmail.value = currentPendingEmail;
                const loginPass = document.getElementById('login-password');
                if (loginPass) loginPass.focus();
            }, 1200);
        } catch (error) {
            if (verifyCodeError) verifyCodeError.textContent = error.message;
            showToast(error.message, 'error');
        } finally {
            setButtonLoading(btn, false, 'Verificar Cuenta');
        }
    });
}

// Resend Code
let resendInterval = null;
if (btnResend) {
    btnResend.addEventListener('click', async () => {
        if (!currentPendingEmail) {
            showToast('No hay correo registrado para reenviar', 'error');
            return;
        }

        try {
            btnResend.disabled = true;
            await API.resendCode({ email: currentPendingEmail });
            showToast('Nuevo código enviado a tu correo', 'success');
            
            // 45s countdown
            let seconds = 45;
            if (resendTimer) resendTimer.style.display = 'inline';
            if (timerCount) timerCount.textContent = seconds;
            
            if (resendInterval) clearInterval(resendInterval);
            resendInterval = setInterval(() => {
                seconds--;
                if (timerCount) timerCount.textContent = seconds;
                if (seconds <= 0) {
                    clearInterval(resendInterval);
                    btnResend.disabled = false;
                    if (resendTimer) resendTimer.style.display = 'none';
                }
            }, 1000);
        } catch (error) {
            btnResend.disabled = false;
            showToast(error.message, 'error');
        }
    });
}
