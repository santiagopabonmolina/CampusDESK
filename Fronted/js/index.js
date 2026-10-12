import { API } from './api.js';
import { setSession, isLoggedIn } from './auth.js';
import { showToast, setButtonLoading } from './ui.js';

// Redirect if already logged in
if (isLoggedIn()) {
    window.location.href = 'dashboard.html';
}

// Tabs logic
const tabLogin = document.getElementById('tab-login');
const tabRegister = document.getElementById('tab-register');
const loginForm = document.getElementById('login-form');
const registerForm = document.getElementById('register-form');

tabLogin.addEventListener('click', () => {
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    loginForm.classList.remove('hidden');
    registerForm.classList.add('hidden');
});

tabRegister.addEventListener('click', () => {
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    registerForm.classList.remove('hidden');
    loginForm.classList.add('hidden');
});

// Login Submit
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Clear previous errors
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
    
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const btn = document.getElementById('btn-login');

    try {
        setButtonLoading(btn, true, 'Acceder');
        const data = await API.login({ email, password });
        
        if (data && data.token) {
            setSession(data);
            showToast('Bienvenido a CampusDESK', 'success');
            setTimeout(() => window.location.href = 'dashboard.html', 1000);
        }
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        setButtonLoading(btn, false, 'Acceder');
    }
});

// Register Submit
registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    
    // Clear previous errors
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
    
    const firstName = document.getElementById('reg-firstname').value;
    const lastName = document.getElementById('reg-lastname').value;
    const email = document.getElementById('reg-email').value;
    const password = document.getElementById('reg-password').value;
    const btn = document.getElementById('btn-register');

    if (password.length < 6) {
        document.getElementById('reg-password-error').textContent = 'La contraseña debe tener al menos 6 caracteres';
        return;
    }

    try {
        setButtonLoading(btn, true, 'Crear Cuenta');
        await API.register({ firstName, lastName, email, password });
        
        showToast('Cuenta creada exitosamente. Ahora puedes iniciar sesión.', 'success');
        registerForm.reset();
        tabLogin.click(); // Switch to login tab
    } catch (error) {
        showToast(error.message, 'error');
    } finally {
        setButtonLoading(btn, false, 'Crear Cuenta');
    }
});
