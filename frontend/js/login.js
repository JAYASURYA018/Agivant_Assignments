

document.addEventListener('DOMContentLoaded', () => {
    
    if (localStorage.getItem('currentUser')) {
        window.location.href = '../index.html';
    }

    setupLoginListener();
});

function setupLoginListener() {
    const btnSubmit = document.getElementById('btn-login-submit');

    btnSubmit.addEventListener('click', async () => {
        if (!validateForm()) {
            return;
        }

        const email = document.getElementById('login-email').value.trim();
        const password = document.getElementById('login-password').value;

        const payload = {
            email: email,
            password: password
        };

        try {
            btnSubmit.disabled = true;
            btnSubmit.textContent = 'Signing In...';

            const user = await apiRequest('/auth/login', {
                method: 'POST',
                body: payload
            });

            
            localStorage.setItem('currentUser', JSON.stringify(user));
            showToast(`Welcome back, ${user.firstName}!`, 'success');

            
            setTimeout(() => {
                window.location.href = '../index.html';
            }, 400);

        } catch (error) {
            btnSubmit.disabled = false;
            btnSubmit.textContent = 'Sign In';
            showToast(error.message || 'Login failed. Please check your credentials.', 'error');
        }
    });
}

function validateForm() {
    let isValid = true;

    const emailInput = document.getElementById('login-email');
    const passwordInput = document.getElementById('login-password');

    const emailError = document.getElementById('login-email-error');
    const passwordError = document.getElementById('login-password-error');

    
    emailInput.classList.remove('is-invalid');
    passwordInput.classList.remove('is-invalid');
    emailError.style.display = 'none';
    passwordError.style.display = 'none';

    
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailInput.value.trim() || !emailRegex.test(emailInput.value.trim())) {
        emailInput.classList.add('is-invalid');
        emailError.style.display = 'block';
        isValid = false;
    }

    
    if (!passwordInput.value) {
        passwordInput.classList.add('is-invalid');
        passwordError.style.display = 'block';
        isValid = false;
    }

    return isValid;
}
